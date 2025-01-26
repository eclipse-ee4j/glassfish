/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Indentation Information:
 * 0. Please do not (try to) preserve these settings.
 * 1. Tabs are not preferred over spaces.
 * 2. In vi/vim -
 *             :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *             1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *             2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *             3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

/*
 * @author byron.nevins@sun.com
 */

package org.glassfish.web.jsp;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.wasp.JspC;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.runtime.JspConfig;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;
import org.glassfish.web.deployment.runtime.WebProperty;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

public final class JSPCompiler {

    private static final Logger LOG = LogFacade.getLogger();


    public static void compile(File inWebDir, File outWebDir, WebBundleDescriptor wbd, ServerContext serverContext)
        throws DeploymentException {
        compile(inWebDir, outWebDir, wbd, (String) null, serverContext);
    }


    public static void compile(File inWebDir, File outWebDir, WebBundleDescriptor wbd, List<String> classpathList,
        ServerContext serverContext) throws DeploymentException {
        String classpath = classpathList == null ? null : getClasspath(classpathList);
        compile(inWebDir, outWebDir, wbd, classpath, serverContext);
    }


    public static void compile(File inWebDir, File outWebDir, WebBundleDescriptor wbd, String classpath,
        ServerContext serverContext) throws DeploymentException {
        LOG.log(Level.FINEST, "Compiling JSP: in={0}, out={1}, classpath={2}",
            new Object[] {inWebDir, outWebDir, classpath});
        JspC jspc = new JspC();

        if (classpath != null && !classpath.isEmpty()) {
            jspc.setClassPath(classpath);
        }

        String appName = wbd.getApplication().getName();

        // so far, this is not segragated per web bundle, all web-bundles will get the
        // same sysClassPath
        String sysClassPath = ASClassLoaderUtil.getModuleClassPath(
            serverContext.getDefaultServices(), appName, null);
        jspc.setSystemClassPath(sysClassPath);

        verify(inWebDir, outWebDir);

        configureJspc(jspc, wbd);
        jspc.setOutputDir(outWebDir.getAbsolutePath());
        jspc.setUriroot(inWebDir.getAbsolutePath());
        jspc.setCompile(true);
        LOG.log(INFO, LogFacade.START_MESSAGE);

        try {
            jspc.execute();
        } catch (Exception je) {
            throw new DeploymentException("JSP Compilation Error: " + je, je);
        } finally {
            // bnevins 9-9-03 -- There may be no jsp files in this web-module
            // in such a case the code above will create a useless, and possibly
            // problematic empty directory.     If the directory is empty -- delete
            // the directory.

            String[] files = outWebDir.list();


            if(files == null || files.length <= 0) {
                if (!outWebDir.delete()) {
                    LOG.log(FINE, LogFacade.CANNOT_DELETE_FILE, outWebDir);
                }
            }

            LOG.log(INFO, LogFacade.FINISH_MESSAGE);
        }
    }

    private static void verify(File inWebDir, File outWebDir) throws DeploymentException {
        // inWebDir must exist, outWebDir must either exist or be creatable
        if (!FileUtils.safeIsDirectory(inWebDir)) {
            throw new DeploymentException("inWebDir is not a directory: " + inWebDir);
        }

        if (!FileUtils.safeIsDirectory(outWebDir)) {
            if (!outWebDir.mkdirs()) {
                LOG.log(FINE, LogFacade.CANNOT_DELETE_FILE, outWebDir);
            }

            if (!FileUtils.safeIsDirectory(outWebDir)) {
                throw new DeploymentException("outWebDir is not a directory, and it can't be created: " + outWebDir);
            }
        }
    }

    private static String getClasspath(List<String> paths) {
        if (paths == null) {
            return null;
        }

        String classpath = null;

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String path : paths) {
            if (first) {
                first = false;
            } else {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(path);
        }

        if (sb.length() > 0) {
            classpath = sb.toString();
        }

        return classpath;
    }

