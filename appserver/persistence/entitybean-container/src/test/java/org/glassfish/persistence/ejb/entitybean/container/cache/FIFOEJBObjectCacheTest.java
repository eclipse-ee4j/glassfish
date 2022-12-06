/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package org.glassfish.persistence.ejb.entitybean.container.cache;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class FIFOEJBObjectCacheTest
{
    @Test
    void unitTest_1() throws Exception {

        FIFOEJBObjectCache cache = new FIFOEJBObjectCache("UnitTestCache");
        cache.init(512, 0, 0, (float)1.0, null);

        int maxCount = 14;
        ArrayList keys = new ArrayList();
        for (int i=0; i<maxCount; i++) {
            keys.add("K_"+i);
        }

        for (int i=0; i<maxCount; i++) {
            String key = (String) keys.get(i);
            System.out.println("****  put(" + key + ", " + key + ", i" +
                               ((i%2) == 0) + ")");
            cache.put(key, key, ((i%2)==0));
        }

        System.out.println("***  Only odd numbered keys must be printed  ***");
        cache.print();
        System.out.println("************************************************");

        for (int i=0; i<maxCount; i++) {
            String key = (String) keys.get(i);
            cache.get(key, ((i % 2) != 0));
        }

        System.out.println("****  NONE SHOULD BE PRINTED ****");
        cache.print();
        System.out.println("************************************************");

        cache.put("K__15", "K__15", true);
        cache.put("K__16", "K__15", true);
        cache.get("K__16", true);   //K__16 has refCount == 2
        cache.put("K__17", "K__17");//K__17 has refCount == 0

        System.out.println("****  Only K__17 must be printed ****");
        cache.print();
        System.out.println("************************************************");

        for (int i=0; i<maxCount; i++) {
            String key = (String) keys.get(i);
            if (cache.remove(key) == null) {
                throw new RuntimeException("Remove must have returned null!!");
            }
        }

        Object k15 = cache.remove("K__15");
        Object k16_1 = cache.remove("K__16");
        Object k16_2 = cache.remove("K__16");
        Object k17 = cache.remove("K__17");

        if (k15 == null) {
            System.out.println("** FAILED for K_15");
        }

        if (k16_1 != null) {
            System.out.println("** FAILED for K_16_1");
        }

        if (k16_2 == null) {
            System.out.println("** FAILED for K_16_2");
        }

        if (k17 == null) {
            System.out.println("** FAILED for K_17");
        }

        // Now the list id completely empty, add some more items
        for (int i=0; i<maxCount; i+=2) {
            String key = (String) keys.get(i);
            cache.put(key, key, (i%4)==0);
        }
        cache.print();


        //Make the FIFO list empty
        for (int i=0; i<maxCount; i+=2) {
            String key = (String) keys.get(i);
            cache.get(key, true);
        }
        cache.print();

        // Now the FIFO list id completely empty, add some more items
        for (int i=1; i<maxCount; i+=2) {
            String key = (String) keys.get(i);
            cache.put(key, key, (i%9)==0);
        }
        cache.print();
    }
}
