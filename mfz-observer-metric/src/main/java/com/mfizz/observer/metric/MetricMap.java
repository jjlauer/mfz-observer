package com.mfizz.observer.metric;

/*
 * #%L
 * mfizz-observer-metric
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

import com.mfizz.observer.common.ResetDetectedException;
import com.mfizz.util.TimePeriod;
import java.util.HashMap;
import java.util.Map;

/**
 * Map of metrics.
 * 
 * @author joe@mfizz.com
 */
public class MetricMap implements ObserveMetric, ObserveMetricDelta<MetricMap>, ObserveMetricAggregate<MetricMap>, ObserveMetricSummary<MetricMap> {
    
    protected Map<String,ObserveMetric> metrics;
    
    public MetricMap() {
        this.metrics = new HashMap<String,ObserveMetric>();
    }

    public Map<String,ObserveMetric> getMetrics() {
        return metrics;
    }
    
    public ObserveMetric put(String name, ObserveMetric metric) {
        return this.metrics.put(name, metric);
    }
    
    @Override
    public boolean contains(String name) {
        return this.metrics.containsKey(name);
    }
    
    @Override
    public ObserveMetric get(String name) {
        ObserveMetric m = this.metrics.get(name);
        if (m == null) {
            return NullObserveMetric.INSTANCE;
        } else {
            return m;
        }
    }
    
    @Override
    public ObserveMetric getUnsafely(String name) {
        return this.metrics.get(name);
    }
    
    @Override
    public Long getLong() {
        // no actual value for a map - always return null
        return null;
    }

    @Override
    public String getString() {
        // no actual value for a map - always return null
        return null;
    }
    
    public void put(MetricPath path, ObserveMetric metric) {
        String[] names = path.getNames();
        MetricMap lastMap = this;
        int lastIndex = names.length - 1;
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            
            // if this is the last index (put the metric here)
            if (i == lastIndex) {
                lastMap.put(name, metric);
            } else {
                // try to get the submap of this
                MetricMap subMap = null;
                ObserveMetric tempSubMetric = lastMap.getUnsafely(name);
                
                // if a metric with that name already exists verify its a map!
                if (tempSubMetric != null) {
                    if (tempSubMetric instanceof MetricMap) {
                        subMap = (MetricMap)tempSubMetric;
                    } else {
                        throw new IllegalArgumentException("metric [" + name + "] already exists in path [" + path + "] with type [" + tempSubMetric.getClass().getSimpleName() + "]");
                    }
                } else {
                    subMap = new MetricMap();
                    lastMap.put(name, subMap);
                }
                
                lastMap = subMap;
            }
        }
    }
    
    public ObserveMetric get(MetricPath path) {
        String[] names = path.getNames();
        ObserveMetric lastMetric = null;
        search:
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (lastMetric == null) {
                // first search this map
                lastMetric = getUnsafely(name);
            } else {
                // search within the last metric
                lastMetric = lastMetric.getUnsafely(name);
            }
            // if last metric is still null then the path doesn't exist
            if (lastMetric == null) {
                break search;
            }
        }
        return lastMetric;
    }
    
    public MetricMap filter(MetricPath ... filters) {
        // generate a new metric map that only includes filtered metric names
        MetricMap filtermap = null;
        for (MetricPath f : filters) {
            // get the metric for this path
            ObserveMetric metric = get(f);
            // if a metric was found then add it to the filter map
            if (metric != null) {
                // add this metric to the map (at the correct path!)
                if (filtermap == null) {
                    filtermap = new MetricMap();
                }
                filtermap.put(f, metric);
            }
        }
        return filtermap;
    }
    
    // specifics that are better to use
    
    public MetricMap getMap(String name) {
        ObserveMetric metric = this.metrics.get(name);
        if (metric == null) {
            return null;
        }
        if (metric instanceof MetricMap) {
            return (MetricMap)metric;
        } else {
            return null;
        }
    }
    
    @Override
    public void delta(MetricMap currentData, MetricMap lastData) throws ResetDetectedException, Exception {
        // loop thru metrics in current data -- so we can create new delta'ed metrics
        for (Map.Entry<String,ObserveMetric> entry : currentData.metrics.entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric currentMetric = entry.getValue();
            ObserveMetric lastMetric = lastData.metrics.get(metricName);
            
            // verify metric exists in lastData as well
            if (lastMetric == null) {
                // policy just to skip missing metrics
                continue;
            }

            // create a new instance of the exact type of metric in currentData
            ObserveMetric newDeltaMetric = entry.getValue().getClass().newInstance();
            
            // verify this metric can be "delta"'ed or "aggregate"'ed
            if (newDeltaMetric instanceof ObserveMetricDelta) {
                ObserveMetricDelta deltaMetric = (ObserveMetricDelta)newDeltaMetric;
                deltaMetric.delta(currentMetric, lastMetric);
                this.metrics.put(metricName, newDeltaMetric);
            }
        }
    }
    
    @Override
    public void aggregate(MetricMap deltaData) throws Exception {
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        for (Map.Entry<String,ObserveMetric> entry : deltaData.getMetrics().entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric deltaMetric = entry.getValue();
            
            if (deltaMetric instanceof ObserveMetricAggregate) {
                // get or create new aggregate metric
                ObserveMetric newAggMetric = this.metrics.get(metricName);
                if (newAggMetric == null) {
                    // we already know that "deltaMetric" supports aggregates - so we should be fine
                    newAggMetric = deltaMetric.getClass().newInstance();
                    this.metrics.put(metricName, newAggMetric);
                }

                if (newAggMetric instanceof ObserveMetricAggregate) {
                    ObserveMetricAggregate aggMetric = (ObserveMetricAggregate)newAggMetric;
                    // time to aggregate the delta data into the aggregate metric
                    aggMetric.aggregate(deltaMetric);
                }
            }
        }
    }
    
    @Override
    public MetricMap createSummaryMetric() {
        return new MetricMap();
    }
    
    @Override
    public void summarize(TimePeriod period, MetricMap aggData) throws Exception {
        // loop thru metrics in delta data -- so we can create new metrics and aggregate them
        for (Map.Entry<String,ObserveMetric> entry : aggData.getMetrics().entrySet()) {
            String metricName = entry.getKey();
            ObserveMetric newAggMetric = entry.getValue();
            
            if (newAggMetric instanceof ObserveMetricAggregate) {
                ObserveMetricAggregate aggMetric = (ObserveMetricAggregate)newAggMetric;
                
                // get or create new summary metric
                ObserveMetric newSumMetric = this.metrics.get(metricName);
                if (newSumMetric == null) {
                    newSumMetric = aggMetric.createSummaryMetric();
                    this.metrics.put(metricName, newSumMetric);
                }
                
                if (newSumMetric instanceof ObserveMetricSummary) {
                    ObserveMetricSummary sumMetric = (ObserveMetricSummary)newSumMetric;
                    // time to summarize this from the aggregrate metric
                    sumMetric.summarize(period, newAggMetric);
                }
            }
        }
    }

    @Override
    public void summarizeComplete(TimePeriod period, int count) throws Exception {
        // finishing summarizing all metrics
        for (ObserveMetric metric : metrics.values()) {
            if (metric instanceof ObserveMetricSummary) {
                ObserveMetricSummary sumMetric = (ObserveMetricSummary)metric;
                sumMetric.summarizeComplete(period, count);
            }
        }
    }

    public int size() {
        return this.metrics.size();
    }

    public boolean isEmpty() {
        return this.metrics.isEmpty();
    }
    
}
