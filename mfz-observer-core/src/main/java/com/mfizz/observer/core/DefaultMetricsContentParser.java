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

/**
 *
 * @author joe@mfizz.com
 */
public class DefaultMetricsContentParser<M extends DefaultMetricsModel> implements ContentParser<DefaultMetrics> {
    
    private final Class<M> modelClass;
    private final ContentParser<M> contentParser;
    
    public DefaultMetricsContentParser(ContentParser<M> contentParser, Class<M> modelClass) {
        this.modelClass = modelClass;
        this.contentParser = contentParser;
    }

    @Override
    public String getContentType() {
        return this.contentParser.getContentType();
    }

    @Override
    public DefaultMetrics parse(String content, Class<DefaultMetrics> dataClass) throws Exception {
        // rather than creating the dataClass from json -- we'll actually create the model
        M model = this.contentParser.parse(content, modelClass);
        // convert model into defaultMetrics
        return model.createDefaultMetrics();
    }
    
}
