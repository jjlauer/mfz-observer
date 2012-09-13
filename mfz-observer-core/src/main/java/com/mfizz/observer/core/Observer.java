package com.mfizz.observer.core;

/*
 * #%L
 * mfizz-observer-core
 * %%
 * Copyright (C) 2012 mfizz
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.mfizz.util.ConsecutiveCounter;
import com.mfizz.util.ConsecutiveCounterToggledSet;
import com.mfizz.util.TimePeriod;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe@mfizz.com
 */
public class Observer<D extends Delta> {
    static final private Logger logger = LoggerFactory.getLogger(Observer.class);
    
    protected final ServiceObserverConfiguration serviceConfig;
    protected final ObserverConfiguration configuration;
    protected final String name;
    protected final Timer responseTimer;
    // httpclient to use for polling
    protected final HttpClient httpclient;
    // toggled set of consecutive completed/failed counters (increment of failed will
    // automatically reset success counter to zero)
    private final AtomicLong snapshotAttemptCounter;
    private final ConsecutiveCounterToggledSet snapshotCounterSet;
    private final ConsecutiveCounter snapshotCompletedCounter;
    private final ConsecutiveCounter snapshotFailedCounter;
    private final Class<D> dataClass;
    private final ContentParser<D> contentParser;
    // for atomically updating snapshots of readings
    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;
    private final AtomicLong snapshotAttemptTimestamp;              // should match timestamp of snapshot
    protected final LinkedList<ObserveSnapshot<D>> snapshots;       // series of snapshots (first item most recent)
    protected ObserveDeltaSnapshot<D> delta;                        // only if last snapshot() correctly created a delta
    protected final LinkedList<SnapshotException> exceptions;       // series of exceptions (first item most recent)
    
