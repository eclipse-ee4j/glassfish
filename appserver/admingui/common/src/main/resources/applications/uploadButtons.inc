<!--

    Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<!-- applications/uploadButtons.inc -->

<sun:button id="uploadButton" text="$resource{i18n.button.OK}"
    onClick="javascript:
        if(document.getElementById('#{pageSession.radioChoosenId}').value=='client') {
            result = (admingui.deploy.checkFileInputRequired('#{fileuploadId}', '#{reqMsg}'));
        } else {
            result = (checkRequired('#{dirPathId}', '#{reqMsg}'));
        }
        if (result == true) {
	    if (admingui.deploy.checkType('#{pageSession.typeId}', '$resource{i18n.msg.JS.deploy.selectType}' )){
                if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                    if (admingui.deploy.checkTarget('#{pageSession.onlyDASExist}', '#{pageSession.commonAddRemoveId}'+'_list_value', '$resource{i18n.msg.JS.deploy.confirmNoTarget}')){
                        disableAllButtons();
                        return submitAndDisable(this, '$resource{i18n.button.Processing}', '_self');
                    }
                }
	    }
        }
        return false;"
     >
    <!command
        compare(obj1="#{uploadRdBtn}" obj2="client" objEqual=>$attribute{needUpload});
        if (${needUpload}){
            gf.logger(logString="GUI deployment: uploadToTempfile", level="INFO");
            uploadFileToTempDir(file="#{uploadedFile}" uploadedTempFile="#{requestScope.filePath}", origPath="#{requestScope.origPath}");
            gf.logger(logString="filePath=#{requestScope.filePath} origPath=#{requestScope.origPath}", level="FINE");
        }
        if (! ${needUpload}){
            setAttribute(key="origPath", value="#{dirPath}");
            setAttribute(key="filePath", value="#{dirPath}");
            gf.logger(logString="No upload needed. origPath=#{requestScope.origPath}, filePath=#{requestScope.filePath} ",level="FINE");

        }
        deploy(filePath="$attribute{filePath}"
              origPath="$attribute{origPath}"
              allMaps = "#{pageSession.deployMap}"
              appType = "#{pageSession.appType}"
              propertyList="$attribute{newList}"
              targets = "#{pageSession.selectedTargets}"
        );
        if( ${needUpload}) {
            gf.logger(logString="Deleting the file uploaded to Temp Directory", level="INFO");
            deleteFileFromTempDir(deleteTempFile="$attribute{filePath}");
        }
	gf.redirect(page="#{request.contextPath}/common/removeFrame.jsf?#{pageSession.listPageLink}&#{extraArgs}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&bare=true");
    />
</sun:button>

<sun:button id="cancelButton" text="$resource{i18n.button.Cancel}" primary="#{false}" immediate="true"
    onClick="top.admingui.ajax.loadPage({url:'#{request.contextPath}/#{pageSession.cancelPage}?#{pageSession.extraArgs}'}); return false;">
</sun:button>
