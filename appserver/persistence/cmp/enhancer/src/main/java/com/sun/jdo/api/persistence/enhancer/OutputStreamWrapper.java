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

//OutputStreamWrapper - Java Source


//***************** package ***********************************************

package com.sun.jdo.api.persistence.enhancer;


//***************** import ************************************************

import java.io.OutputStream;


//#########################################################################
/**
 *  This class serves as a wrapper for an output stream of a class file. The
 *  stream is passed as a parameter to the byte code enhancer, that can
 *  sets the classname of the written Java class to the wrapper.
 *  <br>
 *  This wrapper is necessary to determine the classname outside the enhancer,
 *  after the class has been enhanced, since do do not always know the
 *  classname of an opened input stream.
 *  </p>
 */
//#########################################################################

public class OutputStreamWrapper
{


    /**
     *  The wrapped output stream.
     */
    private OutputStream out;


    /**
     *  The classname of the written Java class. This parameter
     *  is set by the enhancer.
     */
    private String className = null;


    /**********************************************************************
     *  Constructs a new object.
     *
     *  @param  out  The output stream to wrap.
     *********************************************************************/

    public OutputStreamWrapper (OutputStream out)
    {

        this.out = out;

    }  //OutputStreamWrapper.<init>


    /**********************************************************************
     *  Gets the wrapped output stream.
     *
     *  @return  The wrapped output stream.
     *
     *  @see #out
     *********************************************************************/

    public final OutputStream getStream ()
    {

        return this.out;

    }  //NamedOuptutStream.getStream()


    /**********************************************************************
     *  Gets the classname of the written Java class. This method should be
     *  called after the class has been enhanced.
     *
     *  @return  The name of the written Java class.
     *
     *  @see  #className
     *********************************************************************/

    public final String getClassName ()
    {

        return this.className;

    }  //OutputStreamWrapper.getClassName()


    /**********************************************************************
     *  Sets the name of the written Java class. This method should be called
     *  by the enhancer.
     *
     *  @param  classname  The name of the Java class.
     *
     *  @see  #className
     *********************************************************************/

    public final void setClassName (String classname)
    {

        this.className = classname;

    }  //OutputStreamWrapper.setClassName()


}  //OutputStreamWrapper


//OutputStreamWrapper - Java Source End
