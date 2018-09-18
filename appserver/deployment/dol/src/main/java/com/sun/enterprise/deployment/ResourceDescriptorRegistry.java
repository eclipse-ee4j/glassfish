/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import java.io.Serializable;
import java.util.*;

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * This class maintains registry for all resources and used by all Descriptor and BundleDescriptor classes.
 * User: naman
 * Date: 24/5/12
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceDescriptorRegistry implements Serializable {

    private static Map<JavaEEResourceType,Set<Class>>invalidResourceTypeScopes = new HashMap<JavaEEResourceType, Set<Class>>();

    /*
    This map contains the list of descriptors for where a particular annotation is not applicable. In future update
    this list for non applicable descriptor.

    e.g. ConnectionFactoryDescriptor and AdminObjectDescriptor is not allowed to define at Application Client Descriptor.
     */
    static {
        invalidResourceTypeScopes.put(JavaEEResourceType.MSD,new HashSet<Class>());
        invalidResourceTypeScopes.put(JavaEEResourceType.DSD,new HashSet<Class>());
        invalidResourceTypeScopes.put(JavaEEResourceType.JMSCFDD,new HashSet<Class>());
        invalidResourceTypeScopes.put(JavaEEResourceType.JMSDD,new HashSet<Class>());
        invalidResourceTypeScopes.put(JavaEEResourceType.CFD,new HashSet<Class>(Arrays.asList(new Class[]{ApplicationClientDescriptor.class})));
        invalidResourceTypeScopes.put(JavaEEResourceType.AODD,new HashSet<Class>(Arrays.asList(new Class[]{ApplicationClientDescriptor.class})));

    }

    private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ResourceDescriptorRegistry.class);

    private Map<JavaEEResourceType,Set<ResourceDescriptor>> resourceDescriptors
            = new HashMap<JavaEEResourceType,Set<ResourceDescriptor>>();

    /**
     * This method returns all descriptors associated with the app.
     * @return
     */
    public Set<ResourceDescriptor> getAllResourcesDescriptors() {
        Set<ResourceDescriptor> allResourceDescriptors = new HashSet<ResourceDescriptor>();
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.DSD));
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.MSD));
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.CFD));
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.AODD));
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.JMSCFDD));
        allResourceDescriptors.addAll(this.getResourceDescriptors(JavaEEResourceType.JMSDD));
        return allResourceDescriptors;
    }

    /**
     * This method returns all valid descriptor for given class. USes 'invalidResourceTypeScopes' to validate the
     * scope for givneClazz
     * @param givenClazz - Class which is either AppClientDescriptor, Application etc.
     * @return
     */
    public Set<ResourceDescriptor> getAllResourcesDescriptors(Class givenClazz) {
        Set<ResourceDescriptor> allResourceDescriptors = new HashSet<ResourceDescriptor>();

        for(JavaEEResourceType javaEEResourceType: JavaEEResourceType.values()) {
            Set<Class> invalidClassSet = invalidResourceTypeScopes.get(javaEEResourceType);
            if(invalidClassSet!=null && invalidClassSet.size()>0) {
                for(Class invalidClass: invalidClassSet) {
                    if(!invalidClass.isAssignableFrom(givenClazz)) {
                        allResourceDescriptors.addAll(this.getResourceDescriptors(javaEEResourceType));
                    }
                }
            } else if(invalidClassSet!=null) {
                allResourceDescriptors.addAll(this.getResourceDescriptors(javaEEResourceType));
            }
        }
        return allResourceDescriptors;
    }

    /**
     * Return descriptor by name.
     * @param name
     * @return
     */
    protected ResourceDescriptor getResourcesDescriptor(String name) {
        ResourceDescriptor descriptor = null;
        for (ResourceDescriptor thiDescriptor : this.getAllResourcesDescriptors()) {
            if (thiDescriptor.getName().equals(name)) {
                descriptor = thiDescriptor;
                break;
            }
        }
        return descriptor;
    }

    /**
     * Validate descriptor is already defined or not.
     * @param reference
     * @return
     */
    private boolean isDescriptorRegistered(ResourceDescriptor reference) {
        ResourceDescriptor descriptor = getResourcesDescriptor(reference.getName());
        if (descriptor != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns descriptor based on the Resource Type.
     * @param javaEEResourceType
     * @return
     */
    public Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType javaEEResourceType) {
        Set<ResourceDescriptor> resourceDescriptorSet = resourceDescriptors.get(javaEEResourceType);
        if(resourceDescriptorSet==null) {
            resourceDescriptors.put(javaEEResourceType,new HashSet<ResourceDescriptor>());
        }
        return resourceDescriptors.get(javaEEResourceType);
    }

    /**
     * Return descriptors based on resource type and given name.
     * @param javaEEResourceType
     * @param name
     * @return
     */
    protected ResourceDescriptor getResourceDescriptor(JavaEEResourceType javaEEResourceType, String name) {
        for (ResourceDescriptor resourceDescriptor : getResourceDescriptors(javaEEResourceType)) {
            if (resourceDescriptor.getName().equals(name)) {
                return resourceDescriptor;
            }
        }
        return null;
    }

    /**
     * Adding resource descriptor for gvien reference
     * @param reference
     */
    public void addResourceDescriptor(ResourceDescriptor reference) {
        if (isDescriptorRegistered(reference)) {
            throw new IllegalStateException(
                    localStrings.getLocalString("exceptionwebduplicatedescriptor",
                            "This app cannot have descriptor definitions of same name : [{0}]",
                            reference.getName()));
        } else {
            Set<ResourceDescriptor> resourceDescriptorSet = getResourceDescriptors(reference.getResourceType());
            resourceDescriptorSet.add(reference);
            resourceDescriptors.put(reference.getResourceType(),resourceDescriptorSet);
        }
    }

    /**
     * Remove resource descriptor based on resource type and given reference
     * @param javaEEResourceType
     * @param reference
     */
    public void removeResourceDescriptor(JavaEEResourceType javaEEResourceType, ResourceDescriptor reference) {
        Set<ResourceDescriptor> resourceDescriptorSet = getResourceDescriptors(reference.getResourceType());
        resourceDescriptorSet.remove(reference);
        resourceDescriptors.put(javaEEResourceType,resourceDescriptorSet);
    }
}
