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
 * Builds and parses the naming service paths.
 * <p>
 * The nine operations are not a fresh design: they are exactly the methods of
 * {@code com.sun.enterprise.naming.impl.SerialContextProvider}, the remote
 * interface GlassFish already exposes over IIOP for JNDI. Mapping them onto
 * HTTP is a change of transport, not of contract, which is why the server side
 * can delegate straight to the existing {@code TransientContext} root.
 *
 * <pre>
 * POST   {ctx}/naming/v1/lookup/{name}
 * GET    {ctx}/naming/v1/lookuplink/{name}
 * PUT    {ctx}/naming/v1/bind/{name}
 * PATCH  {ctx}/naming/v1/rebind/{name}
 * DELETE {ctx}/naming/v1/unbind/{name}
 * PATCH  {ctx}/naming/v1/rename/{name}?new={newName}
 * GET    {ctx}/naming/v1/list/{name}
 * PUT    {ctx}/naming/v1/create-subcontext/{name}
 * DELETE {ctx}/naming/v1/destroy-subcontext/{name}
 * </pre>
 *
 * {@code lookup} is a {@code POST} even though it reads: a JNDI lookup in a
 * live namespace is not idempotent from the caller's point of view and must
 * not be served from an intermediary's cache.
 */
public final class NamingRoutes {

    private static final int CTX_SEGMENTS = 1;
    public static final int IDX_SERVICE = CTX_SEGMENTS;
    public static final int IDX_VERSION = CTX_SEGMENTS + 1;
    public static final int IDX_OPERATION = CTX_SEGMENTS + 2;
    public static final int IDX_NAME = CTX_SEGMENTS + 3;

    /** Query parameter carrying the new name of a {@code rename}. */
    public static final String PARAM_NEW_NAME = "new";

    private NamingRoutes() {
    }

    /** A parsed naming request. */
    public record Request(String operation, String jndiName) {
    }

    public static String path(String contextPath, String operation, String jndiName) {
        StringBuilder sb = new StringBuilder(96);
        sb.append(contextPath)
          .append('/').append(Protocol.SVC_NAMING)
          .append('/').append(Protocol.VERSION_SEGMENT)
          .append('/').append(operation);
        if (jndiName != null && !jndiName.isEmpty()) {
            for (String part : jndiName.split("/", -1)) {
                sb.append('/').append(EjbRoutes.encode(part));
            }
        }
        return sb.toString();
    }

    /**
     * Parses a scanned naming path.
     *
     * @throws ProtocolException if the path is not a well-formed naming path
     */
    public static Request parse(PathScanner path) throws ProtocolException {
        if (path.count() < IDX_OPERATION + 1) {
            throw new ProtocolException("naming path is too short: " + path.count() + " segments");
        }
        if (!path.segmentEquals(IDX_SERVICE, Protocol.SVC_NAMING)
                || !path.segmentEquals(IDX_VERSION, Protocol.VERSION_SEGMENT)) {
            throw new ProtocolException("not a naming path");
        }
        String operation = path.segment(IDX_OPERATION);
        // An empty name is legal: it addresses the root context, as list("") does.
        String name = path.count() > IDX_NAME ? path.joinFrom(IDX_NAME) : "";
        return new Request(operation, name);
    }

    /** @return the HTTP method this operation is invoked with. */
    public static String methodFor(String operation) {
        return switch (operation) {
            case Protocol.OP_LOOKUP -> "POST";
            case Protocol.OP_LOOKUP_LINK, Protocol.OP_LIST -> "GET";
            case Protocol.OP_BIND, Protocol.OP_CREATE_SUBCONTEXT -> "PUT";
            case Protocol.OP_REBIND, Protocol.OP_RENAME -> "PATCH";
            case Protocol.OP_UNBIND, Protocol.OP_DESTROY_SUBCONTEXT -> "DELETE";
            default -> throw new IllegalArgumentException("unknown naming operation: " + operation);
        };
    }
}
