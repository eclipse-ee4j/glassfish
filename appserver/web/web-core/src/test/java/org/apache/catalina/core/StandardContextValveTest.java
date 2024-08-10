/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2021, 2022 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.apache.catalina.core;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.easymock.Capture;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.web.valve.GlassFishValve;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.easymock.EasyMock.captureInt;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardContextValveTest {

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private HttpServletResponse httpServletResponse;
    private final StandardContextValve standardContextValve = new StandardContextValve();


    @ParameterizedTest
    @ValueSource(strings = {"WEB-INF/web.xml", "META-INF/MANIFEST.MF"})
    public void preventAccessToInternalDirectoryWithEmptyContextRootTest(String resource) throws Exception {
        httpRequest = createNiceMock(HttpRequest.class);
        httpResponse = createNiceMock(HttpResponse.class);
        httpServletResponse = createStrictMock(HttpServletResponse.class);

        DataChunk dataChunkURL = DataChunk.newInstance();
        dataChunkURL.setString(resource);

        expect(httpRequest.getCheckRestrictedResources()).andReturn(true);
        expect(httpRequest.getRequestPathMB()).andReturn(dataChunkURL);
        expect(httpResponse.getResponse()).andReturn(httpServletResponse);

        Capture<Integer> capturedError = newCapture();
        httpServletResponse.sendError(captureInt(capturedError));
        expectLastCall().andVoid();

        replay(httpRequest, httpResponse, httpServletResponse);

        int pipelineResult = standardContextValve.invoke(httpRequest, httpResponse);
        verify(httpRequest, httpResponse, httpServletResponse);

        assertEquals(GlassFishValve.END_PIPELINE, pipelineResult);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, capturedError.getValue());
    }


    /**
     * Tests URLs after normalization
     */
    @ParameterizedTest
    @CsvSource({
        "/app/../some/../something/../my.jsp, /my.jsp",
        "/app/./some/./something/./my.jsp, /app/some/something/my.jsp",
        "./my.jsp, /my.jsp"
    })
    public void normalizeURLTest(String path, String expected) {
        String result = StandardContextValve.normalize(path);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "/app/./some/./something/./my.jsp, /app/some/something/my.jsp"
    })
    public void evaluateNormalizedPathWithSinglePointTest(String path, String expected) {
        var normalized = StandardContextValve.evaluateNormalizedPathWithSinglePoint(path);
        assertEquals(expected, normalized);
    }
}
