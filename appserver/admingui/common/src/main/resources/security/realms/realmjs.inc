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

 function initClassname(option, createFlag) {
    var selectedOption = getSelectedValueFromForm(document.forms['form1'], 'classnameOption');
    enableClassnameFields(option, createFlag);
    if (option == 'input'){ 
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{rbPropId}:optB', true); });
    }else{  
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{rbPropId}:optA', true); });
    }  
  }

 function enableClassnameFields(opt, createFlag){
    if(opt == 'input') {
	enableComponent('#{classnameTextId}', 'text');
        if (createFlag){
            getTextElement('#{classnameTextId}').focus();
        }
	require(['webui/suntheme/dropDown'], function (dropDown) { dropDown.setDisabled('#{classnameDropdownId}', true); });
        showDisplay('none'); 
        document.getElementById('form1:option').value='input'; 
    }else{
        //We need to set a timeout to delay the call to getTextElement inside disable component.
        //otherwise getTextElement will always return null, causing JS error.
	window.setTimeout("disableComponent('#{classnameTextId}', 'text')", 1); 
	require(['webui/suntheme/dropDown'], function (dropDown) { dropDown.setDisabled('#{classnameDropdownId}', false); });
        showDisplay(''); 
        document.getElementById('form1:option').value='predefine'; 
    }
 }

function checkClassname() {
    var opt = getSelectedValueFromForm(document.forms['form1'], 'classnameOption');
    if (opt == 'predefine') {
	type = getSelectElement('#{classnameDropdownId}').value;
	if (type.match('FileRealm') != null) {
	    return (checkRequired('form1:fileSection:jaax:jaax', '#{reqMsg}')  &&
		    checkRequired('form1:fileSection:keyFile:keyFile',  '#{reqMsg}')  )
	} else if (type.match('LDAPRealm') != null) {
	    return ( checkRequired(id="form1:ldapSection:jaax:jaax" , '#{reqMsg}') &&
		checkRequired(id="form1:ldapSection:directory:directory" , '#{reqMsg}')  &&
		checkRequired(id="form1:ldapSection:baseDn:baseDn" , '#{reqMsg}'))
	} else if (type.match('SolarisRealm') != null) {
	    return checkRequired(id="form1:solarisSection:jaax:jaax" , '#{reqMsg}') ;
	} else if (type.match('JDBCRealm') != null) {
	    return checkRequired(id="form1:jdbcSection:jaax:jaax" , '#{reqMsg}' ) &&
		checkRequired(id="form1:jdbcSection:jndi:jndi" , '#{reqMsg}' )  &&
		checkRequired(id="form1:jdbcSection:userTable:userTable" , '#{reqMsg}' )  &&
		checkRequired(id="form1:jdbcSection:userName:userName", '#{reqMsg}' )  &&
		checkRequired(id="form1:jdbcSection:pswd:pswd" , '#{reqMsg}' )  &&
		checkRequired(id="form1:jdbcSection:groupTable:groupTable" , '#{reqMsg}' )  &&
		checkRequired(id="form1:jdbcSection:groupName:groupName" , '#{reqMsg}' ) &&
                checkRequired(id="form1:jdbcSection:pswdEnc:pswdEnc" , '#{reqMsg}' );
	} else if (type.match('PamRealm') != null) {
	    return checkRequired(id="form1:pamSection:jaax:jaax" , '#{reqMsg}') ;
	} else if (type == '') {
	    return showAlert('$resource{i18n.msg.JS.realm.needClassName}');
	}
	// No special options to check for this type... continue
	return true;
    } else if (opt == 'input' ) {
	cla = getTextElement('#{classnameTextId}').value;
	if (checkForSelectedValue('#{classnameTextId}')) {
	    return true;
	} else {
	    return showAlert('$resource{i18n.msg.JS.realm.needClassName}');
	}
    }
}

function showDisplay(testtype) {
    document.getElementById('#{fileSectionId}').style.display = 'none'; 
    document.getElementById('#{ldapSectionId}').style.display = 'none'; 
    document.getElementById('#{jdbcSectionId}').style.display = 'none'; 
    document.getElementById('#{certSectionId}').style.display = 'none'; 
    document.getElementById('#{solarisSectionId}').style.display = 'none';
    document.getElementById('#{pamSectionId}').style.display = 'none';
    
    if (testtype.match('none')){  
        return;  
    }  
    type = getSelectElement('#{classnameDropdownId}').value;
    rtype = type.match('FileRealm');  
    if (rtype != null){  
        document.getElementById('#{fileSectionId}').style.display = 'block';   
        return;  
    }  
    rtype = type.match('LDAPRealm');  
    if (rtype != null){  
        document.getElementById('#{ldapSectionId}').style.display = 'block';   
        return;  
    }  
    rtype = type.match('SolarisRealm');  
    if (rtype != null){  
        document.getElementById('#{solarisSectionId}').style.display = 'block';   
        return;  
    }  
    rtype = type.match('JDBCRealm');  
    if (rtype != null){  
        document.getElementById('#{jdbcSectionId}').style.display = 'block';   
        return;  
    }  
    rtype = type.match('CertificateRealm');  
    if (rtype != null){  
        document.getElementById('#{certSectionId}').style.display = 'block';   
        return;  
    }
    rtype = type.match('PamRealm');
    if (rtype != null){
        document.getElementById('#{pamSectionId}').style.display = 'block';
        return;
    }
}    

</script>
</f:verbatim>

