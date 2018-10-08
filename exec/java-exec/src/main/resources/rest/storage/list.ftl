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
                <#--<a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export</a>-->
                <a href="#" class="btn btn-default" name="${plugin.getName()}" title="File type" data-toggle="popover" data-trigger="focus" data-placement="top" data-popover-content="#enabled-plugins">Export</a>
                <#--TODO: proper plugin.getName() jstl in pop-up windows (here only "cp" is obtained)-->
                <#--<div id="enabled-plugins" class="hide">-->
                  <ul id="enabled-plugins" class="nav nav-pills nav-stacked hide">
                    <li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as JSON</a></li>
                    <li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as HOCON</a></li>
                  </ul>
                <#--</div>-->
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
                <a href="#" class="btn btn-default" name="${plugin.getName()}" title="File type" data-toggle="popover" data-trigger="focus" data-placement="top" data-popover-content="#disabled-plugins">Export</a>
              </td>
            </tr>
            <div id="disabled-plugins" class="hide">
              <ul class="nav nav-pills nav-stacked">
                <li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as JSON</a></li>
                <li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as HOCON</a></li>
              </ul>
            </div>
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
    <label for="fileType">File type</label>
    <div class="radio">
      <label>
        <input type="radio" name="fileType" id="json" value="json" checked>
        JSON
      </label>
    </div>
    <div class="radio">
      <label>
        <input type="radio" name="fileType" id="hocon" value="conf">
        HOCON
      </label>
    </div>
    <a class="btn btn-default" href="/storage/export_all/"">Export all</a>
  </div>

  <!-- Content for Export #1 -->
  <#--<div id="popover-content" class="hide">-->
    <#--<ul class="nav nav-pills nav-stacked">-->
      <#--<li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as JSON</a></li>-->
      <#--&lt;#&ndash;<li><a class="btn btn-default" href="/storage/${plugin.getName()}/export">Export as HOCON</a></li>&ndash;&gt;-->
      <#--<li><a class="btn btn-default" href="#">Export as JSON</a></li>-->
      <#--<li><a class="btn btn-default" href="/lol/">Export as HOCON</a></li>-->
    <#--</ul>-->
  <#--</div>-->

  <script>
    function doSubmit() {
      var name = document.getElementById("storageName");
      var form = document.getElementById("newStorage");
      form.action = "/storage/" + name.value;
      form.submit();
    }
    function doEnable(name, flag) {
      $.get("/storage/" + name + "/enable/" + flag, function(data) {
        location.reload();
      });
    }
    $(function(){
        $('[data-toggle="popover"]').popover({
            container: 'body',
            html: true,
            content: function () {
                var clone = $($(this).data('popover-content')).clone(true).removeClass('hide');
                // OBJECT.attr("name") TODO: get name of main button and transfer it to children buttons
                return clone;
            }
        }).click(function(e) {
            e.preventDefault();
        });
    });
  </script>
</#macro>

<@page_html/>