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
 * EntityManagerQueryMethod.java
 * $Id: EntityManagerQueryMethod.java,v 1.2 2007/05/05 05:31:16 tcfujii Exp $
 * $Date: 2007/05/05 05:31:16 $
 * $Revision: 1.2 $
 */

package com.sun.enterprise.container.common.spi.util;

public enum EntityManagerQueryMethod {

    GET_RESULT_LIST {
        public String toString() {
            return "getResultList()";
        }
    },

    GET_SINGLE_RESULT {
        public String toString() {
            return "getSingleResult()";
        }
    },

    EXECUTE_UPDATE {
        public String toString() {
            return "executeUpdate()";
        }
    },

    SET_MAX_RESULTS {
        public String toString() {
            return "setMaxResults(int maxResult)";
        }
    },

    GET_MAX_RESULTS {
        public String toString() {
            return "getMaxResults()";
        }
    },

    SET_FIRST_RESULT {
        public String toString() {
            return "setFirstResult(int startPosition)";
        }
    },

    GET_FIRST_RESULT {
        public String toString() {
            return "getFirstResult()";
        }
    },

    SET_HINT {
        public String toString() {
            return "setHint(String hintName, Object value)";
        }
    },

    GET_HINTS {
        public String toString() {
            return "getHints()";
        }
    },

    SET_PARAMETER_PARAMETER_OBJECT {
        public String toString() {
            return "setParameter(Parameter<T> param, T value)";
        }
    },

    SET_PARAMETER_PARAMETER_DATE_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(Parameter<Date> param, Date value,  TemporalType temporalType)";
        }
    },

    SET_PARAMETER_PARAMETER_CALENDAR_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(Parameter<Calendar> param, Calendar value,  TemporalType temporalType)";
        }
    },

    SET_PARAMETER_STRING_OBJECT {
        public String toString() {
            return "setParameter(String name, Object value)";
        }
    },

    SET_PARAMETER_STRING_DATE_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(String name, Date value, TemporalType temporalType)";
        }
    },

    SET_PARAMETER_STRING_CALENDAR_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(String name, Calendar value, TemporalType temporalType)";
        }
    },

    SET_PARAMETER_INT_OBJECT {
        public String toString() {
            return "setParameter(int position, Object value)";
        }
    },

    SET_PARAMETER_INT_DATE_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(int position, Date value, TemporalType temporalType)";
        }
    },

    SET_PARAMETER_INT_CALENDAR_TEMPORAL_TYPE {
        public String toString() {
            return "setParameter(int position, Calendar value, TemporalType temporalType)";
        }
    },

    GET_PARAMETERS {
        public String toString() {
            return "getParameter()";
        }
    },

    GET_PARAMETER_NAME {
        public String toString() {
            return "getParameter(String name)";
        }
    },

    GET_PARAMETER_NAME_TYPE {
        public String toString() {
            return "getParameter(String name, Class<T> type)";
        }
    },

    GET_PARAMETER_NAME_CLASS {
        public String toString() {
            return "getParameter(String name, Class<T> type)";
        }
    },

    GET_PARAMETER_POSITION {
        public String toString() {
            return "getParameter(int position)";
        }
    },

    GET_PARAMETER_POSITION_CLASS {
        public String toString() {
            return "getParameter(int position, Class<T> type)";
        }
    },

    IS_BOUND_PARAMETER {
        public String toString() {
            return "isBound(Parameter)";
        }
    },

    GET_PARAMETER_VALUE_PARAMETER {
        public String toString() {
            return "getParameterValue(Parameter)";
        }
    },

    GET_PARAMETER_VALUE_STRING {
        public String toString() {
            return "getParameterValue(String)";
        }
    },

    GET_PARAMETER_VALUE_INT {
        public String toString() {
            return "getParameterValue(int)";
        }
    },

    GET_FLUSH_MODE {
        public String toString() {
            return "getFlushMode()";
        }
    },

    SET_FLUSH_MODE {
        public String toString() {
            return "setFlushMode(FlushModeType flushMode)";
        }
    },

    SET_LOCK_MODE {
        public String toString() {
            return "setLock(LockModeType lockMode)";
        }
    },

    GET_LOCK_MODE {
        public String toString() {
            return "getLockMode()";
        }
    },

    UNWRAP {
        public String toString() {
            return "unwrap()";
        }
    }

}
