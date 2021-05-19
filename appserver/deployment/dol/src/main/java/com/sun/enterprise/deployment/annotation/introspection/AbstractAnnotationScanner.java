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

package com.sun.enterprise.deployment.annotation.introspection;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract superclass for instance-based annotation scanners.
 *
 * @author tjquinn
 */
public abstract class AbstractAnnotationScanner implements AnnotationScanner {

    /** holds the annotations of interest to the specific type of scanner */
    protected volatile Set<String> annotations=null;

    /** Creates a new instance of AbstractAnnotationScanner */
    public AbstractAnnotationScanner() {
    }

    /**
     * Invoked so the concrete subclass can populate the annotations set.
     * <p>
     * Concrete implementations of this method should add to the set one or more Strings
     * corresponding to the annotations of interest.
     *
     * @param annotationsSet the Set object to be populated
     */
    protected abstract void init(Set<String> annotationsSet);

    /**
     * Test if the passed constant pool string is a reference to
     * a Type.TYPE annotation of a J2EE component
     *
     * @String the constant pool info string
     * @return true if it is an annotation reference of interest to this scanner
     */
    public boolean isAnnotation(String value) {
        if (annotations==null) {
            synchronized(this) {
                if (annotations == null) {
                    annotations = new HashSet();
                    init(annotations);
                }
            }
        }
        return annotations.contains(value);
    }

    @Override
    public Set<String> getAnnotations() {
        return constantPoolToFQCN(annotations);
    }

    public static Set<String> constantPoolToFQCN(Set<String> annotations) {
        // for now I transform ConstantPoolInfo type in FQCN
        Set<String> fqcns = new HashSet<String>();
        for (String annotation : annotations) {
            String fqcn = annotation.substring(1, annotation.length()-1).replaceAll("/",".");
            fqcns.add(fqcn);
        }
        return fqcns;
    }
}

