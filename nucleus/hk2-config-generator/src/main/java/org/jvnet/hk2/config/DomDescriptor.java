/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.jvnet.hk2.config.provider.internal.Creator;

/**
 * Created by IntelliJ IDEA.
 * User: makannan
 * Date: 4/28/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DomDescriptor<T>
    extends AbstractActiveDescriptor<T> {
    /**
     * For serialization
     */
    //private static final long serialVersionUID = -9196390718074767455L;

    private Dom theDom;

    private Creator<T> creator;

    private T theOne;

    /**
     * For serializable
     */
    public DomDescriptor() {
        super();
    }

    /**
     * Creates the constant descriptor
     *
     * @param theDom May not be null
     * @param advertisedContracts
     * @param scope
     * @param name
     * @param qualifiers
     */
    public DomDescriptor(Dom theDom, Set<Type> advertisedContracts,
                        Class<? extends Annotation> scope, String name,
                        Set<Annotation> qualifiers) {
        super(advertisedContracts,
                scope,
                name,
                qualifiers,
                DescriptorType.CLASS,
                DescriptorVisibility.NORMAL,
                0,
                null,
                null,
                null,
                null);
        super.addAdvertisedContract(ConfigBeanProxy.class.getName());
        if (theDom == null) {
            throw new IllegalArgumentException();
        }

        this.theDom = theDom;
        setImplementation(theDom.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return theDom.getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return theDom.getClass();
    }

    @Override
    public Type getImplementationType() {
        return theDom.getClass();
    }

    @Override
    public void setImplementationType(Type t) {
        throw new AssertionError("May not set type of DomDescriptor");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        initTheOne();
        return theOne;
    }

    private void initTheOne() {
        if (theOne == null) {
            Class c = theDom.getImplementationClass();
            creator = (ConfigBeanProxy.class.isAssignableFrom(c)
                    ? new DomProxyCreator(c, theDom)
                    : new ConfiguredCreator(theDom.createCreator(c), theDom));
        }

        theOne = creator.create();
    }

    @Override
    public boolean equals(Object a) {
        if (a instanceof DomDescriptor && super.equals(a)) {
            DomDescriptor other = (DomDescriptor) a;
            return theDom.equals(other.theDom);

        }
        return false;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
