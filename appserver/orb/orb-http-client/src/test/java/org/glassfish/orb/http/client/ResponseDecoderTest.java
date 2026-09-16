package org.glassfish.orb.http.client;





import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every status this decoder gives meaning to.
 *
 * <p>The round-trip tests reach two of them, because those are the two a
 * working server produces. The rest are what a client sees when something has
 * gone wrong, which is when getting the distinction right matters most: a
 * caller can retry a 408 and must not retry a 403, and neither is an
 * application exception.
 */
class ResponseDecoderTest {

    private final Marshaller marshaller = new JavaSerializationMarshaller();
    private final ResponseDecoder decoder = new ResponseDecoder();

    private HttpTransport.Response response(int status, String kind, Object payload) throws IOException {
        InputStream body;
        if (payload == null) {
            body = new ByteArrayInputStream(new byte[0]);
        } else {
            ChunkedOutput out = new ChunkedOutput();
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject(payload);
                writer.writeObject(new HashMap<String, Object>());
                writer.flush();
            }
            body = new ByteArrayInputStream(out.toByteArray());
        }
        String contentType = kind == null
                ? null
                : ContentType.of(marshaller.codec(), kind).toHeaderValue();
        return new HttpTransport.Response(status, contentType, Map.of(), body);
    }

    @Test
    void okCarriesTheResult() throws Throwable {
        Object result = decoder.decodeInvocationResult(
                response(Protocol.SC_OK, ContentType.KIND_RESPONSE, "hello"), getClass().getClassLoader());
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("an accepted async invocation has no result yet, and that is not a failure")
    void acceptedAndNoContentYieldNull() throws Throwable {
        assertNull(decoder.decodeInvocationResult(
                response(Protocol.SC_ACCEPTED, null, null), getClass().getClassLoader()));
        assertNull(decoder.decodeInvocationResult(
                response(Protocol.SC_NO_CONTENT, null, null), getClass().getClassLoader()));
    }

    @Test
    void anApplicationExceptionIsRethrownAsItself() throws Exception {
        IllegalStateException thrown = new IllegalStateException("business rule");
        IllegalStateException caught = assertThrows(IllegalStateException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_EXCEPTION, ContentType.KIND_EXCEPTION, thrown),
                        getClass().getClassLoader()));
        assertEquals("business rule", caught.getMessage());
    }

    @Test
    void notFoundMeansTheBeanOrSessionIsGone() throws Exception {
        assertThrows(NoSuchEJBException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_NOT_FOUND, null, null), getClass().getClassLoader()));
    }

    @Test
    @DisplayName("403 is an access failure, not a missing bean - retrying it would be wrong")
    void forbiddenMeansNotAuthorised() throws Exception {
        assertThrows(EJBAccessException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_FORBIDDEN, null, null), getClass().getClassLoader()));
    }

    @Test
    void cancelledIsReportedAsCancellation() throws Exception {
        assertThrows(CancellationException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_CANCELLED, null, null), getClass().getClassLoader()));
    }

    @Test
    @DisplayName("a version or encoding the server refuses is a protocol failure, not a bean failure")
    void notAcceptableAndBadRequestAreProtocolFailures() throws Exception {
        assertThrows(ProtocolException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_NOT_ACCEPTABLE, null, null), getClass().getClassLoader()));
        assertThrows(ProtocolException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_BAD_REQUEST, null, null), getClass().getClassLoader()));
    }

    @Test
    void anUnexpectedStatusIsNotSilentlyTreatedAsSuccess() throws Exception {
        EJBException thrown = assertThrows(EJBException.class,
                () -> decoder.decodeInvocationResult(
                        new HttpTransport.Response(503, null, Map.of(),
                                new ByteArrayInputStream(new byte[0])),
                        getClass().getClassLoader()));
        assertTrue(thrown.getMessage().contains("503"));
    }

    @Test
    @DisplayName("the reason the server gave survives into the client exception")
    void theServersReasonIsReported() {
        HttpTransport.Response refused = new HttpTransport.Response(Protocol.SC_NOT_FOUND, null,
                Map.of("X-GF-Reason", List.of("stateful session creation is not wired to this container")),
                new ByteArrayInputStream(new byte[0]));

        Throwable thrown = assertThrows(NoSuchEJBException.class,
                () -> decoder.decodeInvocationResult(refused, getClass().getClassLoader()));
        // Without this the caller was told only "no such bean or session",
        // which describes the status code and not the failure.
        assertTrue(thrown.getMessage().contains("stateful session creation"), thrown.getMessage());
    }

    @Test
    @DisplayName("a server that explains itself is heard even when its body is unreadable")
    void aReasonIsReportedAlongsideAnUndecodableBody() {
        HttpTransport.Response corrupt = new HttpTransport.Response(Protocol.SC_EXCEPTION,
                ContentType.of(marshaller.codec(), ContentType.KIND_EXCEPTION).toHeaderValue(),
                Map.of("X-GF-Reason", List.of("bean threw during passivation")),
                new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        Throwable thrown = assertThrows(EJBException.class,
                () -> decoder.decodeInvocationResult(corrupt, getClass().getClassLoader()));
        assertTrue(thrown.getMessage().contains("passivation"), thrown.getMessage());
    }

    @Test
    @DisplayName("a silent server still yields the generic message")
    void anAbsentReasonFallsBackToTheGenericMessage() {
        HttpTransport.Response refused = new HttpTransport.Response(Protocol.SC_NOT_FOUND, null,
                Map.of(), new ByteArrayInputStream(new byte[0]));

        Throwable thrown = assertThrows(NoSuchEJBException.class,
                () -> decoder.decodeInvocationResult(refused, getClass().getClassLoader()));
        assertTrue(thrown.getMessage().contains("no such bean or session"), thrown.getMessage());
    }

    @Test
    @DisplayName("a reply from a newer protocol version is refused rather than misread")
    void anUnsupportedVersionIsRefused() {
        HttpTransport.Response future = new HttpTransport.Response(Protocol.SC_OK,
                "application/x-gf-jser-response; version=99", Map.of(),
                new ByteArrayInputStream(new byte[0]));

        assertThrows(ProtocolException.class,
                () -> decoder.decodeInvocationResult(future, getClass().getClassLoader()));
    }

    @Test
    void aBodyOfTheWrongKindIsRefused() throws Exception {
        assertThrows(ProtocolException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_OK, ContentType.KIND_VALUE, "hello"),
                        getClass().getClassLoader()));
    }

    @Test
    @DisplayName("a failure whose detail cannot be decoded is still reported as a failure")
    void anUndecodableErrorBodyDoesNotBecomeSuccess() throws Exception {
        HttpTransport.Response corrupt = new HttpTransport.Response(Protocol.SC_EXCEPTION,
                ContentType.of(marshaller.codec(), ContentType.KIND_EXCEPTION).toHeaderValue(),
                Map.of(), new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        Throwable thrown = assertThrows(EJBException.class,
                () -> decoder.decodeInvocationResult(corrupt, getClass().getClassLoader()));
        assertTrue(thrown.getMessage().contains("could not be decoded"));
    }

    @Test
    @DisplayName("a 500 carrying something that is not a Throwable is not thrown as one")
    void aNonThrowableErrorBodyIsWrapped() throws Exception {
        Throwable thrown = assertThrows(EJBException.class,
                () -> decoder.decodeInvocationResult(
                        response(Protocol.SC_EXCEPTION, ContentType.KIND_EXCEPTION, "just a string"),
                        getClass().getClassLoader()));
        assertTrue(thrown.getMessage().contains("String"));
    }

    @Test
    void theResponseIsAlwaysClosed() throws Throwable {
        boolean[] closed = { false };
        InputStream tracked = new ByteArrayInputStream(new byte[0]) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        HttpTransport.Response response =
                new HttpTransport.Response(Protocol.SC_NO_CONTENT, null, Map.of(), tracked);

        decoder.decodeInvocationResult(response, getClass().getClassLoader());

        assertTrue(closed[0], "a response left open leaks a connection out of the pool");
    }

    @Test
    void firstHeaderReadsTheFirstValueOrNull() {
        HttpTransport.Response response = new HttpTransport.Response(200, null,
                Map.of("X-Thing", List.of("a", "b")), new ByteArrayInputStream(new byte[0]));

        assertEquals("a", response.firstHeader("X-Thing"));
        assertNull(response.firstHeader("Absent"));
        assertSame(null, response.firstHeader("Absent"));
    }
}
