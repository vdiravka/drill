<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<#include "*/generic.ftl">
<#macro page_head>
</#macro>

<#macro page_body>
  <div class="page-header">
  </div>

  <div class="table-responsive" style="display: inline-block;">
      <#--background-color:blue;-->
      <#--TODO: transform second table for disabled plugins as a second column for the first table-->
    <h4>Enabled Storage Plugins</h4>
    <table class="table">
      <tbody>
        <#list model as plugin>
          <#if plugin.enabled() == true>
            <tr>
              <td style="border:none; width:200px;">
                ${plugin.getName()}
              </td>
              <td style="border:none;">
                <a class="btn btn-primary" href="/storage/${plugin.getName()}">Update</a>
                <a class="btn btn-default" onclick="doEnable('${plugin.getName()}', false)">Disable</a>
                <a class="btn btn-default" href="#" name="${plugin.getName()}" title="File type" rel="plugin-popover" data-trigger="focus" data-placement="right" data-popover-content=".list-popover">Export</a>
              </td>
            </tr>
          </#if>
        </#list>
      </tbody>
    </table>
  </div>
  <#--<div class="page-header">-->
  <#--</div>-->

  <div class="table-responsive" style="display: inline-block">
      <#--background-color:red;-->
    <h4>Disabled Storage Plugins</h4>
    <table class="table">
      <tbody>
        <#list model as plugin>
          <#if plugin.enabled() == false>
            <tr>
              <td style="border:none; width:200px;">
                ${plugin.getName()}
              </td>
              <td style="border:none;">
                <a class="btn btn-primary" href="/storage/${plugin.getName()}">Update</a>
                <a class="btn btn-primary" onclick="doEnable('${plugin.getName()}', true)">Enable</a>
                <a class="btn btn-default" href="#" name="${plugin.getName()}" title="File type" rel="plugin-popover" data-trigger="focus" data-placement="right" data-popover-content=".list-popover">Export</a>
              </td>
            </tr>
          </#if>
        </#list>
      </tbody>
    </table>
  </div>
  <div class="page-header">
  </div>
  <div>
    <h4>New Storage Plugin</h4>
    <form class="form-inline" id="newStorage" role="form" action="/" method="GET">
      <div class="form-group">
        <input type="text" class="form-control" id="storageName" placeholder="Storage Name">
      </div>
      <button type="submit" class="btn btn-default" onclick="doSubmit()">Create</button>
    </form>
  </div>
  <div class="page-header">
  </div>
  <div>
    <h4>Export Storage Plugins</h4>
    <#--<label for="fileType">File type</label>-->
    <#--<div class="radio">-->
      <#--<label>-->
        <#--<input type="radio" name="fileType" id="json" value="json" checked>-->
        <#--JSON-->
      <#--</label>-->
    <#--</div>-->
    <#--<div class="radio">-->
      <#--<label>-->
        <#--<input type="radio" name="fileType" id="hocon" value="conf">-->
        <#--HOCON-->
      <#--</label>-->
    <#--</div>-->
    <#--<a class="btn btn-default" href="/storage/export_all/"">Export all</a>-->
  <#--</div>-->
    <a class="btn btn-default" href="#" name="enabled" title="File type" rel="all-plugins-popover" data-trigger="focus" data-placement="right" data-popover-content=".list-popover">Export enabled</a>
    <a class="btn btn-default" href="#" name="disabled" title="File type" rel="all-plugins-popover" data-trigger="focus" data-placement="right" data-popover-content=".list-popover">Export disabled</a>
    <a class="btn btn-default" href="#" name="all" title="File type" rel="all-plugins-popover" data-trigger="focus" data-placement="right" data-popover-content=".list-popover">Export all</a>
  </div>
  <!-- Content for Export Plugins -->
  <div class="list-popover hide">
    <ul class="nav nav-pills nav-stacked">
      <li><a class="JSON btn btn-default">Export as JSON</a></li>
      <li><a class="HOCON btn btn-default">Export as HOCON</a></li>
    </ul>
  </div>

  <script>
    function doSubmit() {
      const name = document.getElementById("storageName");
      const form = document.getElementById("newStorage");
      form.action = "/storage/" + name.value;
      form.submit();
    }
    function doEnable(name, flag) {
      $.get("/storage/" + name + "/enable/" + flag, function(data) {
        location.reload();
      });
    }
    $('[rel="plugin-popover"]').popover({
      container: 'body',
      html: true,
      content: function () {
        const plugin = $($(this)).attr("name");
        $('.JSON').attr("href", "/storage/" + plugin + "/export/json");
        $('.HOCON').attr("href", "/storage/" + plugin + "/export/hocon");
        return $($(this).data('popover-content')).clone().html();
      }
    })
    $('[rel="all-plugins-popover"]').popover({
      container: 'body',
      html: true,
      content: function () {
        const plugin = $($(this)).attr("name");
        $('.JSON').attr("href", "/storage/" + plugin + "/export/json");
        $('.HOCON').attr("href", "/storage/" + plugin + "/export/hocon");
        return $($(this).data('popover-content')).clone().html();
      }
    })
  </script>
</#macro>

<@page_html/>