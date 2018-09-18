<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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

<ui:event type="initPage">
    setResourceBundle(key="i18nce" bundle="org.glassfish.admingui.community-theme.Strings");
    setPageSessionAttribute(key="contentCSS" value="position:absolute; overflow:auto; top:0px; width:100%; height:auto; bottom:200px;");
</ui:event>
<f:verbatim>
    <div id="adframe" style="position:absolute; bottom:0px; height:197px; width:100%; z-index:100;">
	&nbsp;
    </div>
    <script type="text/javascript">
	function frameOnload() {
	    var adframe = document.getElementById('adframe');
	    adframe.innerHTML = '<iframe id="upsell" style="height:100%; width:100%;" frameborder="0" src="#{request.scheme}://$resource{i18nce.promotionLink}"></iframe>';
	}
    </script>
</f:verbatim>
