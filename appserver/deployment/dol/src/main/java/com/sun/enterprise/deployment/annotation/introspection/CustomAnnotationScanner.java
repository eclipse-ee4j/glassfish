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

/**
 * This interface allows custom annotation scanner to be created.
 *
 * @author Qingqing Ouyang
 */
public interface CustomAnnotationScanner {

    /**
     * Test if the passed constant pool string is a reference to a Type.TYPE annotation of a component defined in this scanner
     *
     * @String the constant pool info string
     * @return true if it is an annotation reference
     */
    boolean isAnnotation(String value);
}
