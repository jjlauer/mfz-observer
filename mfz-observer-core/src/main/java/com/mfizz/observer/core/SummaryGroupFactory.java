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

import com.mfizz.observer.common.SummaryPeriod;
import com.mfizz.util.TimePeriod;
import java.util.ArrayList;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe@mfizz.com
 */
public class SummaryGroupFactory<S extends Summary, A extends Aggregate> {
    private static final Logger logger = LoggerFactory.getLogger(SummaryGroupFactory.class);
    
    private final ArrayList<SummaryProcessor> processors;

    public SummaryGroupFactory(long timestamp, Class<S> summaryClass, TreeSet<SummaryPeriod> periods) throws Exception {
        this.processors = new ArrayList<SummaryProcessor>();
        // create a new processor for each period
        for (SummaryPeriod sp : periods) {
            long cutoffTimestamp = timestamp - sp.getMillis();
            SummaryProcessor processor = new SummaryProcessor(sp, summaryClass.newInstance(), cutoffTimestamp);
            this.processors.add(processor);
        }
    }
    
    public SummaryGroup<S> createSummaryGroup() {
        SummaryGroup<S> group = new SummaryGroup<S>();
        for (SummaryProcessor processor : processors) {
            group.getValues().put(processor.summaryPeriod, processor.summary);
        }
        return group;
    }
    
    public void beginAll() throws Exception {
        for (SummaryProcessor processor : processors) {
            processor.summary.summarizeBegin();
        }
    }
    
    public void summarize(TimePeriod period, A aggregate) throws Exception {
        for (SummaryProcessor processor : processors) {
            // only include periods entirely occurring before the cutoffTimestamp
            if (period.getTimestamp() >= processor.cutoffTimestamp) {
                processor.summarize(period, aggregate);
            }
        }
    }
    
    public void completeAll() throws Exception {
        for (SummaryProcessor processor : processors) {
            processor.summary.summarizeComplete(processor.createPeriod(), processor.count);
        }
    }
    
    private class SummaryProcessor {
        public SummaryPeriod summaryPeriod;
        public S summary;
        public long cutoffTimestamp;
        public TimePeriod fromPeriod;
        public TimePeriod toPeriod;
        public long duration;       // better to actually just sum all aggregate durations
        public int count;
        
        public SummaryProcessor(SummaryPeriod summaryPeriod, S summary, long cutoffTimestamp) {
            this.summaryPeriod = summaryPeriod;
            this.summary = summary;
            this.cutoffTimestamp = cutoffTimestamp;
        }
        
        public void summarize(TimePeriod period, A aggregate) throws Exception {
            //logger.debug("summarizing agg for " + name + " w/ period ts " + period.getTimestamp() + " dur " + period.getDuration());
            this.summary.summarize(period, aggregate);
            this.count++;
            this.duration += period.getDuration();
            
            if (fromPeriod == null || period.getTimestamp() < fromPeriod.getTimestamp()) {
                fromPeriod = period;
            }
            if (toPeriod == null || period.getTimestamp() > toPeriod.getTimestamp()) {
                toPeriod = period;
            }
        }
        
        public TimePeriod createPeriod() {
            // since an aggregate in the middle may technically have been missed -- we more or less just care about the duration
            // and tag this summary with a timestamp of the most recent on
            if (toPeriod != null) {
                return new ObserverTimePeriod(toPeriod.getTimestamp(), duration);
            } else {
                return new ObserverTimePeriod(cutoffTimestamp, duration);
            }
        }
    }
}
