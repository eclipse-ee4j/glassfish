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

import jakarta.ejb.*;
import jakarta.annotation.*;

/*
    SingletonBean1 exposes local interface St6. St5 isn't business interface
 */

@Remote
@Singleton
public class SingletonBean1 implements St5, St6 {

    public String st5() {
        return "SingletonBean1.st5";
    }

    public String st6() {
        return "SingletonBean1.st6";
    }
}
