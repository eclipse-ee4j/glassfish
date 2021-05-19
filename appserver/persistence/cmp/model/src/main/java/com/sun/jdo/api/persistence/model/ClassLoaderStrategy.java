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
 * ClassLoaderStrategy.java
 *
 * Created on August 29, 2003, 3:19 PM
 */

package com.sun.jdo.api.persistence.model;

/**
 *
 * @author mvatkina
 * @version %I%
 */
public class ClassLoaderStrategy
{
    /** System property key used to define the model behavior concerning
     * multiple class loaders.
     * Value should be one of
     * {@link #MULTIPLE_CLASS_LOADERS_IGNORE},
     * {@link #MULTIPLE_CLASS_LOADERS_RELOAD}, or
     * {@link #MULTIPLE_CLASS_LOADERS_ERROR}
     */
    public static final String PROPERTY_MULTIPLE_CLASS_LOADERS =
        "com.sun.jdo.api.persistence.model.multipleClassLoaders"; //NOI18N

    /** Constant representing the value "ignore" of the System property
     * com.sun.jdo.api.persistence.model.multipleClassLoaders
     * Setting the system property to "ignore" causes the model to ignore
     * any new class loader for the same fully qualified class name.
     * @see RuntimeModel#findClassLoader
     */
    public static final String MULTIPLE_CLASS_LOADERS_IGNORE = "ignore"; //NOI18N

    /** Constant representing the value "reload" of the System property
     * com.sun.jdo.api.persistence.model.multipleClassLoaders
     * Setting the system property to "reload" causes the model to reload
     * the class mapping if it is specified with a new class loader.
     * @see RuntimeModel#findClassLoader
     */
    public static final String MULTIPLE_CLASS_LOADERS_RELOAD = "reload"; //NOI18N

    /** Constant representing the value "error" of the System property
     * com.sun.jdo.api.persistence.model.multipleClassLoaders
     * Setting the system property to "reload" causes the model to throw an
     * exception if the same class is used with a diferent class loader.
     * @see RuntimeModel#findClassLoader
     */
    public static final String MULTIPLE_CLASS_LOADERS_ERROR = "error"; //NOI18N

    /** Value of the property used to define the model behavior concerning
     * multiple class loaders.
     */
    private static String _strategy = System.getProperty(
        PROPERTY_MULTIPLE_CLASS_LOADERS, MULTIPLE_CLASS_LOADERS_ERROR);

    /** Get the value of the property
     * {@link #PROPERTY_MULTIPLE_CLASS_LOADERS} used to define the model
     * behavior concerning multiple class loaders.
     * @return the value of the property, one of
     * {@link #MULTIPLE_CLASS_LOADERS_IGNORE},
     * {@link #MULTIPLE_CLASS_LOADERS_RELOAD}, or
     * {@link #MULTIPLE_CLASS_LOADERS_ERROR}
     */
    public static String getStrategy ()
    {
        return _strategy;
    }

    /** Sets the value of the property
     * {@link #PROPERTY_MULTIPLE_CLASS_LOADERS} used to define the model
     * behavior concerning multiple class loaders.
     * @param strategy the new value of the property. Value should be one of
     * {@link #MULTIPLE_CLASS_LOADERS_IGNORE},
     * {@link #MULTIPLE_CLASS_LOADERS_RELOAD}, or
     * {@link #MULTIPLE_CLASS_LOADERS_ERROR}
     * @see RuntimeModel#findClassLoader
     */
    public static void setStrategy (String strategy)
    {
        _strategy = strategy;
    }

}
