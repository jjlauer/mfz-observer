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
public class MetricMapTest {
    
    @Test
    public void getPath() throws Exception {
        MetricMap map = new MetricMap();
        
        // build map
        MetricMap path1Map = new MetricMap();
        MetricMap path2Map = new MetricMap();
        MetricMap path3Map = new MetricMap();
        
        map.put("path1", path1Map);
        path1Map.put("path2", path2Map);
        path2Map.put("path3", path3Map);
        
        Assert.assertSame(path1Map, map.get("path1"));
        Assert.assertSame(path1Map, map.get(new MetricPath("path1")));
        Assert.assertSame(path2Map, map.get(new MetricPath("path1/path2")));
        Assert.assertSame(path3Map, map.get(new MetricPath("path1/path2/path3")));
        
        // should return null for any of these as well
        Assert.assertNull(map.get(new MetricPath("path1/path3")));
        Assert.assertNull(map.get(new MetricPath("path1/path2/path3/nopath")));
        Assert.assertNull(map.get(new MetricPath("nopath")));
        Assert.assertNull(map.get(new MetricPath("path2")));
        Assert.assertNull(map.get(new MetricPath("path3")));
    }
    
    @Test
    public void putPath() throws Exception {
        MetricMap map = new MetricMap();
        
        LongCounter lc1 = new LongCounter(1);
        MetricMap path3Map = new MetricMap();
        
        // put this metric at a specific path in
        map.put(new MetricPath("path1/path2/path3"), path3Map);
        
        // retrieve this path back out
        Assert.assertSame(path3Map, map.get(new MetricPath("path1/path2/path3")));
        
        // add the long counter a path as well
        map.put(new MetricPath("path1/path2/lc1"), lc1);
        Assert.assertSame(lc1, map.get(new MetricPath("path1/path2/lc1")));
        
        // do not allow to put a map underneath lc1 since it already exists
        MetricMap path4Map = new MetricMap();
        
        try {
            map.put(new MetricPath("path1/path2/lc1/path4"), path4Map);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
    }
    
    @Test
    public void filter() throws Exception {
        // create a map we will filter from
        MetricMap map = new MetricMap();
        
        LongCounter lc1 = new LongCounter(1);
        LongCounter lc2 = new LongCounter(2);
        LongGauge lg1 = new LongGauge(1);
        LongGauge lg2 = new LongGauge(2);
        
        // add metrics at various levels
        map.put("lc1", lc1);
        map.put("lg1", lg1);
        map.put(new MetricPath("path1/path2/lc2"), lc2);
        map.put(new MetricPath("path1/path2/lg2"), lg2);
        
        // filter out just 1 property
        MetricMap filter1 = map.filter(new MetricPath("lc1"));
        
        Assert.assertEquals(1, filter1.size());
        Assert.assertSame(lc1, filter1.get("lc1"));
        // verify that null metric is return on "safe" get
        Assert.assertSame(NullObserveMetric.INSTANCE, filter1.get("lc2"));
        Assert.assertNull(filter1.getUnsafely("lc2"));
        
        
        // filter out property further down
        MetricMap filter2 = map.filter(new MetricPath("path1/path2/lc2"));
        
        Assert.assertEquals(1, filter2.size());
        Assert.assertSame(lc2, filter2.get(new MetricPath("path1/path2/lc2")));
        // verify that null metric is return on "safe" get
        Assert.assertNull(filter1.get(new MetricPath("path1/path2/lg2")));
        
        
        // filter out a part of the tree
        MetricMap filter3 = map.filter(new MetricPath("path1/path2"));
        
        Assert.assertEquals(1, filter3.size());
        Assert.assertSame(lc2, filter3.get(new MetricPath("path1/path2/lc2")));
        Assert.assertSame(lg2, filter3.get(new MetricPath("path1/path2/lg2")));
        // verify specifics of the tree
        MetricMap path1Map = filter3.getMap("path1");
        Assert.assertEquals(1, path1Map.size());
        MetricMap path2Map = path1Map.getMap("path2");
        Assert.assertEquals(2, path2Map.size());
        Assert.assertSame(lc2, path2Map.get("lc2"));
        Assert.assertSame(lg2, path2Map.get("lg2"));
        // verify that null metric is return on "safe" get
        Assert.assertNull(filter3.getUnsafely("lc1"));
        
        // filter out no properties
        MetricMap filter4 = map.filter(new MetricPath("nopath"));
        Assert.assertNull(filter4);
        
        
        // filter out 2 properties
        MetricMap filter5 = map.filter(new MetricPath("path1/path2/lc2"), new MetricPath("lc1"));
        Assert.assertEquals(2, filter5.size());
        Assert.assertSame(lc1, filter5.get(new MetricPath("lc1")));
        Assert.assertSame(lc2, filter5.get(new MetricPath("path1/path2/lc2")));
        Assert.assertNull(filter5.get(new MetricPath("path1/path2/lg2")));
    }
}
