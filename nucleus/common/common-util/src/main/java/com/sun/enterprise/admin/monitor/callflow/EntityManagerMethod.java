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
 * EntityManagerMethod.java
 * $Id: EntityManagerMethod.java,v 1.1 2006/11/08 20:55:16 harpreet Exp $
 * $Date: 2006/11/08 20:55:16 $
 * $Revision: 1.1 $
 */

package        com.sun.enterprise.admin.monitor.callflow;

public enum EntityManagerMethod {

    PERSIST {
        public String toString() {
            return "persist(Object entity)";
        }
    },

    MERGE {
        public String toString() {
            return "merge(<T> entity)";
        }
    },

    REMOVE {
        public String toString() {
            return "remove(Object entity)";
        }
    },

    FIND {
        public String toString() {
            return "find(Class<T> entityClass, Object primaryKey)";
        }
    },

    GET_REFERENCE {
        public String toString() {
            return "getReference(Class<T> entityClass, Object primaryKey)";
        }
    },

    FLUSH {
        public String toString() {
            return "flush()";
        }
    },

    SET_FLUSH_MODE {
        public String toString() {
            return "setFlushMode(FlushModeType flushMode)";
        }
    },

    GET_FLUSH_MODE {
        public String toString() {
            return "getFlushMode()";
        }
    },

    LOCK {
        public String toString() {
            return "lock(Object entity, LockModeType lockMode)";
        }
    },

    REFRESH {
        public String toString() {
            return "refresh(Object entity)";
        }
    },

    CLEAR {
        public String toString() {
            return "clear()";
        }
    },

    CONTAINS {
        public String toString() {
            return "contains(Object entity)";
        }
    },

    CREATE_QUERY {
        public String toString() {
            return "createQuery(String qlString)";
        }
    },

    CREATE_NAMED_QUERY {
        public String toString() {
            return "createNamedQuery(String name)";
        }
    },

    CREATE_NATIVE_QUERY_STRING {
        public String toString() {
            return "createNativeQuery(String sqlString)";
        }
    },

    CREATE_NATIVE_QUERY_STRING_CLASS {
        public String toString() {
            return "createNativeQuery(String sqlString, Class resultClass)";
        }
    },

    CREATE_NATIVE_QUERY_STRING_STRING {
        public String toString() {
            return "createNativeQuery(String sqlString, String resultSetMapping)";
        }
    },

    JOIN_TRANSACTION {
        public String toString() {
            return "joinTransaction()";
        }
    },

    GET_DELEGATE {
        public String toString() {
            return "getDelegate()";
        }
    },

    CLOSE {
        public String toString() {
            return "close()";
        }
    },

    IS_OPEN {
        public String toString() {
            return "isOpen()";
        }
    },

    GET_TRANSACTION {
        public String toString() {
            return "getTransaction()";
        }
    }

}
