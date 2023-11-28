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

 function initClassname(option) {
    var selectedOption = getSelectedValueFromForm(document.forms['form1'], 'classnameOption');
    enableClassnameFields(option);   
    if (option == 'input'){ 
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{rbPropId}:optB', true); });
    }else{  
        require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked('#{rbPropId}:optA', true); });
    }  
  }

 function enableClassnameFields(opt){
    if(opt == 'input') {
	enableComponent('#{classnameTextId}', 'text');  
	getTextElement('#{classnameTextId}').focus(); 
	require(['webui/suntheme/dropDown'], function (dropDown) { dropDown.setDisabled('#{classnameDropdownId}', true); });
        //showDisplay('none');
        document.getElementById('form1:option').value='input'; 
    }else{
        //We need to set a timeout to delay the call to getTextElement inside disable component.
        //otherwise getTextElement will always return null, causing JS error.
	window.setTimeout("disableComponent('#{classnameTextId}', 'text')", 1); 
	require(['webui/suntheme/dropDown'], function (dropDown) { dropDown.setDisabled('#{classnameDropdownId}', false); });
        //showDisplay('');
        document.getElementById('form1:option').value='predefine';
    }
 }
 var factoryMap = #{pageSession.factoryMap};
 function updateFactoryClass(selected) {
    var component = document.getElementById("#{pageSession.factoryClassTextId}");
    component.value = factoryMap[selected];
 }
 function updateFactoryClassOnClick() {
    var classDD = document.getElementById("#{pageSession.classnameDropdownId}");
    var component = document.getElementById("#{pageSession.factoryClassTextId}");
    component.value = factoryMap[classDD.value];
 }
</script>
</f:verbatim>

