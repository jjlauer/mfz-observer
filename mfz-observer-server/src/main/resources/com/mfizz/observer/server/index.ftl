<#--
 #%L
 mfz-observer-server
 %%
 Copyright (C) 2012 mfizz
 %%
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 #L%
-->
<#escape x as x?html>
<!DOCTYPE html>
<html>
  <head>
    <title>observer-${configuration.id}</title>
    <meta charset="utf-8" />
    <meta name="description" content="Distributed observer server" />
    <link rel="stylesheet" href="/assets/simple.css" type="text/css" media="screen" />
    <body>
      <h1>Server Information</h1>
      <table>
        <tr><th>ID</th><td>${configuration.id}</td></tr>
        <tr><th>Version</th><td>${version}</td></tr>
        <tr><th>Now</th><td>${now}</td></tr>
      </table>

      <h1>Services [<a href="/api/services">json</a> | <a href="/api/services?verbose=true">verbose</a>]</h1>
      <table>
        <tr>
          <th>Name</th>
          <th>Step (ms)</th>
          <th colspan="4">Last Snapshot All</th>
          <th>Observers</th>
          <th>Groups</th>
          <th>Periods</th>
          <th>Metrics</th>
        </tr>
        <tr>
          <th></th>
          <th></th>
          <th>OK</th>
          <th>Failed</th>
          <th>Time (ms)</th>
          <th>DateTime</th>
          <th></th>
          <th></th>
          <th></th>
          <th></th>
        </tr>
        <#list serviceObservers as so> 
        <tr valign="top">
          <td align="center"><a href="/api/services/${so.serviceName}">${so.serviceName}</a></td>
          <td align="right">${so.configuration.stepMillis}</td>
          <td align="right"><#if so.lastResult??>${so.lastResult.snapshotsCompleted}</#if></td>
          <td align="right"><#if so.lastResult??>${so.lastResult.snapshotsFailed}</#if></td>
          <td align="right"><#if so.lastResult??>${so.lastResult.responseTime}</#if></td>
          <td align="right">${so.lastSnapshotAllDateTime!"N/A"}</td>
          <td align="center"><a href="/api/services/${so.serviceName}/observers">${so.observers?size}</a> [<a href="/api/services/${so.serviceName}/observers?verbose=true">verbose</a>]</td>
          <td align="center">
             <a href="/api/services/${so.serviceName}/groups">${so.groups?size}</a> [<a href="/api/services/${so.serviceName}/groups?verbose=true">verbose</a>]<br>
             <#list so.groups as g><#if so.groups?first != g>, </#if><a href="/api/services/${so.serviceName}/groups/${g}">${g}</a></#list>
          </td>
          <td align="center"><a href="/api/services/${so.serviceName}/periods">${so.periods?size}</a></td>
          <td>
            <#list so.summary?keys as g>
              <a href="/api/services/${so.serviceName}/groups/${g}/metrics?verbose=true">${g}</a> [<#list so.periods as p><#if (so.periods?first).name != p.name>, </#if><a href="/api/services/${so.serviceName}/groups/${g}/metrics/${p.name}">${p.name}</a></#list>]
              <#if (so.summary?keys)?last != g><br></#if>
            </#list>
          </td>
        </tr>
        </#list>
      </table>
    </body>
</html>
</#escape>