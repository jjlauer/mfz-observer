package com.mfizz.observer.server;

/*
 * #%L
 * mfizz-observer-server
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
import com.mfizz.observer.core.DefaultMetrics;
import com.mfizz.observer.core.DefaultServiceObserver;
import com.mfizz.observer.core.DefaultSummaryMetrics;
import com.mfizz.observer.core.Observer;
import com.mfizz.observer.core.ServiceObserver;
import com.mfizz.observer.core.ServiceObserverConfiguration;
import com.mfizz.observer.core.ServiceObservers;
import com.mfizz.observer.core.SummaryGroup;
import com.mfizz.observer.jackson.ObserverModule;
import com.sun.jersey.api.NotFoundException;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;

@Path("/api/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServicesApiResource {
    final private ServiceObservers sos;
    final private ObjectMapper mapper;
    @Context UriInfo uriInfo;

    public ServicesApiResource(ServiceObservers sos) {
        this.sos = sos;
        // for custom json mapping of specific types
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        this.mapper.registerModule(new ObserverModule());
    }

    public DefaultServiceObserver getServiceObserver(String serviceName) throws WebApplicationException {
        DefaultServiceObserver so = (DefaultServiceObserver)this.sos.getServiceObservers().get(serviceName);
        if (so == null) {
            throw new NotFoundException("service [" + serviceName + "] not found");
        }
        return so;
    }
    
    public SummaryGroup<DefaultSummaryMetrics> getGroup(DefaultServiceObserver so, String groupName) throws WebApplicationException {
        SummaryGroup<DefaultSummaryMetrics> group = so.getSummary().get(groupName);
        if (group == null) {
            throw new NotFoundException("group [" + groupName + "] not found");
        }
        return group;
    }
    
    public Observer<DefaultMetrics> getObserver(DefaultServiceObserver so, String observerName) throws WebApplicationException {
        Observer<DefaultMetrics> observer = so.getObservers().get(observerName);
        if (observer == null) {
            throw new NotFoundException("observer [" + observerName + "] not found");
        }
        return observer;
    }
    
    public DefaultSummaryMetrics getMetrics(SummaryGroup<DefaultSummaryMetrics> group, String periodName) throws WebApplicationException {
        SummaryPeriod sp = null;
        try {
            sp = SummaryPeriod.parse(periodName);
        } catch (Exception e) {
            throw new WebApplicationException(new Exception("invalid period [" + periodName + "]"), Response.Status.BAD_REQUEST);
        }
        
        DefaultSummaryMetrics metrics = group.getValues().get(sp);
        if (metrics == null) {
            throw new NotFoundException("metrics for period [" + periodName + "] not found");
        }
        return metrics;
    }
    
    public Map<String,Object> getOrCreateServiceConfigurationInfo(ServiceObserverConfiguration config) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("step_millis", config.getStepMillis());
        map.put("socket_timeout", config.getSocketTimeout());
        map.put("connection_timeout", config.getConnectionTimeout());
        return map;
    }
    
    public Map<String,Object> getOrCreateServiceInfo(DefaultServiceObserver so) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("configuration", getOrCreateServiceConfigurationInfo(so.getConfiguration()));
        map.put("groups", so.getGroups());
        // sorted list of observer names is always nice
        map.put("observers", new TreeSet<String>(so.getObservers().keySet()));
        map.put("periods", so.getPeriods());
        map.put("retention_millis", so.getRetentionMillis());
        map.put("snapshot_all_time", getOrCreateTimerInfo(so.getSnapshotAllTimer()));
        map.put("last_snapshot_all", so.getLastResult());
        return map;
    }
    
    public Map<String,Object> getOrCreateTimerInfo(Timer timer) {
        Map<String,Object> timerMap = new TreeMap<String,Object>();
        timerMap.put("mean", timer.mean());
        timerMap.put("max", timer.max());
        timerMap.put("min", timer.min());
        timerMap.put("count", timer.count());
        Snapshot snapshot = timer.getSnapshot();
        timerMap.put("75th", snapshot.get75thPercentile());
        timerMap.put("95th", snapshot.get95thPercentile());
        timerMap.put("98th", snapshot.get98thPercentile());
        timerMap.put("99th", snapshot.get99thPercentile());
        timerMap.put("999th", snapshot.get999thPercentile());
        return timerMap;
    }
    
    public Map<String,Object> getOrCreateObserverInfo(Observer<DefaultMetrics> observer) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("configuration", observer.getConfiguration());
        map.put("snapshot", observer.getSnapshot());
        map.put("exception", observer.getException());
        map.put("delta", observer.getDeltaSnapshot());
        map.put("snapshot_attempts", observer.getSnapshotAttemptCounter());
        map.put("consecutive_snapshot_completed", observer.getConsecutiveSnapshotCompletedCount());
        map.put("consecutive_snapshot_failed", observer.getConsecutiveSnapshotFailedCount());
        map.put("snapshot_time", getOrCreateTimerInfo(observer.getResponseTimer()));
        return map;
    }
    
    @GET @Timed
    public String getServices(@QueryParam("verbose") BooleanParam verbose) throws Exception {
        // non-verbose mode is default (only list service names)
        if (verbose == null || !verbose.get().booleanValue()) {
            // create sorted array of observer configs
            TreeSet<String> sortedServicesNames = new TreeSet<String>(this.sos.getServiceObservers().keySet());
            return mapper.writeValueAsString(sortedServicesNames);
        } else {
            // create sorted map of service info
            TreeMap<String,Object> services = new TreeMap<String,Object>();
            for (ServiceObserver so : this.sos.getServiceObservers().values()) {
                Object info = getOrCreateServiceInfo((DefaultServiceObserver)so);
                services.put(so.getServiceName(), info);
            }
            return mapper.writeValueAsString(services);
        }
    }
    
    @GET @Path("/{serviceName}")
    public String getService(@PathParam("serviceName") String serviceName) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        Object info = getOrCreateServiceInfo(so);
        return mapper.writeValueAsString(info);
    }
    
    @GET @Timed @Path("/{serviceName}/observers")
    public String getObservers(@PathParam("serviceName") String serviceName, @QueryParam("verbose") BooleanParam verbose) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        
        // non-verbose mode is default (only list all observer names)
        if (verbose == null || !verbose.get().booleanValue()) {
            // create sorted array of observer configs
            TreeSet<String> sortedObserverNames = new TreeSet<String>(so.getObservers().keySet());
            return mapper.writeValueAsString(sortedObserverNames);
        } else {
            // create sorted map of observer info
            TreeMap<String,Object> observers = new TreeMap<String,Object>();
            for (Observer<DefaultMetrics> ob : so.getObservers().values()) {
                observers.put(ob.getName(), getOrCreateObserverInfo(ob));
            }
            return mapper.writeValueAsString(observers);
        }
    }
    
    @GET @Timed @Path("/{serviceName}/observers/{observerName}")
    public String getObserver(@PathParam("serviceName") String serviceName, @PathParam("observerName") String observerName) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        Observer<DefaultMetrics> observer = getObserver(so, observerName);
        Object info = getOrCreateObserverInfo(observer);
        return mapper.writeValueAsString(info);
    }
    
    @GET @Timed @Path("/{serviceName}/periods")
    public String getPeriods(@PathParam("serviceName") String serviceName, @PathParam("groupName") String groupName) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        List<String> periodNames = new ArrayList<String>();
        for (SummaryPeriod sp : so.getConfiguration().getPeriods()) {
            periodNames.add(sp.getName());
        }
        return mapper.writeValueAsString(periodNames);
    }
    
    @GET @Timed @Path("/{serviceName}/groups")
    public String getGroups(@PathParam("serviceName") String serviceName, @QueryParam("verbose") BooleanParam verbose) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        
        // non-verbose mode is default (only list all group names)
        if (verbose == null || !verbose.get().booleanValue()) {
            return mapper.writeValueAsString(so.getGroups());
        } else {
            Map<String,Set<String>> sortedGroupsWithMembers = new TreeMap<String,Set<String>>();
            // create a set of observers in each group
            for (String group : so.getGroups()) {
                Set<String> members = new TreeSet<String>();
                for (Observer ob : so.getObservers().values()) {
                    if (ob.getConfiguration().getGroups().contains(group)) {
                        members.add(ob.getName());
                    }
                }
                sortedGroupsWithMembers.put(group, members);
            }
            return mapper.writeValueAsString(sortedGroupsWithMembers);
        }
    }
    
    @GET @Timed @Path("/{serviceName}/groups/{groupName}")
    public String getGroup(@PathParam("serviceName") String serviceName, @PathParam("groupName") String groupName) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        
        // verify this group exists
        if (!so.getGroups().contains(groupName)) {
            throw new NotFoundException("group [" + groupName + "] does not exist");
        }
        
        Set<String> sortedMembers = new TreeSet<String>();
        // create a set of observers in each group
        for (Observer ob : so.getObservers().values()) {
            if (ob.getConfiguration().getGroups().contains(groupName)) {
                sortedMembers.add(ob.getName());
            }
        }
        return mapper.writeValueAsString(sortedMembers);
    }
    
    @GET @Timed @Path("/{serviceName}/groups/{groupName}/metrics")
    public String getGroupMetrics(@PathParam("serviceName") String serviceName, @PathParam("groupName") String groupName, @QueryParam("verbose") BooleanParam verbose) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        SummaryGroup<DefaultSummaryMetrics> group = getGroup(so, groupName);
        
        // non-verbose mode is default (only list all period names)
        if (verbose == null || !verbose.get().booleanValue()) {
            List<String> periodNames = new ArrayList<String>();
            for (SummaryPeriod sp : group.getValues().keySet()) {
                periodNames.add(sp.getName());
            }
            return mapper.writeValueAsString(periodNames);
        } else {
            // order of insertion important
            Map<String,DefaultSummaryMetrics> map = new LinkedHashMap<String,DefaultSummaryMetrics>();
            for (Map.Entry<SummaryPeriod,DefaultSummaryMetrics> entry : group.getValues().entrySet()) {
                map.put(entry.getKey().getName(), entry.getValue());
            }
            return mapper.writeValueAsString(map);
        }
    }
    
    @GET @Timed @Path("/{serviceName}/groups/{groupName}/metrics/{periodName}")
    public String getGroupMetric(@PathParam("serviceName") String serviceName, @PathParam("groupName") String groupName, @PathParam("periodName") String periodName) throws Exception {
        DefaultServiceObserver so = getServiceObserver(serviceName);
        SummaryGroup<DefaultSummaryMetrics> group = getGroup(so, groupName);
        DefaultSummaryMetrics metrics = getMetrics(group, periodName);
        return mapper.writeValueAsString(metrics);
    }
}