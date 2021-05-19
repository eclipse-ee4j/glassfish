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
import com.sun.corba.ee.spi.orbutil.newtimer.NamedBase ;
import com.sun.corba.ee.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.ee.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;

// Test NamedBase
public class NamedBaseSuite {
    private String name = "MyName" ;
    private TimerFactory factory ;
    private NamedTest nb1 ;
    private NamedTest nb2 ;

    private static class NamedTest extends NamedBase {
        public NamedTest( TimerFactory factory, String name ) {
            super( factory, name ) ;
        }

        public void finish( TimerFactory factory ) {
            setFactory( factory ) ;
        }
    }

    @Configuration( beforeTest = true )
    public void setUp() {
        factory = TimerFactoryBuilder.make( "NTF", "No description" ) ;
        nb1 = new NamedTest( factory, name ) ;
        nb2 = new NamedTest( null, name ) ;
    }

    @Configuration( afterTest = true )
    public void tearDown() {
        TimerFactoryBuilder.destroy( factory ) ;
    }

    @Test()
    public void name1() {
        Assert.assertEquals( name, nb1.name() ) ;
    }

    @Test()
    public void name2() {
        Assert.assertEquals( name, nb2.name() ) ;
    }

    @Test()
    public void factory1() {
        Assert.assertEquals( factory, nb1.factory() ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } )
    public void factory2() {
        TimerFactory tf = nb2.factory() ;
    }

    @Test()
    public void equals() {
        Assert.assertEquals( nb1, nb2 ) ;
    }

    @Test()
    public void hashCode1() {
        Assert.assertEquals( nb1.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void hashCode2() {
        Assert.assertEquals( nb2.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void toString1() {
        Assert.assertEquals( factory.name() + ":" +
            name, nb1.toString() ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } )
    public void toString2() {
        String ts = nb2.toString() ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } )
    public void setFactory1() {
        nb1.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "toString2", "factory2" } )
    public void setFactory2() {
        nb2.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void factory2Post() {
        Assert.assertEquals( factory, nb2.factory() ) ;
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void toString2Post() {
        Assert.assertEquals( factory.name() + ":" +
            name, nb2.toString() ) ;
    }
}
