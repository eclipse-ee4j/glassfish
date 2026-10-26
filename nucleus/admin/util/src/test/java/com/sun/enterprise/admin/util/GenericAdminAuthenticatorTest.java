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

package com.sun.enterprise.admin.util;

import org.glassfish.grizzly.http.server.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.sun.enterprise.admin.util.AdminConstants.GLASSFISH_REMOTE_HOST_HEADER;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link GenericAdminAuthenticator#isLocalAdminConsoleRequest(Request)}, which decides
 * whether a request comes from the Admin Console. The Admin Console is trusted to report the real
 * remote host of its user and its REST session token follows the admin session timeout, so other
 * clients must not be able to pass for it.
 */
public class GenericAdminAuthenticatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "::1", "localhost"})
    public void localRequestWithHeaderIsFromAdminConsole(String localAddress) {
        Request request = mockRequest("some-client.example.com", localAddress);
        assertTrue(GenericAdminAuthenticator.isLocalAdminConsoleRequest(request));
        verify(request);
    }

    @Test
    public void remoteRequestWithHeaderIsNotFromAdminConsole() {
        // The Admin Console always runs co-located with the server, so a remote client
        // can't become trusted just by sending the header.
        Request request = mockRequest("some-client.example.com", "203.0.113.42");
        assertFalse(GenericAdminAuthenticator.isLocalAdminConsoleRequest(request));
        verify(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void blankHeaderIsNotFromAdminConsole(String header) {
        Request request = mockRequestWithHeaderOnly(header);
        assertFalse(GenericAdminAuthenticator.isLocalAdminConsoleRequest(request));
        verify(request);
    }

    @Test
    public void missingHeaderIsNotFromAdminConsole() {
        // The mock fails the test if the remote address is resolved anyway, because
        // that check enumerates the local network interfaces.
        Request request = mockRequestWithHeaderOnly(null);
        assertFalse(GenericAdminAuthenticator.isLocalAdminConsoleRequest(request));
        verify(request);
    }

    private static Request mockRequest(String header, String remoteAddress) {
        Request request = createMock(Request.class);
        expect(request.getHeader(GLASSFISH_REMOTE_HOST_HEADER)).andReturn(header);
        expect(request.getRemoteAddr()).andReturn(remoteAddress);
        replay(request);
        return request;
    }

    /**
     * @return a request which fails the test if anything but the header is read from it.
     */
    private static Request mockRequestWithHeaderOnly(String header) {
        Request request = createMock(Request.class);
        expect(request.getHeader(GLASSFISH_REMOTE_HOST_HEADER)).andReturn(header);
        replay(request);
        return request;
    }
}
