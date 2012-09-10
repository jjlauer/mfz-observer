package com.mfizz.observer.core;

/*
 * #%L
 * mfizz-observer-core
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

import com.mfizz.observer.metric.MetricMap;
import com.mfizz.observer.metric.MetricPath;
import com.mfizz.util.TimePeriod;

/**
 *
 * @author joe@mfizz.com
 */
public class DefaultMetrics extends MetricMap implements Delta<DefaultMetrics> {
    
    public DefaultMetrics() {
        // do nothing
    }
    
    @Override
    public void delta(TimePeriod period, DefaultMetrics currentData, DefaultMetrics lastData) throws ResetDetectedException, Exception {
        // we are a metric map -- just delta the two together
        super.delta(currentData, lastData);
    }
    
    @Override
    public DefaultMetrics filter(MetricPath ... path) {
        // call underlying filter method
        MetricMap filter = super.filter(path);
        if (filter != null) {
            DefaultMetrics newmetrics = new DefaultMetrics();
            newmetrics.metrics = filter.getMetrics();
            return newmetrics;
        } else {
            return null;
        }
    }
    
}
