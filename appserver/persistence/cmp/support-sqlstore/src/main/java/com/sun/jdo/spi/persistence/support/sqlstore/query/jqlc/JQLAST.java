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
 * JQLAST.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.jqlc;

import com.sun.jdo.spi.persistence.support.sqlstore.RetrieveDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.query.util.type.Type;

import antlr.ASTFactory;
import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * This class represents a node in the intermediate representation (AST)
 * used by the query compiler.
 * It provides
 * - line info
 * - column info
 * - type info (object of class util.type.Type): the semantic analysis calculates
 *   the type of an expression and adds this info to each node.
 * - RetrieveDesc info
 * - value: this allows to add an arbitrary value to a node.
 *   This is used in compile time calulation of constant expression.
 * @author  Michael Bouschen
 * @version 0.1
 */
public class JQLAST
    extends CommonAST
{
    /** */
    private static char SEPARATOR = '\n';

    /** */
    private static String INDENT = "  "; //NOI18N

    protected int line = 0;
    protected int column = 0;
    protected Type jqlType;
    protected RetrieveDesc rd;
    protected Object value;

    public JQLAST()
    {
    }

    public JQLAST(int type, String text, Type jqlType)
    {
        initialize(type, text, jqlType);
    }

    public JQLAST(int type, String text, Type jqlType, Object value)
    {
        initialize(type, text, jqlType, value);
    }

    public JQLAST(Token t)
    {
        initialize(t);
    }

    public JQLAST(JQLAST ast)
    {
        initialize(ast);
    }

    public void initialize(int type)
    {
        setType(type);
    }

    public void initialize(int type, String text)
    {
        setType(type);
        setText(text);
    }

    public void initialize(Token t)
    {
        setType(t.getType());
        setText(t.getText());
        setLine(t.getLine());
        setColumn(t.getColumn());
    }

    public void initialize(int type, String text, Type jqlType)
    {
        setType(type);
        setText(text);
        setJQLType(jqlType);
    }

    public void initialize(int type, String text, Type jqlType, Object value)
    {
        setType(type);
        setText(text);
        setJQLType(jqlType);
        setValue(value);
    }

    public void initialize(AST ast)
    {
        initialize((JQLAST)ast);
    }

    public void initialize(JQLAST ast)
    {
        setType(ast.getType());
        setText(ast.getText());
        setLine(ast.getLine());
        setColumn(ast.getColumn());
        setJQLType(ast.getJQLType());
        setValue(ast.getValue());
        setRetrieveDesc(ast.getRetrieveDesc());
        setValue(ast.getValue());
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getLine()
    {
        return line;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }

    public int getColumn()
    {
        return column;
    }

    public void setJQLType(Type jqlType)
    {
        this.jqlType = jqlType;
    }

    public Type getJQLType()
    {
        return jqlType;
    }

    public void setRetrieveDesc(RetrieveDesc rd)
    {
        this.rd = rd;
    }

    public RetrieveDesc getRetrieveDesc()
    {
        return rd;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public Object getValue()
    {
        return value;
    }

    /**
     * Returns a string representation of this JQLAST w/o child nodes.
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer repr = new StringBuffer();
        Object jqlType = getJQLType();
        RetrieveDesc rd = getRetrieveDesc();
        // token text
        repr.append((getText() == null ? "null" : getText())); //NOI18N
        repr.append(" ["); //NOI18N
        // token type
        repr.append(getType());
        // line/column info
        repr.append(", ("); //NOI18N
        repr.append(getLine() + "/" + getColumn()); //NOI18N
        repr.append(")"); //NOI18N
        // type info
        repr.append(", "); //NOI18N
        repr.append(jqlType);
        // RetrieveDesc info
        repr.append(", "); //NOI18N
        repr.append(getRetrieveDescRepr(rd));
        repr.append("]"); //NOI18N
        return repr.toString();
    }

    /**
     * Returns a full string representation of this JQLAST.
     * The returned string starts with the specified title string,
     * followed by the string representation of this ast,
     * followed by the string representation of the child ast nodes of this ast.
     * The method dumps each ast node on a separate line.
     * Child ast nodes are indented.
     * The method calls toString to dump a single node w/o children.
     * @return string representation of this ast including children.
     */
    public String getTreeRepr(String title)
    {
        return title + this.getTreeRepr(0);
    }

    /** Helper method for getTreeRepr. */
    private String getTreeRepr(int level)
    {
        StringBuffer repr = new StringBuffer();
        // current node
        repr.append(SEPARATOR);
        repr.append(getIndent(level));
        repr.append(this.toString());
        // handle children
        for (JQLAST node = (JQLAST)this.getFirstChild();
             node != null;
             node = (JQLAST)node.getNextSibling()) {
            repr.append(node.getTreeRepr(level+1));
        }
        return repr.toString();
    }

    /** Returns a string representation of the spceified RetrieveDesc. */
    public static String getRetrieveDescRepr(RetrieveDesc rd)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("RD:"); //NOI18N
        if (rd == null)
        {
            buf.append("null"); //NOI18N
        }
        else
        {
            String pcClassName = rd.getPersistenceCapableClass().toString();
            if (pcClassName.startsWith("class ")) //NOI18N
                buf.append(pcClassName.substring(6));
            else
                buf.append(pcClassName);
            buf.append("@"); //NOI18N
            buf.append(System.identityHashCode(rd));
        }
        return buf.toString();
    }

    /** Returns the indent specified by level. */
    private String getIndent(int level)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < level; i++) {
            buf.append(INDENT);
        }
        return buf.toString();
    }

    /**
     * Factory to create and connect JQLAST nodes.
     */
    public static class Factory
        extends ASTFactory
    {
        /** The singleton Factory instance. */
        private static Factory factory = new Factory();

        /**
         * Get an instance of Factory.
         * @return an instance of Factory
         */
        public static Factory getInstance()
        {
            return factory;
        }

        /** */
        protected Factory()
        {
            this.theASTNodeTypeClass = JQLAST.class;
            this.theASTNodeType = this.theASTNodeTypeClass.getName();
        }

        /** */
        public AST create()
        {
            return new JQLAST();
        }
    }
}

