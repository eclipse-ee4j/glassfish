/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) [2021-2022] Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.apache.catalina.core;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.easymock.Capture;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.web.valve.GlassFishValve;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
    @Test
    public void normalizeURLTest() {
        String path1 = "/app/../some/../something/../my.jsp";
        String path2 = "/app/./some/./something/./my.jsp";
        String path3 = "./my.jsp";

        String result = standardContextValve.normalize(path1);
        assertEquals("/my.jsp", result);

        result = standardContextValve.normalize(path2);
        assertEquals("/app/some/something/my.jsp", result);

        result = standardContextValve.normalize(path3);
        assertEquals("/my.jsp", result);
    }
}