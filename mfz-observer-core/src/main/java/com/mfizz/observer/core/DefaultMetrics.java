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
import com.mfizz.util.TimePeriod;

/**
 *
 * @author joe@mfizz.com
 */
public class DefaultMetrics extends MetricMap implements Delta<DefaultMetrics> {
    
    public DefaultMetrics() {
        // do nothing
    }
    
    /**
    public MetricMap getMap(String name) {
        ObserveMetric metric = this.metrics.get(name);
        if (metric == null) {
            return null;
        }
        if (metric instanceof MetricMap) {
            return (MetricMap)metric;
        } else {
            return null;
        }
    }
    
    public Long getLong(String name) {
        ObserveMetric metric = this.metrics.get(name);
        if (metric == null) {
            return null;
        }
        if (metric instanceof LongSnapshot) {
            return ((LongSnapshot)metric).getValue();
        } else if (metric instanceof LongCounter) {
            return ((LongCounter)metric).getValue();
        } else if (metric instanceof LongGauge) {
            return ((LongGauge)metric).getValue();
        } else {
            return null;
        }
    }
    
    public String getString(String name) {
        ObserveMetric metric = this.metrics.get(name);
        if (metric == null) {
            return null;
        }
        if (metric instanceof StringSnapshot) {
            return ((StringSnapshot)metric).getValue();
        } else {
            return null;
        }
    }
    
    public Long getLongSnapshot(String name) {
        LongSnapshot v = (LongSnapshot)this.metrics.get(name);
        if (v == null) {
            return null;
        }
        return v.getValue();
    }
    
    public String getStringSnapshot(String name) {
        StringSnapshot v = (StringSnapshot)this.metrics.get(name);
        if (v == null) {
            return null;
        }
        return v.getValue();
    }
    
    public LongCounter getLongCounter(String name) {
        return (LongCounter)this.metrics.get(name);
    }
    
    public LongGauge getLongGauge(String name) {
        return (LongGauge)this.metrics.get(name);
    }
    */
    
    @Override
    public void delta(TimePeriod period, DefaultMetrics currentData, DefaultMetrics lastData) throws ResetDetectedException, Exception {
        // we are a metric map -- just delta the two together
        super.delta(currentData, lastData);
    }
    
    /**
    @Override
    public void delta(TimePeriod period, DefaultMetrics currentData, DefaultMetrics lastData) throws ResetDetectedException, Exception {
        // loop thru metrics in current data -- so we can create new delta'ed metrics
        for (Map.Entry<String,ObserveMetric> entry : currentData.getMetrics().entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric currentMetric = entry.getValue();
            ObserveMetric lastMetric = lastData.getMetrics().get(metricName);
            
            // verify metric exists in lastData as well
            if (lastMetric == null) {
                // policy just to skip missing metrics or throw exception?
                continue;
            }

            // create a new instance of the exact type of metric in currentData
            ObserveMetric deltaMetric = entry.getValue().getClass().newInstance();
            deltaMetric.delta(currentMetric, lastMetric);
            
            this.getMetrics().put(metricName, deltaMetric);
        }
    }
    */
    
}
