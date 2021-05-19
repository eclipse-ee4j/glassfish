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
 * $Id: EntityManagerMethod.java,v 1.2 2007/05/05 05:31:16 tcfujii Exp $
 * $Date: 2007/05/05 05:31:16 $
 * $Revision: 1.2 $
 */

package com.sun.enterprise.container.common.spi.util;

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

    FIND_CLASS_OBJECT_MAP {
        public String toString() {
            return "find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)";
        }
    },

    FIND {
        public String toString() {
            return "find(Class<T> entityClass, Object primaryKey)";
        }
    },

    FIND_CLASS_OBJECT_LOCKMODETYPE {
        public String toString() {
            return "find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)";
        }
    },

    FIND_CLASS_OBJECT_LOCKMODETYPE_PROPERTIES {
        public String toString() {
            return "find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map properties)";
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

    LOCK_LOCKMODETYPE_MAP {
        public String toString() {
            return "Object entity, LockModeType lockMode, Map properties";
        }
    },

    REFRESH {
        public String toString() {
            return "refresh(Object entity)";
        }
    },

    REFRESH_OBJECT_PROPERTIES {
        public String toString() {
            return "refresh(Object entity, Map<String, Object> properties)";
        }
    },

    REFRESH_OBJECT_LOCKMODETYPE {
        public String toString() {
            return "refresh(Object entity, LockModeType lockMode)";
        }
    },

    REFRESH_OBJECT_LOCKMODETYPE_MAP {
        public String toString() {
            return "refresh(Object entity, LockModeType lockMode, Map properties)";
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

    GET_LOCK_MODE {
        public String toString() {
            return "getLockMode()";
        }
    },

    SET_PROPERTY {
        public String toString() {
            return "settProperties())";
        }
    },

    GET_PROPERTIES {
        public String toString() {
            return "getProperties())";
        }
    },

    CREATE_QUERY {
        public String toString() {
            return "createQuery(String qlString)";
        }
    },

    CREATE_QUERY_STRING_CLASS {
        public String toString() {
            return "createQuery(String qlString, Class<T> resultClass)";
        }
    },

    CREATE_QUERY_CRITERIA_QUERY {
        public String toString() {
            return "createQuery(CriteriaQuery criteriaQuery)";
        }
    },

    CREATE_NAMED_QUERY {
        public String toString() {
            return "createNamedQuery(String name)";
        }
    },

    CREATE_NAMED_QUERY_STRING_CLASS {
        public String toString() {
            return "createNamedQuery(String name, Class<T> resultClass)";
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
    },

    GET_ENTITY_MANAGER_FACTORY {
        public String toString() {
            return "getEntityManagerFactory()";
        }
    },

    GET_CRITERIA_BUILDER {
        public String toString() {
            return "getCriteriaBuilder()";
        }
    },

    GET_METAMODEL {
        public String toString() {
            return "getMetamodel()";
        }
    },

    DETATCH {
        public String toString() {
            return "detatch()";
        }
    },

    UNWRAP {
        public String toString() {
            return "unwrap()";
        }
    },

    CREATE_NAMED_STORED_PROCEDURE_QUERY  {
        public String toString() {
            return "createNamedStoredProcedureQuery(String name)";
        }
    },

    CREATE_STORED_PROCEDURE_QUERY  {
        public String toString() {
            return "createStoredProcedureQuery(String procedureName)";
        }
    },

    CREATE_STORED_PROCEDURE_QUERY_STRING_CLASS  {
        public String toString() {
            return "createStoredProcedureQuery(String procedureName, Class... resultClasses)";
        }
    },

    CREATE_STORED_PROCEDURE_QUERY_STRING_STRING  {
        public String toString() {
            return "createStoredProcedureQuery(String procedureName, String... resultSetMappings)";
        }
    },

    CREATE_QUERY_CRITERIA_UPDATE {
        public String toString() {
            return "createQuery(CriteriaUpdate updateQuery)";
        }
    },

    CREATE_QUERY_CRITERIA_DELETE {
        public String toString() {
            return "createQuery(CriteriaDelete deleteQuery)";
        }
    },

    IS_JOINED_TO_TRANSACTION  {
        public String toString() {
            return "isJoinedToTransaction()";
        }
    },

    CREATE_ENTITY_GRAPH_CLASS  {
        public String toString() {
            return "createEntityGraph(Class<T> rootType)";
        }
    },

    CREATE_ENTITY_GRAPH_STRING  {
        public String toString() {
            return "createEntityGraph(String graphName)";
        }
    },

    GET_ENTITY_GRAPH  {
        public String toString() {
            return "getEntityGraph(String graphName)";
        }
    },

    GET_ENTITY_GRAPHS  {
        public String toString() {
            return "getEntityGraphs(Class<T> entityClass)";
        }
    }
}
