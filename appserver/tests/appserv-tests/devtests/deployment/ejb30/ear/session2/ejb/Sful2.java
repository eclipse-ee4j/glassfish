/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import jakarta.ejb.Remote;

@Remote
public interface Sful2
{
    public String hello();

    // Associated with an @Remove method that has retainIfException=true.
    // If argument is true, the method will throw an exception, which should
    // keep the bean from being removed.  If argument is false, the bean
    // should stll be removed.
    public void removeRetainIfException(boolean throwException)
        throws Exception;

    // Associated with an @Remove method that has retainIfException=false.
    // Whether the argument is true or false, the bean should still be
    // removed.
    public void removeNotRetainIfException(boolean throwException)
        throws Exception;

    // Associated with an @Remove method that has retainIfException=true
    // and throws a system exception if param = true.
    // retainIfException only applies to application exceptions, so
    // this bean should always be removed.
    public void removeMethodThrowSysException(boolean throwException);

}
