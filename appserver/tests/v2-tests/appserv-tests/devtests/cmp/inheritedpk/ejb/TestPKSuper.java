/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package pkvalidation;

/*
 * TestPKSuper.java
 *
 * Created on May 16, 2003, 12:32 PM
 */

/**
 *
 * @author  Marina Vatkina
 */
public class TestPKSuper implements java.io.Serializable {
    public long id;

    /** Creates a new instance of TestPKSuper */
    public TestPKSuper() {
    }

    public boolean equals(java.lang.Object obj) {
        if( obj==null ||
            !this.getClass().equals(obj.getClass()) ) return( false );

        TestPKSuper o=(TestPKSuper) obj;
        return ( this.id == o.id );
    }

    public int hashCode() {
        int hashCode=0;
        hashCode += id;
        return( hashCode );
    }

}
