/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import org.glassfish.api.deployment.ApplicationContext;
import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class WeldApplicationContainerTest {
    @Test
    public void testAll() throws Exception {
        WeldApplicationContainer weldApplicationContainer = new WeldApplicationContainer();
        assertNull( weldApplicationContainer.getDescriptor() );
        assertTrue( weldApplicationContainer.start( null ) );
        assertTrue( weldApplicationContainer.stop( null ) );
        assertFalse( weldApplicationContainer.suspend() );
        assertFalse( weldApplicationContainer.resume() );
        assertNull( weldApplicationContainer.getClassLoader() );
    }
}
