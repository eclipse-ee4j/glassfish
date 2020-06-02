<!--

    Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.

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

<f:verbatim>
    <script type="text/javascript">
    function showSection(type, sshSection, sshDirId, configDirId, installProp, archProp){
         var sshSheet = document.getElementById(sshSection);
         if (type == 'CONFIG'){
             sshSheet.style.display = 'none';
             
             require(['webui/suntheme/common'], function (common) {
             	 common.setVisible(sshDirId, false);
             	 common.setVisible(configDirId, true);				
			  });
             

             if (installProp != null){

             require(['webui/suntheme/common'], function (common) {
                 common.setVisible(installProp, false);
                 common.setVisible(archProp, false);
			  });

             }
         }
         if (type == 'SSH'){
             sshSheet.style.display = 'block';
             
             require(['webui/suntheme/common'], function (common) {
              common.setVisible(sshDirId, true);
              common.setVisible(configDirId, false);
 			  });
             
             
             if(installProp != null){
             require(['webui/suntheme/common'], function (common) {
                common.setVisible(installProp, true);
                common.setVisible(archProp, true);
 			  });             
             }
         }
    }

    function showSshPswdSection(type, ps1Id, pswdAId){
        if (type == '1'){
            hide(ps1Id);
            hide(pswdAId);
            show('#{pageSession.setupSshPropId}');
            show('#{pageSession.keyfilePropId}');
            showSetupSSH();
            return;
        }
        if (type == '2'){
            show(ps1Id);
            hide(pswdAId);
            hide('#{pageSession.setupSshPropId}');
            hide('#{pageSession.keyfilePropId}');
            hide('#{pageSession.generateId}');
            hide('#{pageSession.setupPswdPropId}');
            hide('#{pageSession.publicKeyId}');
            return;
        }
        if (type == '3'){
            hide(ps1Id);
            show(pswdAId);
            hide('#{pageSession.setupSshPropId}');
            hide('#{pageSession.keyfilePropId}');
            hide('#{pageSession.generateId}');
            hide('#{pageSession.setupPswdPropId}');
            hide('#{pageSession.publicKeyId}');
            return;
        }
    }

    function showPswdSection(type, ps1Id, pswdAId){
        if (type == '1'){
            hide(ps1Id);
            hide(pswdAId);
            return;
        }
        if (type == '2'){
            show(ps1Id);
            hide(pswdAId);
            return;
        }
        if (type == '3'){
            hide(ps1Id);
            show(pswdAId);
            return;
        }
    }

    function showSetupSSH(){
        var checked = document.getElementById('#{pageSession.setupSshId}').checked;
        if (checked){
            show('#{pageSession.generateId}');
            show('#{pageSession.setupPswdPropId}');
            show('#{pageSession.publicKeyId}');
        }else{
            hide('#{pageSession.generateId}');
            hide('#{pageSession.setupPswdPropId}');
            hide('#{pageSession.publicKeyId}');
        }

    }
    function checkSSHPswd(typeId, nodeHostId, psSelectId, ps1Id, setupSshId, setupPswdId, winPsSelectId, winPs1Id){
        if (! guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
	    return false;
	}
        var atype=document.getElementById(typeId).value;
        if ( atype == 'CONFIG'){
            return true;
        }

        if (!checkRequired(nodeHostId, '#{reqMsg}')){
            return false;
        }
        if (atype == 'SSH'){
            if ( document.getElementById(psSelectId).value == '2'){
                if (!checkRequired(ps1Id, '#{reqMsg}')){
                    return false;
                }else{
                    //ensure don't call setup-ssh'
                    document.getElementById(setupSshId).checked=false;
                    return true;
                }
            }
            if ( document.getElementById(psSelectId).value == '3'){
                //must have pswd alias specified
                var alcomp = document.getElementById('#{pageSession.pswdAliasId}');
                if (alcomp.value=='') {
                    return showAlert('$resource{i18ncs.msg.JS.noPasswordAlias}');
                }else{
                    //ensure don't call setup-ssh'
                    document.getElementById(setupSshId).checked=false;
                    return true;
                }
            }
            if ( (document.getElementById(psSelectId).value == '1') &&
                 (document.getElementById(setupSshId).checked ) &&
                 (document.getElementById(setupPswdId).value == '' ) ){
                    return checkRequired(setupPswdId, '#{reqMsg}');
                 }
            return true;
        }

        return true;
    }


function confirmEmptyPswd(ps1Id, emptyMsg) {
    var psComp = document.getElementById(ps1Id);
    var pswd = psComp.value;
    if (pswd == null || pswd == ''){
        psComp.select();
        psComp.focus();
        if ( getConfirm(this, emptyMsg) ){
            return true;
        }else{
            return false;
        }
    }
    return true;
}

    
    function disableTextComponent(textId) {
        var comp = document.getElementById(textId);
        if (comp != null){
            var textVal = comp.value;
            disableComponent(textId, 'text');
            comp.value=textVal;
        }
    }

    function  showTestDir(validateId, testdirId ){
        var ss = document.getElementById(validateId).checked;
        if (ss){
			require(['webui/suntheme/common'], function (common) {
			    common.setVisible(testdirId, true);
			});        
        }else{
			require(['webui/suntheme/common'], function (common) {
			    common.setVisible(testdirId, false);
			}); 
        }
    }

    function show(compId){
			require(['webui/suntheme/common'], function (common) {
			    common.setVisible(compId, true);
			});    
     }

    function hide(compId){
			require(['webui/suntheme/common'], function (common) {
			    common.setVisible(compId, false);
			});       
    }

    </script>
</f:verbatim>
