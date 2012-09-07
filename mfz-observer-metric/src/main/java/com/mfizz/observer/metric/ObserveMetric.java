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

import com.mfizz.observer.common.ResetDetectedException;

/**
 *
 * @author joe@mfizz.com
 */
public interface ObserveMetric<M extends ObserveMetric, S extends ObserveSummaryMetric> {
    
    // methods always present on all metrics
    
    public boolean contains(String name);
    
    public ObserveMetric get(String name);
    
    public Long getLong();
    
    public String getString();
    
    
    // methods for deltas and aggregates
    
    public void delta(M currentData, M lastData) throws ResetDetectedException, Exception;
    
    public boolean shouldAggregate();
    
    public void aggregate(M deltaData) throws Exception;
    
    public S createSummaryMetric();
    
}
