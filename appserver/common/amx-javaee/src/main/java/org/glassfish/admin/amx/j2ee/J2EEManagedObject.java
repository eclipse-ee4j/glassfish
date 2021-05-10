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

package org.glassfish.admin.amx.j2ee;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.core.AMXProxy;


/**
 * The J2EEManagedObject model is the base model of all managed objects
 * in the J2EE Management Model. All managed objects in the J2EE Platform
 * must implement the J2EEManagedObject model.
 */
public interface J2EEManagedObject extends AMXProxy {

    /**
     * The ObjectName of the J2EEManagedObject.
     * All managed objects must have a unique name within the context of
     * the management domain. The name must not be null.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "getobjectName" as defined by JSR 77.
     *
     * @return the ObjectName of the object, as a String
     */
    @ManagedAttribute
    String getobjectName();


    /**
     * If true, indicates that the managed object provides event
     * notification about events that occur on that object.
     * NOTE: JSR 77 defines the Attribute name as "eventProvider".
     */
    @ManagedAttribute
    boolean iseventProvider();


    /**
     * If true, indicates that this managed object implements the
     * StateManageable model and is state manageable.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "stateManageable" as defined by JSR 77.
     */
    @ManagedAttribute
    boolean isstateManageable();


    /**
     * If true, indicates that the managed object supports performance
     * statistics and therefore implements the StatisticsProvider model.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "statisticProvider" as defined by JSR 77.
     */
    @ManagedAttribute
    boolean isstatisticsProvider();
}
