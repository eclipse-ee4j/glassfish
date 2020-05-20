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
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

/*
    St1 can not be both local and remote
 */
@Stateless
@Local({St1.class, St2.class})
@Remote({St1.class, St3.class})
public class NegEJB1 {
    public String st1() throws Exception {
        return "NegEJB1.st1";
    }

    public String st2() throws Exception {
        return "NegEJB1.st2";
    }

    public String st3() throws Exception {
        return "NegEJB1.st3";
    }
}
