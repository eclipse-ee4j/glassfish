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
 * RetrieveDesc.java
 *
 * Created on March 3, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore;


/**
 * <P>This interface represents a retrieve descriptor used by an application
 * to retrieve container-managed entity beans from a persistent store. It
 * allows you specify which persistent fields an application wants to retrieve.
 * In addition, it allows an application to specify sophisticated constraints
 * on its object retrieval.
 */
public interface RetrieveDesc extends ActionDesc {

    /**
    * The addResult method is used to specify which fields should be
    * returned in a persistent object. If the field requested is a
    * reference to another persistent object then a RetrieveDesc may be
    * provided which describes which fields of the referenced object
    * should be returned and, optionally, constraints on it.
    * The parameter <code>projection</code> specifies, if the field
    * specified by <code>name</code> should be projected.
    *
    * @param name The name of the field to return.
    * @param foreignConstraint
    * RetrieveDesc describing fields and constraints for a referenced object.
    * @param projection Specifies, if this is a projection.
    */
   public void addResult(String name, RetrieveDesc foreignConstraint, boolean projection);

    /**
     * The addResult method can be used to specify <it>global</it>
     * query attributes that don't end up in the where clause.
     * Aggregate functions and the distinct op code are examples for
     * those query options. The result type defines the object to be
     * returned by an aggregate query. In case of distinct the result
     * type should be FieldTypeEnumeration.NOT_ENUMERATED. The method
     * might be called twice, in case of a JDOQL query having both an
     * aggregate and distinct:
     * query.setResult("avg (distinct salary)");
     * ->
     * retrieveDesc.addResult(OP_AVG, FieldTypeEnumeration.DOUBLE);
     * retrieveDesc.addResult(OP_DISTINCT, FieldTypeEnumeration.NOT_ENUMERATED);
     * retrieveDesc.addResult("salary", null, true);
     *
     * @param opCode The operation code.
     * @param resultType The object type returned by aggregate queries.
     * @see com.sun.jdo.spi.persistence.utility.FieldTypeEnumeration
     */
    public void addResult(int opCode, int resultType);

    /**
     * <P>Adds a constraint on the persistent field specified by
     * <code>name</code>. The valid values for <code>operation
     * </code> are defined in <b>ActionDesc</b>. The parameter
     * <code>value</code> specifies the constraint value.
     * <P>By default, multiple constraints are implicitly ANDed together.
     * If the applications want to OR together the constraints,
     * it can explicitly add <b>OP_OR</b> constraints. For example,
     * to OR together two constraints, an application can do the following:
     * <PRE>
     * addConstraint("field1", ActionDesc.OP_EQ, "field1Value");
     * addConstraint("field2", ActionDesc.OP_EQ, "field2Value");
     * addConstraint(null, ActionDesc.OP_OR, null);
     * </PRE>
     * <P>The important thing to note about the above example is
     * that the constraints are processed in postfix order, so
     * the above example should be read as
     * <PRE>
     * (field1 == "field1Value") OR (field2 == "field2Value")
     * </PRE>
     */
    public void addConstraint(String name, int operation, Object value);

    /**
     * <P>Adds a constraint on the foreign field specified by
     * <code>name</code>. This method is used to specify a relationship
     * navigation on field <code>name</code> to the class represented by
     * the retrieve descriptor <code>foreignConstraint</code>.
     * If <code>name</code> is null, an unrelated constraint is added.
     * A constraint is unrelated, if there is neither a foreign field
     * nor a local field connecting to the retrieve descriptor
     * <code>foreignConstraint</code>.
     */
    void addConstraint(String name, RetrieveDesc foreignConstraint);

    /**
     * <P>Adds a constraint on the field specified by <code>name</code>.
     * This method is useful e.g. for comparisons of local fields with field of a related object:
     *   emp.addConstraint("lastName", ActionDesc.OP_EQ, mgr, lastName");
     * compares the employee's lastName field with the lastName field of the related manager.
     */
    void addConstraint(String name, int operator, RetrieveDesc foreignConstraint, String foreignFieldName);

    /**
     * Sets a navigational id on the retrieve descriptor.
     * This id will be used to discriminate different retrieve descriptors which
     * use the same navigational field.
     */
    void setNavigationalId(Object navigationalId);

    /** Sets the prefetchEnabled option.
     *
     * The prefetchEnabled option specifies whether prefetch of relationship
     * fields should be enabled for this retrieve descriptor. The prefetch
     * is enabled by default if such fields are part of DFG. A user needs
     * to explicitely disable prefetch for any particular query if the related
     * instances will not be used in this transaction.
     *
     * @param prefetchEnabled the setting of the prefetchEnabled option.
     */
    void setPrefetchEnabled(boolean prefetchEnabled);
}
