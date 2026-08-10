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

package com.sun.enterprise.v3.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.grizzly.config.GrizzlyListener;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.internal.grizzly.ContextMapper;
import org.glassfish.kernel.KernelLoggerInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies how {@link ContainerMapper} reacts to a request URI it is not able to decode.
 * <p>
 * Such a request is malformed - the client sent garbage - so it has to be answered with 400 and it
 * must not be reported as an internal server error. Anybody can trigger it by sending arbitrary
 * bytes to an HTTP listener (a TLS handshake arriving at a plain HTTP listener is the usual source),
 * so a stack trace at WARNING would let any client flood the server log.
 *
 * @author Renat R. Safiullin
 */
public class ContainerMapperTest {

    private static final Logger LOGGER = KernelLoggerInfo.getLogger();

    private final List<LogRecord> logRecords = new ArrayList<>();

    private Handler logCollector;
    private Level originalLevel;
    private ContainerMapper mapper;
    private ContextMapper contextMapper;

    @BeforeEach
    public void prepare() {
        originalLevel = LOGGER.getLevel();
        LOGGER.setLevel(Level.ALL);
        logCollector = new Handler() {

            @Override
            public void publish(LogRecord record) {
                logRecords.add(record);
            }


            @Override
            public void flush() {
                // nothing to do
            }


            @Override
            public void close() {
                // nothing to do
            }
        };
        logCollector.setLevel(Level.ALL);
        LOGGER.addHandler(logCollector);

        GrizzlyService service = createNiceMock(GrizzlyService.class);
        expect(service.obtainMapperLock()).andStubReturn(new ReentrantReadWriteLock());
        replay(service);

        contextMapper = createNiceMock(ContextMapper.class);

        mapper = new ContainerMapper(service, createNiceMock(GrizzlyListener.class));
        mapper.setMapper(contextMapper);
    }


    @AfterEach
    public void cleanup() {
        LOGGER.removeHandler(logCollector);
        LOGGER.setLevel(originalLevel);
        logRecords.clear();
    }


    /**
     * Raw bytes which are not valid in the URI encoding end up as a CharConversionException,
     * a truncated percent escape as an IndexOutOfBoundsException and a non-hexadecimal one as
     * a NumberFormatException - all of them are the client's fault, all of them must give 400.
     */
    @ParameterizedTest(name = "request URI: {0}")
    @ValueSource(strings = {"Ýõf­", "/%", "/%ZZ", "/%E0%A4%A"})
    public void undecodableRequestUriIsAnsweredWithBadRequest(String requestURI) throws Exception {
        expect(contextMapper.getHttpHandler()).andStubReturn(null);
        replay(contextMapper);

        Response response = createMock(Response.class);
        expect(response.getResponse()).andReturn(createNiceMock(HttpResponsePacket.class));
        response.sendError(400);
        expectLastCall();
        replay(response);

        mapper.service(mockRequest(requestURI), response);

        verify(response);
    }


    @Test
    public void undecodableRequestUriIsNotLoggedAsServerError() throws Exception {
        expect(contextMapper.getHttpHandler()).andStubReturn(null);
        replay(contextMapper);

        mapper.service(mockRequest("Ýõf­"), niceResponse());

        assertThat("Records logged at WARNING or higher", recordsAtLeast(Level.WARNING), empty());
        assertThat("Records logged at FINE", recordsAtLeast(Level.FINE), hasSize(1));

        LogRecord record = recordsAtLeast(Level.FINE).get(0);
        assertEquals(Level.FINE, record.getLevel());
        assertThat("Message parameters", List.of(record.getParameters()),
            contains(instanceOf(String.class), instanceOf(Throwable.class)));
    }


    /**
     * A failure of the mapped handler is a genuine server error and must keep its previous
     * behaviour - 500 and a WARNING with the exception.
     */
    @Test
    public void handlerFailureIsStillAnsweredWithInternalServerError() throws Exception {
        HttpHandler failingHandler = new HttpHandler() {

            @Override
            public void service(Request request, Response response) throws Exception {
                throw new IllegalStateException("Deployed application failed");
            }
        };
        expect(contextMapper.getHttpHandler()).andStubReturn(failingHandler);
        replay(contextMapper);

        Response response = createMock(Response.class);
        expect(response.getResponse()).andReturn(createNiceMock(HttpResponsePacket.class));
        response.sendError(500);
        expectLastCall();
        replay(response);

        mapper.service(mockRequest("/anything"), response);

        verify(response);
        List<LogRecord> warnings = recordsAtLeast(Level.WARNING);
        assertThat("Records logged at WARNING or higher", warnings, hasSize(1));
        assertThat("Logged exception", warnings.get(0).getThrown(), instanceOf(IllegalStateException.class));
    }


    private Request mockRequest(String requestURI) {
        HttpRequestPacket packet = HttpRequestPacket.builder()
            .method("GET")
            .uri(requestURI)
            .protocol(Protocol.HTTP_1_1)
            .build();

        Request request = createNiceMock(Request.class);
        request.addAfterServiceListener(anyObject());
        expectLastCall().asStub();
        expect(request.getRequest()).andStubReturn(packet);
        expect(request.getRemoteAddr()).andStubReturn("192.0.2.10");
        replay(request);
        return request;
    }


    private Response niceResponse() throws Exception {
        Response response = createNiceMock(Response.class);
        expect(response.getResponse()).andStubReturn(createNiceMock(HttpResponsePacket.class));
        replay(response);
        return response;
    }


    private List<LogRecord> recordsAtLeast(Level level) {
        return logRecords.stream().filter(record -> record.getLevel().intValue() >= level.intValue()).toList();
    }
}
