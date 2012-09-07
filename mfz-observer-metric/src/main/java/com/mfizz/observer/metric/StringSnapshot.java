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
public class StringSnapshot extends BaseStringObserveMetric<StringSnapshot,NullObserveSummaryMetric> {
    
    protected boolean absolute;
    
    public StringSnapshot() {
        // do nothing
    }

    public StringSnapshot(String value) {
        this(value, false);
    }
    
    public StringSnapshot(String value, boolean absolute) {
        super(value);
        this.absolute = absolute;
    }

    @Override
    public void delta(StringSnapshot currentData, StringSnapshot lastData) throws ResetDetectedException, Exception {
        // if absolute then make sure values match -- otherwise throw reset
        if (absolute && (currentData.value == null ? lastData.value != null : !currentData.value.equals(lastData.value))) {
            throw new ResetDetectedException();
        }
        // current value is the best "delta"
        this.value = currentData.value;
    }
    
    @Override
    public boolean shouldAggregate() {
        return false;
    }
    
    @Override
    public void aggregate(StringSnapshot deltaData) throws Exception {
        // no aggregation in a snapshot
    }
    
    @Override
    public NullObserveSummaryMetric createSummaryMetric() {
        return NullObserveSummaryMetric.INSTANCE;
    }
    
}
