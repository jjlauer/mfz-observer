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

import com.mfizz.observer.metric.LongSummaryCounter;
import java.io.IOException;
import java.text.DecimalFormat;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 *
 * @author joe@mfizz.com
 */
public class LongSummaryCounterSerializer extends JsonSerializer<LongSummaryCounter> {

    static private DecimalFormat format = new DecimalFormat("0.##");
    
    @Override
    public void serialize(LongSummaryCounter t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        jg.writeStartObject();
        jg.writeNumberField("value", t.getValue());
        jg.writeFieldName("avg_rate");
        jg.writeNumber(format.format(t.getAvgRate()));
        jg.writeFieldName("min_rate");
        jg.writeNumber(format.format(t.getMinRate()));
        jg.writeFieldName("max_rate");
        jg.writeNumber(format.format(t.getMaxRate()));
        jg.writeFieldName("unit");
        jg.writeString(t.getRateUnit().name());
        jg.writeEndObject();
    }
    
}
