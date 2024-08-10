/*
 * Copyright (c) 2006, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.annotation.factory.SJSASFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Set;

import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 * This class contains the list of all annotations types name which can be present at the class level (Type.TYPE).
 *
 * @author Jerome Dochez
 */
@Service(name = "default")
@Singleton
public class DefaultAnnotationScanner implements AnnotationScanner, PostConstruct {

    @Inject
    SJSASFactory factory;

    private Set<String> annotations;
    private Set<String> annotationsMetaDataComplete;

    @Override
    public boolean isAnnotation(String value) {
        return annotations.contains(value);
    }

    @Override
    public void postConstruct() {
        annotations = factory.getAnnotations(false);
        annotationsMetaDataComplete = factory.getAnnotations(true);
    }

    @Override
    public Set<String> getAnnotations() {
        return getAnnotations(false);
    }

    public Set<String> getAnnotations(boolean isMetaDataComplete) {
        if (!isMetaDataComplete) {
            return AbstractAnnotationScanner.constantPoolToFQCN(annotations);
        }

        return AbstractAnnotationScanner.constantPoolToFQCN(annotationsMetaDataComplete);
    }
}
