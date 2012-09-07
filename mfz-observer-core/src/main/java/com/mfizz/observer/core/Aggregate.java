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
public interface Aggregate<D extends Delta> {
    
    
    public void aggregateBegin() throws Exception;
    
    /**
     * If this object is A and obj is B, then this effectively does A + B where "+"
     * is really a sum.  A sum is arbitrary and up to the implementer -- it
     * simply represents whatever is useful to combine between delta snapshots
     * of data.
     */
    public void aggregate(String name, D delta) throws Exception;
    
    public void aggregateComplete(TimePeriod period, int count) throws Exception;
    
}
