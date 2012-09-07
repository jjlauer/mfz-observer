package com.mfizz.observer.server;

/*
 * #%L
 * mfizz-observer-server
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

import com.google.common.cache.CacheBuilderSpec;
import com.mfizz.observer.core.DefaultServiceObserver;
import com.mfizz.observer.core.DefaultServiceObserverFactory;
import com.mfizz.observer.core.ObserverNamingStrategy;
import com.mfizz.observer.core.ServiceObservers;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.views.ViewBundle;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ObserverServer extends Service<ObserverServerConfiguration> {
    static private Log log = Log.forClass(ObserverServer.class);
    
    public static void main(String[] args) throws Exception {
        new ObserverServer().run(args);
    }

    private ObserverServer() {
        super("observer-server");
        // for enabling mustache and freemarker templates
        addBundle(new ViewBundle());
        // for enabling static assets
        // By default a restart will be required to pick up any changes to assets.
        // Use the following spec to disable that behaviour, useful when developing.
        CacheBuilderSpec cacheSpec = CacheBuilderSpec.disableCaching();
        //CacheBuilderSpec cacheSpec = AssetsBundle.DEFAULT_CACHE_SPEC;
        addBundle(new AssetsBundle("/assets/", cacheSpec));
    }

    @Override
    protected void initialize(ObserverServerConfiguration configuration, Environment environment) {
        
        // executors to be used for all observers
        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
            private AtomicInteger seq = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Observer-" + seq.incrementAndGet());
                return t;
            }
        });
        
        // build service observers
        ServiceObservers sos = new ServiceObservers();
        
        for (Map.Entry<String,ServiceConfiguration> entry : configuration.getServices().entrySet()) {
            String serviceName = entry.getKey();
            ServiceConfiguration config = entry.getValue();
            
            log.info("service: " + serviceName + " with contentParser: " + config.getContentParserClass());
            
            // merge hash key into actual config
            config.setName(serviceName);
            
            DefaultServiceObserver so = null;
            try {
                ObserverNamingStrategy ons = ObserverNamingStrategy.DEFAULT;
                if (config.getNamingStrategyClass() != null) {
                    ons = config.getNamingStrategyClass().newInstance();
                }
                so = DefaultServiceObserverFactory.create(config, executor, config.getContentParserClass().newInstance(), ons);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            if (entry.getValue().getObservers() != null) {
                for (ObserverEndpointConfiguration endpoint : entry.getValue().getObservers()) {
                    log.info(" * observer: " + endpoint.getUrl() + " for groups " + endpoint.getGroups());
                    try {
                        so.addObserver(endpoint.getUrl(), endpoint.getGroupsString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            try {
                sos.addServiceObserver(so);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        // add any resources (rest services)
        environment.addResource(new IndexResource(configuration, sos));
        environment.addResource(new ServicesApiResource(sos));
        
        // actual managed object that executes ServiceObservers
        environment.manage(new ServiceObserverManager(sos));
    }

}