/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import jakarta.inject.Singleton;

import java.util.Set;


/**
 * This class contains the list of all annotations types name
 * which can be present at the class level (Type.TYPE).
 *
 * @author Jerome Dochez
 */
@Service(name="default")
@Singleton
public class DefaultAnnotationScanner implements AnnotationScanner,
    PostConstruct {

    @Inject
    SJSASFactory factory;

    private Set<String> annotations=null;
    private Set<String> annotationsMetaDataComplete=null;

    /**
     * Test if the passed constant pool string is a reference to
     * a Type.TYPE annotation of a J2EE component
     *
     * @String the constant pool info string
     * @return true if it is a J2EE annotation reference
     */
    public boolean isAnnotation(String value) {
        return annotations.contains(value);
    }

    public void postConstruct() {
        annotations = factory.getAnnotations(false);
        annotationsMetaDataComplete = factory.getAnnotations(true);
    }

    public Set<String> getAnnotations(boolean isMetaDataComplete) {
        if (!isMetaDataComplete) {
            return AbstractAnnotationScanner.constantPoolToFQCN(annotations);
        } else {
            return AbstractAnnotationScanner.constantPoolToFQCN(annotationsMetaDataComplete);
        }
    }

    @Override
    public Set<String> getAnnotations() {
        return getAnnotations(false);
    }
}
