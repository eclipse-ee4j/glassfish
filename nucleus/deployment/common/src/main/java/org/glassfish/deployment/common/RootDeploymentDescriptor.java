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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * This descriptor contains all common information amongst root element
 * of the J2EE Deployment Descriptors (application, ejb-jar, web-app,
 * connector...).
 *
 * @author Jerome Dochez
 */
public abstract class RootDeploymentDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "invalidSpecVersion:  {0}", level = "WARNING")
    private static final String INVALID_SPEC_VERSION = "NCLS-DEPLOYMENT-00046";

    /**
     * each module is uniquely identified with a moduleID
     */
    private String moduleID;

    /**
     * version of the specification loaded by this descriptor
     */
    private String specVersion;

    /**
     * optional index string to disambiguate when serveral extensions are
     * part of the same module
     */
    private String index;

    /**
     * class loader associated to this module to load classes
     * contained in the archive file
     */
    protected transient ClassLoader classLoader;

    /**
     * Extensions for this module descriptor, keyed by type, indexed using the instance's index
     */
    protected Map<Class<? extends RootDeploymentDescriptor>, List<RootDeploymentDescriptor>> extensions = new HashMap<>();

    /**
     * contains the information for this module (like it's module name)
     */
    protected ModuleDescriptor<RootDeploymentDescriptor> moduleDescriptor;

    /**
     * Construct a new RootDeploymentDescriptor
     */
    public RootDeploymentDescriptor() {
        super();
    }

    /**
     * Construct a new RootDeploymentDescriptor with a name and description
     */
    public RootDeploymentDescriptor(String name, String description) {
        super(name, description);
    }

    /**
     * each module is uniquely identified with a moduleID
     * @param moduleID for this module
     */
    public final void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    /**
     * @return the module ID for this module descriptor
     */
    public String getModuleID() {
        return this.moduleID;
    }

    /**
     * @return the default version of the deployment descriptor loaded by this descriptor
     */
    public abstract String getDefaultSpecVersion();

    /**
     * Returns true if this root deployment descriptor does not describe anything
     *
     * @return true if this root descriptor is empty
     */
    public abstract boolean isEmpty();


    /**
     * @return the specification version of the deployment descriptor
     * loaded by this descriptor
     */
    public String getSpecVersion() {
        if (specVersion == null) {
            specVersion = getDefaultSpecVersion();
        }
        try {
            Double.parseDouble(specVersion);
        } catch (NumberFormatException nfe) {
            LOG.log(Level.WARNING, INVALID_SPEC_VERSION, new Object[] {specVersion, getDefaultSpecVersion()});
            specVersion = getDefaultSpecVersion();
        }

        return specVersion;
    }


    /**
     * Sets the specification version of the deployment descriptor
     * @param specVersion version number
     */
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    /**
     * @return the module type for this bundle descriptor
     */
    public abstract ArchiveType getModuleType();

    /**
     * @return the tracer visitor for this descriptor
     */
    public DescriptorVisitor getTracerVisitor() {
        return null;
    }

    /**
     * Sets the class loader for this application
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return the class loader associated with this module
     */
    public abstract ClassLoader getClassLoader();

    /**
     * sets the display name for this bundle
     */
    @Override
    public void setDisplayName(String name) {
        setLocalizedDisplayName(null, name);
    }

    /**
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return getLocalizedDisplayName(null);
    }


    @Override
    public void setName(String name) {
        setModuleID(name);
    }


    @Override
    public String getName() {
        if (getModuleID() == null) {
            return getDisplayName();
        }
        return getModuleID();
    }


    public void setSchemaLocation(String schemaLocation) {
        addExtraAttribute("schema-location", schemaLocation);
    }


    public String getSchemaLocation() {
        return (String) getExtraAttribute("schema-location");
    }


    /**
     * @return the module descriptor for this bundle
     */
    public <T extends RootDeploymentDescriptor> ModuleDescriptor<T> getModuleDescriptor() {
        if (moduleDescriptor == null) {
            moduleDescriptor = new ModuleDescriptor<>();
            moduleDescriptor.setModuleType(getModuleType());
            moduleDescriptor.setDescriptor(this);
        }
        return (ModuleDescriptor<T>) moduleDescriptor;
    }


    /**
     * Sets the module descriptor for this bundle
     *
     * @param descriptor for the module
     */
    public void setModuleDescriptor(ModuleDescriptor<RootDeploymentDescriptor> descriptor) {
        moduleDescriptor = descriptor;
        for (List<RootDeploymentDescriptor> extByType : this.extensions.values()) {
            if (extByType != null) {
                for (RootDeploymentDescriptor ext : extByType) {
                    ext.setModuleDescriptor(descriptor);
                }
            }
        }
    }


    /**
     * @return true if this module is an application object
     */
    public abstract boolean isApplication();


    /**
     * print a meaningful string for this object
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n Module Type = ").append(getModuleType());
        toStringBuffer.append("\n Module spec version = ").append(getSpecVersion());
        if (moduleID != null) {
            toStringBuffer.append("\n Module ID = ").append(moduleID);
        }
        if (getSchemaLocation() != null) {
            toStringBuffer.append("\n Client SchemaLocation = ").append(getSchemaLocation());
        }
    }

    /**
     * This method returns all the extensions deployment descriptors in the scope
     * @return an unmodifiable collection of extensions or empty collection if none.
     */
    public Collection<RootDeploymentDescriptor> getExtensionsDescriptors() {
        ArrayList<RootDeploymentDescriptor> flattened = new ArrayList<>();
        for (List<? extends RootDeploymentDescriptor> extensionsByType : extensions.values()) {
            flattened.addAll(extensionsByType);
        }
        return Collections.unmodifiableCollection(flattened);
    }

    /**
     * This method returns all extensions of the passed type in the scope
     * @param type requested extension type
     * @return an unmodifiable collection of extensions or empty collection if none.
     */
    public <T extends RootDeploymentDescriptor> Collection<T> getExtensionsDescriptors(Class<T> type) {
        for (Map.Entry<Class<? extends RootDeploymentDescriptor>, List<RootDeploymentDescriptor>> entry : extensions.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return Collections.unmodifiableCollection((Collection<T>) entry.getValue());
            }
        }
        return Collections.emptyList();
    }


    /**
     * This method returns one extension of the passed type in the scope with the right index
     *
     * @param type requested extension type
     * @param index is the instance index
     * @return an unmodifiable collection of extensions or empty collection if none.
     */
    public <T extends RootDeploymentDescriptor> T getExtensionsDescriptors(
        Class<? extends RootDeploymentDescriptor> type, String index) {
        for (T extension : (Collection<T>) getExtensionsDescriptors(type)) {
            String extensionIndex = ((RootDeploymentDescriptor) extension).index;
            if (index == null) {
                if (extensionIndex == null) {
                    return extension;
                }
            } else {
                if (index.equals(extensionIndex)) {
                    return extension;
                }
            }
        }
        return null;
    }


    public synchronized <T extends RootDeploymentDescriptor> void addExtensionDescriptor(
        Class<? extends RootDeploymentDescriptor> type, T instance, String index) {
        List<RootDeploymentDescriptor> values;
        if (extensions.containsKey(type)) {
            values = extensions.get(type);
        } else {
            values = new ArrayList<>();
            extensions.put(type, values);
        }
        ((RootDeploymentDescriptor) instance).index = index;
        values.add(instance);
    }

    /**
     * @return whether this descriptor is an extension descriptor
     *         of a main descriptor, e.g. the EjbBundleDescriptor for
     *         ejb in war case should return true.
     */
    public boolean isExtensionDescriptor() {
        if (getModuleDescriptor().getDescriptor() != this) {
            return true;
        }
        return false;
    }

    /**
     * @return the main descriptor associated with it if it's
     *         an extension descriptor, otherwise return itself
     */
    public RootDeploymentDescriptor getMainDescriptor() {
        if (isExtensionDescriptor()) {
            return getModuleDescriptor().getDescriptor();
        }
        return this;
    }
}

