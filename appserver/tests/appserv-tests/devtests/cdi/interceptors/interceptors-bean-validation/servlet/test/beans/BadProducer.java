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

package test.beans;

import javax.enterprise.inject.Produces;


/**
 * @author <a href="mailto:phil.zampino@oracle.com">Phil Zampino</a>
 */
public class BadProducer {

    @Produces @Preferred
    public TestProduct getTestProduct() {
        System.out.println(getClass().getName() + "#getTestProducer() invoked...returning null");
        return null; // This producer is bad because it always returns null
    }

    @Produces @Preferred
    public String getPreferredString() {
        return null;
    }

}
