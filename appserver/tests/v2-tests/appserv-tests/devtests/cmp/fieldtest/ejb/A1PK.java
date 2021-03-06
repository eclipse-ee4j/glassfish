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

package fieldtest;

public class A1PK implements java.io.Serializable {

        public String id1;
        public java.util.Date iddate;

        public A1PK() {}

        public boolean equals(java.lang.Object obj) {
            if( obj==null ||
            !this.getClass().equals(obj.getClass()) ) return( false );
            A1PK o=(A1PK) obj;
            if( !this.id1.equals(o.id1) || !this.iddate.equals(o.iddate) ) return( false );
            return( true );
        }

        public int hashCode() {
            int hashCode=0;
            hashCode += id1.hashCode() + iddate.hashCode();
            return( hashCode );
        }
}
