/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.javaee.core.deployment;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.versioning.VersioningSyntaxException;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

/**
 * list-sub-components command
 */
@Service(name="list-sub-components")
@I18n("list.sub.components")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="list-sub-components",
        description="List subcomponents",
        params={
            @RestParam(name="modulename", value="$parent")
        })
})
public class ListSubComponentsCommand implements AdminCommand {

    @Param(primary=true)
    private String modulename = null;

    @Param(optional=true)
    private String appname = null;

    @Param(optional=true)
    private String type = null;

    @Inject
    public ApplicationRegistry appRegistry;

    @Param(optional=true, defaultValue="false")
    private Boolean resources = false;

    @Param(optional=true, defaultValue="false", shortName="t")
    public Boolean terse = false;

    @Inject
    public Deployment deployment;

    @Inject
    public Applications applications;

    @Inject
    private CommandRunner commandRunner;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListSubComponentsCommand.class);

    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();

        String applicationName = modulename;
        if (appname != null) {
            applicationName = appname;
        }

        try {
            VersioningUtils.checkIdentifier(applicationName);
        } catch (VersioningSyntaxException ex) {
            report.setMessage(ex.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!deployment.isRegistered(applicationName)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", applicationName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        Application application = applications.getApplication(applicationName);

        if (application.isLifecycleModule()) {
            if (!terse) {
                part.setMessage(localStrings.getLocalString("listsubcomponents.no.elements.to.list", "Nothing to List."));
            }
            return;
        }

        ApplicationInfo appInfo = appRegistry.get(applicationName);
        if (appInfo == null) {
            report.setMessage(localStrings.getLocalString("application.not.enabled","Application {0} is not in an enabled state", applicationName));
            return;
        }

        com.sun.enterprise.deployment.Application app = appInfo.getMetaData(com.sun.enterprise.deployment.Application.class);

        if (app == null) {
            if (!terse) {
                part.setMessage(localStrings.getLocalString("listsubcomponents.no.elements.to.list", "Nothing to List."));
            }
            return;
        }

        Map<String, String> subComponents ;
        Map<String, String> subComponentsMap = new HashMap<>();

        if (appname == null) {
            subComponents = getAppLevelComponents(app, type, subComponentsMap);
        } else {
            BundleDescriptor bundleDesc = app.getModuleByUri(modulename);
            if (bundleDesc == null) {
                report.setMessage(localStrings.getLocalString("listsubcomponents.invalidmodulename", "Invalid module name", appname, modulename));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            subComponents = getModuleLevelComponents(
                bundleDesc, type, subComponentsMap);
        }

        // the type param can only have values "ejbs" and "servlets"
        if (type != null)  {
            if (!type.equals("servlets") && !type.equals("ejbs")) {
                report.setMessage(localStrings.getLocalString("listsubcomponents.invalidtype", "The type option has invalid value {0}. It should have a value of servlets or ejbs.", type));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        List<String> subModuleInfos = new ArrayList<>();
        if (!app.isVirtual()) {
            subModuleInfos = getSubModulesForEar(app, type);
        }

        int[] longestValue = new int[2];
        for (Map.Entry<String, String> entry : subComponents.entrySet()) {
            String key = entry.getKey();
            if (key.length() > longestValue[0]) {
                longestValue[0] = key.length();
            }
            String value = entry.getValue();
            if (value.length() > longestValue[1]) {
                longestValue[1] = value.length();
            }
        }
        StringBuilder formattedLineBuf = new StringBuilder();
        for (int j = 0; j < 2; j++) {
            longestValue[j] += 2;
            formattedLineBuf.append("%-")
                    .append(longestValue[j])
                    .append("s");
        }
        String formattedLine = formattedLineBuf.toString();
        if (!terse && subComponents.isEmpty()) {
            part.setMessage(localStrings.getLocalString("listsubcomponents.no.elements.to.list", "Nothing to List."));
        }
        int i=0;
        for (Entry<String, String> subComponent : subComponents.entrySet()) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(
                    String.format(formattedLine,
                            new Object[]{subComponent.getKey(), subComponent.getValue()} ));
            if (appname == null && !app.isVirtual()) {
                // we use the property mechanism to provide
                // support for JSR88 client
                if (subModuleInfos.get(i) != null) {
                    childPart.addProperty("moduleInfo",
                            subModuleInfos.get(i));
                }
            }
            if (resources) {
                Module module = application.getModule(subComponent.getKey());
                if (module != null) {
                    ActionReport subReport = report.addSubActionsReport();
                    CommandInvocation inv = commandRunner.getCommandInvocation("_list-resources", subReport, context.getSubject());
                    final ParameterMap parameters = new ParameterMap();
                    parameters.add("appname", application.getName());
                    parameters.add("modulename", module.getName());
                    inv.parameters(parameters).execute();

                    ActionReport.MessagePart subPart = subReport.getTopMessagePart();
                    for (ActionReport.MessagePart cp : subPart.getChildren()) {
                        ActionReport.MessagePart resourcesChildPart = childPart.addChild();
                        resourcesChildPart.setMessage("  " + cp.getMessage());
                    }
                }
            }
            i++;
        }

        for (Entry<String, String> subComponentMap : subComponentsMap.entrySet()) {
            part.addProperty(subComponentMap.getKey(), subComponentMap.getValue());
        }
        // now this is the normal output for the list-sub-components command
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    // list sub components for ear
    private List<String> getSubModulesForEar(com.sun.enterprise.deployment.Application application, String type) {
        List<String> moduleInfoList = new ArrayList<>();
        Collection<ModuleDescriptor<BundleDescriptor>> modules =
            getSubModuleListForEar(application, type);
        for (ModuleDescriptor moduleDesc : modules) {
            String moduleInfo = moduleDesc.getArchiveUri() + ":" +
                moduleDesc.getModuleType();
             if (moduleDesc.getModuleType().equals(DOLUtils.warType())) {
                 moduleInfo = moduleInfo + ":" + moduleDesc.getContextRoot();
             }
             moduleInfoList.add(moduleInfo);
        }
        return moduleInfoList;
    }

    private Map<String, String> getAppLevelComponents(com.sun.enterprise.deployment.Application application, String type, Map<String, String> subComponentsMap) {
        Map<String, String> subComponentList = new LinkedHashMap<>();
        if (application.isVirtual()) {
            // for standalone module, get servlets or ejbs
            BundleDescriptor bundleDescriptor =
                application.getStandaloneBundleDescriptor();
            subComponentList = getModuleLevelComponents(bundleDescriptor, type, subComponentsMap);
        } else {
            // for ear case, get modules
            Collection<ModuleDescriptor<BundleDescriptor>> modules =
                getSubModuleListForEar(application, type);

            for (ModuleDescriptor module : modules) {

                StringBuffer sb = new StringBuffer();
                String moduleName = module.getArchiveUri();
                sb.append("<");
                String moduleType = getModuleType(module);
                sb.append(moduleType);
                sb.append(">");
                subComponentList.put(moduleName, sb.toString());
                subComponentsMap.put(module.getArchiveUri(), moduleType);
            }
        }
        return subComponentList;
    }

    private Collection<ModuleDescriptor<BundleDescriptor>> getSubModuleListForEar(com.sun.enterprise.deployment.Application application, String type) {
        Collection<ModuleDescriptor<BundleDescriptor>> modules =
            new ArrayList<>();
        if (type == null) {
            modules = application.getModules();
        } else if (type.equals("servlets")) {
            modules = application.getModuleDescriptorsByType(
                DOLUtils.warType());
        } else if (type.equals("ejbs")) {
            modules = application.getModuleDescriptorsByType(
                DOLUtils.ejbType());
            // ejb in war case
            Collection<ModuleDescriptor<BundleDescriptor>> webModules =
                application.getModuleDescriptorsByType(DOLUtils.warType());
            for (ModuleDescriptor webModule : webModules) {
                if (webModule.getDescriptor().getExtensionsDescriptors(EjbBundleDescriptor.class).size() > 0) {
                    modules.add(webModule);
                }
            }
        }

        return modules;
    }

    private Map<String, String> getModuleLevelComponents(BundleDescriptor bundle,
        String type, Map<String, String> subComponentsMap) {
        Map<String, String> moduleSubComponentMap = new LinkedHashMap<>();
        if (bundle instanceof WebBundleDescriptor) {
            WebBundleDescriptor wbd = (WebBundleDescriptor)bundle;
            // look at ejb in war case
            Collection<EjbBundleDescriptor> ejbBundleDescs = wbd.getExtensionsDescriptors(EjbBundleDescriptor.class);
            if (ejbBundleDescs.size() > 0) {
                EjbBundleDescriptor ejbBundle = ejbBundleDescs.iterator().next();
                moduleSubComponentMap.putAll(getModuleLevelComponents(ejbBundle, type, subComponentsMap));
            }

            if (type != null && type.equals("ejbs")) {
                return moduleSubComponentMap;
            }
            for (WebComponentDescriptor wcd :
                    wbd.getWebComponentDescriptors()) {
                StringBuffer sb = new StringBuffer();
                String canonicalName = wcd.getCanonicalName();
                sb.append("<");
                String wcdType = (wcd.isServlet() ? "Servlet" : "JSP");
                sb.append(wcdType);
                sb.append(">");
                moduleSubComponentMap.put(canonicalName, sb.toString());
                subComponentsMap.put(wcd.getCanonicalName(), wcdType);
            }
        } else if (bundle instanceof EjbBundleDescriptor)  {
            if (type != null && type.equals("servlets")) {
                return moduleSubComponentMap;
            }
            EjbBundleDescriptor ebd = (EjbBundleDescriptor) bundle;
            for (EjbDescriptor ejbDesc : ebd.getEjbs()) {
                StringBuffer sb = new StringBuffer();
                String ejbName = ejbDesc.getName();
                sb.append("<");
                String ejbType = ejbDesc.getEjbTypeForDisplay();
                sb.append(ejbType);
                sb.append(">");
                moduleSubComponentMap.put(ejbName, sb.toString());
                subComponentsMap.put(ejbDesc.getName(), ejbType);
            }
        }

        return moduleSubComponentMap;
    }

    private String getModuleType(ModuleDescriptor modDesc) {
        String type = null;
        if (modDesc.getModuleType().equals(DOLUtils.ejbType())) {
            type = "EJBModule";
        } else if (modDesc.getModuleType().equals(DOLUtils.warType())) {
            type = "WebModule";
        } else if (modDesc.getModuleType().equals(DOLUtils.carType())) {
            type = "AppClientModule";
        } else if (modDesc.getModuleType().equals(DOLUtils.rarType())) {
            type = "ConnectorModule";
        }

        return type;
    }
}
