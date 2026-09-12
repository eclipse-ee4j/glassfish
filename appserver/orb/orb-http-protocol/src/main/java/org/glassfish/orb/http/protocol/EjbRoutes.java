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





import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Builds and parses the EJB service paths.
 *
 * <pre>
 * POST   {ctx}/ejb/v1/invoke/{app}/{module}/{distinct}/{bean}/{session}/{view}/{method}/{paramTypes}
 * POST   {ctx}/ejb/v1/open/{app}/{module}/{distinct}/{bean}
 * DELETE {ctx}/ejb/v1/cancel/{app}/{module}/{distinct}/{bean}/{invocationId}/{interrupt}
 * </pre>
 *
 * A segment that has no value is written as {@code "-"} rather than left empty,
 * so that the segment count is fixed and a path can be routed by position.
 */
public final class EjbRoutes {

    /** Placeholder for an absent optional segment. */
    public static final String ABSENT = "-";

    // Segment indices, counted from the start of the whole path.
    // 0 is the first segment of the context path.
    private static final int CTX_SEGMENTS = 1;
    public static final int IDX_SERVICE = CTX_SEGMENTS;
    public static final int IDX_VERSION = CTX_SEGMENTS + 1;
    public static final int IDX_OPERATION = CTX_SEGMENTS + 2;
    public static final int IDX_APP = CTX_SEGMENTS + 3;
    public static final int IDX_MODULE = CTX_SEGMENTS + 4;
    public static final int IDX_DISTINCT = CTX_SEGMENTS + 5;
    public static final int IDX_BEAN = CTX_SEGMENTS + 6;
    public static final int IDX_SESSION = CTX_SEGMENTS + 7;
    public static final int IDX_VIEW = CTX_SEGMENTS + 8;
    public static final int IDX_METHOD = CTX_SEGMENTS + 9;
    public static final int IDX_PARAM_TYPES = CTX_SEGMENTS + 10;

    public static final int INVOKE_SEGMENTS = IDX_PARAM_TYPES + 1;
    public static final int OPEN_SEGMENTS = IDX_BEAN + 1;

    private EjbRoutes() {
    }

    /** A parsed invoke path. */
    public record Invocation(String appName,
                             String moduleName,
                             String distinctName,
                             String beanName,
                             String sessionId,
                             String viewClass,
                             String methodName,
                             String[] paramTypes) {

        /** @return the target bean, ignoring the view and method. */
        public String beanIdentity() {
            return appName + '/' + moduleName + '/' + distinctName + '/' + beanName;
        }
    }

