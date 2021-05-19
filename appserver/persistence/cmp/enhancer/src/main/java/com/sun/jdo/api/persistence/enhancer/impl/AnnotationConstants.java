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

package com.sun.jdo.api.persistence.enhancer.impl;

import com.sun.jdo.api.persistence.enhancer.classfile.VMConstants;

//@olsen: cosmetics
//@olsen: moved: this class -> package impl
//@olsen: subst: (object)state -> flags
//@olsen: subst: (object)reference -> stateManager
//@olsen: subst: [iI]Persistent -> [pP]ersistenceCapable
//@olsen: removed: proprietary support for HashCode


/**
 * AnnotationConstants defines a set of constants for use across the
 * filter subsystem.
 */
public interface AnnotationConstants extends VMConstants {

    /* bit mask constants to describe the types of code annotation needed
     * at specific points within methods
     */

    /* "this" needs to be fetched *///NOI18N
    public final static int FetchThis       = 0x0001;

    /* "this" needs to be dirtied *///NOI18N
    public final static int DirtyThis       = 0x0002;

    /* a java.lang.Object  needs to be fetched */
//@olsen: disabled feature / not used anymore
/*
    public final static int FetchObject     = 0x0004;
*/

    /* a java.lang.Object  needs to be dirtied */
//@olsen: disabled feature / not used anymore
/*
    public final static int DirtyObject     = 0x0008;
*/

    /* an PersistenceCapable needs to be fetched */
    public final static int FetchPersistent = 0x0010;

    /* an PersistenceCapable needs to be dirtied */
    public final static int DirtyPersistent = 0x0020;

    /* an array instance needs to be fetched */
//@olsen: disabled feature
/*
    public final static int FetchArray      = 0x0040;
*/

    /* an array instance needs to be dirtied */
//@olsen: disabled feature
/*
    public final static int DirtyArray      = 0x0080;
*/

    /* the fetch/dirty point is unconditionally reached in the method */
//@olsen: disabled feature
/*
    public final static int Unconditional   = 0x0100;
*/

    /* the fetch/dirty point is within some type of loop structure */
//@olsen: disabled feature
/*
    public final static int InLoop          = 0x0200;
*/

    /* the fetch/dirty requires a check for null */
//@olsen: disabled feature
/*
    public final static int CheckNull       = 0x0400;
*/

    /* the fetch/dirty is not mediated by the StateManager */
    //@olsen: added constant
    public final static int DFGField        = 0x0800;

    /* the dirty is mediated by the StateManager */
    //@olsen: added constant
    public final static int PKField         = 0x1000;

    /* clear stateManager and flags fields in "this" *///NOI18N
//@olsen: disabled feature
/*
    public final static int MakeThisTransient = 0x2000;
*/

    /* needs to clear jdo fields after a call of super.clone() */
    //@olsen: added constant
    public final static int SuperClone = 0x8000;

    /* a bit mask covering a field which describes the type of array
       being fetched or dirtied */
//@olsen: disabled feature
/*
    public final static int ArrayTypeMask      = 0x78000000;
*/

    /* Specific values for the array type field */
//@olsen: disabled feature
/*
    public final static int ArrayTypeBoolean   = 0x08000000;
    public final static int ArrayTypeByte      = 0x10000000;
    public final static int ArrayTypeChar      = 0x18000000;
    public final static int ArrayTypeShort     = 0x20000000;
    public final static int ArrayTypeInt       = 0x28000000;
    public final static int ArrayTypeLong      = 0x30000000;
    public final static int ArrayTypeFloat     = 0x38000000;
    public final static int ArrayTypeDouble    = 0x40000000;
    public final static int ArrayTypeObject    = 0x48000000;
*/

    /* pre-combined masks formed from the above masks */

//@olsen: disabled feature / not used anymore
/*
    public final static int FetchAny        =
    FetchThis | FetchObject | FetchPersistent | FetchArray;
    public final static int DirtyAny        =
    DirtyThis | DirtyObject | DirtyPersistent | DirtyArray;
*/
}

