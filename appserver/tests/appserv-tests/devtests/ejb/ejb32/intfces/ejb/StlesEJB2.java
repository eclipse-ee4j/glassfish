/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ejb32.intrfaces;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
/*
    StlesEJB2 exposes remote interface St7. St3 isn't business interface
 */

@Stateless
@Local
public class StlesEJB2 implements St3, St7 {
    @Override
    public String st3() throws Exception {
        return "StlesEJB2.st3";
    }

    @Override
    public String st7() throws Exception {
        return "StlesEJB2.st7";
    }
}
