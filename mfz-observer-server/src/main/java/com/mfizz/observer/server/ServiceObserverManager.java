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

import com.mfizz.observer.core.ServiceObservers;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.logging.Log;

/**
 *
 * @author joe@mfizz.com
 */
public class ServiceObserverManager implements Managed {
    static private Log log = Log.forClass(ServiceObserverManager.class);
    
    private ServiceObservers sos;
    
    public ServiceObserverManager(ServiceObservers sos) {
        this.sos = sos;
    }
    
    @Override
    public void start() throws Exception {
        sos.start();
    }

    @Override
    public void stop() throws Exception {
        // do nothing
    }
    
}
