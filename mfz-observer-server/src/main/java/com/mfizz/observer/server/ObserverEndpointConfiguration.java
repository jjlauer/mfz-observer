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

import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author joe@mfizz.com
 */
public class ObserverEndpointConfiguration {
    
    private String url;
    private Set<String> groups;

    public String getUrl() {
        return url;
    }

    public Set<String> getGroups() {
        return groups;
    }
    
    @JsonIgnore
    public String getGroupsString() {
        if (this.groups == null) {
            return "";
        }
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (String s : groups) {
            if (i != 0) { sb.append(","); }
            sb.append(s);
            i++;
        }
        return sb.toString();
    }
}
