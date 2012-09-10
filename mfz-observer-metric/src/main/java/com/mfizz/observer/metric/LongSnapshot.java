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
 * String value only retained in a "snapshot" (not aggregated or summarized)
 * 
 * @author joe@mfizz.com
 */
public class LongSnapshot extends BaseLongObserveMetric implements ObserveMetricDelta<LongSnapshot> {
    
    protected boolean absolute;
    
    public LongSnapshot() {
        // do nothing
    }

    public LongSnapshot(long value) {
        this(value, false);
    }
    
    public LongSnapshot(long value, boolean absolute) {
        super(value);
        this.absolute = absolute;
    }
    
    @Override
    public void delta(LongSnapshot currentData, LongSnapshot lastData) throws ResetDetectedException, Exception {
        // if absolute then make sure values match -- otherwise throw reset
        if (absolute && currentData.value != lastData.value) {
            throw new ResetDetectedException();
        }
        // current value is the best "delta"
        this.value = currentData.value;
    }
    
}
