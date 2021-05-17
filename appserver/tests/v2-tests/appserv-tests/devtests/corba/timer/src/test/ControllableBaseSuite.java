/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package corba.timer ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.ee.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.ee.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Named ;
import com.sun.corba.ee.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.ee.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;

import com.sun.corba.ee.impl.orbutil.newtimer.ControllableBase ;
import com.sun.corba.ee.impl.orbutil.newtimer.TimerFactoryImpl ;

// Test NamedBase
public class ControllableBaseSuite {
    private String name = "MyName" ;
    private int id = 26 ;
    private String description = "Another simple test" ;
    private TimerFactory factory ;
    private ControllableTest ct ;

    private static class ControllableTest extends ControllableBase {
    public ControllableTest( int id, String name, String description,
        TimerFactory factory ) {

        super( id, name, description, TimerFactoryImpl.class.cast( factory ) ) ;
    }
    }

    @Configuration( beforeTest = true )
    public void setUp() {
    factory = TimerFactoryBuilder.make( "CTF", "No description" ) ;
    ct = new ControllableTest( id, name, description, factory ) ;
    }

    @Configuration( afterTest = true )
    public void tearDown() {
    TimerFactoryBuilder.destroy( factory ) ;
    }

    @Test()
    public void testId() {
    Assert.assertEquals( id, ct.id() ) ;
    }

    @Test()
    public void testDescription() {
    Assert.assertEquals( description, ct.description() ) ;
    }

    @Test()
    public void testEnable() {
    Assert.assertFalse( ct.isEnabled() ) ;
    ct.enable() ;
    Assert.assertTrue( ct.isEnabled() ) ;
    ct.disable() ;
    Assert.assertFalse( ct.isEnabled() ) ;
    }
}
