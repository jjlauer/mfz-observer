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
import java.util.TreeSet;

/**
 *
 * @author joe@mfizz.com
 */
public class ServiceObserverConfiguration {
    
    private String name;
    private long stepMillis;                // length of time between observations
    private int maxSnapshots;               // number of raw snapshots each observer retains
    private long connectionTimeout;
    private long socketTimeout;
    private TreeSet<SummaryPeriod> periods;
    
    public ServiceObserverConfiguration() {
        this.stepMillis = 2000L;
        this.maxSnapshots = 2;
        this.socketTimeout = 5000L;
        this.connectionTimeout = 5000L;
        this.periods = new TreeSet<SummaryPeriod>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStepMillis() {
        return stepMillis;
    }

    public void setStepMillis(long stepMillis) {
        this.stepMillis = stepMillis;
    }

    public int getMaxSnapshots() {
        return maxSnapshots;
    }

    public void setMaxSnapshots(int maxSnapshots) {
        this.maxSnapshots = maxSnapshots;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(long socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public TreeSet<SummaryPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(TreeSet<SummaryPeriod> periods) {
        this.periods = periods;
    }
    
}
