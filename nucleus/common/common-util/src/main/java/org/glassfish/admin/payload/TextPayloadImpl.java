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

package org.glassfish.admin.payload;

import java.io.InputStream;
import java.util.Iterator;

import org.glassfish.api.admin.Payload;

/**
 * Implements the Payload API for a message containing only a single text part.
 * <p>
 * This class is mainly useful so the RemoteCommand logic can treat the return
 * payload from a command the same, regardless of whether it is actually
 * a text-only payload (containing only the command report text itself) or
 * a multi-part payload with different Parts.
 * <p>
 * This class is here primarily to make the plain text in a response look like
 * the more general multi-part responses so the RemoteCommand class is free
 * from dealing with the details of payload formatting - in particular, free
 * from knowing how to tell if the payload contains just the text report or
 * contains other parts as well.
 * <p>
 * Note that if an outbound payload contains only one Part then, currently, the
 * Payload.Outbound.Impl.writeTo method copies the contents of that Part into
 * the request or response stream rather than writing a multi-part payload that
 * contains a single part.  This is for compatibility with existing clients
 * (such as NetBeans) which expect only the text report as the return payload.
 *
 * @author tjquinn
 */
public class TextPayloadImpl {

    /**
     * requests and responses using the text payload implementation should have
     * the Content-Type set to text/*.
     */
    private static final String PAYLOAD_IMPL_CONTENT_TYPE =
            "text/";

    public static class Inbound extends PayloadImpl.Inbound {

        private final InputStream is;
        private final String contentType;

        public static Inbound newInstance(final String messageContentType, final InputStream is) {
            return new Inbound(messageContentType, is);
        }

        /**
         * Does this Inbound Payload implementation support the given content type?
         * @return true if the content type is supported
         */
        public static boolean supportsContentType(String contentType) {
            return PAYLOAD_IMPL_CONTENT_TYPE.regionMatches(true, 0,
                    contentType, 0, PAYLOAD_IMPL_CONTENT_TYPE.length());
        }

        private Inbound(final String contentType, final InputStream is) {
            this.contentType = contentType;
            this.is = is;
        }

        public Iterator<Payload.Part> parts() {
            return new Iterator<Payload.Part>() {
                private boolean hasReturnedReport = false;

                public boolean hasNext() {
                    return ! hasReturnedReport;
                }

                public Payload.Part next() {
                    hasReturnedReport = true;
                    return new PayloadImpl.Part.Streamed(contentType, "report", null, is);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }
}
