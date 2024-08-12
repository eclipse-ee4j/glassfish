/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.introspection;

import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import org.jvnet.hk2.annotations.Service;

@Service(name = "rar")
@Singleton
public class ResourceAdapterAnnotationScanner implements AnnotationScanner {

    private Set<String> annotations;

    @Override
    public boolean isAnnotation(String value) {
        if (annotations == null) {
            synchronized (ResourceAdapterAnnotationScanner.class) {
                if (annotations == null)
                    init();
            }
        }

        return annotations.contains(value);
    }

    @Override
    public Set<String> getAnnotations() {
        return AbstractAnnotationScanner.constantPoolToFQCN(annotations);
    }

    private void init() {
        annotations = new HashSet<>();
        annotations.add("Ljakarta/resource/spi/Connector;");
        annotations.add("Ljakarta/resource/spi/AdministeredObject;");
        annotations.add("Ljakarta/resource/spi/Activation;");
        annotations.add("Ljakarta/resource/spi/AuthenticationMechanism;");
        annotations.add("Ljakarta/resource/spi/ConfigProperty;");
        annotations.add("Ljakarta/resource/spi/ConnectionDefinition;");
        annotations.add("Ljakarta/resource/spi/ConnectionDefinitions;");
        annotations.add("Ljakarta/resource/spi/SecurityPermission;");
    }
}
