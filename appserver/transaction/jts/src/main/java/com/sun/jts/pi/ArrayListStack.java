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
 * ArrayListStack.java
 * Author: darpan.dinker@sun.com
 * Created on June 21, 2002, 3:42 PM
 *
 */

package com.sun.jts.pi;

import java.util.ArrayList;

/**
 * The <code>ArrayListStack</code> class represents a last-in-first-out
 * (LIFO) stack of objects. It encapsulates class <tt>ArrayList</tt> with four
 * operations that allow a list to be treated as a stack. The usual
 * <tt>push</tt> and <tt>pop</tt> operations are provided, as well as a
 * method to <tt>peek</tt> at the top item on the stack, and a method to test
 * for whether the stack is <tt>empty</tt>
 * <p>
 * When a stack is first created, it contains no items.
 * @author  Darpan Dinker, $Author: tcfujii $
 * @version $Revision: 1.3 $ on $Date: 2005/12/25 04:12:09 $
 */
public class ArrayListStack {
    private int curIndex;
    private ArrayList list;

    /** Creates a stack with the given initial size */
    public ArrayListStack(int size) {
        curIndex = 0;
        list = new ArrayList(size);
    }

    /** Creates a stack with a default size */
    public ArrayListStack() {
        this(20);
    }

    /**
     * Provides the current size of the stack.
     * @return int return the current size.
     */
    public int size() {
        return curIndex;
    }

    /**
     * Pushes an item onto the top of this stack. This method will internally
     * add elements to the <tt>ArrayList</tt> if the stack is full.
     * @param   obj   the object to be pushed onto this stack.
     * @see     java.util.ArrayList#add
     */
    public void push(Object obj) {
        list.add(curIndex, obj);
        curIndex += 1;
    }

    /**
     * Removes the object at the top of this stack and returns that
     * object as the value of this function.
     * @return     The object at the top of this stack (the last item
     *             of the <tt>ArrayList</tt> object). Null if stack is empty.
     */
    public Object pop() {
        if (curIndex > 0) {
            curIndex -= 1;
            return list.remove(curIndex);
        }
        return null;
    }

    /**
     * Tests if this stack is empty.
     * @return  <code>true</code> if and only if this stack contains
     *          no items; <code>false</code> otherwise.
     */
    public boolean empty() {
        return curIndex == 0;
    }

    /**
     * Looks at the object at the top of this stack without removing it
     * from the stack.
     * @return     the object at the top of this stack (the last item
     *             of the <tt>ArrayList</tt> object).  Null if stack is empty.
     */
    public Object peek() {
        Object top = null;
        if (curIndex > 0) {
            top = list.get(curIndex - 1);
        }
        return top;
    }
}
