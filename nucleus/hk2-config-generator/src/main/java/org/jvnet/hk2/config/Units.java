/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.management.DescriptorKey;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
    Units annotation to be used on @Configured interfaces.  Units are strings because
    they are an unbounded set, but do try to use the provided values for consistency.

   @author Lloyd Chambers
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface Units {
    public final String MILLISECONDS = "milliseconds";
    public final String SECONDS = "seconds";
    public final String MINUTES = "minutes";
    public final String HOURS = "hours";
    public final String DAYS = "days";

    public final String BYTES = "bytes";
    public final String KILOBYTES = "kilobytes";
    public final String MEGABYTES = "megabytes";
    public final String GIGABYTES = "gigabytes";

    /** value is an arbitrary count */
    public final String COUNT = "count";

    /**
    Units should always be lower-case and appropriate for human viewing.  Suggested units:
    <ul>
        <li>"seconds", "milliseconds", "minutes", "hours", "days"</li>
        <li>"count"</li>
        <li>"bytes", "kilobytes", "megabytes", "gigabytes"</li>
    </ul>
    */
    @DescriptorKey("units")
    public String units();
}






