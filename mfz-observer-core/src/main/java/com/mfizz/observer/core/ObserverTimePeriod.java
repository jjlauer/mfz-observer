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

import com.mfizz.util.TimePeriod;

/**
 *
 * @author joe@mfizz.com
 */
public class ObserverTimePeriod implements TimePeriod {
    
    // timestamp of most recent snapshot
    private final long timestamp;
    // duration (essentially backwards from timestamp to find last snapshot)
    private final long duration;

    public ObserverTimePeriod(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }
    
    public ObserverTimePeriod(TimePeriod period) {
        this.timestamp = period.getTimestamp();
        this.duration = period.getDuration();
    }
    
    static public ObserverTimePeriod createFromTimestamps(long fromTimestamp, long toTimestamp) {
        return new ObserverTimePeriod(toTimestamp, (toTimestamp - fromTimestamp));
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
    
}
