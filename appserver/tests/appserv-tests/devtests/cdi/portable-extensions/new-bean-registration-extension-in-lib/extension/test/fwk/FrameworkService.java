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

package test.fwk;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RetentionPolicy.RUNTIME)

/**
 * A CDI Qualifier that represents a reference to a
 * service in the framework. If this qualifiers annotates an injection point, 
 * the framework extension discovers and instantiates
 * a service implementing the service interface type of the injection point
 * and makes it available for injection to that injection point.
 */ 
public @interface FrameworkService {
    /**
     * Determines if the service reference that is injected
     * refers to a dynamic proxy or the actual service reference obtained
     * from the framework service registry  
     */
   boolean dynamic() default false; //dynamic
   
   /**
    * service discovery criteria
    */
   String serviceCriteria() default ""; 
   
   /**
    * wait specified in millis. -1 indicates indefinite wait
    */
   int waitTimeout() default -1; 
}
