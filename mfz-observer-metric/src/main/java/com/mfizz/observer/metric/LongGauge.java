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
 * Gauge (same as RRD gauge type; queue size; queue consumer count value an example)
  - Value from source is absolute measure at that point in time (deltas are meaningless)
  - DeltaValue = CurrentValue
  - AggValue += DeltaValue
  - SumValue doesn't exist, but avg/min/max of it does
  * 
 * @author joe@mfizz.com
 */
public class LongGauge extends BaseLongObserveMetric implements ObserveMetricDelta<LongGauge>, ObserveMetricAggregate<LongGauge> {
    
    public LongGauge() {
        // do nothing
    }

    public LongGauge(long value) {
        super(value);
    }

    @Override
    public void delta(LongGauge currentData, LongGauge lastData) throws ResetDetectedException, Exception {
        // current measurement is best value to use for delta
        this.value = currentData.value;
    }
    
    @Override
    public void aggregate(LongGauge deltaData) throws Exception {
        // total guage value aggregated from all nodes is simple addition
        this.value += deltaData.value;
    }
    
    @Override
    public LongSummaryGauge createSummaryMetric() {
        return new LongSummaryGauge();
    }
    
}
