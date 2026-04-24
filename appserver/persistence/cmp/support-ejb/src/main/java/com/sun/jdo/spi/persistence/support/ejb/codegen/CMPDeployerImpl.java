/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.ejb.codegen;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.deployment.Application;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.CMPProcessor;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.EJBHelper;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.spi.CMPDeployer;
import org.glassfish.persistence.common.I18NHelper;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Generates concrete impls for CMP beans in an archive.
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
@Service
public class CMPDeployerImpl implements CMPDeployer {

    private static final Logger LOG = System.getLogger(CMPDeployerImpl.class.getName());
    private static final ResourceBundle messages = I18NHelper.loadBundle(CMPDeployerImpl.class);

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    @Optional
    private JavaConfig javaConfig;

    /**
     * Generates the concrete impls for all CMPs in the application.
     *
     * @throws DeploymentException if this exception was thrown while generating concrete impls
     */
    @Override
    public void deploy(DeploymentContext ctx) throws DeploymentException {

        // deployment descriptor object representation for the archive
        Application application = null;

        // deployment descriptor object representation for each module
        EjbBundleDescriptorImpl bundle = null;

        // ejb name
        String beanName = null;

        final CmpGeneratorException cause = new CmpGeneratorException();
        try {
            CMPGenerator gen = new JDOCodeGenerator();

            // stubs dir for the current deployment (generated/ejb)
            File stubsDir = ctx.getScratchDir("ejb");

            application = ctx.getModuleMetaData(Application.class);

            LOG.log(DEBUG, "Start of CMP section for [{0}]", application.getRegistrationName());

            List<File> cmpFiles = new ArrayList<File>();
            final ClassLoader jcl = application.getClassLoader();

            bundle = ctx.getModuleMetaData(EjbBundleDescriptorImpl.class);

            // This gives the dir where application is exploded
            String archiveUri = ctx.getSource().getURI().getSchemeSpecificPart();

            {
                LOG.log(DEBUG, () -> "[CMPC] Module Dir name is " + archiveUri);
            }

            // xml dir for the current deployment (generated/xml)
            String generatedXmlsPath = ctx.getScratchDir("xml").getCanonicalPath();

            {
                LOG.log(DEBUG, () -> "[CMPC] Generated XML Dir name is " + generatedXmlsPath);
            }

            try {
                long start = System.currentTimeMillis();
                gen.init(bundle, ctx, archiveUri, generatedXmlsPath);

                Iterator<EjbDescriptor> ejbs=bundle.getEjbs().iterator();
                while ( ejbs.hasNext() ) {

                    EjbDescriptor desc = ejbs.next();
                    beanName = desc.getName();

                    {
                        LOG.log(DEBUG, () -> "[CMPC] Ejb Class Name: " + desc.getEjbClassName());
                    }

                    if (desc instanceof IASEjbCMPEntityDescriptor) {

                        // generate concrete CMP class implementation
                        IASEjbCMPEntityDescriptor entd = (IASEjbCMPEntityDescriptor) desc;

                        {
                            LOG.log(DEBUG, () -> "[CMPC] Home Object Impl name  is " + entd.getLocalHomeImplClassName());
                        }

                        // The classloader needs to be set else we fail down the road.
                        ClassLoader ocl = entd.getClassLoader();
                        entd.setClassLoader(jcl);

                        try {
                            gen.generate(entd, stubsDir, stubsDir);
                        } catch (GeneratorException e) {
                            cause.addSuppressed(e);
                        }  finally {
                            entd.setClassLoader(ocl);
                        }
                    }

                } // end while ejbs.hasNext()
                beanName = null;
                cmpFiles.addAll(gen.cleanup());
                long end = System.currentTimeMillis();
                LOG.log(DEBUG, () -> "CMP Generation: " + (end - start) + " ms");

            } catch (GeneratorException e) {
                cause.addSuppressed(e);
            }

            bundle = null; // Used in exception processing

            // Compile the generated classes
            if (!cause.hasErrors()) {

                long start = System.currentTimeMillis();
                compileClasses(ctx, cmpFiles, stubsDir);

                LOG.log(DEBUG, () -> "Java Compilation: " + (System.currentTimeMillis() - start) + " ms");

                 // Do Java2DB if needed
                long start2 = System.currentTimeMillis();

                CMPProcessor processor = new CMPProcessor(ctx);
                processor.process();

                long end = System.currentTimeMillis();
                LOG.log(DEBUG, () -> "Java2DB processing: " + (end - start2) + " ms");
                LOG.log(DEBUG, "JDO83006: End of CMP section for [{0}]", application.getRegistrationName());
            }

        } catch (GeneratorException e) {
            throw new DeploymentException(e);
        } catch (Throwable e) {
            cause.addSuppressed(e);
            String eType = e.getClass().getName();
            String appName = application.getRegistrationName();
            String exMsg = e.getMessage();

            final String msg;
            if (bundle == null) {
                // Application or compilation error
                msg = I18NHelper.getMessage(messages, "cmpc.cmp_app_error", eType, appName, exMsg);
            } else {
                String bundleName = bundle.getModuleDescriptor().getArchiveUri();
                if (beanName == null) {
                    // Module processing error
                    msg = I18NHelper.getMessage(messages, "cmpc.cmp_module_error", eType, appName, bundleName, exMsg);
                } else {
                    // CMP bean generation error
                    msg = I18NHelper.getMessage(messages, "cmpc.cmp_bean_error", eType, beanName, appName, bundleName, exMsg);
                }
            }
            throw new DeploymentException(msg, cause.fillInStackTrace());
        }

        if (cause.hasErrors()) {
            throw new DeploymentException("Deployment failed.\n" + cause.getMessage(), cause.fillInStackTrace());
        }
    }

