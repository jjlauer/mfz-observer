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

import com.mfizz.observer.core.ContentParser;
import com.mfizz.observer.core.DefaultMetrics;
import com.mfizz.observer.core.ObserverNamingStrategy;
import com.mfizz.observer.core.ServiceObserverConfiguration;
import java.util.List;

/**
 * Adds any configuration required to make a ServiceObserver work.
 * 
 * @author joe@mfizz.com
 */
public class ServiceConfiguration extends ServiceObserverConfiguration {
    
    private Class<ContentParser<DefaultMetrics>> contentParserClass;
    private Class<ObserverNamingStrategy> namingStrategyClass;
    private List<ObserverEndpointConfiguration> observers;

    public Class<ContentParser<DefaultMetrics>> getContentParserClass() {
        return contentParserClass;
    }

    public void setContentParserClass(Class<ContentParser<DefaultMetrics>> contentParserClass) {
        this.contentParserClass = contentParserClass;
    }

    public Class<ObserverNamingStrategy> getNamingStrategyClass() {
        return namingStrategyClass;
    }

    public void setNamingStrategyClass(Class<ObserverNamingStrategy> namingStrategyClass) {
        this.namingStrategyClass = namingStrategyClass;
    }

    public List<ObserverEndpointConfiguration> getObservers() {
        return observers;
    }
}
