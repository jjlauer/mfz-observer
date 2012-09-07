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

/**
 * Represents a "duration" for a summary such as "current" or "5min".  Durations
 * are ordered by their millisecond duration. So "current" would come first 
 * followed by "1min" then "5min", etc.
 * @author joe@mfizz.com
 */
public class SummaryPeriod implements Comparable<SummaryPeriod> {
    
    final private String name;
    final private long millis;

    public SummaryPeriod(String name) {
        SummaryPeriod sp = parse(name);
        this.name = sp.name;
        this.millis = sp.millis;
    }
    
    private SummaryPeriod(String name, long millis) {
        this.name = name;
        this.millis = millis;
    }

    public String getName() {
        return name;
    }

    public long getMillis() {
        return millis;
    }
    
    static public SummaryPeriod parse(String duration) throws IllegalArgumentException {
        if (duration.equalsIgnoreCase("current")) {
            return new SummaryPeriod("current", 0L);
        } else {
            // <integer><token>
            String amountString = duration.substring(0, duration.length()-1);
            int amount = 0;
            try {
                amount = Integer.parseInt(amountString);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse [" + duration + "]; [" + amountString + "] not a valid integer");
            }
            
            char lastChar = duration.charAt(duration.length()-1);
            if (lastChar == 's') {
                return new SummaryPeriod(amount + "s", amount*1000L);
            } else if (lastChar == 'm') {
                return new SummaryPeriod(amount + "m", amount*60*1000L);
            } else if (lastChar == 'h') {
                return new SummaryPeriod(amount + "h", amount*60*60*1000L);
            } else if (lastChar == 'd') {
                return new SummaryPeriod(amount + "d", amount*24*60*60*1000L);
            } else {
                throw new IllegalArgumentException("Unable to parse [" + duration + "]; [" + lastChar + "] not a valid span");
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.millis ^ (this.millis >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SummaryPeriod other = (SummaryPeriod) obj;
        if (this.millis != other.millis) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(SummaryPeriod t) {
        long diff = this.millis - t.millis;
        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return name;
    }
    
}
