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

import com.mfizz.observer.metric.SummaryMetricMap;
import com.mfizz.util.TimePeriod;

/**
 *
 * @author joe@mfizz.com
 */
public class DefaultSummaryMetrics extends SummaryMetricMap implements Summary<DefaultAggregateMetrics> {

    private TimePeriod period;
    private int count;
    //private Map<String,ObserveSummaryMetric> metrics;

    public DefaultSummaryMetrics() {
        //this.metrics = new HashMap<String,ObserveSummaryMetric>();
    }
    
    @Override
    public TimePeriod getPeriod() {
        return period;
    }

    public int getCount() {
        return count;
    }

    /**
    public Map<String,ObserveSummaryMetric> getMetrics() {
        return (Map<String,ObserveSummaryMetric>)this.metrics;
    }
    */
    
    @Override
    public void summarizeBegin() throws Exception {
        // do nothing
    }
    
    @Override
    public void summarize(TimePeriod period, DefaultAggregateMetrics aggData) throws Exception {
        super.summarize(period, aggData);
    }
    
    /**
    @Override
    public void summarize(TimePeriod period, DefaultAggregateMetrics aggData) throws Exception {
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
    */

    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        this.period = period;
        this.count = count;
        super.summarizeComplete(period, count);
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        //for (ObserveSummaryMetric sumMetric : metrics.values()) {
        //    sumMetric.summarizeComplete(period, count);
        //}
    }
    
}
