<!--

    Copyright (c) 2023 Contributors to the Eclipse Foundation
    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

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

function enableSecurityMapFields(opt){
    if(opt == 'principals') {
 	enableComponent('#{principalsId}', 'text');
        getTextElement('#{userGroupsId}').value='';
        disableComponent('#{userGroupsId}', 'text');
	getTextElement('#{principalsId}').focus();
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{pPropId}:optB', true); });
    }else{
        getTextElement('#{principalsId}').value='';
	disableComponent('#{principalsId}', 'text');
        enableComponent('#{userGroupsId}', 'text');
        getTextElement('#{userGroupsId}').focus();
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{grpPropId}:optA', true); });
    }
 }


function checkClassname(){
    //alert('in checkClassname');
    var opt = getSelectedValueFromForm(document.forms['propertyForm'], 'usersOption');
    //alert('opt='+opt);
    if (opt == 'users'){
        if(checkForSelectedValue('#{userGroupsId}')){
	    return true;
	}else{
            return showAlert('$resource{i18njca.msg.JS.securityMap.enterUserGroups}')
	}
    }
    if(opt == 'principals' ){
	if(checkForSelectedValue('#{principalsId}')){
	    return true;
	}else{
            return showAlert('$resource{i18njca.msg.JS.securityMap.enterPrincipals}')
	}
    }
}

function enableWorkSecurityMapFields(opt){
    if(opt == 'principals') {
        var val = getTextElement('#{eisuserGroupsId}').value;
        disableComponent('#{eisuserGroupsId}', 'text');
        getTextElement('#{eisuserGroupsId}').value=val;
        enableComponent('#{eisprincipalsId}', 'text');
        getTextElement('#{eisprincipalsId}').focus();
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{pPropId}:optB', true); });
    }else{
        var val = getTextElement('#{eisprincipalsId}').value;
	disableComponent('#{eisprincipalsId}', 'text');
        getTextElement('#{eisprincipalsId}').value=val;
        enableComponent('#{eisuserGroupsId}', 'text');
        getTextElement('#{eisuserGroupsId}').focus();
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{grpPropId}:optA', true); });
    }
 }

function isSecurityMappingPresent(reqMsg) {
    var isGroup = document.getElementById('#{grpPropId}:optA').checked;
    var isPrincipal = document.getElementById('#{pPropId}:optB').checked;
    if (isGroup) {
        var comp = document.getElementById('#{eisuserGroupsId}');
        var groups = comp.value;
        if (groups == null || groups == '') {
            comp.select();
            comp.focus();
            return showAlert(reqMsg + ' ' + getLabel(comp));
        }
    }
    if (isPrincipal) {
        var comp = document.getElementById('#{eisprincipalsId}');
        var principals = comp.value;
        if (principals == null || principals == '') {
            comp.select();
            comp.focus();
            return showAlert(reqMsg + ' ' + getLabel(comp));
        }
    }
    return true;
}

</script>
</f:verbatim>
