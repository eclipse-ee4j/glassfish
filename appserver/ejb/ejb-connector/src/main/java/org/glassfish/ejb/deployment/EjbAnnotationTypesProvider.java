/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.internal.deployment.AnnotationTypesProvider;

import jakarta.ejb.MessageDriven;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.ejb.Singleton;
import java.lang.annotation.Annotation;

/**
 * Provides the annotation types for the EJB Types
 *
 * @author Jerome Dochez
 */
@Service(name="EJB")
public class EjbAnnotationTypesProvider implements AnnotationTypesProvider {
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return new Class[] {
                MessageDriven.class, Stateful.class, Stateless.class, Singleton.class };    }

    public Class getType(String typename) throws ClassNotFoundException {
        return getClass().getClassLoader().loadClass(typename);
    }
}
