/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.util.List;

/**
 *
 * @author Mahesh Meswani
 */
public class XmlProbe {
    String probeName = null;
    String probeMethod = null;
    List<XmlProbeParam> probeParams = null;
    boolean hasSelf = false;
    boolean isHidden = false;
    boolean stateful = false;
    boolean statefulReturn = false;
    boolean statefulException = false;
    String profileNames = null;

    public String getProbeName() {
        return probeName;
    }

    public String getProbeMethod() {
        return probeMethod;
    }

    public List<XmlProbeParam> getProbeParams() {
        return probeParams;
    }

    public boolean hasSelf() {
        return hasSelf;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public boolean getStateful() { return stateful; }
    public boolean getStatefulReturn() { return statefulReturn; }
    public boolean getStatefulException() { return statefulException; }
    public String getProfileNames() { return profileNames; }

    public XmlProbe(String probeName, String method, List<XmlProbeParam> params, boolean hasSelf, boolean isHidden) {
        this.probeName = probeName;
        probeMethod = method;
        probeParams = params;
        this.hasSelf = hasSelf;
        this.isHidden = isHidden;
    }

    public XmlProbe(String probeName, String method, List<XmlProbeParam> params, boolean hasSelf, boolean isHidden,
            boolean isStateful, boolean statefulReturn, boolean statefulException, String profileNames) {
        this.probeName = probeName;
        probeMethod = method;
        probeParams = params;
        this.hasSelf = hasSelf;
        this.isHidden = isHidden;
        this.stateful = isStateful;
        this.statefulReturn = statefulReturn;
        this.statefulException = statefulException;
        this.profileNames = profileNames;
    }

    @Override
    public String toString() {
        final StringBuilder paramsStr = new StringBuilder("     \n");
        for (XmlProbeParam param : probeParams) {
            paramsStr.append("         , Param ").append(param.toString());
        }
        return (" Probe name = " + probeName +
                " , method = " + probeMethod + paramsStr.toString());
    }
}
