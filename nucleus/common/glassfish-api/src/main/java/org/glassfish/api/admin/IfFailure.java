/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.lang.annotation.*;

/**
 * Annotation used to indicate what type of failure action should be performed if the
 * annotated method was to return a failure error code or throw an exception.
 *
 * @author Jerome Dochez
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IfFailure {

    /**
     * Returns the intent action to perform if the annotated method does not execute
     * successfully (expressed by a faulty error code or an exception thrown).
     *
     * @return the intent action to perform 
     */
    FailurePolicy value();
}
