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

import com.mfizz.observer.common.SummaryPeriod;
import com.mfizz.util.TimePeriod;
import com.mfizz.util.TimeSeries;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe@mfizz.com
 */
public class ServiceObserver<D extends Delta, A extends Aggregate, S extends Summary> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceObserver.class);
    
    static public final String DEFAULT_GROUP = "all";
    
    private final ServiceObserverConfiguration serviceConfig;
    private final ExecutorService executor;
    private final ConcurrentHashMap<String,Observer<D>> observers;
    private final Class<D> dataClass;
    private final Class<A> aggregateClass;
    private final Class<S> summaryClass;
    private final ContentParser<D> contentParser;
    private final ObserverNamingStrategy ons;
    // httpclient used between all observers
    private final DefaultHttpClient httpclient;
    // counters & metrics
    private final Timer snapshotAllTimer;
    private final AtomicLong snapshotAllAttemptedCounter;
    private final AtomicLong snapshotAllCompletedCounter;
    private final AtomicReference<SnapshotAllResult> lastSnapshotAllResult;
    
    //private final AtomicLong lastSnapshotAllTimestamp;
    //private final AtomicInteger lastSnapshotsCompletedCounter;
    //private final AtomicInteger lastSnapshotsFailedCounter;
    
    // atomic references to prepared instantaneous/1/5/15 min summaries for each group
    private final TreeSet<String> groups;
    private final ConcurrentHashMap<String,SummaryGroup<S>> summary;
    private final ConcurrentHashMap<String,TimeSeries<ObserveAggregateSnapshot<A>>> snapshots;
    
    public ServiceObserver(ServiceObserverConfiguration serviceConfig, ExecutorService executor, Class<D> dataClass, Class<A> aggregateClass, Class<S> summaryClass, ContentParser<D> contentParser) {
        this(serviceConfig, executor, dataClass, aggregateClass, summaryClass, contentParser, null);
    }
    
    public ServiceObserver(ServiceObserverConfiguration serviceConfig, ExecutorService executor, Class<D> dataClass, Class<A> aggregateClass, Class<S> summaryClass, ContentParser<D> contentParser, ObserverNamingStrategy ons) {
        this.serviceConfig = serviceConfig;
        this.executor = executor;
        this.observers = new ConcurrentHashMap<String,Observer<D>>();
        this.dataClass = dataClass;
        this.aggregateClass = aggregateClass;
        this.summaryClass = summaryClass;
        this.contentParser = contentParser;
        if (ons == null) {
            this.ons = ObserverNamingStrategy.DEFAULT;
        } else {
            this.ons = ons;
        }
        this.snapshotAllTimer = Metrics.newTimer(new MetricName("com.mfizz.observer.ServiceObservers", serviceConfig.getName(), "snapshot-all-time"), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
        this.snapshotAllAttemptedCounter = new AtomicLong();
        this.snapshotAllCompletedCounter = new AtomicLong();
        this.lastSnapshotAllResult = new AtomicReference<SnapshotAllResult>();
        
        //this.lastSnapshotAllTimestamp = new AtomicLong(0);
        //this.lastSnapshotsCompletedCounter = new AtomicInteger();
        //this.lastSnapshotsFailedCounter = new AtomicInteger();
        
        this.groups = new TreeSet<String>();
        this.summary = new ConcurrentHashMap<String,SummaryGroup<S>>();
        this.snapshots = new ConcurrentHashMap<String,TimeSeries<ObserveAggregateSnapshot<A>>>();
        
        // always make sure "current" is added to set of vars to track
        // "current" always tracked for all
        this.serviceConfig.getPeriods().add(SummaryPeriod.parse("current"));
        
        //
        // create high performance http client that can be reused by all observers
        //
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        // increase max total connection to 200
        cm.setMaxTotal(200);
        // increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
        
        this.httpclient = new DefaultHttpClient(cm);
        this.httpclient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                // always return false -- we never want a request retried
                return false;
            }
        });
        this.httpclient.getParams()
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer((int)serviceConfig.getSocketTimeout()))
            .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer((int)serviceConfig.getConnectionTimeout()))
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
    }

    public ServiceObserverConfiguration getConfiguration() {
        return serviceConfig;
    }
    
    public String getServiceName() {
        return serviceConfig.getName();
    }
    
    static public Set<String> createGroupSet(String groups) throws Exception {
        // always create a set regardless if groups is null
        Set<String> groupSet = new TreeSet<String>();
        
        if (groups != null) {
            String[] tokens = groups.split(",");
            for (String t : tokens) {
                String y = t.trim();
                if (y.equalsIgnoreCase(DEFAULT_GROUP)) {
                    throw new Exception("Reserved group name [" + DEFAULT_GROUP + " prohibited");
                }
                groupSet.add(y);
            }
        }
        
        // always add "default" group
        groupSet.add(DEFAULT_GROUP);
        
        return groupSet;
    }
    
    public void addObserver(String url) throws Exception {
        addObserver(url, null, null);
    }
    
    public void addObserver(String url, String groups) throws Exception {
        addObserver(url, groups, null);
    }
    
    public void addObserver(String url, String groups, String name) throws Exception {
        ObserverConfiguration config = new ObserverConfiguration();
        config.setUrl(url);
        
        String observerName = name;
        if (name == null) {
            observerName = this.ons.createName(url);
        }
        config.setName(observerName);
        
        Set<String> groupSet = createGroupSet(groups);
        config.setGroups(groupSet);
        
        Observer<D> observer = new Observer<D>(serviceConfig, config, dataClass, contentParser, httpclient, observerName);
        
        addObserver(observer);
    }
    
    private void addObserver(Observer<D> observer) throws Exception {
        // verify this observer doesn't already exist
        if (observers.containsKey(observer.getName())) {
            throw new Exception("Observer with name [" + observer.getName() + "] already exists (observer names MUST be unique)");
        }
        
        // add to list of observers as well as group
        observers.put(observer.getName(), observer);
        this.groups.addAll(observer.getConfiguration().getGroups());
    }

    public int getObserverCount() {
        return this.observers.size();
    }

    public Timer getSnapshotAllTimer() {
        return snapshotAllTimer;
    }
    
    public SnapshotAllResult getLastResult() {
        return lastSnapshotAllResult.get();
    }

    public long getLastSnapshotAllTimestamp() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return 0;
        }
        return result.getBeginTimestamp();
    }
    
    public long getLastSnapshotAllTime() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return -1;
        }
        return result.getResponseTime();
    }
    
    public DateTime getLastSnapshotAllDateTime() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return null;
        }
        return new DateTime(result.getBeginTimestamp(), DateTimeZone.UTC);
    }
    
    public Exception getLastSnapshotAllException() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return null;
        }
        return result.getException();
    }
    
    public int getLastSnapshotsAttempted() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return 0;
        }
        return result.getSnapshotsAttempted();
    }
    
    public int getLastSnapshotsCompleted() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return 0;
        }
        return result.getSnapshotsCompleted();
    }
    
    public int getLastSnapshotsFailed() {
        SnapshotAllResult result = this.lastSnapshotAllResult.get();
        if (result == null) {
            return 0;
        }
        return result.getSnapshotsFailed();
    }
    
    
    public Set<String> getGroups() {
        return groups;
    }
    
    public Set<SummaryPeriod> getPeriods() {
        return this.serviceConfig.getPeriods();
    }
    
    public long getRetentionMillis() {
        return this.serviceConfig.getPeriods().last().getMillis();
    }

    public ConcurrentHashMap<String,Observer<D>> getObservers() {
        return observers;
    }

    public ConcurrentHashMap<String, SummaryGroup<S>> getSummary() {
        return summary;
    }

    public ConcurrentHashMap<String, TimeSeries<ObserveAggregateSnapshot<A>>> getSnapshots() {
        return snapshots;
    }
    
    public SnapshotAllResult snapshotAll() throws Exception {
        final SnapshotAllResult result = new SnapshotAllResult();
        
        result.beginTimestamp = System.currentTimeMillis();
        final TimerContext timerContext = snapshotAllTimer.time();
        try {
            snapshotAllAttemptedCounter.incrementAndGet();
            doSnapshotAll(result);
            // if we get to here then the snapshotAll call worked
            snapshotAllCompletedCounter.incrementAndGet();
        } catch (Exception e) {
            // save exception into result
            result.exception = e;
            throw e;
        } finally {
            result.completeTimestamp = System.currentTimeMillis();
            timerContext.stop();
            // always save reference to result
            this.lastSnapshotAllResult.set(result);
        }

        return result;
    }
    
    private void doSnapshotAll(SnapshotAllResult result) throws Exception {
        //
        // create list of snapshots that will be executed
        //
        ArrayList<SnapshotTask> snapshotTasks = new ArrayList<SnapshotTask>();
        for (Observer<D> observer : observers.values()) {
            snapshotTasks.add(new SnapshotTask(observer, result.beginTimestamp));
        }
        result.snapshotsAttempted = snapshotTasks.size();
        
        // this will run all the update tasks and wait for them all to finish
        executor.invokeAll(snapshotTasks);
        
        // create an aggregate for each group
        TreeMap<String,ObserveAggregateSnapshot<A>> aggs = new TreeMap<String,ObserveAggregateSnapshot<A>>();
        
        // process deltas from each observer
        for (Observer<D> observer : observers.values()) {
            
            // determine if last snapshot completed or failed
            if (observer.getConsecutiveSnapshotCompletedCount() > 0) {
                result.snapshotsCompleted++;
            } else {
                result.snapshotsFailed++;
            }
            
            // was this the first snapshot attempt for this observer?
            long snapshotAttempts = observer.getSnapshotAttemptCounter();
            
            // each group will aggregate the same delta snapshot from each observer
            ObserveDeltaSnapshot<D> ods = observer.getDeltaSnapshot();
            
            if (ods == null) {
                //logger.debug("delta snapshot for observer {} was null", observer.getName());
                SnapshotException e = observer.getException();
                if (e == null) {
                    if (snapshotAttempts <= 1) {
                        // first runs we don't expect any deltas
                    } else {
                        logger.error("observer [{}] for service [{}] had null delta AND exception values (previous snapshot maybe failed?)", observer.getName(), getServiceName());
                    }
                } else {
                    // this is now logged in SnapshotTask below
                    //logger.warn("exception during snapshot for observer " + observer.getName(), e);
                }
            } else {
                // period should be the same across all deltas
                TimePeriod period = ods.getPeriod();
                // TODO: verify periods match each other as safety check?
                
                // create or get aggregate for each group this observer belongs to
                for (String group : observer.configuration.getGroups()) {
                    ObserveAggregateSnapshot<A> oas = aggs.get(group);
                    if (oas == null) {
                        oas = new ObserveAggregateSnapshot<A>(period, aggregateClass.newInstance());
                        aggs.put(group, oas);
                    }
                    oas.add(observer.getName(), ods.getData());
                }
            }
        }
        
        if (snapshotAllAttemptedCounter.get() > 1 && aggs.isEmpty()) {
            logger.warn("snapshotAll() for service [{}] generated no aggregated snapshots!", this.getServiceName());
        }
        
        // at this point, the new snapshots from each observer have generated
        // new aggregates for this point-in-time -- add this to our rolling time series
        for (String group : aggs.keySet()) {
            // last aggregate snapshot
            ObserveAggregateSnapshot<A> oas = aggs.get(group);
            
            // get or create new series of aggregate snapshots for each group
            TimeSeries<ObserveAggregateSnapshot<A>> aggseries = snapshots.get(group);
            
            if (aggseries == null) {
                // figure out capacity of time series (retentionTime / step + fudgeFactor)
                long retentionMillis = getRetentionMillis();
                int initialCapacity = (int)(retentionMillis / this.serviceConfig.getStepMillis()) + 2;
                logger.info("Creating new TimeSeries for service [{}] group [{}] with retentionMillis=" + retentionMillis + "; initialCapacity=" + initialCapacity, getServiceName(), group);
                aggseries = new TimeSeries<ObserveAggregateSnapshot<A>>(retentionMillis, initialCapacity);
                snapshots.put(group, aggseries);
            }
            
            // add aggregate snapshot to the time series for each group
            // this will also prune old snapshots that are older than the retention period
            // the timestamp of the aggregate becomes the relative "now" timestamp for calculating retentions
            // this is how we'll always at least keep "current" times
            aggseries.add(oas, oas.getTimestamp());
            
            // create an updated summary for each interval for this group
            SummaryGroupFactory<S,A> sfg = new SummaryGroupFactory<S,A>(oas.getTimestamp(), this.summaryClass, this.serviceConfig.getPeriods());
            
            sfg.beginAll();
            
            Iterator<ObserveAggregateSnapshot<A>> it = aggseries.getSeries().iterator();
            while (it.hasNext()) {
                ObserveAggregateSnapshot<A> tempoas = it.next();
                sfg.summarize(tempoas.getPeriod(), tempoas.getAggregate());
            }
            
            sfg.completeAll();
            
            SummaryGroup<S> sg = sfg.createSummaryGroup();
            summary.put(group, sg);
        }
    }
    
    static public class SnapshotAllResult {
        
        private long beginTimestamp;
        private long completeTimestamp;
        private int snapshotsAttempted;
        private int snapshotsCompleted;
        private int snapshotsFailed;
        private Exception exception;

        public SnapshotAllResult() {
            // do nothing
        }

        public long getBeginTimestamp() {
            return beginTimestamp;
        }

        public long getCompleteTimestamp() {
            return completeTimestamp;
        }
        
        public long getResponseTime() {
            return completeTimestamp - beginTimestamp;
        }

        public int getSnapshotsAttempted() {
            return snapshotsAttempted;
        }

        public int getSnapshotsCompleted() {
            return snapshotsCompleted;
        }

        public int getSnapshotsFailed() {
            return snapshotsFailed;
        }

        public Exception getException() {
            return exception;
        }
        
    }
    
    class SnapshotTask implements Callable<Object> {

        private final Observer observer;
        private final long timestamp;

        public SnapshotTask(Observer observer, long timestamp) {
            this.observer = observer;
            this.timestamp = timestamp;
        }

        @Override
        public Object call() throws Exception {
            long start = System.currentTimeMillis();
            Exception exception = null;
            try {
                observer.snapshot(timestamp);
                return null;
            } catch (Exception e) {
                exception = e;
                return null;
            } finally {
                long stop = System.currentTimeMillis();
                long duration = stop-start;
                StringBuilder sb = new StringBuilder();
                sb.append("snapshot for service [").append(serviceConfig.getName()).append("] observer [").append(observer.getName()).append("]: time=").append((stop-start)).append(" ms");
                if (exception == null) {
                    sb.append("; result was ok");
                    // only print out good case if > step
                    if (duration >= serviceConfig.getStepMillis()) {
                        logger.warn("{} but exceeded stepMillis", sb);
                    }
                } else {
                    sb.append("; exception: ").append(exception.getMessage());
                    logger.warn("{}", sb);
                    logger.warn("", exception);
                }
            }
        }
    }
}
