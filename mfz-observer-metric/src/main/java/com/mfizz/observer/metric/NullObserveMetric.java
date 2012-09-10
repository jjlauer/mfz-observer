package com.mfizz.observer.metric;

/*
 * #%L
 * mfizz-observer-metric
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
 *
 * @author joe@mfizz.com
 */
public class NullObserveMetric implements ObserveMetric {

    static public NullObserveMetric INSTANCE = new NullObserveMetric();
    
    @Override
    public boolean contains(String name) {
        return false;
    }
    
    @Override
    public ObserveMetric get(String name) {
        // always returns itself (for chaining)
        return this;
    }
    
    @Override
    public ObserveMetric getUnsafely(String name) {
        return null;
    }

    @Override
    public Long getLong() {
        return null;
    }

    @Override
    public String getString() {
        return null;
    }
    
}
