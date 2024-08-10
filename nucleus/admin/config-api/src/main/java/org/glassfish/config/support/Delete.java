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

package org.glassfish.config.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ExecuteOn;
import org.jvnet.hk2.config.GenerateServiceFromMethod;
import org.jvnet.hk2.config.GeneratedServiceName;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Delete command annotation
 *
 * A method annotated with the Delete annotation will generate a generic implementation of a delete administrative
 * command responsible for delete instances of the type as referenced by the annotated method.
 *
 * The deleted type is determine by the annotated method which must follow one of the two following patterns : List<X>
 * getXs(); or void setX(X x);
 *
 * X is the type of instance that will be deleted, the name of the method is actually immaterial. So for example, the
 * following
 *
 * <pre>
 * <code>
 * &#64;Delete
 * public List<Foo> getAllMyFoos();
 * &#64;Delete
 * public void setMySuperFoo(Foo foo);
 * &#64;Delete
 * public List<Foo> getFoos();
 * &#64;Delete
 * public List<Foo> getFoo();
 * </code>
 * </pre>
 *
 * will all be valid annotated methods.
 *
 * The instance to be removed from the parent must be resolved by the resolver attribute. The resolver can use injection
 * or the command invocation parameters (using the {@link org.glassfish.api.Param} annotation) to get enough information
 * to look up or retrieve the instance to be deleted.
 *
 * Usually, most instances can be looked up by using the instance type and its key (provided by the --name or --target
 * parameters for instance).
 *
 * See {@link Create} for initialization information
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
@GenerateServiceFromMethod(implementation = "org.glassfish.config.support.GenericDeleteCommand", advertisedContracts = "org.glassfish.api.admin.AdminCommand")
public @interface Delete {

    /**
     * Name of the command that will be used to register this generic command implementation under.
     *
     * @return the command name as the user types it.
     */
    @GeneratedServiceName
    String value();

    /**
     * Returns the instance of the configured object that should be deleted. The implementation of that interface can use
     * the command parameters to make a determination about which instance should be used.
     *
     * @return the instance targeted for deletion.
     */
    Class<? extends CrudResolver> resolver() default CrudResolver.DefaultResolver.class;

    /**
     * Returns a decorator type that should be looked up and called when a configuration element of the annotated type is
     * deleted.
     *
     * @return a deletion decorator for the annotated type
     */
    Class<? extends DeletionDecorator> decorator() default DeletionDecorator.NoDecoration.class;

    /**
     * Returns the desired behaviors in a clustered environment. By default, using all the
     * {@link org.glassfish.api.admin.ExecuteOn} default values
     *
     * @return the cluster information
     */
    ExecuteOn cluster() default @ExecuteOn();

    /**
     * Returns the i18n key that will be used to look up a localized string in the annotated type module.
     *
     * @return the key to look up localized description for the command.
     */
    I18n i18n();

}
