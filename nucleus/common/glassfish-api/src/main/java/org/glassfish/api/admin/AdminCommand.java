/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.annotations.Contract;

/**
 * <p>
 * This is an admin command interface, command implementations have to be stateless and should also have a
 * {@link org.jvnet.hk2.component.Scope} value of {@link org.glassfish.hk2.api.PerLookup}
 * </p>
 *
 * Command implementations should use the {@link org.glassfish.api.Param} annotation to annotate the command parameters.
 *
 * Command implementations are normal services and are therefore following the normal hk2 service lifecycle and
 * injection features.
 * 
 * <p>
 * Internationalization can be provided by using the {@link org.glassfish.api.I18n}} annotation. Each parameter
 * declaration can also be annotated with an {@link org.glassfish.api.I18n} annotation to point to the parameter .
 * </p>
 *
 * By default, if an {@link org.glassfish.api.I18n} is used to annotate implementations, the value of the annotation
 * will be used as follow to lookup strings in the module's local strings properties files.
 *
 * key provide a short description of the command role and expected output key.usagetext [optional] if not provided,
 * usage text will be calculated based on parameters declaration key.paramName [optional] provide a description for the
 * parameter "paramName", it can be overriden by annotating the @Param annotated field/method with a
 * {@link org.glassfish.api.I18n}
 *
 * @author Jerome Dochez
 */
@Contract
public interface AdminCommand {

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the parameter names and the
     * values are the parameter values
     * 
     * @param context information
     */
    public void execute(AdminCommandContext context);

}
