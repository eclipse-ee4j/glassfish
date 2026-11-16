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

import java.util.Locale;

/**
 * A protocol content type of the form
 * {@code application/x-gf-<codec>-<kind>; version=<n>}.
 * <p>
 * Both the marshalling codec and the protocol version travel in the content
 * type, which is what allows one endpoint to serve several of each at once and
 * to negotiate: a client may probe with {@code HEAD} and a server that cannot
 * satisfy the requested version answers {@link Protocol#SC_NOT_ACCEPTABLE}.
 * Adding JBoss Marshalling later - and with it interoperability with WildFly
 * clients - is a new codec token, not a new protocol.
 */
public record ContentType(String codec, String kind, int version) {

    private static final String PREFIX = "application/x-gf-";

    /** Marshalled invocation: transaction prefix, parameters, attachments. */
    public static final String KIND_INVOCATION = "invocation";
    /** Marshalled successful result plus attachments. */
    public static final String KIND_RESPONSE = "response";
    /** Marshalled Throwable plus attachments. */
    public static final String KIND_EXCEPTION = "exception";
    /** A single marshalled value, used by the naming service. */
    public static final String KIND_VALUE = "value";

    /** Plain Java serialization. */
    public static final String CODEC_JSER = "jser";
    /** JBoss Marshalling (River). Not implemented yet; reserved for interop. */
    public static final String CODEC_JBMAR = "jbmar";

    public ContentType {
        if (codec == null || codec.isEmpty()) {
            throw new IllegalArgumentException("codec required");
        }
        if (kind == null || kind.isEmpty()) {
            throw new IllegalArgumentException("kind required");
        }
    }

    public static ContentType of(String codec, String kind) {
        return new ContentType(codec, kind, Protocol.VERSION);
    }

    /** @return e.g. {@code application/x-gf-jser-invocation; version=1} */
    public String toHeaderValue() {
        return PREFIX + codec + '-' + kind + "; version=" + version;
    }

    /**
     * Parses a content-type header value.
     *
     * @throws ProtocolException if the value is not a protocol content type
     */
    public static ContentType parse(String headerValue) throws ProtocolException {
        if (headerValue == null) {
            throw new ProtocolException("missing Content-Type");
        }
        String value = headerValue.trim();
        int semi = value.indexOf(';');
        String media = (semi < 0 ? value : value.substring(0, semi)).trim().toLowerCase(Locale.ROOT);

        if (!media.startsWith(PREFIX)) {
            throw new ProtocolException("not a GlassFish ORB/HTTP content type: " + headerValue);
        }
        String body = media.substring(PREFIX.length());
        int dash = body.indexOf('-');
        if (dash <= 0 || dash == body.length() - 1) {
            throw new ProtocolException("malformed content type: " + headerValue);
        }
        String codec = body.substring(0, dash);
        String kind = body.substring(dash + 1);

        int version = Protocol.VERSION;
        if (semi >= 0) {
            Integer parsed = findVersionParameter(value, semi + 1);
            if (parsed != null) {
                version = parsed;
            }
        }
        return new ContentType(codec, kind, version);
    }

    private static Integer findVersionParameter(String value, int from) throws ProtocolException {
        for (String param : value.substring(from).split(";")) {
            String p = param.trim();
            if (p.regionMatches(true, 0, "version=", 0, "version=".length())) {
                String raw = p.substring("version=".length()).trim();
                try {
                    return Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    throw new ProtocolException("malformed version parameter: " + raw, e);
                }
            }
        }
        return null;
    }

    public boolean isVersionSupported() {
        return version == Protocol.VERSION;
    }

    @Override
    public String toString() {
        return toHeaderValue();
    }
}
