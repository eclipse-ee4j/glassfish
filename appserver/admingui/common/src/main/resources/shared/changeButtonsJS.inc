<!--

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

  #  When editing this file, make sure that there is backslash escape is at the END of the line

 " <script type="text/javascript">

  " function changeButtonAndDropDown(){   \
     var components = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     components[0] = gp.concat(":button1");  \
     components[1] = gp.concat(":dropdown1");  \
     changeButtons(components,"$pageSession{tableId}");  \
       \
  }

" function changeOneDropdown(){   \
     var components = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     components[1] = gp.concat(":dropdown1");  \
     changeButtons(components,"$pageSession{tableId}");  \
       \
  }
  
  "  function changeOneTableButton3(){   \
     var buttons = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     buttons[0] = gp.concat(":button1");  \
     changeButtons(buttons,"$pageSession{tableId3}");  \
       \
  }
  
  "  function changeOneTableButton(){   \
     var buttons = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     buttons[0] = gp.concat(":button1");  \
     changeButtons(buttons,"$pageSession{tableId}");  \
       \
  }
  
  " function changeTwoTableButtons(){   \
     var buttons = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     buttons[0] = gp.concat(":button1");  \
     buttons[1] = gp.concat(":button2");  \
     changeButtons(buttons,"$pageSession{tableId}");  \
       \
  }
    
  " function changeThreeTableButtons(){    \
     var buttons = new Array();  \
     var gp = "$pageSession{topActionGroup}";  \
     buttons[0] = gp.concat(":button1");  \
     buttons[1] = gp.concat(":button2");  \
     buttons[2] = gp.concat(":button3");  \
     changeButtons(buttons,"$pageSession{tableId}");  \
  }

  " function changeThreeTableButtons2(){  \
     var buttons = new Array();  \
     var gp = "$pageSession{topActionGroup2}";  \
     buttons[0] = gp.concat(":button1");  \
     buttons[1] = gp.concat(":button2");  \
     buttons[2] = gp.concat(":button3");  \
     changeButtons(buttons,"$pageSession{tableId2}");  \
  }

  " function changeButtons(buttons,tableId){   \
    var table = document.getElementById(tableId);  \
    var selections = table.getAllSelectedRowsCount();  \
    var disabled = (selections > 0) ? false : true;  \
    for (count=0; count < buttons.length; count++) {  \
       var element = document.getElementById(buttons[count]);  \
       if (element) {  \
         if (element.setDisabled) {  \
           element.setDisabled(disabled);  \
         }  \
         else {  \
           element.disabled = disabled;  \
         }  \
       }  \
     }  \
  }

  " function initAllRows() {  \
    var table = document.getElementById("$pageSession{tableId}");  \
    table.initAllRows();  \
  }

  " function initAllRows2() {  \
    var table = document.getElementById("$pageSession{tableId2}");  \
    table.initAllRows();  \
  }

  " function initAllRows3() {  \
    var table = document.getElementById("$pageSession{tableId3}");  \
    table.initAllRows();  \
  }

  " function initAllRows4() {  \
    var table = document.getElementById("$pageSession{tableId4}");  \
    table.initAllRows();  \
  }

 " </script>
