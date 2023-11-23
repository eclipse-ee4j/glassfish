<!--

    Copyright (c) 2023 Contributors to the Eclipse Foundation
    Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.

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
        function disableTableField(selStr, selValidCNVal) {
            var val = document.getElementById("$pageSession{valMethod}").value;
            var isConnValidation = document.getElementById("$pageSession{conValidationId}").checked;
            if (isConnValidation && val == 'table'){
                disableAllJdbcRadioButtons(false, 'table');
                var index = findSelectOptionIndex(document.getElementById("$pageSession{tableDropdownTextId}"), selStr);
                var optionsExists = (document.getElementById("$pageSession{tableDropdownTextId}").options.length > 0);
                var isSelStrExists = (selStr != null) && (selStr != '') && (isWhitespace(selStr) == false);
                if (index == undefined || index == -1) {
                    if (isSelStrExists == true) {
                        enableJdbcRadioButtons('text', 'table');
                    }
                    else {
                        if (optionsExists == true) {
                            enableJdbcRadioButtons('dropdown', 'table');
                        } else {
                            enableJdbcRadioButtons('text', 'table');
                        }
                    }
                } else {
                    if(index >= 0) {
                        document.getElementById('$pageSession{tableTextId}').value='';
                        enableJdbcRadioButtons('dropdown', 'table');
                    }
                    else if('$pageSession{myTableOption}' == 'dropdown') {
                        enableJdbcRadioButtons('dropdown', 'table');
                    } else {
                        enableJdbcRadioButtons('text', 'table');
                    }
                }
                enableBtnComponent('$pageSession{tableNamesButtonId}');
                disableComponent('$pageSession{validCNDropdownTextId}', 'select');
                disableTextComponent('$pageSession{validCNTextId}');
                disableAllJdbcRadioButtons(true, 'validCN');
            }else if (isConnValidation && val == 'custom-validation') {
                disableAllJdbcRadioButtons(false, 'validCN');
                disableBtnComponent('$pageSession{tableNamesButtonId}');
                var dropDownVal = document.getElementById("$pageSession{validCNDropdownTextId}").value;
                if (dropDownVal == '') {
                    enableJdbcRadioButtons('text', 'validCN');
                } else {
                    if(dropDownVal == selValidCNVal) {
                        document.getElementById('$pageSession{validCNTextId}').value='';
                        enableJdbcRadioButtons('dropdown', 'validCN');
                    }
                    else if('$pageSession{myValidCNOption}' == 'dropdown') {
                        enableJdbcRadioButtons('dropdown', 'validCN');
                    } else {
                        enableJdbcRadioButtons('text', 'validCN');
                    }
                }
                disableComponent('$pageSession{tableDropdownTextId}', 'select');
                disableTextComponent('$pageSession{tableTextId}');
                disableAllJdbcRadioButtons(true, 'table');
            }else{
                disableComponent('$pageSession{tableDropdownTextId}', 'select');
                disableBtnComponent('$pageSession{tableNamesButtonId}');
                disableTextComponent('$pageSession{tableTextId}');
                disableAllJdbcRadioButtons(true, 'table');
                disableComponent('$pageSession{validCNDropdownTextId}', 'select');
                disableTextComponent('$pageSession{validCNTextId}');
                disableAllJdbcRadioButtons(true, 'validCN');
            }
        }
        function disableTextComponent(textId) {
            var textVal = document.getElementById(textId).value;
            disableComponent(textId, 'text');
            document.getElementById(textId).value=textVal;
        }
        function getDropdownInfo(tableAttrs, validCNAttrs, tableVal, validCNVal) {
            var isConnValidation = document.getElementById("$pageSession{conValidationId}").checked;
            var methodVal = document.getElementById("$pageSession{valMethod}").value;
            if (isConnValidation && methodVal == 'custom-validation'){
                var c= validCNAttrs.toString();
                admingui.ajax.invoke("gf.restRequest", {
                    endpoint:"#{sessionScope.REST_URL}/resources/get-validation-class-names",
                    attrs:c,
                    method:"get",
                    result:""}, function(result) {
                    processResult(result, validCNVal, "validationClassNames=", "$pageSession{validCNDropdownTextId}");
                }, 3, false);
            }
        }

        function getValidationTableNames(tableAttrs, tableVal) {
            var b= tableAttrs.toString();
            var size = document.getElementById("$pageSession{tableDropdownTextId}").options.length;
            if (size < 1) {
                admingui.ajax.invoke("gf.restRequest", {
                    endpoint:"#{sessionScope.REST_URL}/resources/get-validation-table-names",
                    attrs:b,
                    method:"get",
                    result:""}, function(result) {
                    processResult(result, tableVal, "validationTableNames=", "$pageSession{tableDropdownTextId}");
                }, 3, false);
            }
        }

        function processResult(result, selStr, searchStr, dropdownId) {
            if(result.result.data != undefined){
                var exitCode = result.result.data.exit_code.toString();
                if (exitCode == "SUCCESS") {
                    var res = result.result.data.extraProperties.toString();                 
                    if (res != null) {
                        var index = res.indexOf(searchStr);
                        if (index != -1) {
                            var lastIndex = res.indexOf("]", index + searchStr.length + 1);
                            var tableNamesStr = res.substring(index + searchStr.length + 1, lastIndex);
                            setDropdownOptions(tableNamesStr, selStr, dropdownId);
                        }
                    }
                }
            }
        }
        function setDropdownOptions(tableNamesStr, selStr, dropdownId) {
            var strs = tableNamesStr.split(", ");
            for (var i = 0; i < strs.length; i++){
                var optn;
                optn = document.createElement("option");
                optn.value = optn.text = strs[i];
                if(selStr == strs[i]) {
                    optn.selected = true;
                }
                document.getElementById(dropdownId).options.add(optn);
            }            
        }
        function disableAllJdbcRadioButtons(opt, type) {
            if (type=='table') {
                disableTwoRadioButtons(opt, '$pageSession{tablePropId}', '$pageSession{tableOptionId}');
            } else {
                disableTwoRadioButtons(opt, '$pageSession{validCNPropId}', '$pageSession{validCNOptionId}');
            }
        }
        function enableJdbcRadioButtons(opt, type) {
            if (type=='table') {
                enableRadioButtons(opt, '$pageSession{tableTextId}', '$pageSession{tableDropdownTextId}', '$pageSession{tablePropId}', '$pageSession{tableOptionId}');
            } else {
                enableRadioButtons(opt, '$pageSession{validCNTextId}', '$pageSession{validCNDropdownTextId}', '$pageSession{validCNPropId}', '$pageSession{validCNOptionId}');
            }
        }
        function enableRadioButtons(opt, textId, dropdownId, propId, optionId){
            if(opt == 'dropdown') {
                var textVal = document.getElementById(textId).value;
                disableComponent(textId, 'text');
                document.getElementById(textId).value=textVal;
                enableComponent(dropdownId, 'select');
                require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked(propId+':optB', false); });
                require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked(propId+':optA', true); });
                document.getElementById(optionId).value='dropdown';
            }else{
                enableComponent(textId, 'text');
                disableComponent(dropdownId, 'select');
                getTextElement(textId).focus();
                require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked(propId+':optB', true); });
                require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setChecked(propId+':optA', false); });
                document.getElementById(optionId).value='text';
            }         
      }
      function disableTwoRadioButtons(opt, propId, optionId){
          require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setDisabled(propId+':optA', opt); });
          require(['webui/suntheme/radiobutton'], function (radiobutton) { radiobutton.setDisabled(propId+':optB', opt); });
          document.getElementById(optionId).value='';
      }
    </script>
</f:verbatim>   
