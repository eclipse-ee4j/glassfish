/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.protocol;

/**
 * Wire constants for the GlassFish "ORB over HTTP" protocol, version 1.
 * <p>
 * The shape of this protocol deliberately mirrors the WildFly HTTP Client
 * wire specification v1 (paths, methods, header semantics, affinity cookie),
 * so that a future marshalling codec can make the two interoperable. What
 * differs today is only the marshalling: WildFly uses JBoss Marshalling
 * ({@code jbmar}), this implementation uses plain Java serialization
 * ({@code jser}). The token is part of every content type precisely so both
 * can coexist on one endpoint - see {@link ContentType}.
 * <p>
 * <strong>Nothing here depends on the HTTP version.</strong> The protocol is
 * expressed purely in HTTP semantics - method, path, headers, body - with no
 * trailers, no server push, no custom bidirectional streams and no assumption
 * about delivery ordering between requests. HTTP/1.1, HTTP/2 and (later)
 * HTTP/3 are therefore a deployment choice, not a protocol choice.
 */
public final class Protocol {

    private Protocol() {
    }

    /** Default context path under which every service is mounted. */
    public static final String CONTEXT_PATH = "/glassfish-services";

    /** Protocol version carried in the {@code version} content-type parameter. */
    public static final int VERSION = 1;

    // ---- services -------------------------------------------------------

    public static final String SVC_EJB = "ejb";
    public static final String SVC_NAMING = "naming";
    public static final String SVC_TXN = "txn";
    public static final String SVC_COMMON = "common";

    public static final String VERSION_SEGMENT = "v1";

    // ---- ejb operations -------------------------------------------------

    /**
     * {@code POST .../ejb/v1/invoke/{app}/{module}/{distinct}/{bean}/{session}/{view}/{method}/{paramTypes}}
     */
    public static final String OP_INVOKE = "invoke";

    /** {@code POST .../ejb/v1/open/{app}/{module}/{distinct}/{bean}} - creates an SFSB session. */
    public static final String OP_OPEN = "open";

    /** {@code DELETE .../ejb/v1/cancel/{app}/{module}/{distinct}/{bean}/{invocationId}/{interrupt}} */
    public static final String OP_CANCEL = "cancel";

    /**
     * {@code DELETE .../ejb/v1/remove/{app}/{module}/{distinct}/{bean}/{session}}
     * <p>
     * {@code EJBObject.remove()} from the EJB 2.x component view. It is not an
     * ordinary invocation because it is not a business method: it ends the
     * conversation, and the container - not the bean - decides what that means.
     */
    public static final String OP_REMOVE = "remove";

    // ---- naming operations ----------------------------------------------
    // These map one-to-one onto com.sun.enterprise.naming.impl.SerialContextProvider.

    public static final String OP_LOOKUP = "lookup";
    public static final String OP_LOOKUP_LINK = "lookuplink";
    public static final String OP_BIND = "bind";
    public static final String OP_REBIND = "rebind";
    public static final String OP_UNBIND = "unbind";
    public static final String OP_RENAME = "rename";
    public static final String OP_LIST = "list";
    public static final String OP_CREATE_SUBCONTEXT = "create-subcontext";
    public static final String OP_DESTROY_SUBCONTEXT = "destroy-subcontext";

    // ---- common ----------------------------------------------------------

    /** {@code GET .../common/v1/affinity} - mints the affinity cookie. */
    public static final String OP_AFFINITY = "affinity";

    // ---- headers ---------------------------------------------------------

    /** Session id of a newly opened stateful bean, returned by {@link #OP_OPEN}. */
    public static final String H_SESSION_ID = "x-gf-ejb-session-id";

    /** Client-minted id, lets {@link #OP_CANCEL} name an in-flight invocation. */
    public static final String H_INVOCATION_ID = "x-gf-invocation-id";

    /** Marshalled transaction id. */
    public static final String H_TXN_ID = "x-gf-txn-id";

    /**
     * The caller's remaining transaction timeout in seconds, sent with an
     * invocation that carries a transaction. Without it the server would
     * import the branch under its own default timeout, which can outlive the
     * coordinator's and leave a branch held after the caller has given up.
     */
    public static final String H_TXN_TIMEOUT = "x-gf-txn-timeout";

    /**
     * Session affinity cookie. Deliberately {@code JSESSIONID}: it makes every
     * off-the-shelf HTTP load balancer able to pin a stateful conversation
     * without knowing anything about this protocol - which is what replaces
     * the IIOP FOLB machinery (RoundRobinPolicy, IiopFolbGmsClient).
     */
    public static final String AFFINITY_COOKIE = "JSESSIONID";

    // ---- status codes with protocol meaning ------------------------------

    public static final int SC_OK = 200;
    public static final int SC_ACCEPTED = 202;          // async invocation, no result yet
    public static final int SC_NO_CONTENT = 204;        // open succeeded, id is in the header
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_CANCELLED = 408;
    public static final int SC_NOT_ACCEPTABLE = 406;    // unsupported version or encoding
    public static final int SC_EXCEPTION = 500;         // application exception in the body

    // ---- HTTP/2 tuning ----------------------------------------------------

    /**
     * Grizzly defaults the HTTP/2 initial window to {@code 64 * 1024 - 1}
     * (see {@code org.glassfish.grizzly.config.dom.Http#INITIAL_WINDOW_SIZE_IN_BYTES}).
     * That is sized for web pages, not for a marshalled EJB result set: a
     * multi-megabyte response is then delivered in 64 KB instalments, each
     * costing a WINDOW_UPDATE round trip, which makes HTTP/2 measurably
     * <em>slower</em> than HTTP/1.1 on a low-latency LAN. A listener dedicated
     * to this protocol should raise it.
     */
    public static final int RECOMMENDED_H2_INITIAL_WINDOW_SIZE = 4 * 1024 * 1024;

    /**
     * Grizzly defaults to 100 concurrent streams. That is the ceiling on
     * concurrent invocations per connection - IIOP/GIOP has no such limit,
     * so the default is a regression for a busy client.
     */
    public static final int RECOMMENDED_H2_MAX_CONCURRENT_STREAMS = 512;
}
