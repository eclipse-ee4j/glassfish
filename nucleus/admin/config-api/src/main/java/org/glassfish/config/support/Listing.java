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

package org.glassfish.config.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.api.I18n;
import org.jvnet.hk2.config.GenerateServiceFromMethod;
import org.jvnet.hk2.config.GeneratedServiceName;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * List command annotation.
 *
 * Follow the same pattern as {@link Create} or {@link Delete} annotations to generate a command implementation to list
 * elements.
 *
 * Types of elements are listed are infered from the annotated method and parent instance to get the list of elements
 * from must be returned by the resolver.
 *
 * See {@link Create} for initialization information
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
@GenerateServiceFromMethod(implementation = "org.glassfish.config.support.GenericListCommand", advertisedContracts = "org.glassfish.api.admin.AdminCommand")
public @interface Listing {

    /**
     * Name of the command that will be used to register this generic command implementation under.
     *
     * @return the command name as the user types it.
     */
    @GeneratedServiceName
    String value();

    /**
     * Returns the instance of the parent that should be used get the list of children.
     *
     * @return the parent instance.
     */
    Class<? extends CrudResolver> resolver() default CrudResolver.DefaultResolver.class;

    /**
     * Returns the i18n key that will be used to look up a localized string in the annotated type module.
     *
     * @return the key to look up localized description for the command.
     */
    I18n i18n();
}
