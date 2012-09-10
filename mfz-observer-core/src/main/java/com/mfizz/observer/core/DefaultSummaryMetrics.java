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
public class DefaultSummaryMetrics extends MetricMap implements Summary<DefaultAggregateMetrics> {

    // add a few properties onto default summary map
    private TimePeriod period;
    private int count;

    public DefaultSummaryMetrics() {
        // do nothing
    }
    
    @Override
    public TimePeriod getPeriod() {
        return period;
    }

    public int getCount() {
        return count;
    }
    
    @Override
    public DefaultSummaryMetrics filter(MetricPath ... path) {
        // call underlying filter method
        MetricMap filter = super.filter(path);
        if (filter != null) {
            DefaultSummaryMetrics newmetrics = new DefaultSummaryMetrics();
            newmetrics.count = this.count;
            newmetrics.period = this.period;
            newmetrics.metrics = filter.getMetrics();
            return newmetrics;
        } else {
            return null;
        }
    }
    
    @Override
    public void summarizeBegin() throws Exception {
        // do nothing
    }
    
    @Override
    public void summarize(TimePeriod period, DefaultAggregateMetrics aggData) throws Exception {
        super.summarize(period, aggData);
    }

    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        this.period = period;
        this.count = count;
        super.summarizeComplete(period, count);
    }
    
}
