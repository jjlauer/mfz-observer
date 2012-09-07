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

import com.mfizz.util.TimePeriod;

/**
 * 
 * @author joe@mfizz.com
 */
public class NullObserveSummaryMetric implements ObserveSummaryMetric<Object> {
    
    static public NullObserveSummaryMetric INSTANCE = new NullObserveSummaryMetric();
    
    public NullObserveSummaryMetric() {
        // do nothing
    }
    
    @Override
    public boolean contains(String name) {
        return false;
    }
    
    @Override
    public ObserveSummaryMetric get(String name) {
        return INSTANCE;
    }
    
    @Override
    public void summarize(TimePeriod period, Object aggValue) throws Exception {
        // do nothing
    }
    
    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        // do nothing
    }
    
}
