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

package org.glassfish.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identify an I18n resource associated with the annotated element. The value() holds the name of the resource as it
 * stored in the LocalStrings.properties and can be used by the runtime to generate appropriate localized meta-data.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD })
public @interface I18n {

    /**
     * Returns the string identify the i18n resource from the resource bundle associated with the class containing the
     * annotation.
     *
     * @return a string identifying the resource in the bundle
     */
    public String value();

}
