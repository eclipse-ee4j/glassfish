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
 * MappingElementProperties.java
 *
 * Created on April 28, 2000, 6:24 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface MappingElementProperties
{
    /** Name of {@link MappingElement#getName name} property.
     */
    public static final String PROP_NAME = "name";                    // NOI18N

    /** Name of {@link MappingClassElement#isModified modified}
     * flag for {@link MappingClassElement class elements}.
     */
    public static final String PROP_MODIFIED = "modified";            // NOI18N

    /** Name of {@link MappingClassElement#getConsistencyLevel consistencyLevel}
     * property for {@link MappingClassElement class elements}.
     */
    public static final String PROP_CONSISTENCY = "consistencyLevel";    // NOI18N

    /** Name of {@link MappingClassElement#setDatabaseRoot root}
     * property for {@link MappingClassElement class elements}.
     */
    public static final String PROP_DATABASE_ROOT = "schema";    // NOI18N

    /** Name of {@link MappingClassElement#getTables tables}
     * property for {@link MappingClassElement class elements}.
     */
    public static final String PROP_TABLES = "tables";                // NOI18N

    /** Name of {@link MappingClassElement#getFields fields}
     * property for {@link MappingClassElement class elements}.
     */
    public static final String PROP_FIELDS = "fields";                // NOI18N

    /** Name of {@link MappingClassElement#isNavigable navigable}
     * property for {@link MappingClassElement class elements}.
     */
    public static final String PROP_NAVIGABLE = "navigable";        // NOI18N

    /** Name of {@link MappingFieldElement#isReadOnly read only}
     * property for {@link MappingFieldElement field elements}.
     */
    public static final String PROP_READ_ONLY = "readOnly";            // NOI18N

    /** Name of {@link MappingFieldElement#isInConcurrencyCheck in concurrency
     * check} property for {@link MappingFieldElement field elements}.
     */
    public static final String PROP_IN_CONCURRENCY_CHECK =
        "inConcurrencyCheck";                                        // NOI18N

    /** Name of {@link MappingFieldElement#isVersion version field}
     * property for {@link MappingFieldElement field elements}.
     */
    public static final String PROP_VERSION_FIELD = "versionField";        // NOI18N

    /** Name of {@link MappingFieldElement#getFetchGroup fetch group}
     * property for {@link MappingFieldElement field elements}.
     */
    public static final String PROP_FETCH_GROUP = "fetchGroup";        // NOI18N

    /** Name of {@link MappingFieldElement#getColumns columns}
     * property for {@link MappingFieldElement field elements}.
     */
    public static final String PROP_COLUMNS = "columns";            // NOI18N

    /** Name of {@link MappingReferenceKeyElement#getTable table} and
     * {@link MappingTableElement#getTable table} property for
     * {@link MappingReferenceKeyElement reference key elements} and
     * {@link MappingTableElement mapping table elements}.
     */
    public static final String PROP_TABLE = "table";                // NOI18N

    /** Name of {@link MappingTableElement#getReferencingKeys key columns}
     * and {@link MappingTableElement#getKey key columns} property for
     * {@link MappingReferenceKeyElement reference key elements} and
     * {@link MappingTableElement mapping table elements}.
     */
    public static final String PROP_KEY_COLUMNS = "keyColumns";        // NOI18N

    /** Name of {@link MappingRelationshipElement#getAssociatedColumns
     * associated columns} property for {@link MappingRelationshipElement
     * relationship elements}.
     */
    public static final String PROP_ASSOCIATED_COLUMNS =
        "associatedColumns";                                        // NOI18N

    /** Name of {@link MappingTableElement#getReferencingKeys referencing
     * keys} property for {@link MappingTableElement mapping table elements}.
     */
    public static final String PROP_REFERENCING_KEYS =
        "referencingKeys";                                            // NOI18N
}
