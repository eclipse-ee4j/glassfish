/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.util;

/**
 * Store the sql queries executed by applications along with the number of
 * times executed and the time stamp of the last usage.
 * Used for monitoring information.
 *
 * @author Shalini M
 */
public class SQLTrace implements Comparable {

    private String queryName;
    private int numExecutions;
    private long lastUsageTime;

    public SQLTrace(String query, int numExecutions, long time) {
        this.queryName = query;
        this.numExecutions = numExecutions;
        this.lastUsageTime = time;
    }

    /**
     * Get the value of queryName
     *
     * @return the value of queryName
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Set the value of queryName
     *
     * @param queryName new value of queryName
     */
    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    /**
     * Get the value of numExecutions
     *
     * @return the value of numExecutions
     */
    public int getNumExecutions() {
        return numExecutions;
    }

    /**
     * Set the value of numExecutions
     *
     * @param numExecutions new value of numExecutions
     */
    public void setNumExecutions(int numExecutions) {
        this.numExecutions = numExecutions;
    }

    /**
     * Get the value of lastUsageTime
     *
     * @return the value of lastUsageTime
     */
    public long getLastUsageTime() {
        return lastUsageTime;
    }

    /**
     * Set the value of lastUsageTime
     *
     * @param lastUsageTime new value of lastUsageTime
     */
    public void setLastUsageTime(long lastUsageTime) {
        this.lastUsageTime = lastUsageTime;
    }

    /**
     * Check for equality of the SQLTrace with the object passed by
     * comparing the queryName stored.
     *
     * @param obj against which the equality is to be checked.
     * @return true or false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(!(obj instanceof SQLTrace)) {
            return false;
        }
        final SQLTrace other = (SQLTrace) obj;
        if ((this.queryName == null) || (other.queryName == null) ||
                !this.queryName.equals(other.queryName)) {
            return false;
        }
        return true;
    }

    /**
     * Generate hash code for this obejct using all the fields.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.queryName != null ? this.queryName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SQLTrace)) {
            throw new ClassCastException("SqlTraceCache object is expected");
        }
        int number = ((SQLTrace) o).numExecutions;
        long t = ((SQLTrace) o).getLastUsageTime();

        int compare = 0;
        if (number == this.numExecutions) {
            compare = 0;
        } else if (number < this.numExecutions) {
            compare = -1;
        } else {
            compare = 1;
        }
        if (compare == 0) {
            //same number of executions. Hence compare based on time.
            long timeCompare = this.getLastUsageTime() - t;
            if(timeCompare == 0) {
                compare = 0;
            } else if(timeCompare < 0) {
                //compare = -1;
                compare = 1;
            } else {
                //compare = 1;
                compare = -1;
            }
        }
        return compare;
    }
}
