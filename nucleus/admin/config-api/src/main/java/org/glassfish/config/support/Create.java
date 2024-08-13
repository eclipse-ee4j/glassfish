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
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.ExecuteOn;
import org.jvnet.hk2.config.GenerateServiceFromMethod;
import org.jvnet.hk2.config.GeneratedServiceName;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create command annotation.
 *
 * Methods annotated with this annotation delegates to the framework to provide a generic administrative command that
 * create configured instances.
 *
 * A Create annotation is used on a method of a configured interface. The method identifies the setter of a type that
 * can be added to the configured interface.
 *
 * The annotated method must follow one of the two pattern : List<X> getXs(); or void setX(X x);
 *
 * the name of the method is immaterial, only the generic type of the returned List or the single parameter of the
 * setter method are used to determine the type of configured instance the command will create.
 *
 * the resolver is used to find which instance of the parent type should be used to add the newly created child. The
 * resolver can also be annotated with {@link org.glassfish.api.Param} to get parameters passed to the command
 * invocation (like a name or a target parameter).
 *
 * The generic command implementation will use the parameters passed to the command invocation with the
 * {@link org.glassfish.api.Param} annotations on the child type to match command parameters to configuration
 * attributes.
 *
 * Sometimes, the creation of a new configuration will require more work or more attributes/elements creation than what
 * can be provided or specified during the command invocation. In such a case, the Create annotation can specify a
 * decorator that will be called during the generic command execution, with the newly created instance.
 *
 * The {@link CreationDecorator} will be looked up by its type and normal injection or parameter injection can happen.
 *
 * Internationalization of generic commands follow the same rule as described in the {@link AdminCommand} javadocs. The
 * {@link I18n} annotation referenced from this annotation will be used as the top level command annotation, which
 * should provide the command description and expected result.
 *
 * Parameters can be annotated with @I18n as well to override the default mapping and all resources must be located in
 * the target type module local strings properties file.
 *
 * Sometimes, the @Create annotation cannot be used in the parent configuration object because the parent cannot have a
 * direct reference its children types.
 *
 * For instance, if you have a declaration like <code>
 *     public interface ParentContainer {
 *      &#64;Element("*)
 *      List<ParentConfigType> children();
 *     }
 * </code>
 *
 * you cannot use the @Create annotation in such declaration because you do not know which subtypes of ParentConfigType
 * will exist.
 *
 * In such cases, you should place the @Create on the child type and use the @Decorate annotation alongside to specify
 * the parent's method used to add the element to the parent.
 *
 * <code>
 *     &#64;Create(....)
 *     &#64;Decorate(parentType=ParentContainer.class, methodName="children",
 *      with={Create.class})
 *     public interface SomeChild extends ParentConfigType {
 *      ...
 *     }
 * </code>
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@GenerateServiceFromMethod(implementation = "org.glassfish.config.support.GenericCreateCommand", advertisedContracts = "org.glassfish.api.admin.AdminCommand")
public @interface Create {

    /**
     * Name of the command that will be used to register this generic command implementation under.
     *
     * @return the command name as the user types it.
     */
    @GeneratedServiceName
    String value();

    /**
     * Returns the instance of the parent that should be used to add the newly created instance under. The implementation of
     * that interface can use the command parameters to make a determination about which instance should be used.
     *
     * @return the parent instance.
     */
    Class<? extends CrudResolver> resolver() default CrudResolver.DefaultResolver.class;

    /**
     * Returns a decorator type that should be looked up and called when a new configuration element of the annotated type
     * is created.
     *
     * @return a decorator for the annotated type
     */
    Class<? extends CreationDecorator> decorator() default CreationDecorator.NoDecoration.class;

    /**
     * Returns the desired behaviors in a clustered environment. By default, using all the {@link ExecuteOn} default values
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
