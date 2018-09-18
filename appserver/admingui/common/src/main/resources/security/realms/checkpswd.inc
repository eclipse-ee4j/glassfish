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

<f:verbatim>
    <script type="text/javascript">
        
    function checkPSW() {
        var ps1 = getTextElement('#{ps1}').value;
        var ps2 = getTextElement('#{ps2}').value;
        if (ps1 != ps2){
            return showAlert('$resource{i18n.msg.JS.manageUser.passwordNotMatch}');
        }
        if ( (ps1==null || ps1=='') && (ps2==null || ps2=='')){
            if ( getConfirm(this,'$resource{i18n.msg.JS.manageUser.setEmptyPassword}') ){
                return true;
            }else{
                return false;
            }
        }
        return true;
        }

</script>
</f:verbatim>
