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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Path("/")
public class IndexResource {
    
    final private ObserverServerConfiguration configuration;
    final private ServiceObservers sos;

    public IndexResource(ObserverServerConfiguration configuration, ServiceObservers sos) {
        this.configuration = configuration;
        this.sos = sos;
    }

    @GET
    public IndexView get() {
        return new IndexView(new DateTime(DateTimeZone.UTC), configuration, Version.getLongVersion(), sos);
    }
}