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

  " function enableRadioButtonFields(opt){   \
        if(opt == 'dropdown') {  \
            var textVal = document.getElementById('$pageSession{textId}').value;  \
            disableComponent('$pageSession{textId}', 'text');  \
            document.getElementById('$pageSession{textId}').value=textVal;  \
            enableComponent('$pageSession{dropdownTextId}', 'select');  \
            #{themeJavascript.JS_PREFIX}.radiobutton.setChecked('#{propId}:optB', false);  \
            #{themeJavascript.JS_PREFIX}.radiobutton.setChecked('#{propId}:optA', true);  \
            document.getElementById("#{optionId}").value='dropdown';  \
        }else{  \
            enableComponent('$pageSession{textId}', 'text');  \
            disableComponent('$pageSession{dropdownTextId}', 'select');  \
            getTextElement('$pageSession{textId}').focus();  \
            #{themeJavascript.JS_PREFIX}.radiobutton.setChecked('#{propId}:optB', true);  \
            #{themeJavascript.JS_PREFIX}.radiobutton.setChecked('#{propId}:optA', false);  \
            document.getElementById("#{optionId}").value='text';  \
        }  \
        \
  }

  " function disableRadioButtons(opt){   \
        #{themeJavascript.JS_PREFIX}.radiobutton.setDisabled('#{propId}:optA', opt);  \
        #{themeJavascript.JS_PREFIX}.radiobutton.setDisabled('#{propId}:optB', opt);  \
        document.getElementById("#{optionId}").value='';  \
        \
  }

 " </script>
