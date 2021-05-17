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
 * PersistenceElementProperties.java
 *
 * Created on March 2, 2000, 12:54 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface PersistenceElementProperties
{
    /** Name of {@link PersistenceElement#getName name} property.
     */
    public static final String PROP_NAME = "name";                    // NOI18N

    /** Name of {@link PersistenceClassElement#isModified modified}
     * flag for {@link PersistenceClassElement class elements}.
     */
    public static final String PROP_MODIFIED = "modified";            // NOI18N

    /** Name of {@link PersistenceClassElement#getObjectIdentityType identity}
     * property for {@link PersistenceClassElement class elements}.
     */
    public static final String PROP_IDENTITY = "identity";            // NOI18N

    /** Name of {@link PersistenceClassElement#getKeyClass key class}
     * property for {@link PersistenceClassElement class elements}.
     */
    public static final String PROP_KEY_CLASS = "keyClass";            // NOI18N

    /** Name of {@link PersistenceClassElement#getFields fields}
     * property for {@link PersistenceClassElement class elements}.
     */
    public static final String PROP_FIELDS = "fields";                // NOI18N

    /** Name of {@link PersistenceClassElement#getConcurrencyGroups concurrency
     * groups} property for {@link PersistenceClassElement class elements}.
     */
    public static final String PROP_GROUPS = "groups";                // NOI18N

    /** Name of {@link PersistenceFieldElement#getPersistenceType persistence}
     * property for {@link PersistenceFieldElement field elements}.
     */
    public static final String PROP_PERSISTENCE = "persistence";    // NOI18N

    /** Name of {@link PersistenceFieldElement#isReadSensitive read sensitivity}
     * and {@link PersistenceFieldElement#isWriteSensitive write sensitivity}
     * property for {@link PersistenceFieldElement field elements}.
     */
    public static final String PROP_SENSITIVITY = "sensitivity";    // NOI18N

    /** Name of {@link PersistenceFieldElement#isKey key field}
     * property for {@link PersistenceFieldElement field elements}.
     */
    public static final String PROP_KEY_FIELD = "keyField";            // NOI18N

    /** Name of {@link RelationshipElement#getUpdateAction update action}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_UPDATE_ACTION = "updateAction";    // NOI18N

    /** Name of {@link RelationshipElement#getDeleteAction delete action}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_DELETE_ACTION = "deleteAction";    // NOI18N

    /** Name of {@link RelationshipElement#isPrefetch prefetch}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_PREFETCH = "prefetch";            // NOI18N

    /** Name of {@link RelationshipElement#getLowerBound lower bound}
     * and {@link RelationshipElement#getUpperBound upper bound}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_CARDINALITY = "cardinality";    // NOI18N

    /** Name of {@link RelationshipElement#getCollectionClass collection class}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_COLLECTION_CLASS =
        "collectionClass";                                            // NOI18N

    /** Name of {@link RelationshipElement#getElementClass element class}
     * property for {@link RelationshipElement relationship elements}.
     */
    public static final String PROP_ELEMENT_CLASS = "elementClass";    // NOI18N

    /** Name of {@link RelationshipElement#getInverseRelationshipName inverse
     * relationship name} property for {@link RelationshipElement relationship
     * elements}.
     */
    public static final String PROP_INVERSE_FIELD = "relatedField";    // NOI18N
}
