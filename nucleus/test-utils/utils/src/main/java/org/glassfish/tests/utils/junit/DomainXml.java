/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.mock.TestDocument;
import org.jvnet.hk2.config.DomDocument;

/**
 * Path to custom domain.xml used to load configuration by the {@link HK2JUnit5Extension}
 *
 * @author David Matejcek
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainXml {

    /**
     * @return Resource path with domain.xml-like content used for the test.
     * The path is relative to the test classloader.
     */
    String value();


    /**
     * Default is {@link TestDocument}.
     *
     * @return DOM tree representation. Must have public constructor with a single parameter
     *         {@link ServiceLocator}
     */
    Class<? extends DomDocument<?>> domDocumentClass() default TestDocument.class;
}
