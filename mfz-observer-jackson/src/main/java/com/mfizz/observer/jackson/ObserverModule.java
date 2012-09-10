package com.mfizz.observer.jackson;

/*
 * #%L
 * mfizz-observer-jackson
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

import com.mfizz.observer.core.DefaultSummaryMetrics;
import com.mfizz.observer.metric.LongCounter;
import com.mfizz.observer.metric.LongGauge;
import com.mfizz.observer.metric.LongSnapshot;
import com.mfizz.observer.metric.LongSummaryCounter;
import com.mfizz.observer.metric.LongSummaryGauge;
import com.mfizz.observer.metric.MetricMap;
import com.mfizz.observer.metric.StringSnapshot;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 *
 * @author joe@mfizz.com
 */
public class ObserverModule extends SimpleModule {
    public ObserverModule() {
        super("ObserverModule", new Version(1,0,0,null));
        // first deserializers
        
        // then serializers:
        //addSerializer(DefaultMetrics.class, new DefaultMetricsSerializer());
        addSerializer(DefaultSummaryMetrics.class, new DefaultSummaryMetricsSerializer());
        addSerializer(LongCounter.class, new LongCounterSerializer());
        addSerializer(LongGauge.class, new LongGaugeSerializer());
        addSerializer(LongSummaryCounter.class, new LongSummaryCounterSerializer());
        addSerializer(LongSummaryGauge.class, new LongSummaryGaugeSerializer());
        addSerializer(MetricMap.class, new MetricMapSerializer());
        addSerializer(LongSnapshot.class, new LongSnapshotSerializer());
        addSerializer(StringSnapshot.class, new StringSnapshotSerializer());
    }
}