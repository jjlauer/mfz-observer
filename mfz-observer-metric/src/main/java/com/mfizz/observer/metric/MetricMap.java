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
import java.util.HashMap;
import java.util.Map;

/**
 * Map of metrics.
 * 
 * @author joe@mfizz.com
 */
public class MetricMap implements ObserveMetric<MetricMap,SummaryMetricMap> {
    
    protected Map<String,ObserveMetric> metrics;
    
    public MetricMap() {
        this.metrics = new HashMap<String,ObserveMetric>();
    }

    public Map<String,ObserveMetric> getMetrics() {
        return metrics;
    }
    
    public void put(String name, ObserveMetric metric) {
        this.metrics.put(name, metric);
    }
    
    @Override
    public boolean contains(String name) {
        return this.metrics.containsKey(name);
    }
    
    @Override
    public ObserveMetric get(String name) {
        ObserveMetric m = this.metrics.get(name);
        if (m == null) {
            return NullObserveMetric.INSTANCE;
        } else {
            return m;
        }
    }
    
    @Override
    public Long getLong() {
        // no actual value for a map - always return null
        return null;
    }

    @Override
    public String getString() {
        // no actual value for a map - always return null
        return null;
    }
    
    // specifics that are better to use
    
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
    
    /**
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
    
    public LongSnapshot getLongSnapshot(String name) {
        return (LongSnapshot)this.metrics.get(name);
    }
    
    public StringSnapshot getStringSnapshot(String name) {
        return (StringSnapshot)this.metrics.get(name);
    }
    
    public LongCounter getLongCounter(String name) {
        return (LongCounter)this.metrics.get(name);
    }
    
    public LongGauge getLongGauge(String name) {
        return (LongGauge)this.metrics.get(name);
    }
    */
    
    @Override
    public void delta(MetricMap currentData, MetricMap lastData) throws ResetDetectedException, Exception {
        // loop thru metrics in current data -- so we can create new delta'ed metrics
        for (Map.Entry<String,ObserveMetric> entry : currentData.metrics.entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric currentMetric = entry.getValue();
            ObserveMetric lastMetric = lastData.metrics.get(metricName);
            
            // verify metric exists in lastData as well
            if (lastMetric == null) {
                // policy just to skip missing metrics
                continue;
            }

            // create a new instance of the exact type of metric in currentData
            ObserveMetric deltaMetric = entry.getValue().getClass().newInstance();
            deltaMetric.delta(currentMetric, lastMetric);
            
            this.metrics.put(metricName, deltaMetric);
        }
    }
    
    @Override
    public boolean shouldAggregate() {
        return true;
    }
    
    @Override
    public void aggregate(MetricMap deltaData) throws Exception {
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        for (Map.Entry<String,ObserveMetric> entry : deltaData.getMetrics().entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric deltaMetric = entry.getValue();
            
            if (deltaMetric.shouldAggregate()) {
                // get or create new aggregate metric
                ObserveMetric aggMetric = this.metrics.get(metricName);
                if (aggMetric == null) {
                    aggMetric = deltaMetric.getClass().newInstance();
                    this.metrics.put(metricName, aggMetric);
                }

                // time to aggregate the delta data into the aggregate metric
                aggMetric.aggregate(deltaMetric);
            }
        }
    }
    
    @Override
    public SummaryMetricMap createSummaryMetric() {
        return new SummaryMetricMap();
    }
}
