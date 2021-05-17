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

package org.glassfish.ejb.deployment.descriptor.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import org.glassfish.deployment.common.Descriptor;

public class PrefetchDisabledDescriptor extends Descriptor {

    private ArrayList methodDescs = new ArrayList();
    private ArrayList convertedMethodDescs = new ArrayList();
    private EjbDescriptor ejbDescriptor = null;

    /** Default constructor. */
    public PrefetchDisabledDescriptor() {
    }

     /**
      * Getter for method
      * @return Value of MethodDescriptor list
      */
    public ArrayList getMethodDescriptors() {
        return methodDescs;
    }

     /**
      * Getter for converted method
      * @return Value of style converted MethodDescriptor list
      */
    public ArrayList getConvertedMethodDescs() {
       if (convertedMethodDescs.isEmpty()) {
           convertStylePrefetchDisabledMethods();
       }
       return convertedMethodDescs;
    }


    /**
      * Getter for ejbDescriptor
      * @return Value of ejbDescriptor
      */
    public EjbDescriptor getEjbDescriptor() {
        return ejbDescriptor;
    }

    /**
     * Setter for ejbDescriptors
     * @param ejbDescriptors New value of ejbDescriptor.
     */
    public void setEjbDescriptor(
        EjbDescriptor ejbDescriptor) {
        this.ejbDescriptor = ejbDescriptor;
    }


    /**
     * Setter for method
     * @param MethodDescriptor New value of MethodDescriptor to add.
     */
    public void addMethodDescriptor(MethodDescriptor methodDesc) {
        methodDescs.add(methodDesc);
    }

    private void convertStylePrefetchDisabledMethods() {
        Set allMethods = ejbDescriptor.getMethodDescriptors();
        for (Iterator mdItr = methodDescs.iterator(); mdItr.hasNext();) {
            MethodDescriptor methodDesc = (MethodDescriptor) mdItr.next();

            // the ejb-name element defined in the method element will
            // be always ignored and overriden by the one defined in
            // ejb element
            methodDesc.setEjbName(ejbDescriptor.getName());

            // Convert to style 3 method descriptors
            Vector mds = methodDesc.doStyleConversion(ejbDescriptor, allMethods);
            convertedMethodDescs.addAll(mds);
        }
    }

    public boolean isPrefetchDisabledFor(MethodDescriptor methodDesc) {
        return getConvertedMethodDescs().contains(methodDesc);
    }
}
