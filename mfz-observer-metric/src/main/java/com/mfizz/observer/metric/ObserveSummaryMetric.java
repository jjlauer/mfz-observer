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

public interface ObserveSummaryMetric<M> {
    
    // methods always present on all metrics
    
    public boolean contains(String name);
    
    public ObserveSummaryMetric get(String name);
    
    // methods for summarizing
    
    public void summarize(TimePeriod period, M aggData) throws Exception;
    
    public void summarizeComplete(TimePeriod period, int count) throws Exception;
    
}
