package com.mfizz.observer.metric;

/*
 * #%L
 * mfz-observer-metric
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

/**
 * Represents an immutable path to a metric.
 */
public class MetricPath {
    
    private final String path;
    private final String[] names;
    
    public MetricPath(String path) {
        if (path == null) {
            this.names = null;
        } else {
            // fix issues with trailing slash
            if (path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
            }
            this.names = path.split("/");
        }
        this.path = path;
    }

    public String[] getNames() {
        return names;
    }
    
    public int getDepth() {
        return (this.names == null ? 0 : this.names.length);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
    
}
