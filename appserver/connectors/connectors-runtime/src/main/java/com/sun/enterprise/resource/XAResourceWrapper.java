/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.lang.System.Logger;
import java.util.Objects;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * This is class is used for debugging. It prints out
 * trace information on TM calls to XAResource before
 * directing the call to the actual XAResource object
 *
 * @author Tony Ng
 * @author David Matějček
 */
public class XAResourceWrapper implements XAResource {

    private static final Logger LOG = System.getLogger(XAResourceWrapper.class.getName());
    private final XAResource resource;

    /**
     * Wraps the resource
     *
     * @param resource
     */
    public XAResourceWrapper(XAResource resource) {
        this.resource = Objects.requireNonNull(resource, "resource");
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOG.log(DEBUG, () -> "commit: " + xidToString(xid) + ", onePhase=" + onePhase);
        resource.commit(xid, onePhase);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        LOG.log(DEBUG, () -> "end: " + xidToString(xid) + ", flags=" + flagToString(flags));
        resource.end(xid, flags);
    }


    @Override
    public void forget(Xid xid) throws XAException {
        LOG.log(DEBUG, () -> "forget: " + xidToString(xid));
        resource.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return resource.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        if (xares instanceof XAResourceWrapper) {
            XAResourceWrapper other = (XAResourceWrapper) xares;
            return resource.isSameRM(other.resource);
        }
        return resource.isSameRM(xares);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        LOG.log(TRACE, () -> "prepare: " + xidToString(xid));
        int result = resource.prepare(xid);
        LOG.log(DEBUG, () -> "prepare: " + xidToString(xid) + ", result=" + prepareResultToString(result));
        return result;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        LOG.log(DEBUG, () -> "recover: flag=" + flagToString(flag));
        return resource.recover(flag);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        LOG.log(DEBUG, () -> "rollback: " + xidToString(xid));
        resource.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        LOG.log(DEBUG, () -> "setTransactionTimeout: " + seconds + " s");
        return resource.setTransactionTimeout(seconds);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        LOG.log(DEBUG, () -> "start: " + xidToString(xid) + ", flags=" + flagToString(flags));
        resource.start(xid, flags);
    }

    private static String xidToString(Xid xid) {
        return "XID[global/branch]=" + new String(xid.getGlobalTransactionId(), ISO_8859_1) + "/"
            + new String(xid.getBranchQualifier(), ISO_8859_1);
    }


    private static String flagToString(int flag) {
        // See start+recover javadoc!
        switch (flag) {
            case TMSTARTRSCAN:
                return "TMSTARTRSCAN";
            case TMENDRSCAN:
                return "TMENDRSCAN";
            case TMNOFLAGS:
                return "TMNOFLAGS";
            case TMJOIN:
                return "TMJOIN";
            case TMRESUME:
                return "TMRESUME";
            case TMFAIL:
                return "TMFAIL";
            case TMSUCCESS:
                return "TMSUCCESS";
            case TMSUSPEND:
                return "TMSUSPEND";
            case TMONEPHASE:
                return "TMONEPHASE";
            default:
                return "UNKNOWN[" + Integer.toHexString(flag) + "]";
        }
    }

    private static String prepareResultToString(int result) {
        // See prepare javadoc!
        switch (result) {
            case XA_RDONLY:
                return "XA_RDONLY";
            case XA_OK:
                return "XA_OK";
            default:
                return "UNKNOWN[" + result + "]";
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
            XAResource other = ((XAResourceWrapper) obj).resource;
            return resource.equals(other);
        }
        if (obj instanceof XAResource) {
            XAResource other = (XAResource) obj;
            return resource.equals(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }
}
