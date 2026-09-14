package org.glassfish.orb.http.glassfish;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.orb.http.server.ServerExchange;

/**
 * One Grizzly request as a {@link ServerExchange}.
 *
 * <p>The dispatchers were written against an interface rather than against
 * Grizzly precisely so that this class could be small and so that the protocol
 * could be tested without a server. It is small.
 */
final class GrizzlyServerExchange implements ServerExchange {

    private final Request request;
    private final Response response;
    private final byte[] path;

    GrizzlyServerExchange(Request request, Response response) {
        this.request = request;
        this.response = response;
        // The undecoded request URI. PathScanner is built to work over the
        // bytes as they arrived, and Grizzly does hold them in a DataChunk;
        // taking the String and re-encoding it gives up that property for one
        // allocation per request. Worth revisiting once there is a profile
        // that says it matters - not before.
        this.path = request.getRequestURI().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String method() {
        return request.getMethod().getMethodString();
    }

    @Override
    public byte[] pathBytes() {
        return path;
    }

    @Override
    public int pathOffset() {
        return 0;
    }

    @Override
    public int pathLength() {
        return path.length;
    }

    @Override
    public String requestHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String queryParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public InputStream requestBody() {
        return request.getInputStream();
    }

    @Override
    public void setStatus(int status) {
        response.setStatus(status);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if ("Set-Cookie".equalsIgnoreCase(name)) {
            response.addHeader(name, value);
        } else {
            response.setHeader(name, value);
        }
    }

    @Override
    public void writeBody(ByteBuffer[] body) throws IOException {
        long total = 0;
        for (ByteBuffer buffer : body) {
            total += buffer.remaining();
        }
        response.setContentLengthLong(total);

        // ChunkedOutput hands out read-only views of its chunks, and a
        // read-only ByteBuffer reports hasArray() false and refuses array().
        // Grizzly's wrap has to take some other path for those, and a reply
        // shorter than the Content-Length just promised leaves the client
        // waiting for bytes that never arrive - which is what the JDK's HTTP
        // client reports as "EOF reached while reading".
        //
        // So anything that will not show its array is copied into one that
        // will. That is a copy this class was written to avoid, and it is
        // taken only for the buffers that need it.
        for (ByteBuffer buffer : body) {
            ByteBuffer writable = buffer;
            if (!buffer.hasArray()) {
                writable = ByteBuffer.allocate(buffer.remaining());
                writable.put(buffer.duplicate()).flip();
            }
            response.getNIOOutputStream().write(
                    Buffers.wrap(response.getRequest().getContext().getMemoryManager(), writable));
        }
        response.getNIOOutputStream().flush();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The credential is checked against the server's realm before the
     * identity is believed - reading a name out of the header and passing it
     * on would let any caller assert any identity.
     *
     * <p>A request with no credential is anonymous, which is what an unsecured
     * bean expects. A request whose credential fails is refused rather than
     * downgraded to anonymous: proceeding with fewer rights than were asked
     * for is still an authorization decision taken on a credential nobody
     * accepted.
     *
     * @throws SecurityException if a credential is present and does not pass
     */
    public String authenticatedUser() {
        return RealmAuthenticator.authenticate(requestHeader("Authorization"));
    }
}
