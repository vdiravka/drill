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
                <button type="button" class="btn btn-primary" onclick="location.href='/storage/${plugin.getName()}'">Update</button>
                <button type="button" class="btn btn-default" onclick="doEnable('${plugin.getName()}', false)">Disable</button>
                <button type="button" class="btn btn-default export" name="${plugin.getName()}" data-toggle="modal" data-target="#pluginsModal">Export</button>
              </td>
            </tr>
          </#if>
        </#list>
      </tbody>
    </table>
  </div>

  <div class="table-responsive" style="display: inline-block; margin-left: 100px;">
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
                <button type="button" class="btn btn-primary" onclick="location.href='/storage/${plugin.getName()}'">Update</button>
                <button type="button" class="btn btn-primary" onclick="doEnable('${plugin.getName()}', true)">Enable</button>
                <button type="button" class="btn btn-default export" name="${plugin.getName()}" data-toggle="modal" data-target="#pluginsModal">Export</button>
              </td>
            </tr>
          </#if>
        </#list>
      </tbody>
    </table>
  </div>

  <div class="page-header">
  </div>

  <div style="display: inline-block;/* width: 100%; *//* max-width: 536px; */position: relative;">
    <h4>New Storage Plugin</h4>
    <#--<form class="form-inline" id="newStorage" role="form" action="/" method="GET">-->
      <#--<div class="form-group">-->
        <#--<input type="text" class="form-control" id="storageName" placeholder="Storage Name">-->
      <#--</div>-->
      <#---->
    <#--</form>-->
    <button type="submit" class="btn btn-default" onclick="doSubmit()">Create</button>
  </div>

  <div style="display: inline-block;float: right;position: relative;">
    <h4>Export All Storage Plugin configs</h4>
    <div style="float: right;">
      <button type="button" id="export-all" class="btn btn-primary export" name="all" data-toggle="modal" data-target="#pluginsModal">Export</button>
    </div>
  </div>

  <#-- Modal window for exporting plugins -->
  <div class="modal fade" id="pluginsModal" tabindex="-1" role="dialog" aria-labelledby="exportPlugin" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="exportPlugin">Export Plugin configs</h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div id="fileType" style="display: inline-block; position: relative;">
            <label for="fileType">File type</label>
            <div class="radio">
              <label>
                <input type="radio" name="fileType" id="json" value="json" checked="checked">
                JSON
              </label>
            </div>
            <div class="radio">
              <label>
                <input type="radio" name="fileType" id="hocon" value="conf">
                HOCON
              </label>
            </div>
          </div>

          <div id="plugins-number" class="" style="display: inline-block; position: relative; float: right;">
            <label for="fileType">Plugins number</label>
            <div class="radio">
              <label>
                <input type="radio" name="number" id="all" value="all" checked="checked">
                ALL
              </label>
            </div>
            <div class="radio">
              <label>
                <input type="radio" name="number" id="enabled" value="enabled">
                ENABLED
              </label>
            </div>
            <div class="radio">
              <label>
                <input type="radio" name="number" id="disabled" value="disabled">
                DISABLED
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
          <button type="button" id="export" class="btn btn-primary">Export</button>
        </div>
      </div>
    </div>
  </div>
  <#-- Modal window for exporting plugins -->

  <#-- Modal window for creating plugin -->
  <div class="modal fade" id="pluginsModal" tabindex="-1" role="dialog" aria-labelledby="exportPlugin" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="exportPlugin">Configuration</h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form class="form-inline" id="newStorage" role="form" action="/" method="GET">
            <div class="form-group">
              <input type="text" class="form-control" id="storageName" placeholder="Storage Name">
            </div>

          </form>
          <h3>Configuration</h3>
          <form id="updateForm" role="form" action="/storage/${model.getName()}" method="POST">
            <input type="hidden" name="name" value="${model.getName()}" />
            <div class="form-group">
              <div id="editor" class="form-control"></div>
              <textarea class="form-control" id="config" name="config" data-editor="json" style="display: none;" >
              </textarea>
            </div>
            <a class="btn btn-default" href="/storage">Back</a>
            <button class="btn btn-default" type="submit" onclick="doUpdate();">
                  <#if model.exists()>Update<#else>Create</#if>
            </button>
            <#if model.exists()>
              <#if model.enabled()>
                <a id="enabled" class="btn btn-default">Disable</a>
              <#else>
                <a id="enabled" class="btn btn-primary">Enable</a>
              </#if>
            <#--<a class="btn btn-default" href="#" name="${model.getName()}" title="File type" data-toggle="popover" data-trigger="focus" data-placement="bottom" data-popover-content=".list-popover">Export</a>-->
              <button type="button" class="btn btn-default export" name="${model.getName()}" data-toggle="modal" data-target="#pluginsModal">Export</button>
              <a id="del" class="btn btn-danger" onclick="deleteFunction()">Delete</a>
            </#if>
            </form>
            <br>
          <div id="message" class="hidden alert alert-info">
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
          <button type="button" id="create" class="btn btn-primary">Create</button>
        </div>
      </div>
    </div>
  </div>
  <#-- Modal window for creating plugin -->

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

    let exportInstance;
    $('#pluginsModal').on('show.bs.modal', function(event) {
      const button = $(event.relatedTarget); // Button that triggered the modal
      const modal = $(this);
      exportInstance = button.attr("name");

      const optionalBlock = modal.find('#plugins-number');
      if (exportInstance === "all") {
        optionalBlock.removeClass('hide');
        modal.find('.modal-title').text('Export All Plugin configs');
      } else {
        modal.find('#plugins-number').addClass('hide');
        modal.find('.modal-title').text('Export '+ exportInstance.toUpperCase() +' Plugin configs');
        // alert("1" + exportInstance);
      }

      modal.find('#export').click(function() {
        let fileType;
        if (modal.find('#json').is(":checked")) {
          fileType = 'json';
        }
        if (modal.find('#hocon').is(":checked")) {
          fileType = 'conf';
        }
        // alert($(event.relatedTarget).attr("name"));
        // alert("2 "+ exportInstance);
        let url;
        if (exportInstance === "all") {
          let pluginsNumber = "";
          if (modal.find('#all').is(":checked")) {
            pluginsNumber = 'all';
          } else if (modal.find('#enabled').is(":checked")) {
            pluginsNumber = 'enabled';
          } else if (modal.find('#disabled').is(":checked")) {
            pluginsNumber = 'disabled';
          }
          url = '/storage/' + pluginsNumber + '/plugins/export/' + fileType;
        } else {
          url = '/storage/' + exportInstance + '/export/' + fileType;
        }
        window.open(url);
      });
    });

    $('#pluginsModal').on('hidden.bs.modal', function () {
      const modal = $(this);
      const pluginsNumber = modal.find('#plugins-number');
      if (!pluginsNumber.hasClass('hide')) {
        pluginsNumber.addClass('hide');
      }
    })

  </script>
  <style>
    .modal-dialog{
      position: relative;
      display: table;
      overflow-y: auto;
      overflow-x: auto;
      width: auto;
      min-width: 300px;
    }
  </style>
</#macro>

<@page_html/>