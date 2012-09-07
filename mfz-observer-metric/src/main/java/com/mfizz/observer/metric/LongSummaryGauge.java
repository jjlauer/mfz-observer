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

import com.mfizz.observer.common.ObserverUtil;
import com.mfizz.util.TimePeriod;

/**
 * Gauge (same as RRD gauge type; queue size; queue consumer count value an example)
  - Value from source is absolute measure at that point in time (deltas are meaningless)
  - DeltaValue = CurrentValue
  - AggValue += DeltaValue
  - SumValue doesn't exist, but avg/min/max of it does
 * @author joe@mfizz.com
 */
public class LongSummaryGauge implements ObserveSummaryMetric<LongGauge> {
    
    private long sumValue;
    private double avgValue;
    private Long minValue;
    private Long maxValue;
    
    public LongSummaryGauge() {
        // do nothing
    }
    
    @Override
    public boolean contains(String name) {
        return false;
    }
    
    @Override
    public ObserveSummaryMetric get(String name) {
        return NullObserveSummaryMetric.INSTANCE;
    }

    public double getAvgValue() {
        return avgValue;
    }

    public long getMinValue() {
        return minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }
    
    @Override
    public void summarize(TimePeriod period, LongGauge aggValue) throws Exception {
        this.sumValue += aggValue.getValue();
        if (minValue == null || aggValue.getValue() < minValue) {
            this.minValue = aggValue.getValue();
        }
        if (maxValue == null || aggValue.getValue() > maxValue) {
            this.maxValue = aggValue.getValue();
        }
    }
    
    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        // calculate avg value
        this.avgValue = (double)this.sumValue/(double)count;
    }

    @Override
    public String toString() {
        return "[" + "avgv=" + ObserverUtil.format(avgValue) + ", minv=" + ObserverUtil.format(minValue) + ", maxv=" + ObserverUtil.format(maxValue) + ']';
    }
    
}
