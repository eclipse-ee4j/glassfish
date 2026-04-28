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

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import com.sun.jdo.api.persistence.mapping.ejb.EJBInfoHelper;
import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.spi.persistence.support.ejb.model.DeploymentDescriptorModel;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.DeploymentHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;
import org.netbeans.modules.dbschema.SchemaElement;

/** This is a class which implements the EJBInfoHelper interface
 * based on EjbBundleDescriptor and other DOL classes.
 *
 * @author Rochelle Raccah 2004
 */
public class EJBBundleInfoHelper implements EJBInfoHelper {
    private static final char UNDERLINE = '_';
    private static final char DOT = '.';

    private final EjbBundleDescriptorImpl bundleDescriptor;
    private Collection<String> availableSchemaNames;
    private NameMapper nameMapper;    // standard one
    private Model model;

    /** Creates a new instance of EJBBundleInfoHelper
     * @param bundleDescriptor the EjbBundleDescriptor which defines the
     * universe of names for this application.
     * @param availableSchemaNames a Collection of available schemas
     * in the application - used only during development
     */
    public EJBBundleInfoHelper(EjbBundleDescriptorImpl bundleDescriptor, Collection<String> availableSchemaNames) {
        this(bundleDescriptor, null, null, availableSchemaNames);
    }

    /** Creates a new instance of EJBBundleInfoHelper
     * @param bundleDescriptor the EjbBundleDescriptor which defines the
     * universe of names for this application.
     * @param nameMapper the NameMapper object to be used - allows
     * a client to supply its own mapper to the helper rather than
     * have the helper construct a new instance
     * @param model the Model object to be used - allows
     * a client to supply its own mapper to the helper rather than
     * have the helper construct a new instance
     * @param availableSchemaNames a Collection of available schemas
     * in the application - used only during development
     */
    EJBBundleInfoHelper(EjbBundleDescriptorImpl bundleDescriptor,
            NameMapper nameMapper, Model model,
            Collection<String> availableSchemaNames) {
        this.bundleDescriptor = bundleDescriptor;
        this.nameMapper = nameMapper;
        this.model = model;
        this.availableSchemaNames = availableSchemaNames;
    }

    /** Gets the EjbBundleDescriptor which defines the universe of
     * names for this application.
     * @return the EjbBundleDescriptor which defines the universe of
     * names for this application.
     */
    private EjbBundleDescriptorImpl getBundleDescriptor() {
        return bundleDescriptor;
    }

    /**
     * @see EJBInfoHelper#getEjbJarDisplayName
     */
    @Override
    public String getEjbJarDisplayName() {
        return bundleDescriptor.getName();
    }

    /** Gets a collection of names of schemas defined in this
     * ejb jar.  This implementation simply returns the list passed in
     * the constructor or <code>null</code> if there was none supplied.
     * @return a collection schema names
     */
    @Override
    public Collection<String> getAvailableSchemaNames () {
        return availableSchemaNames;
    }

    /** Gets the name to use for schema generation.  This implementation
     * uses a combo of app name, module name, etc.
     * @return the name to use for schema generation
     */
    @Override
    public String getSchemaNameToGenerate() {
        // make sure there is no '.' in schema name
        return DeploymentHelper.getDDLNamePrefix(
            getBundleDescriptor()).replace(DOT, UNDERLINE);
    }

    /** Gets the schema with the specified name, loading it if necessary.
     * This implementation uses the class loader as the extra context
     * information used to load.
     * @param schemaName the name of the schema to be loaded
     * @return the schema object
     */
    @Override
    public SchemaElement getSchema(String schemaName) {
        return SchemaElement.forName(schemaName, getClassLoader());
    }

    /**
     * @see EJBInfoHelper#getEjbNames
     */
    @Override
    public Collection<String> getEjbNames() {
        Iterator<EjbDescriptor> iterator = getBundleDescriptor().getEjbs().iterator();
        ArrayList<String> returnList = new ArrayList<>();

        while (iterator.hasNext()) {
            EjbDescriptor ejb = iterator.next();

            if (ejb instanceof EjbCMPEntityDescriptor) {
                returnList.add(ejb.getName());
            }
        }

        return returnList;
    }

    /**
     * @see EJBInfoHelper#getFieldsForEjb
     */
    @Override
    public Collection<String> getFieldsForEjb(String ejbName) {
        Iterator<String> iterator = getModel().getFields(ejbName).iterator();
        ArrayList<String> returnList = new ArrayList<>();

        while (iterator.hasNext()) {
            returnList.add(iterator.next());
        }

        return returnList;
    }

    /**
     * @see EJBInfoHelper#getRelationshipsForEjb
     */
    @Override
    public Collection<String> getRelationshipsForEjb(String ejbName) {
        Iterator<RelationshipDescriptor> iterator = getBundleDescriptor().getRelationships().iterator();
        ArrayList<String> returnList = new ArrayList<>();

        // TODO: issue of usage of this - several iterations of this if
        // iterating all the bean - but, I think it can change, so can't
        // cache it in a map (same comment applies to getEjbNames and
        // getFieldsForEjb)
        while (iterator.hasNext()) {
            RelationshipDescriptor relD =
                iterator.next();
            RelationRoleDescriptor testRole = relD.getSource();
            String cmrField = null;

            if (ejbName.equals(testRole.getOwner().getName())) {
                cmrField = testRole.getCMRField();
                if (cmrField != null) {
                    returnList.add(cmrField);
                }
            }

            testRole = relD.getSink();
            if (ejbName.equals(testRole.getOwner().getName())) {
                cmrField = testRole.getCMRField();
                if (cmrField != null) {
                    returnList.add(cmrField);
                }
            }
        }

        return returnList;
    }

    /** Gets the class loader which corresponds to this ejb bundle.
     * @return the class loader which corresponds to this ejb bundle
     */
    @Override
    public ClassLoader getClassLoader() {
        return bundleDescriptor.getClassLoader();
    }

    /**
     * @see EJBInfoHelper#getNameMapper
     */
    @Override
    public AbstractNameMapper getNameMapper() {
        return getNameMapperInternal();
    }

    /**
     * @see EJBInfoHelper#createUniqueNameMapper
     */
    @Override
    public AbstractNameMapper createUniqueNameMapper() {
        return new NameMapper(bundleDescriptor);
    }

    private NameMapper getNameMapperInternal() {
        if (nameMapper == null) {
            nameMapper = new NameMapper(bundleDescriptor, false);
        }

        return nameMapper;
    }

    /**
     * @see EJBInfoHelper#createConversionHelper
     */
    @Override
    public ConversionHelper createConversionHelper() {
        return new EjbConversionHelper(getNameMapperInternal());
    }

    /**
     * @see EJBInfoHelper#getModel
     */
    @Override
    public Model getModel() {
        if (model == null) {
            model = new DeploymentDescriptorModel(getNameMapperInternal(),
                getClassLoader());
        }

        return model;
    }
}
