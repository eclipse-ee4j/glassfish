/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.cache;

import com.sun.enterprise.security.store.AsadminSecurityUtil;
import java.io.File;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author mmares
 */
public class AdminCacheWeakReferenceTest extends AdminCacheTstBase {
    
    public AdminCacheWeakReferenceTest() {
        super(AdminCacheWeakReference.getInstance());
    }
    
    @Test
    public void testWithFileDelete() {
        if (isSkipThisTest()) {
            System.out.println(this.getClass().getName() + ".testWithFileDelete(): Must skip this unit test, because something is wrong with file cache writing during build");
        } else {
            System.out.println(this.getClass().getName() + ".testWithFileDelete()");
        }
        String floyd1 = "Wish You Were Here";
        String floyd1Key = TEST_CACHE_COTEXT + "Pink.Floyd.1";
        getCache().put(floyd1Key, floyd1);
        String holder = getCache().get(floyd1Key, String.class); //To be shure that it stay in memory
        assertEquals(floyd1, holder);
        recursiveDelete(new File(AsadminSecurityUtil.getDefaultClientDir(), TEST_CACHE_COTEXT));
        assertEquals(floyd1, getCache().get(floyd1Key, String.class));
        System.out.println(this.getClass().getName() + ".testWithFileDelete(): Done");
    }
    
}
