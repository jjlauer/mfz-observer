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
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author joe@mfizz.com
 */
public class ObserveAggregateSnapshot<A extends Aggregate> implements TimePeriod {
    
    final private TimePeriod period;
    final private A aggregate;
    final private Set<String> names;

    public ObserveAggregateSnapshot(TimePeriod period, A aggregate) {
        this.period = period;
        this.aggregate = aggregate;
        this.names = new TreeSet<String>();
    }

    public TimePeriod getPeriod() {
        return this.period;
    }
    
    @Override
    public long getTimestamp() {
        return period.getTimestamp();
    }
    
    @Override
    public long getDuration() {
        return period.getDuration();
    }

    public A getAggregate() {
        return aggregate;
    }

    public Set<String> getNames() {
        return names;
    }
    
    public int getCount() {
        return this.names.size();
    }
    
    public void begin() throws Exception {
        aggregate.aggregateBegin();
    }
        
    public void add(String name, Delta delta) throws Exception {
        // TODO: ensure no name duplicates?
        names.add(name);
        aggregate.aggregate(name, delta);
    }
    
    public void complete() throws Exception {
        aggregate.aggregateComplete(period, names.size());
    }
}
