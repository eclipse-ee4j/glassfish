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

/*
 * JavaEEScanner.java
 *
 * Created on November 1, 2005, 5:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.apf.impl;

import org.glassfish.apf.ComponentInfo;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;

import java.io.File;
import java.io.IOException;

/**
 * Super class for all JavaEE scanners
 *
 * @author Jerome Dochez
 */
public abstract class JavaEEScanner {

    Types types;
    
    public ComponentInfo getComponentInfo(Class componentImpl){
        return new ComponentDefinition(componentImpl);
    }

    protected void initTypes(File file) throws IOException {
        ParsingContext context = new ParsingContext.Builder().build();
        Parser cp = new Parser(context);
        cp.parse(file, null);
        try {
            cp.awaitTermination();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        types = cp.getContext().getTypes();
    }

    public Types getTypes() {
        return types;
    }

}
