<!--

    Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!initPage
    setResourceBundle(key="i18njms" bundle="org.glassfish.jms.admingui.Strings");
    setResourceBundle(key="helpjms" bundle="org.glassfish.jms.admingui.Helplinks")
    setPageSessionAttribute(key="jmsConfigType" value="Basic");
/>
<sun:property id="jmsConfigTypeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" 
        label="$resource{i18ncs.cluster.jms.ConfigType}">
    <sun:radioButton id="optBasic"  name="jmsConfigOptionGrp" label="$resource{i18ncs.cluster.jms.ConfigTypeBasic}"
        selected="#{pageSession.jmsConfigType}" selectedValue="Basic" onChange="updateUI();"/>
    "<br />
    <sun:radioButton id="optCustom"  name="jmsConfigOptionGrp" label="$resource{i18ncs.cluster.jms.ConfigTypeCustom}"
        selected="#{pageSession.jmsConfigType}" selectedValue="Custom" onChange="updateUI();"/>
    "<br />
<f:verbatim>
    <script type="text/javascript">
#include "/jms/configureJmsCluster.js"
    </script>
</f:verbatim>
</sun:property>
