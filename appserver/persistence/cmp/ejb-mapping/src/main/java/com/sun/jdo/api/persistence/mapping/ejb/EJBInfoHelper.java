/*
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
 * EJBInfoHelper.java
 *
 * Created on October 15, 2004, 12:51 PM
 */

package com.sun.jdo.api.persistence.mapping.ejb;

import com.sun.jdo.api.persistence.model.Model;

import java.util.Collection;

import org.netbeans.modules.dbschema.SchemaElement;

/** This is an interface which represents information found in the
 * ejb-jar.xml descriptor and provides a variety of other information
 * and helper objects needed to do mapping and generating of ejb related
 * things in persistence.
 *
 * @author Rochelle Raccah
 */
public interface EJBInfoHelper {

    /**
     * Gets the name of the ejb jar.
     *
     * @return the name of the ejb jar
     */
    String getEjbJarDisplayName();

    /**
     * Gets a collection of names of schemas defined in this
     * ejb jar.
     *
     * @return a collection schema names
     */
    Collection getAvailableSchemaNames();

    /**
     * Gets the name to use for schema generation. An example might be
     * a combo of app name, module name, etc.
     *
     * @return the name to use for schema generation
     */
    String getSchemaNameToGenerate();

    /**
     * Gets the schema with the specified name, loading it if necessary.
     *
     * @param schemaName the name of the schema to be loaded
     * @return the schema object
     */
    SchemaElement getSchema(String schemaName);

    /**
     * Gets a collection of names of cmp entity beans defined in this
     * ejb jar.
     *
     * @return a collection cmp ejb names
     */
    Collection getEjbNames();

    /**
     * Gets a collection of names of cmp fields and cmr fields defined in
     * this ejb jar for the specified ejb.
     *
     * @param ejbName the name of the ejb for which a list of fields
     *            will be created
     * @return a collection cmp and cmr field names
     */
    Collection getFieldsForEjb(String ejbName);

    /**
     * Gets a collection of names of cmr fields defined in
     * this ejb jar for the specified ejb.
     *
     * @param ejbName the name of the ejb for which a list of cmr fields
     *            will be created
     * @return a collection cmr field names
     */
    Collection getRelationshipsForEjb(String ejbName);

    /**
     * Gets the class loader which corresponds to this ejb jar.
     * Implementations can return <code>null</code> if this is not
     * relevant.
     *
     * @return the class loader which corresponds to this ejb jar
     */
    ClassLoader getClassLoader();

    /**
     * Gets the AbstractNameMapper object to use for this helper.
     *
     * @return the name mapper object
     */
    AbstractNameMapper getNameMapper();

    /**
     * Creates and returns an instance of the AbstractNameMapper object to
     * use for generation of unique names with this helper. Unique names
     * usually means that the mapper doesn't use the same jdo and ejb names.
     * Note that this method is a factory-like method which creates a new
     * instance so the caller can make modifications to it as necessary.
     *
     * @return the name mapper object
     */
    AbstractNameMapper createUniqueNameMapper();

    /**
     * Creates and returns an instance of the ConversionHelper object to
     * use for this helper. Note that this method is a factory-like method
     * which creates a new instance so the caller can make modifications to
     * it as necessary.
     *
     * @return the conversion helper object
     */
    ConversionHelper createConversionHelper();

    /**
     * Gets the Model object to use for this helper.
     *
     * @return the model object
     */
    Model getModel();
}