    public static String invokePath(String contextPath,
                                    String appName,
                                    String moduleName,
                                    String distinctName,
                                    String beanName,
                                    String sessionId,
                                    String viewClass,
                                    String methodName,
                                    String[] paramTypes) {
        StringBuilder sb = new StringBuilder(160);
        sb.append(contextPath)
          .append('/').append(Protocol.SVC_EJB)
          .append('/').append(Protocol.VERSION_SEGMENT)
          .append('/').append(Protocol.OP_INVOKE)
          .append('/').append(encode(appName))
          .append('/').append(encode(moduleName))
          .append('/').append(encode(distinctName))
          .append('/').append(encode(beanName))
          .append('/').append(encode(sessionId))
          .append('/').append(encode(viewClass))
          .append('/').append(encode(methodName))
          .append('/');
        if (paramTypes == null || paramTypes.length == 0) {
            sb.append(ABSENT);
        } else {
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(encode(paramTypes[i]));
            }
        }
        return sb.toString();
    }

    public static String openPath(String contextPath,
                                  String appName,
                                  String moduleName,
                                  String distinctName,
                                  String beanName) {
        return contextPath
                + '/' + Protocol.SVC_EJB
                + '/' + Protocol.VERSION_SEGMENT
                + '/' + Protocol.OP_OPEN
                + '/' + encode(appName)
                + '/' + encode(moduleName)
                + '/' + encode(distinctName)
                + '/' + encode(beanName);
    }

    /**
     * @param sessionId the session to end, encoded
     * @return {@code {ctx}/ejb/v1/remove/{app}/{module}/{distinct}/{bean}/{session}}
     */
    public static String removePath(String contextPath,
                                    String appName,
                                    String moduleName,
                                    String distinctName,
                                    String beanName,
                                    String sessionId) {
        return contextPath
                + '/' + Protocol.SVC_EJB
                + '/' + Protocol.VERSION_SEGMENT
                + '/' + Protocol.OP_REMOVE
                + '/' + encode(appName)
                + '/' + encode(moduleName)
                + '/' + encode(distinctName)
                + '/' + encode(beanName)
                + '/' + encode(sessionId);
    }

    public static String cancelPath(String contextPath,
                                    String appName,
                                    String moduleName,
                                    String distinctName,
                                    String beanName,
                                    String invocationId,
                                    boolean interrupt) {
        return contextPath
                + '/' + Protocol.SVC_EJB
                + '/' + Protocol.VERSION_SEGMENT
                + '/' + Protocol.OP_CANCEL
                + '/' + encode(appName)
                + '/' + encode(moduleName)
                + '/' + encode(distinctName)
                + '/' + encode(beanName)
                + '/' + encode(invocationId)
                + '/' + interrupt;
    }

    /**
     * Parses an invoke path that has already been scanned.
     *
     * @throws ProtocolException if the path is not a well-formed invoke path
     */
    public static Invocation parseInvocation(PathScanner path) throws ProtocolException {
        if (path.count() != INVOKE_SEGMENTS) {
            throw new ProtocolException("invoke path has " + path.count()
                    + " segments, expected " + INVOKE_SEGMENTS);
        }
        if (!path.segmentEquals(IDX_SERVICE, Protocol.SVC_EJB)
                || !path.segmentEquals(IDX_VERSION, Protocol.VERSION_SEGMENT)
                || !path.segmentEquals(IDX_OPERATION, Protocol.OP_INVOKE)) {
            throw new ProtocolException("not an EJB invoke path");
        }
        String rawParams = path.segment(IDX_PARAM_TYPES);
        String[] paramTypes = ABSENT.equals(rawParams) ? new String[0] : rawParams.split(",", -1);
        return new Invocation(
                path.segment(IDX_APP),
                path.segment(IDX_MODULE),
                path.optionalSegment(IDX_DISTINCT),
                path.segment(IDX_BEAN),
                path.optionalSegment(IDX_SESSION),
                path.segment(IDX_VIEW),
                path.segment(IDX_METHOD),
                paramTypes);
    }

    /** Encodes an SFSB session id for use as a path segment. */
    public static String encodeSessionId(byte[] sessionId) {
        return sessionId == null || sessionId.length == 0
                ? ABSENT
                : Base64.getUrlEncoder().withoutPadding().encodeToString(sessionId);
    }

    public static byte[] decodeSessionId(String segment) throws ProtocolException {
        if (segment == null || ABSENT.equals(segment)) {
            return null;
        }
        try {
            return Base64.getUrlDecoder().decode(segment);
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("malformed session id segment", e);
        }
    }

    static String encode(String segment) {
        if (segment == null || segment.isEmpty()) {
            return ABSENT;
        }
        boolean safe = true;
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (!isUnreserved(c)) {
                safe = false;
                break;
            }
        }
        if (safe) {
            return segment;
        }
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Characters that need no escaping inside a path segment. Beyond the
     * unreserved set of RFC 3986 this keeps {@code :} and {@code !}, both of
     * which are legal {@code pchar}s and both of which occur in every portable
     * GlassFish JNDI name - {@code java:global/app/Bean!com.acme.View}.
     * Escaping them would be correct but would make every log line unreadable.
     */
    private static boolean isUnreserved(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '.' || c == '-' || c == '_' || c == '~' || c == '$'
                || c == ':' || c == '!';
    }
}
