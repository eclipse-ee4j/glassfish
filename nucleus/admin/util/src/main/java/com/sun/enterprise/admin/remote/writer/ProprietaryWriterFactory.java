/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.writer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author martinmares
 */
public class ProprietaryWriterFactory {
    
    private static final List<ProprietaryWriter> proprietaryWriters;
    static {
        proprietaryWriters = new ArrayList<ProprietaryWriter>(2);
        proprietaryWriters.add(new ParameterMapFormProprietaryWriter());
        proprietaryWriters.add(new MultipartProprietaryWriter());
    }
    
    public static ProprietaryWriter getWriter(final Object entity) {
        for (ProprietaryWriter pw : proprietaryWriters) {
            if (pw.isWriteable(entity)) {
                return pw;
            }
        }
        return null;
    }
    
}