    /**
     * Integration point for cleanup on undeploy or failed deploy.
     */
    @Override
    public void clean(DeploymentContext ctx) {
        CMPProcessor processor = new CMPProcessor(ctx);
        processor.clean();
    }

    /**
     * Integration point for application unload
     */
    @Override
    public void unload(ClassLoader cl) {
        try {
            EJBHelper.notifyApplicationUnloaded(cl);
        } catch (Exception e) {
            LOG.log(WARNING, "CMP cleanup failed.", e);
        }
    }

    /**
     * Compile .java files.
     *
     * @param    ctx          DeploymentContext associated with the call
     * @param    files        actual source files
     * @param    destDir      destination directory for .class files
     *
     * @exception  GeneratorException  if an error while code compilation
     */
    private void compileClasses(DeploymentContext ctx, List<File> files,
            File destDir) throws GeneratorException {

        if (files.isEmpty() ) {
            return;
        }

        // class path for javac
        String classPath = ctx.getTransientAppMetaData(CMPDeployer.MODULE_CLASSPATH, String.class);
        List<String> options    = new ArrayList<String>();
        if (javaConfig!=null) {
            options.addAll(javaConfig.getJavacOptionsAsList());
        }

        StringBuilder msgBuffer = new StringBuilder();
        boolean compilationResult = false;
        try {
            // add the rest of the javac options
            options.add("-d");
            options.add(destDir.toString());
            options.add("-classpath");
            // TODO do we need to add java.class.path for compilation?
            options.add(System.getProperty("java.class.path") + File.pathSeparator + classPath);

            {
                for(File file : files) {
                    LOG.log(DEBUG, () -> I18NHelper.getMessage(messages, "cmpc.compile", file));
                }

                StringBuilder sbuf = new StringBuilder();
                for (String s : options) {
                    sbuf.append("\n\t").append(s);
                }
                LOG.log(DEBUG, () -> "[CMPC] JAVAC OPTIONS: " + sbuf.toString());
            }

            // Using Java 6 compiler API to compile the generated .java files
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics =
                   new DiagnosticCollector<JavaFileObject>();
            StandardJavaFileManager manager =
                    compiler.getStandardFileManager(diagnostics, null, null);
            Iterable<? extends JavaFileObject> compilationUnits = manager.getJavaFileObjectsFromFiles(files);

            long start = System.currentTimeMillis();

            compilationResult = compiler.getTask(
                    null, manager, diagnostics, options, null, compilationUnits).call();

            long end = System.currentTimeMillis();
            LOG.log(DEBUG, () -> "JAVA compile time (" + files.size() + " files) = " + (end - start));

            // Save compilation erros in msgBuffer to be used in case of failure
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                //Ignore NOTE about generated non safe code
                if (diagnostic.getKind().equals(Diagnostic.Kind.NOTE)) {
                    {
                        msgBuffer.append("\n").append(diagnostic.getMessage(null));
                    }
                    continue;
                }
                msgBuffer.append("\n").append(diagnostic.getMessage(null));
            }

            manager.close();

        } catch(Exception jce) {
            LOG.log(DEBUG, () -> "cmpc.cmp_complilation_exception", jce);
            String msg = I18NHelper.getMessage(messages, "cmpc.cmp_complilation_exception",
                new Object[] {jce.getMessage()});
            GeneratorException ge = new GeneratorException(msg);
            ge.initCause(jce);
            throw ge;
        }

        if (!compilationResult) {
            // Log but throw an exception with a shorter message
            LOG.log(WARNING,
                () -> I18NHelper.getMessage(messages, "cmpc.cmp_complilation_problems", msgBuffer.toString()));
            throw new GeneratorException(I18NHelper.getMessage(messages, "cmpc.cmp_complilation_failed"));
        }

    }


    public static class CmpGeneratorException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * @return true if there was at least one exception added using {@link #addSuppressed(Throwable)}
         */
        public boolean hasErrors() {
            return getSuppressed().length != 0;
        }
    }
}
