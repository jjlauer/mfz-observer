package com.mfizz.observer.metric;

/*
 * #%L
 * mfizz-observer-common
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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joe@mfizz.com
 */
public class MetricPathTest {
    
    @Test
    public void path() throws Exception {
        MetricPath path1 = new MetricPath("path1");
        Assert.assertEquals(1, path1.getNames().length);
        Assert.assertEquals("path1", path1.getNames()[0]);
        
        MetricPath path2 = new MetricPath("path1/path2");
        Assert.assertEquals(2, path2.getNames().length);
        Assert.assertEquals("path1", path2.getNames()[0]);
        Assert.assertEquals("path2", path2.getNames()[1]);
        
        // fix issue of trailing "slash" not really being another path
        MetricPath path3 = new MetricPath("path1/path2/");
        Assert.assertEquals(2, path3.getNames().length);
        Assert.assertEquals("path1", path3.getNames()[0]);
        Assert.assertEquals("path2", path3.getNames()[1]);
    }
}
