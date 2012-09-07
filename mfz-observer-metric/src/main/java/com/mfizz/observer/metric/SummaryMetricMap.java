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
import java.util.HashMap;
import java.util.Map;

/**
 * Counter (same as RRD counter type; stratus submitSuccess value an example)
  - Value from source always increasing (difference between current and previous value is greater than 0)
  - Negative deltas indicate the source "reset" itself
  - DeltaValue = CurrentValue - LastValue
  - AggValue += DeltaValue
  - SumValue += AggValue
     - Sum always includes avg/min/max rate calcs as well (how fast it's changing)
     - NameOfPropertyAvgRate, NameOfPropertyMinRate, NameOfPropertyMaxRate
 * @author joe@mfizz.com
 */
public class SummaryMetricMap implements ObserveSummaryMetric<MetricMap> {
    
    protected Map<String,ObserveSummaryMetric> metrics;
    
    public SummaryMetricMap() {
        this.metrics = new HashMap<String,ObserveSummaryMetric>();
    }

    public Map<String,ObserveSummaryMetric> getMetrics() {
        return metrics;
    }

    public void put(String name, ObserveSummaryMetric metric) {
        this.metrics.put(name, metric);
    }
    
    @Override
    public boolean contains(String name) {
        return this.metrics.containsKey(name);
    }
    
    @Override
    public ObserveSummaryMetric get(String name) {
        ObserveSummaryMetric m = this.metrics.get(name);
        if (m == null) {
            return NullObserveSummaryMetric.INSTANCE;
        } else {
            return m;
        }
    }
    
    public SummaryMetricMap getMap(String name) {
        ObserveSummaryMetric metric = this.metrics.get(name);
        if (metric == null) {
            return null;
        }
        if (metric instanceof SummaryMetricMap) {
            return (SummaryMetricMap)metric;
        } else {
            return null;
        }
    }
    
    @Override
    public void summarize(TimePeriod period, MetricMap aggData) throws Exception {
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        for (Map.Entry<String,ObserveMetric> entry : aggData.getMetrics().entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric aggMetric = entry.getValue();
            
            // get or create new summary metric
            ObserveSummaryMetric sumMetric = this.metrics.get(metricName);
            if (sumMetric == null) {
                sumMetric = aggMetric.createSummaryMetric();
                this.metrics.put(metricName, sumMetric);
            }
            
            // time to summarize this from the aggregrate metric
            sumMetric.summarize(period, aggMetric);
        }
    }

    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        for (ObserveSummaryMetric sumMetric : metrics.values()) {
            sumMetric.summarizeComplete(period, count);
        }
    }
    
}
