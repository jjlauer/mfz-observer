package com.mfizz.observer.common;

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

import java.util.TreeSet;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe@mfizz.com
 */
public class SummaryPeriodTest {
    private static final Logger logger = LoggerFactory.getLogger(SummaryPeriodTest.class);

    @Test
    public void parse() throws Exception {
        SummaryPeriod d = null;
        
        d = SummaryPeriod.parse("current");
        Assert.assertEquals(d.getMillis(), 0L);
        Assert.assertEquals(d.getName(), "current");
        
        d = SummaryPeriod.parse("1s");
        Assert.assertEquals(d.getMillis(), 1000L);
        Assert.assertEquals(d.getName(), "1s");
        
        d = SummaryPeriod.parse("3m");
        Assert.assertEquals(d.getMillis(), 3*60*1000L);
        Assert.assertEquals(d.getName(), "3m");
        
        d = SummaryPeriod.parse("2h");
        Assert.assertEquals(d.getMillis(), 2*60*60*1000L);
        Assert.assertEquals(d.getName(), "2h");
        
        d = SummaryPeriod.parse("5d");
        Assert.assertEquals(d.getMillis(), 5*24*60*60*1000L);
        Assert.assertEquals(d.getName(), "5d");
    }
    
    @Test
    public void testEquals() throws Exception {
        SummaryPeriod d1 = SummaryPeriod.parse("current");
        SummaryPeriod d2 = SummaryPeriod.parse("1m");
        SummaryPeriod d3 = SummaryPeriod.parse("0s");
        SummaryPeriod d4 = SummaryPeriod.parse("60s");
        SummaryPeriod d5 = SummaryPeriod.parse("5m");
        
        // equals is only based on millis value (not name)
        Assert.assertEquals(d1, d1);
        Assert.assertEquals(d1, d3);
        Assert.assertEquals(d2, d4);
        Assert.assertFalse(d2.equals(d5));
    }
    
    @Test
    public void ordering() throws Exception {
        SummaryPeriod d1 = SummaryPeriod.parse("current");
        SummaryPeriod d2 = SummaryPeriod.parse("1s");
        SummaryPeriod d3 = SummaryPeriod.parse("1m");
        SummaryPeriod d4 = SummaryPeriod.parse("5m");
        SummaryPeriod d5 = SummaryPeriod.parse("1h");
        
        TreeSet<SummaryPeriod> durations = new TreeSet<SummaryPeriod>();
        durations.add(d1);
        durations.add(d2);
        durations.add(d3);
        durations.add(d4);
        durations.add(d5);
        
        // verify treeset has the correct ordering
        SummaryPeriod[] a = durations.toArray(new SummaryPeriod[0]);
        Assert.assertEquals(a[0], d1);
        Assert.assertEquals(a[1], d2);
        Assert.assertEquals(a[2], d3);
        Assert.assertEquals(a[3], d4);
        Assert.assertEquals(a[4], d5);
    }
}
