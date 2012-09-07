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
public class LongCounter extends BaseLongObserveMetric<LongCounter, LongSummaryCounter> {
    
    public LongCounter() {
        // do nothing
    }

    public LongCounter(long value) {
        super(value);
    }
    
    @Override
    public void delta(LongCounter currentData, LongCounter lastData) throws ResetDetectedException, Exception {
        long deltaValue = currentData.value - lastData.value;
        if (deltaValue < 0) {
            // counters should be only increasing between deltas -- if they go
            // negative than the source must have reset
            throw new ResetDetectedException();
        }
        this.value = deltaValue;
    }
    
    @Override
    public boolean shouldAggregate() {
        return true;
    }
    
    @Override
    public void aggregate(LongCounter deltaData) throws Exception {
        this.value += deltaData.value;
    }
    
    @Override
    public LongSummaryCounter createSummaryMetric() {
        return new LongSummaryCounter();
    }
    
}
