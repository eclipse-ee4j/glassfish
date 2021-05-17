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
 * PEWebContainerStartStopOperation.java
 *
 * Created on September 24, 2003, 3:47 PM
 * This class is a no-op implementation for stop instance operation
 * HERCULES:add
 */

package com.sun.enterprise.web;

import java.util.ArrayList;

/**
 *
 * @author lwhite
 */
public class PEWebContainerStartStopOperation implements WebContainerStartStopOperation {

    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null;

    /** Creates a new instance of PEWebContainerStartStopOperation */
    public PEWebContainerStartStopOperation() {
    }

    /** Creates a new instance of PEWebContainerStartStopOperation */
    public PEWebContainerStartStopOperation(EmbeddedWebContainer embedded) {
        _embedded = embedded;
    }

    public void doPostStop(ArrayList list) {
        //deliberate no-op
    }

    public ArrayList doPreStop() {
        //deliberate no-op
        return null;
    }

    public void init(EmbeddedWebContainer embedded) {
        _embedded = embedded;
    }

}


