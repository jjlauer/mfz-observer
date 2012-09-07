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
public class DefaultAggregateMetrics extends MetricMap implements Aggregate<DefaultMetrics> {
    
    //private Map<String,ObserveMetric> metrics;

    public DefaultAggregateMetrics() {
        //this.metrics = new HashMap<String,ObserveMetric>();
    }
    
    /**
    public Map<String, ObserveMetric> getMetrics() {
        return metrics;
    }
    */

    @Override
    public void aggregateBegin() throws Exception {
        // do nothing
    }

    @Override
    public void aggregate(String name, DefaultMetrics deltaData) throws Exception {
        super.aggregate(deltaData);
    }
    
    /**
    @Override
    public void aggregate(String name, DefaultMetrics deltaData) throws Exception {
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
    */

    @Override
    public void aggregateComplete(TimePeriod period, int count) throws Exception {
        // do nothing
    }
    
}
