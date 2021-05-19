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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.Libraries;
import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.ObjectName;
import java.util.Set;


/**
 */
public final class LibrariesTest
        extends AMXTestBase {
    public LibrariesTest() {
    }

    private final Set<String> READ_ONLY_LIBRARIES =
            GSetUtil.newUnmodifiableStringSet(
                    "MEjbApp", "__ejb_container_timer_app", "__JWSappclients");

    // see bug#6323557 "admin GUI becomes non-responsive after adding a library"
    private final Set<String> DONT_TEST_LIBRARIES =
            GSetUtil.newUnmodifiableStringSet("admingui");

    private static final String TEST_LIBS = "/foo:/bar";

    /**
     public void
     testGUIHang()
     {
     final String[]   TEST_LIBS   = new String[] { "/foo", "/bar" };
     final ObjectName    objectName  = Util.newObjectName( "amx:j2eeType=X-WebModuleConfig,name=admingui" );
     final WebModuleConfig   cfg = getProxyFactory().getProxy( objectName );
     <p/>
     final String[]  saveLibs    = cfg.getLibraries();
     assert( saveLibs != null );
     <p/>
     final String[]  testLibs    = ArrayUtil.newArray( saveLibs, TEST_LIBS );
     try
     {
     cfg.setLibraries( testLibs );
     }
     finally
     {
     cfg.setLibraries( saveLibs );
     }
     }
     */

    public void
    testLibraries() {
        final Set<Libraries> all = getTestUtil().getAllAMX(Libraries.class);

        for (final Libraries l : all) {
            final AMX amx = (AMX) l;

            if (DONT_TEST_LIBRARIES.contains(amx.getName())) {
                continue;
            }

            final ObjectName objectName = Util.getObjectName(amx);

            final String saveLibs = l.getLibraries();
            assert (saveLibs != null);

            final String testLibs = TEST_LIBS;
            try {
                l.setLibraries(testLibs);
            }
            catch (Exception e) {
                if (!READ_ONLY_LIBRARIES.contains(((AMX) l).getName())) {
                    warning("Couldn't change Libraries Attribute for " + objectName +
                            " (probably read-only, though not advertised as such)");
                }
            }
            finally {
                l.setLibraries( saveLibs );
            }

        }
    }

}


