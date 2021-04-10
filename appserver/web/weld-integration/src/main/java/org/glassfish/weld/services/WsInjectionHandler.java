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

/**
 *
 * @author lukas
 */
interface WsInjectionHandler {
    WsInjectionHandler NOOP = new WsInjectionHandler() {
        @Override
        public boolean handles(AnnotatedField annotatedField) {
            return false;
        }

        @Override
        public void validateWebServiceRef(AnnotatedField annotatedField) {
            //Should not get here
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getJndiName(AnnotatedField annotatedField) {
            //Should not get here
            throw new UnsupportedOperationException("Not supported.");
        }
    };

    boolean handles(AnnotatedField annotatedField);

    void validateWebServiceRef(AnnotatedField annotatedField);

    String getJndiName(AnnotatedField annotatedField);
}
