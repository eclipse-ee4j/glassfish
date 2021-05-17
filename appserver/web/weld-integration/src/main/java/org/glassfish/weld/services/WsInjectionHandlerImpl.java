/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceRef;

/**
 *
 * @author lukas
 */
public final class WsInjectionHandlerImpl implements WsInjectionHandler {

    @Override
    public boolean handles(AnnotatedField annotatedField) {
        try {
            return annotatedField.isAnnotationPresent(WebServiceRef.class);
        } catch (NoClassDefFoundError error) { // in web profile class WebServiceRef is not available
            return false;
        }
    }

    @Override
    public void validateWebServiceRef(AnnotatedField annotatedField) {
        WebServiceRef webServiceRef = annotatedField.getAnnotation(WebServiceRef.class);
        if (webServiceRef != null) {
            if (Service.class.isAssignableFrom(annotatedField.getJavaMember().getType())) {
                return;
            }

            if (!annotatedField.getJavaMember().getType().isInterface()) {
                throw new DefinitionException(
                    "The type of the injection point " + annotatedField.getJavaMember().getName() + " is " +
                    annotatedField.getJavaMember().getType().getName() +
                    ".  This type is invalid for a field annotated with @WebSreviceRef");
            }

            Class<?> serviceClass = webServiceRef.value();
            if (serviceClass != null) {
                if (!Service.class.isAssignableFrom(serviceClass)) {
                    throw new DefinitionException(
                        "The type of the injection point " + annotatedField.getJavaMember().getName() +
                        " is an interface: " + annotatedField.getJavaMember().getType().getName() +
                        ".  The @WebSreviceRef value of " + serviceClass + " is not assignable from " + Service.class.getName());
                }
            }
        }
    }

    @Override
    public String getJndiName(AnnotatedField annotatedField) {
        WebServiceRef webServiceRef = annotatedField.getAnnotation(WebServiceRef.class);
        return InjectionServicesImpl.getJndiName(webServiceRef.lookup(), webServiceRef.mappedName(), webServiceRef.name());
    }
}
