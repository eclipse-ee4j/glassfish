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

<!-- javaConfig/javaConfigTabs.inc -->


<sun:tabSet id="javaConfigTab" immediate="true" selected="#{sessionScope.javaConfigTab}" >

	<sun:tab id="jvmGeneral" immediate="true" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.GeneralTab}">
	<!command
            setSessionAttribute(key="javaConfigTab" value="jvmGeneral");
            gf.redirect(page="#{request.contextPath}/common/javaConfig/serverInstJvmGeneral.jsf?configName=#{configName}");
	/>
	</sun:tab>

	<sun:tab id="pathSettings" immediate="true" text="$resource{i18nc.jvm.PathSettings}" toolTip="$resource{i18nc.jvm.PathSettingsTab}">
	    <!command
                setSessionAttribute(key="javaConfigTab" value="pathSettings");
		gf.redirect(page="#{request.contextPath}/common/javaConfig/serverInstJvmPathSettings.jsf?configName=#{configName}");
	    />        
	</sun:tab>

	<sun:tab id="jvmOptions" immediate="true" text="$resource{i18nc.jvm.Options}" toolTip="$resource{i18nc.jvm.OptionsTab}" >
	    <!command
                setSessionAttribute(key="javaConfigTab" value="jvmOptions");
		gf.redirect(page="#{request.contextPath}/common/javaConfig/serverJvmOptions.jsf?configName=#{configName}");
	    />        
	</sun:tab>

	<sun:tab id="profiler" immediate="true" text="$resource{i18nc.jvm.Profiler}" toolTip="$resource{i18nc.jvm.ProfilerTab}">
	    <!command
                setSessionAttribute(key="javaConfigTab" value="profiler");
		gf.redirect(page="#{request.contextPath}/common/javaConfig/serverInstProfiler.jsf?configName=#{configName}");
	    />        
	</sun:tab>

</sun:tabSet>
