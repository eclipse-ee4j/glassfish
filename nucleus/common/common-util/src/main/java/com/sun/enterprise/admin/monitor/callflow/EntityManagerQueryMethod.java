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
 * $Id: EntityManagerQueryMethod.java,v 1.1 2006/11/08 20:55:16 harpreet Exp $
 * $Date: 2006/11/08 20:55:16 $
 * $Revision: 1.1 $
 */

package        com.sun.enterprise.admin.monitor.callflow;

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

    SET_FIRST_RESULT {
        public String toString() {
            return "setFirstResult(int startPosition)";
        }
    },

    SET_HINT {
        public String toString() {
            return "setHint(String hintName, Object value)";
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

    SET_FLUSH_MODE {
        public String toString() {
            return "setFlushMode(FlushModeType flushMode)";
        }
    }
}
