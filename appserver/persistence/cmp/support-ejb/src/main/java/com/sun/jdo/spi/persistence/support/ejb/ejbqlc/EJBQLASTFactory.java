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
 * EJBQLASTFactory.java
 *
 * Created on November 12, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

import antlr.ASTFactory;
import antlr.collections.AST;

/**
 * Factory to create and connect EJBQLAST nodes.
 *
 * @author  Michael Bouschen
 */
public class EJBQLASTFactory
    extends ASTFactory
{
    /** The singleton EJBQLASTFactory instance. */
    private static EJBQLASTFactory factory = new EJBQLASTFactory();

    /** I18N support. */
    private final static ResourceBundle msgs =
        I18NHelper.loadBundle(EJBQLASTFactory.class);

    /**
     * Get an instance of EJBQLASTFactory.
     * @return an instance of EJBQLASTFactory
     */
    public static EJBQLASTFactory getInstance()
    {
        return factory;
    }

    /**
     * Constructor. EJBQLASTFactory is a singleton, please use
     * {@link #getInstance} to get the factory instance.
     */
    protected EJBQLASTFactory()
    {
        this.theASTNodeTypeClass = EJBQLAST.class;
        this.theASTNodeType = this.theASTNodeTypeClass.getName();
    }

    /** Overwrites superclass method to create the correct AST instance. */
    public AST create()
    {
        return new EJBQLAST();
    }

    /** Overwrites superclass method to create the correct AST instance. */
    public AST create(AST tr)
    {
        return create((EJBQLAST)tr);
    }

    /** Creates a clone of the specified EJBQLAST instance. */
    public EJBQLAST create(EJBQLAST tr)
    {
        try {
            return (tr==null) ? null : (EJBQLAST)tr.clone();
        }
        catch(CloneNotSupportedException ex) {
            throw new EJBQLException(
                I18NHelper.getMessage(msgs, "ERR_UnexpectedExceptionClone"), ex); //NOI18N
        }
    }
}

