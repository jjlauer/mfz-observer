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

import java.util.concurrent.ExecutorService;

/**
 * Helpful utility to standardize a ServiceObserver with default metric classes.
 * 
 * @author joe@mfizz.com
 */
public class DefaultServiceObserver extends ServiceObserver<DefaultMetrics,DefaultAggregateMetrics,DefaultSummaryMetrics> {
    
    public DefaultServiceObserver(ServiceObserverConfiguration serviceConfig, ExecutorService executor, ContentParser<DefaultMetrics> contentParser) {
        this(serviceConfig, executor, contentParser, null);
    }
    
    public DefaultServiceObserver(ServiceObserverConfiguration serviceConfig, ExecutorService executor, ContentParser<DefaultMetrics> contentParser, ObserverNamingStrategy ons) {
        super(serviceConfig, executor, DefaultMetrics.class, DefaultAggregateMetrics.class, DefaultSummaryMetrics.class, contentParser, ons);
    }
    
}
