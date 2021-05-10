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
 * $Id: DDGenerator.java,v 1.3 2005/12/25 04:11:12 tcfujii Exp $
 */

package com.sun.enterprise.deployment.interfaces;

import org.glassfish.deployment.common.Descriptor;

/**
 * This interface defines the common API implemented by dd generator classes
 *
 * @author Sreenivas Munnangi
 */

public interface DDGenerator {

    void setApplicationDirectory(String applicationDirectory);

    void setDescriptor(Descriptor descriptor);

    void generate();
}
