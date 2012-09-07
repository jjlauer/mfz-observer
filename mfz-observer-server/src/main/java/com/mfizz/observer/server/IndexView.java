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

import com.mfizz.observer.core.ServiceObserver;
import com.mfizz.observer.core.ServiceObservers;
import com.yammer.dropwizard.views.View;
import java.util.Collection;
import org.joda.time.DateTime;

/**
 *
 * @author joe@mfizz.com
 */
public class IndexView extends View {
    
    final private DateTime now;
    final private ObserverServerConfiguration configuration;
    final private String version;
    final private ServiceObservers sos;
    
    public IndexView(DateTime now, ObserverServerConfiguration configuration, String version, ServiceObservers sos) {
        super("index.ftl");
        this.now = now;
        this.configuration = configuration;
        this.version = version;
        this.sos = sos;
    }

    public DateTime getNow() {
        return now;
    }

    public ObserverServerConfiguration getConfiguration() {
        return configuration;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Shortcut to return the map of ServiceObserver(s).
     * @return 
     */
    public Collection<ServiceObserver> getServiceObservers() {
        return sos.getServiceObservers().values();
    }
}