    /**
     * Configures the given JspC instance with the jsp-config properties
     * specified in the sun-web.xml of the web module represented by the
     * given WebBundleDescriptor.
     *
     * @param jspc JspC instance to be configured
     * @param wbd WebBundleDescriptor of the web module whose sun-web.xml
     * is used to configure the given JspC instance
     */
    private static void configureJspc(JspC jspc, WebBundleDescriptor wbd) {

        SunWebAppImpl sunWebApp = (SunWebAppImpl) wbd.getSunDescriptor();
        if (sunWebApp == null) {
            return;
        }

        if (sunWebApp.sizeWebProperty() > 0) {
            WebProperty[] props = sunWebApp.getWebProperty();
            for (WebProperty prop : props) {
                String pName = prop.getValue("name");
                String pValue = prop.getValue("value");
                if (pName == null || pValue == null) {
                    throw new IllegalArgumentException(
                        "Missing sun-web-app property name or value");
                }
                if ("enableTldValidation".equals(pName)) {
                    jspc.setIsValidationEnabled(
                        Boolean.parseBoolean(pValue));
                }
            }
        }

        // Configure JspC with the init params of the JspServlet
        Set<WebComponentDescriptor> set = wbd.getWebComponentDescriptors();
        if (!set.isEmpty()) {
            for (WebComponentDescriptor webComponentDesc : set) {
                if ("jsp".equals(webComponentDesc.getCanonicalName())) {
                    Enumeration<InitializationParameter> en = webComponentDesc.getInitializationParameters();
                    if (en != null) {
                        while (en.hasMoreElements()) {
                            InitializationParameter initP = en.nextElement();
                            configureJspc(jspc, initP.getName(), initP.getValue());
                        }
                    }
                    break;
                }
            }
        }

        // Configure JspC with jsp-config properties from sun-web.xml,
        // which override JspServlet init params of the same name.
        JspConfig jspConfig = sunWebApp.getJspConfig();
        if (jspConfig == null) {
            return;
        }
        WebProperty[] props = jspConfig.getWebProperty();
        for (int i=0; props!=null && i<props.length; i++) {
            configureJspc(jspc, props[i].getValue("name"), props[i].getValue("value"));
        }
    }


    /**
     * Configures the given JspC instance with the given property name
     * and value.
     *
     * @jspc The JspC instance to configure
     * @pName The property name
     * @pValue The property value
     */
    private static void configureJspc(JspC jspc, String pName, String pValue) {
        if (pName == null || pValue == null) {
            throw new IllegalArgumentException("Null property name or value");
        }

        if ("xpoweredBy".equals(pName)) {
            jspc.setXpoweredBy(Boolean.parseBoolean(pValue));
        } else if ("classdebuginfo".equals(pName)) {
            jspc.setClassDebugInfo(Boolean.parseBoolean(pValue));
        } else if ("enablePooling".equals(pName)) {
            jspc.setPoolingEnabled(Boolean.parseBoolean(pValue));
        } else if ("trimSpaces".equals(pName)) {
            jspc.setTrimSpaces(Boolean.parseBoolean(pValue));
        } else if ("genStrAsCharArray".equals(pName)) {
            jspc.setGenStringAsCharArray(Boolean.parseBoolean(pValue));
        } else if ("errorOnUseBeanInvalidClassAttribute".equals(pName)) {
            jspc.setErrorOnUseBeanInvalidClassAttribute(Boolean.parseBoolean(pValue));
        } else if ("ignoreJspFragmentErrors".equals(pName)) {
            jspc.setIgnoreJspFragmentErrors(Boolean.parseBoolean(pValue));
        } else if ("compilerSourceVM".equals(pName)) {
            jspc.setCompilerSourceVM(pValue);
        } else if ("compilerTargetVM".equals(pName)) {
            jspc.setCompilerTargetVM(pValue);
        }
    }
}
