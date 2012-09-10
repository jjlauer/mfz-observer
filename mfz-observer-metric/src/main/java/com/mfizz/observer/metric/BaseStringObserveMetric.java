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
public abstract class BaseStringObserveMetric implements ObserveMetric {
    
    protected String value;
    
    public BaseStringObserveMetric() {
        // do nothing
    }

    public BaseStringObserveMetric(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public boolean contains(String name) {
        return false;
    }
    
    @Override
    public ObserveMetric get(String name) {
        // no sub-metrics -- always return a null chainer
        return NullObserveMetric.INSTANCE;
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
        return this.value;
    }
    
}
