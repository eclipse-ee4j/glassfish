/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * PlainTextActionReporter is being used as a fake ActionReporter when one is
 * required.  It is confusing since PTAR does special things.
 * THis one does exactly what it advertises doing in its name!
 * @author Byron Nevins
 */
public class DoNothingActionReporter extends ActionReporter{

    @Override
    public void writeReport(OutputStream os) throws IOException {
    }
}
