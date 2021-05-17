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
 * WebContainerStartStopOperation.java
 * This interface provides for a pluggable
 * implementation of functionality during the
 * stopping of an instance - for PE this is a no-op
 * for EE there is a real implementation
 * HERCULES:add
 *
 * Created on September 24, 2003, 3:38 PM
 */

package com.sun.enterprise.web;

import java.util.ArrayList;

/**
 *
 * @author  lwhite
 */
public interface WebContainerStartStopOperation {

    public ArrayList doPreStop();

    public void doPostStop(ArrayList list);

    public void init(EmbeddedWebContainer embedded);
}