    public Observer(ServiceObserverConfiguration serviceConfig, ObserverConfiguration configuration, Class<D> dataClass, ContentParser<D> contentParser, HttpClient httpclient, String name) {
        this.serviceConfig = serviceConfig;
        this.configuration = configuration;
        this.dataClass = dataClass;
        this.contentParser = contentParser;
        this.httpclient = httpclient;
        this.name = name;
        // these 2 counters are associated w/ each other -- one going non-zero
        // will reset the other (e.g. failed increment will reset ok counter to zero)
        this.snapshotAttemptCounter = new AtomicLong();
        this.snapshotCounterSet = new ConsecutiveCounterToggledSet();
        this.snapshotCompletedCounter = snapshotCounterSet.createToggledCounter();
        this.snapshotFailedCounter = snapshotCounterSet.createToggledCounter();
        this.responseTimer = Metrics.newTimer(new MetricName("com.mfizz.observer.Observers", serviceConfig.getName(), "response-time", getName()), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        // for atomically storing observations
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.snapshotAttemptTimestamp = new AtomicLong(0);
        this.snapshots = new LinkedList<ObserveSnapshot<D>>();
        this.delta = null;
        this.exceptions = new LinkedList<SnapshotException>();
    }

    public ObserverConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Timer getResponseTimer() {
        return this.responseTimer;
    }

    public long getSnapshotAttemptCounter() {
        return snapshotAttemptCounter.get();
    }
    
    public long getConsecutiveSnapshotCompletedCount() {
        return this.snapshotCompletedCounter.get();
    }
    
    public long getConsecutiveSnapshotFailedCount() {
        return this.snapshotFailedCounter.get();
    }
    
    public ObserveSnapshot<D> getSnapshot() {
        readLock.lock();
        try {
            ObserveSnapshot<D> s = this.snapshots.peek();
            if (s != null) {
                // verify the timestamp of last value matches timestamp of snapshot attempt
                if (s.getTimestamp() >= this.snapshotAttemptTimestamp.get()) {
                    return s;
                }
            }
            // otherwise just return null
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    public ObserveDeltaSnapshot<D> getDeltaSnapshot() {
        readLock.lock();
        try {
            return delta;
        } finally {
            readLock.unlock();
        }
    }
    
    public SnapshotException getException() {
        readLock.lock();
        try {
            SnapshotException e = this.exceptions.peek();
            if (e != null) {
                // verify the timestamp of last value matches timestamp of snapshot attempt
                if (e.getTimestamp() >= this.snapshotAttemptTimestamp.get()) {
                    return e;
                }
            }
            // otherwise just return null
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    public void snapshot() throws SnapshotException {
        snapshot(System.currentTimeMillis());
    }
   
    public void snapshot(long snapshotTimestamp) throws SnapshotException {
        // either a snapshot will be taken or an exception generated
        ObserveSnapshot<D> snapshot = null;
        SnapshotException e = null;
        ObserveDeltaSnapshot<D> deltaSnapshot = null;
        
        try {
            snapshotAttemptCounter.incrementAndGet();
            
            //logger.debug("starting snapshot for {}", name);
            String content = sendRequestAndReceiveContent();
            D data = contentParser.parse(content, dataClass);
            //logger.debug("created data from observe");
            snapshot = new ObserveSnapshot<D>(snapshotTimestamp, data);
        
            // create a delta snapshot if its needed
            ObserveSnapshot<D> lastSnapshot = getSnapshot();
            if (lastSnapshot == null) {
                //logger.warn("last snapshot was null for {}", name);
            } else {
                Delta<D> currentData = (Delta<D>)snapshot.getData();
                try {
                    TimePeriod period = ObserverTimePeriod.createFromTimestamps(lastSnapshot.getTimestamp(), snapshotTimestamp);
                    //logger.debug("period for " + name + " ts=" + period.getTimestamp() + " dur=" + period.getDuration());
                    D deltaData = dataClass.newInstance();
                    deltaData.delta(period, currentData, lastSnapshot.getData());
                    //logger.debug("delta for " + name + " created");
                    deltaSnapshot = new ObserveDeltaSnapshot<D>(period, deltaData);
                    //logger.debug("delta snapshot for " + name + " created");
                } catch (ResetDetectedException rde) {
                    // the source data must have rebooted/reset -- this just
                    // means a delta isn't possible this time around
                    logger.warn("Reset detected for observer {}; unable to produce delta", getName());
                }
            }
        } catch (Throwable t) {
            //logger.error("", t);
            e = new SnapshotException(snapshotTimestamp, "Unable to cleanly take snapshot: " + t.getMessage(), t);
        }
        
        writeLock.lock();
        try {
            // always set snapshot attempt timestamp and always reset delta to null
            this.snapshotAttemptTimestamp.set(snapshotTimestamp);
            this.delta = deltaSnapshot;
            
            if (e != null) {
                exceptions.addFirst(e);
                snapshotFailedCounter.incrementAndGet();
                // limit exceptions to max size
                for (int i = exceptions.size(); i > serviceConfig.getMaxSnapshots(); i--) {
                    exceptions.removeLast();
                }
                
                throw e;
            } else if (snapshot != null) {
                //logger.debug("adding snapshot for observer {}", name);
                snapshots.addFirst(snapshot);
                snapshotCompletedCounter.incrementAndGet();
                // limit snapshots to max size
                for (int i = snapshots.size(); i > serviceConfig.getMaxSnapshots(); i--) {
                    snapshots.removeLast();
                }
            } else {
                logger.error("Impossible case reached in observer");
                throw new SnapshotException(snapshotTimestamp, "Impossible case reached during snapshot");
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    public D parseContent(String content) throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        D data = m.readValue(content, this.dataClass);
        return data;
    }
    
    /**
    public void startAsyncHttpRequest() throws BrokerException, ClientProtocolException, IOException {
        HttpAsyncClient client = new DefaultHttpAsyncClient();
        client.getParams()
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer((int)serviceConfig.getSocketTimeout()))
            .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer((int)serviceConfig.getConnectionTimeout()))
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        client.start();

        HttpGet get = new HttpGet(configuration.getUrl());
        
        final TimerContext responseTimerContext = responseTimer.time();
        // start asynchronous http request, get future, and provide response handler
        Future<HttpResponse> response = client.execute(get, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                responseTimerContext.stop();
                // verify 200 OK
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new BrokerException("Failed while receiving observe content: http status error " + response.getStatusLine());
                }

                HttpEntity entity = response.getEntity();

                // do we need to verify the contentType header?
                if (!contentParser.getContentType().equals("")) {
                    // verify content-type header exists
                    Header contentTypeHeader = entity.getContentType();
                    if (contentTypeHeader == null) {
                        throw new BrokerException("Failed while receiving observe content: Content-Type header missing in http response");
                    }

                    // verify content-type starts w/ our expected content-type
                    if (!contentTypeHeader.getValue().toLowerCase().startsWith(contentParser.getContentType().toLowerCase())) {
                        throw new BrokerException("Failed while receiving observe content: content-type was not " + contentParser.getContentType() + " (actual: " + contentTypeHeader.getValue() + ")");
                    }
                }

                // do something useful with the response body
                // and ensure it is fully consumed
                return EntityUtils.toString(entity);
            }

            @Override
            public void failed(final Exception ex) {
                responseTimerContext.stop();
                //System.out.println(request.getRequestLine() + "->" + ex);
            }

            @Override
            public void cancelled() {
                responseTimerContext.stop();
                //System.out.println(request.getRequestLine() + " cancelled");
            }
        });
    }
    */
    
    public String sendRequestAndReceiveContent() throws RequestException, ClientProtocolException, IOException {
        HttpGet get = new HttpGet(configuration.getUrl());

        // The underlying HTTP connection is still held by the response object 
        // to allow the response content to be streamed directly from the network socket. 
        // In order to ensure correct deallocation of system resources 
        // the user MUST either fully consume the response content  or abort request 
        // execution by calling HttpGet#releaseConnection().

        final TimerContext responseTimerContext = responseTimer.time();
        try {
            HttpResponse response = httpclient.execute(get);

            // verify 200 OK
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RequestException("Failed while receiving observe content: http status error " + response.getStatusLine());
            }
            
            HttpEntity entity = response.getEntity();
            
            // do we need to verify the contentType header?
            if (contentParser.getContentType() != null && !contentParser.getContentType().equals("")) {
                // verify content-type header exists
                Header contentTypeHeader = entity.getContentType();
                if (contentTypeHeader == null) {
                    throw new RequestException("Failed while receiving observe content: Content-Type header missing in http response");
                }

                // verify content-type starts w/ our expected content-type
                if (!contentTypeHeader.getValue().toLowerCase().startsWith(contentParser.getContentType().toLowerCase())) {
                    throw new RequestException("Failed while receiving observe content: content-type was not " + contentParser.getContentType() + " (actual: " + contentTypeHeader.getValue() + ")");
                }
            }
            
            // do something useful with the response body
            // and ensure it is fully consumed
            return EntityUtils.toString(entity);
        } finally {
            responseTimerContext.stop();
            get.releaseConnection();
        }
    }
    
}
