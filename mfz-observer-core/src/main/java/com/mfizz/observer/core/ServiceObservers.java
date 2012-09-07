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

import com.mfizz.observer.core.ServiceObserver.SnapshotAllResult;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe@mfizz.com
 */
public class ServiceObservers {
    private static final Logger logger = LoggerFactory.getLogger(ServiceObservers.class);
    
    private ScheduledExecutorService executors;
    private TreeMap<String,ServiceObserver> serviceObservers;
    private TreeMap<String,ScheduledFuture> serviceObserverFutures;
    
    public ServiceObservers() {
        this.serviceObservers = new TreeMap<String,ServiceObserver>();
        this.serviceObserverFutures = new TreeMap<String,ScheduledFuture>();
    }
    
    public void addServiceObserver(ServiceObserver so) throws Exception {
        if (serviceObservers.containsKey(so.getServiceName())) {
            throw new Exception("ServiceObserver for ServiceName [" + so.getServiceName() + "] already exists");
        }
        this.serviceObservers.put(so.getServiceName(), so);
    }

    public Map<String,ServiceObserver> getServiceObservers() {
        return serviceObservers;
    }
    
    public void start() {
        // create pool of scheduled executors
        this.executors = Executors.newScheduledThreadPool(serviceObservers.size(), new ThreadFactory() {
            private AtomicInteger seq = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ServiceObserver-" + seq.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
        
        for (ServiceObserver so : serviceObservers.values()) {
            // schedule each service observer to run at a fixed rate
            long stepMillis = so.getConfiguration().getStepMillis();
            ScheduledFuture<?> future = this.executors.scheduleWithFixedDelay(new SnapshotAllRunnable(so), stepMillis, stepMillis, TimeUnit.MILLISECONDS);
            serviceObserverFutures.put(so.getServiceName(), future);
        }
    }
    
    static public class SnapshotAllRunnable implements Runnable {

        private final ServiceObserver so;

        public SnapshotAllRunnable(ServiceObserver so) {
            this.so = so;
        }

        @Override
        public void run() {
            logger.trace("Executing snapshotAll for service [{}]", so.getServiceName());
            try {
                long start = System.currentTimeMillis();
                SnapshotAllResult result = this.so.snapshotAll();
                long stop = System.currentTimeMillis();
                logger.info("snapshotAll for service [{}] ok: time=" + (stop-start) + " ms, snapshots [attempted=" + result.getSnapshotsAttempted() + ", completed=" + result.getSnapshotsCompleted() + "]", so.getServiceName());
            } catch (Exception e) {
                logger.error("snapshotAll for service [" + so.getServiceName() + "] failed", e);
            }
        }

    }
}
