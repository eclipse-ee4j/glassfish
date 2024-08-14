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
 * EJBQLAST.java
 *
 * Created on November 12, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * An instance of this class represents a node of the intermediate
 * representation (AST) used by the query compiler. It stores per node:
 * <ul>
 * <li> token type info
 * <li> token text
 * <li> line info
 * <li> column info
 * <li> type info the semantic analysis calculates the type of an expression
 * and adds this info to each node.
 * </ul>
 *
 * @author  Michael Bouschen
 */
public class EJBQLAST
    extends CommonAST
    implements Cloneable
{
    /** */
    private static char SEPARATOR = '\n';

    /** */
    private static String INDENT = "  "; //NOI18N

    /** The line info */
    protected int line = 0;

    /** The column info */
    protected int column = 0;

    /** The type info */
    protected transient Object typeInfo;

    /** No args constructor. */
    public EJBQLAST() {}

    /** Constructor taking token type, text and type info. */
    public EJBQLAST(int type, String text, Object typeInfo)
    {
        initialize(type, text, typeInfo);
    }

    /** Copy constructor. */
    public EJBQLAST(EJBQLAST ast)
    {
        initialize(ast);
    }

    /** */
    public void initialize(Token t)
    {
        setType(t.getType());
        setText(t.getText());
        setLine(t.getLine());
        setColumn(t.getColumn());
    }

    /** */
    public void initialize(int type, String text, Object typeInfo)
    {
        setType(type);
        setText(text);
        setTypeInfo(typeInfo);
    }

    /** */
    public void initialize(AST _ast)
    {
        EJBQLAST ast = (EJBQLAST)_ast;
        setType(ast.getType());
        setText(ast.getText());
        setLine(ast.getLine());
        setColumn(ast.getColumn());
        setTypeInfo(ast.getTypeInfo());
    }

    /** */
    public void setLine(int line)
    {
        this.line = line;
    }

    /** */
    public int getLine()
    {
        return line;
    }

    /** */
    public void setColumn(int column)
    {
        this.column = column;
    }

    /** */
    public int getColumn()
    {
        return column;
    }

    /** */
    public void setTypeInfo(Object typeInfo)
    {
        this.typeInfo = typeInfo;
    }

    /** */
    public Object getTypeInfo()
    {
        return typeInfo;
    }

    /**
     * Returns a string representation of this EJBQLAST w/o child ast nodes.
     * @return a string representation of the object.
     */
    public String toString()
    {
        Object typeInfo = getTypeInfo();
        StringBuffer repr = new StringBuffer();
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
        repr.append(typeInfo);
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
        for (EJBQLAST node = (EJBQLAST)this.getFirstChild();
             node != null;
             node = (EJBQLAST)node.getNextSibling()) {
            repr.append(node.getTreeRepr(level+1));
        }
        return repr.toString();
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
     * Creates and returns a copy of this object.
     * The returned EJBQLAST shares the same state as this object, meaning
     * the fields type, text, line, column, and typeInfo have the same values.
     * But it is not bound to any tree structure, thus the child is null
     * and the sibling is null.
     * @return a clone of this instance.
     */
    protected Object clone()
        throws CloneNotSupportedException
    {
        EJBQLAST clone = (EJBQLAST)super.clone();
        clone.setFirstChild(null);
        clone.setNextSibling(null);
        return clone;
    }

}

