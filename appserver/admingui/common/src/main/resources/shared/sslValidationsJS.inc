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

" <script type="text/javascript">
    " function checkSSLRequiredElements() { \
        if (document.getElementById('#{sheetId}:ClientAuthProp:ClientAuth').checked) {   \
            var result = checkForValue(getTextElement('#{sheetId}:CertNicknameProp:CertNickname'));  \
            if(result != true){ \
                return showAlert('$resource{i18n.msg.JS.ssl.errClientChecked}'); \
            } \
        } \
        var comField = document.getElementById('#{cipherId}:CommonCiphersProp:commonAddRemove').selectedValues.value; \
        var ephField = document.getElementById('#{cipherId}:EphemeralCiphersProp:ephemeralAddRemove').selectedValues.value; \
        var bitField = document.getElementById('#{cipherId}:OtherCiphersProp:otherAddRemove').selectedValues.value; \
        var eccField = document.getElementById('#{cipherId}:EccCiphersProp:eccAddRemove').selectedValues.value; \
        var ssl3Prop = document.getElementById('#{sheetId}:SSL3Prop:SSL3').checked; \
        var tlsProp = document.getElementById('#{sheetId}:TLSProp:TLS').checked; \
        var tls11Prop = document.getElementById('#{sheetId}:TLS11Prop:TLS11').checked; \
        var tls12Prop = document.getElementById('#{sheetId}:TLS12Prop:TLS12').checked; \
        var tls13Prop = document.getElementById('#{sheetId}:TLS13Prop:TLS13').checked; \
        if (ssl3Prop || tlsProp || tls11Prop || tls12Prop || tls13Prop) {   \
            var result = checkForValue(getTextElement('#{sheetId}:CertNicknameProp:CertNickname'));  \
            if(result != true){ \
                return showAlert('$resource{i18n.msg.JS.ssl.errSslTlsCert}'); \
            } \
        } \
        if ((comField != '') || (ephField != '') || (bitField != '') || (eccField != '')){ \
            if ((!ssl3Prop) && (!tlsProp)) {   \
                return showAlert('$resource{i18n.msg.JS.ssl.errCiphersSelected}'); \
            } \
        } \
        return true;\
    } 

" </script>

