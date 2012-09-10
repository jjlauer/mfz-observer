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
import com.mfizz.util.PreciseTimeUnitConverter;
import com.mfizz.util.TimePeriod;
import java.util.concurrent.TimeUnit;

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
public class LongSummaryCounter extends BaseLongObserveMetric implements ObserveMetricSummary<LongCounter> {
    
    // additional vars tracked
    private TimeUnit rateUnit;
    private double avgRate;
    private Double minRate;
    private Double maxRate;
    
    public LongSummaryCounter() {
        this.rateUnit = TimeUnit.SECONDS;
    }
    
    public LongSummaryCounter(TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
    }
    
    @Override
    public boolean contains(String name) {
        return false;
    }
    
    @Override
    public ObserveMetric get(String name) {
        return NullObserveMetric.INSTANCE;
    }
    
    @Override
    public ObserveMetric getUnsafely(String name) {
        return null;
    }

    @Override
    public Long getLong() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    public double getAvgRate() {
        return avgRate;
    }

    public double getMinRate() {
        return minRate;
    }

    public double getMaxRate() {
        return maxRate;
    }
    
    private double calculateRateDuration(long millis) {
        return PreciseTimeUnitConverter.convertMillis(millis, rateUnit);
    }
    
    @Override
    public void summarize(TimePeriod period, LongCounter aggValue) throws Exception {
        this.value += aggValue.getValue();
        // convert duration from milliseconds to user-defined preference
        double rateDuration = calculateRateDuration(period.getDuration());
        double aggMinRate = ((double)aggValue.getValue())/rateDuration;
        double aggMaxRate = ((double)aggValue.getValue())/rateDuration;
        if (minRate == null || aggMinRate < minRate) {
            this.minRate = aggMinRate;
        }
        if (maxRate == null || aggMaxRate > maxRate) {
            this.maxRate = aggMaxRate;
        }
    }
    
    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        // convert duration from milliseconds to user-defined preference
        double rateDuration = calculateRateDuration(period.getDuration());
        this.avgRate = ((double)this.value)/rateDuration;
    }

    @Override
    public String toString() {
        return "[avgr=" + ObserverUtil.format(avgRate) + ", minr=" + ObserverUtil.format(minRate) + ", maxr=" + ObserverUtil.format(maxRate) + ']';
    }
    
}
