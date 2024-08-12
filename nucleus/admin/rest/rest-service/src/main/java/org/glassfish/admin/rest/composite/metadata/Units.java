/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite.metadata;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate what units a scalar property has (e.g. that a long property holds bytes)
 *
 * @author tmoreau
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Units {
    public static final String SECONDS = "seconds";
    public static final String MINUTES = "minutes";
    public static final String HOURS = "hours";
    public static final String MILLISECONDS = "milliseconds";
    public static final String BYTES = "bytes";
    public static final String KILOBYTES = "kilobytes";
    public static final String MEGABYTES = "megabytes";
    public static final String DATE = "date";
    public static final String PERCENT = "percent";

    String units();
}
