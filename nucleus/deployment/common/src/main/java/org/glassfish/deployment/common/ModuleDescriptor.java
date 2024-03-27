/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.deployment.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.jvnet.hk2.config.TranslationException;
import org.jvnet.hk2.config.VariableResolver;

/**
 * This class describes a module information for an applicaiton module
 *
 * @author Jerome Dochez
 */
public class ModuleDescriptor<T extends RootDeploymentDescriptor> extends Descriptor {

    private static final long serialVersionUID = 1L;

    /**
     * type of the module, currently EJB, WEB...
     */
    private ArchiveType type;

    /**
     * path for the module bundle
     */
    private String path;

    /**
     * alternate descriptor (if any) path
     */
    private String altDD;

    /**
     * context-root if dealing with a web module
     */
    private String contextRoot;

    /**
     * loaded deployment descriptor for this module
     */
    private T descriptor;

    /**
     * manifest information for this module
     */
    private transient Manifest manifest;

    /**
     * is it a standalone module, or part of a J2EE Application
     */
    private boolean standalone;

    private String moduleName;

    /** Creates new ModuleDescriptor */
    public ModuleDescriptor() {
    }

    public void setModuleType(ArchiveType type) {
        this.type = type;
    }


    /**
     * @return the module type for this module
     */
    public ArchiveType getModuleType() {
        if (descriptor != null) {
            return descriptor.getModuleType();
        }
        return type;
    }

    /**
     * Sets the archive uri as defined in the application xml
     * or the full archive path for standalone modules
     * @path the module path
     */
    public void setArchiveUri(String path) {
        this.path = path;
    }

    /**
     * @return the archive uri for this module
     */
    public String getArchiveUri() {
        return path;
    }

    /**
     * Sets the path to the alternate deployment descriptors
     * in the application archive
     * @param altDD the uri for the deployment descriptors
     */
    public void setAlternateDescriptor(String altDD) {
        this.altDD = altDD;
    }

    /**
     * @return the alternate deployment descriptor path
     * or null if this module does not use alternate
     * deployment descriptors
     */
    public String getAlternateDescriptor() {
        return altDD;
    }

    /**
     * Sets the @see BundleDescriptor descriptor for this module
     * @param descriptor the module descriptor
     */
    public void setDescriptor(T descriptor) {
        this.descriptor = descriptor;
        descriptor.setModuleDescriptor((ModuleDescriptor<RootDeploymentDescriptor>) this);
    }

    /**
     * @return the @see BundleDescriptor module descriptor
     */
    public T getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the context root for Web module
     * @param contextRoot the contextRoot
     */
    public void setContextRoot(String contextRoot) {
        VariableResolver resolver = new VariableResolver() {
            @Override
            protected String getVariableValue(String varName) throws TranslationException {
                return System.getProperty(varName, "");
            }
        };
        this.contextRoot = resolver.translate(contextRoot);
    }

    /**
     * @return the context root for a web module
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * Returns the value of the module-name element in the application.xml if
     * it's defined. The default module name is the pathname of the module in
     * the ear file with any filename extension (.jar, .war, .rar) removed,
     * but with any directory names included.
     *
     * @return the module of this application
     */
    public String getModuleName() {
        String name = moduleName;
        if (moduleName == null) {
            name = VersioningUtils.getUntaggedName(DeploymentUtils.getDefaultEEName(path));
        } else{
            name = VersioningUtils.getUntaggedName(moduleName);
        }
        return name;
    }

    /**
     * Sets the module name
     * @return the module name of this application
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * @return the @see Manifest manifest information
     * for this module
     */
    public Manifest getManifest() {
        return manifest;
    }

    /**
     * Sets the @see Manifest manifest information for this
     * module
     */
    public void setManifest(Manifest m) {
        manifest = m;
    }

    /**
     * @return true if this module is a standalone module
     */
    public boolean isStandalone() {
        return standalone;
    }

    /**
     * Sets the standalone flag
     */
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    /**
     * @return an iterator on the deployment-extension
     */
    public Iterator<?> getWebDeploymentExtensions() {
        Vector<?> extensions = (Vector<?>) getExtraAttribute("web-deployment-extension");
        if (extensions!=null) {
            return extensions.iterator();
        }
        return null;
    }


    /**
     * @return a meaningful string about myself
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append(type + " ModuleDescriptor: [  " + path + " ] , altDD = " + altDD);
        if (contextRoot != null) {
            toStringBuffer.append(" , ContextRoot = " + contextRoot);
        }
    }


    /**
     * Implementation of the serializable interface since ModuleType is not serializable
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // just write this intance fields...
        out.writeObject(path);
        out.writeObject(altDD);
        out.writeObject(contextRoot);
        out.writeObject(descriptor);
        out.writeBoolean(standalone);
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // just read this intance fields...
        path = (String) in.readObject();
        altDD = (String) in.readObject();
        contextRoot = (String) in.readObject();
        descriptor = (T) in.readObject();
        standalone = in.readBoolean();
    }
}
