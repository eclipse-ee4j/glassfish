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
 * Query.java
 *
 * Created on February 25, 2000
 */

package com.sun.jdo.api.persistence.support;
import java.util.Collection;
import java.util.Map;

/** The Query interface allows applications to obtain persistent instances
 * from the data store.
 *
 * The @link PersistenceManager is the factory for Query instances.  There
 * may be many Query instances associated with a PersistenceManager.  Multiple
 * queries might be executed simultaneously by different threads, but the
 * implementation might choose to execute them serially.  In either case, the
 * implementation must be thread safe.
 *
 * <P>There are three required elements in a Query:
 * the class of the candidate instances,
 * the candidate collection of instances, and the filter.
 *
 * <P>There are optional elements: parameter declarations, variable
 * declarations, import statements, and an ordering specification.
 *
 * @author Craig Russell
 * @version 0.1
 */

public interface Query extends java.io.Serializable
{
    /** Set the class of the candidate instances of the query.
     * <P>The class is a PersistenceCapable class which specifies the class
     * of the candidates of the query.  Elements of the candidate collection
     * that are of the specified class are filtered before being
     * put into the result Collection.
     * @param cls the Class of the candidate instances.
     */
    void setClass(Class cls);

    /** Set the candidate Collection to query.
     * @param pcs the Candidate collection.
     */
    void setCandidates(Collection pcs);

    /** Set the filter for the query.
     *
     * The filter is a Java-like boolean expression used to select elements of the
     * candidate Collection.
     * @param filter the query filter.
     */
    void setFilter(String filter);

    /** Set the import statements to be used to identify the package name of
     * variables or parameters.
     * @param imports import statements separated by semicolons.
     */
    void declareImports(String imports);

    /** Set the parameter list for query execution.
     *
     * The types and names of execution parameters are specified as a String
     * separated by commas, similar to formal method declarations.
     *
     * @param parameters the list of parameters separated by commas.
     */
    void declareParameters(String parameters);
    /** Declare the unbound variables to be used in the query.
     *
     * @param variables the variables separated by semicolons.
     */
    void declareVariables(String variables);

    /** Bind the ordering declarations to the query instance.
     * The ordering consists of one or more ordering declarations separated by commas.
     * Each ordering declaration is the name of the field in the name scope of the
     * candidate class followed by one of the following words: ascending or descending.
     *
     * @param ordering the ordering declarations separated by comma.
     */
    void setOrdering (String ordering);

    /**
     * Set the result of the query.
     * <p>
     * The query result is an optional keyword distinct followed by a Java
     * expression, which tells what values are to be returned by the JDO query.
     * If the result is not specified, then it defaults to "distinct this",
     * which has the effect of returning the elements of the candidates
     * that match the filter.
     */
    void setResult(String result);

    /** Sets the prefetchEnabled option.
     *
     * The prefetchEnabled option specifies whether prefetch of relationship
     * fields should be enabled for this query. The prefetch is enabled by
     * default if such fields are part of DFG. A user needs to explicitely
     * disable prefetch for any particular query if the related instances
     * will not be used in this transaction.
     *
     * @param prefetchEnabled the setting of the prefetchEnabled option.
     */
    void setPrefetchEnabled(boolean prefetchEnabled);

    /** Set the ignoreCache option.
     *
     * The ignoreCache option setting specifies whether the query should execute
     * entirely in the back end, instead of in the cache.
     * @param ignoreCache the setting of the ignoreCache option.
     */
    void setIgnoreCache(boolean ignoreCache);

    /** Get the ignoreCache option setting.
     * @return the ignoreCache option setting.
     * @see #setIgnoreCache
     */
    boolean getIgnoreCache();

    /** Verify the elements of the query and provide a hint to the query to
     * prepare and optimize an execution plan.
     */
    void compile();

    /** Execute the query and return the filtered Collection.
     * @return the filtered Collection.
     * @see #executeWithArray (Object[] parameters)
     */
    Object execute();

    /** Execute the query and return the filtered Collection.
     * @return the filtered Collection.
     * @see #executeWithArray (Object[] parameters)
     * @param p1 the value of the first parameter declared.
     */
    Object execute(Object p1);

    /** Execute the query and return the filtered Collection.
     * @return the filtered Collection.
     * @see #executeWithArray (Object[] parameters)
     * @param p1 the value of the first parameter declared.
     * @param p2 the value of the second parameter declared.
     */
    Object execute(Object p1, Object p2);

    /** Execute the query and return the filtered Collection.
     * @return the filtered Collection.
     * @see #executeWithArray (Object[] parameters)
     * @param p1 the value of the first parameter declared.
     * @param p2 the value of the second parameter declared.
     * @param p3 the value of the third parameter declared.
     */
    Object execute(Object p1, Object p2, Object p3);

    /** Execute the query and return the filtered Collection.
     * @return the filtered Collection.
     * @see #executeWithArray (Object[] parameters)
     * @param parameters the Map containing all of the parameters.
     */
    Object executeWithMap (Map parameters);

    /** Execute the query and return the filtered Collection.
     *
     * <P>The execution of the query obtains the values of the parameters and
     * matches them against the declared parameters in order.  The type of
     * the declared parameters must match the type of the passed parameters,
     * except that the passed parameters might need to be unwrapped to get
     * their primitive values.
     *
     * <P>The filter, import, declared parameters, declared variables, and
     * ordering statements are verified for consistency.
     *
     * <P>Each element in the candidate Collection is examined to see that it
     * is assignment compatible to the Class of the query.  It is then evaluated
     * by the boolean expression of the filter.  The element passes the filter
     * if there exist unique values for all variables for which the filter
     * expression evaluates to true.
     * @return the filtered Collection.
     * @param parameters the Object array with all of the parameters.
     */
    Object executeWithArray (Object[] parameters);

    /** Get the PersistenceManager associated with this Query.
     *
     * <P>If this Query has no PersistenceManager return null.
     * @return the PersistenceManager associated with this Query.
     */
    PersistenceManager getPersistenceManager();
}

