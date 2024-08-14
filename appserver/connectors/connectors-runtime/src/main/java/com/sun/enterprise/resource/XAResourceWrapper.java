/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This is class is used for debugging. It prints out
 * trace information on TM calls to XAResource before
 * directing the call to the actual XAResource object
 *
 * @author Tony Ng
 *
 */
public class XAResourceWrapper implements XAResource {

    // the actual XAResource object
    private XAResource res;

    public XAResourceWrapper(XAResource res) {
        this.res = res;
    }

    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002

    private static Logger _logger ;
    static{
           _logger = LogDomains.getLogger(XAResourceWrapper.class, LogDomains.RSR_LOGGER);
          }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        print("XAResource.commit: " + xidToString(xid) + "," + onePhase);
        res.commit(xid, onePhase);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        print("XAResource.end: " + xidToString(xid) + "," +
              flagToString(flags));
        res.end(xid, flags);
    }


    @Override
    public void forget(Xid xid) throws XAException {
        print("XAResource.forget: " + xidToString(xid));
        res.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return res.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        if (xares instanceof XAResourceWrapper) {
            XAResourceWrapper other = (XAResourceWrapper) xares;
            boolean result = res.isSameRM(other.res);
            print("XAResource.isSameRM: " + res + "," + other.res + "," +
                  result);
            return result;
        } else {
            boolean result = res.isSameRM(xares);
            print("XAResource.isSameRM: " + res + "," + xares + "," +
                  result);
            return result;
            //throw new IllegalArgumentException("Has to use XAResourceWrapper for all XAResource objects: " + xares);
        }
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        print("XAResource.prepare: " + xidToString(xid));
        int result = res.prepare(xid);
        print("prepare result = " + flagToString(result));
        return result;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        print("XAResource.recover: " + flagToString(flag));
        return res.recover(flag);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        print("XAResource.rollback: " + xidToString(xid));
        res.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return res.setTransactionTimeout(seconds);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        print("XAResource.start: " + xidToString(xid) + "," +
              flagToString(flags));
        res.start(xid, flags);
    }

    private void print(String s) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,s);
        }
    }

    private static String xidToString(Xid xid) {
        return String.valueOf((new String(xid.getGlobalTransactionId()) +
                               new String(xid.getBranchQualifier())).hashCode());
    }

    private static String flagToString(int flag) {
        switch (flag) {
        case TMFAIL:
            return "TMFAIL";
        case TMJOIN:
            return "TMJOIN";
        case TMNOFLAGS:
            return "TMNOFLAGS";
        case TMONEPHASE:
            return "TMONEPHASE";
        case TMRESUME:
            return "TMRESUME";
        case TMSTARTRSCAN:
            return "TMSTARTRSCAN";
        case TMENDRSCAN:
            return "TMENDRSCAN";
        case TMSUCCESS:
            return "TMSUCCESS";
        case TMSUSPEND:
            return "TMSUSPEND";
        case XA_RDONLY:
            return "XA_RDONLY";
        default:
            return "" + Integer.toHexString(flag);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof XAResourceWrapper) {
            XAResource other = ((XAResourceWrapper) obj).res;
            return res.equals(other);
        }
        if (obj instanceof XAResource) {
            XAResource other = (XAResource) obj;
            return res.equals(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return res.hashCode();
    }
}
