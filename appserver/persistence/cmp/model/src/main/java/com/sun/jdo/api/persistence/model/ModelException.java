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

/*
 * ModelException.java
 *
 * Created on February 29, 2000, 12:22 PM
 */

package com.sun.jdo.api.persistence.model;

/** 
 *
 * @author  raccah
 * @version %I%
 */
public class ModelException extends Exception
{
	/**
	 * Creates new <code>ModelException</code> without detail message.
	 */
	public ModelException ()
	{
	}


	/**
	 * Constructs an <code>ModelException</code> with the specified detail message.
	 * @param msg the detail message.
	 */
	public ModelException (String msg)
	{
		super(msg);
	}
}
