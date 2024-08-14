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

package org.glassfish.quality;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
    Annotation indicating the code needs attention for some reasson
 */
@Retention(RUNTIME) // could be CLASS if desired
@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})
@Documented
public @interface ToDo {
   public enum Priority{
    /** Needs prompt attention, a stop-ship issue */
    CRITICAL,
    /** Needs attention soon, could have important side-effects if not addressed */
    IMPORTANT,
    /** should be fixed, but side-effects are likely minor */
    MINOR,
    /** Use of this value is discouraged, choose one of the above and details() */
    UNKNOWN
   };
   public enum Kind{
    /** Code needs modification. Code means annotations, but not javadoc. */
    CODE,

    /** Documentation needed, javadoc or other forms */
    DOCS,

    /** Both code and documentation are needed */
    CODE_AND_DOCS,
    };

   /** How important */
   Priority priority() default Priority.UNKNOWN;

   /** What kind of activity is required */
   Kind kind() default Kind.CODE;

   /** concise summary of what's required */
   String details() default "unspecified";

   /**
     Optional info to locate responsible party, could be email, name, team, etc
     Could an IDE  insert ${user} when editing?
   */
   String contact() default "";
}

