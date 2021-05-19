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
 * ComponentType.java
 * $Id: ComponentType.java,v 1.3 2006/11/08 20:55:16 harpreet Exp $
 * $Date: 2006/11/08 20:55:16 $
 * $Revision: 1.3 $
 */

package        com.sun.enterprise.admin.monitor.callflow;

public enum ComponentType {

    SERVLET {
        public String toString() { return "SERVLET"; }
    },

    SERVLET_FILTER {
        public String toString() { return "SERVLET_FILTER"; }
    },

    SLSB {
        public String toString() { return "STATELESS_SESSION_BEAN"; }
    },

    SFSB {
        public String toString() { return "STATEFUL_SESSION_BEAN"; }
    },

    BMP        {
        public String toString() { return "BEAN_MANAGED_PERSISTENCE"; }
    },

    CMP        {
        public String toString() {
            return "CONTAINER_MANAGED_PERSISTENCE";
        }
    },

    MDB        {
        public String toString() { return "MESSAGE_DRIVEN_BEAN"; }
    },

    JPA        {
        public String toString () { return "JAVA_PERSISTENCE";}
    };

    public abstract String toString();
}
