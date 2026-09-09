package org.glassfish.orb.http.client;




import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests for {@link RequestUri}.
 *
 * <p>These exist because of a bug that was found late and cheaply, and would
 * have been found earlier and more cheaply still by the first of them. The
 * paths this protocol builds are already percent-encoded, and the obvious
 * multi-argument {@code URI} constructor quotes what it is given - so a
 * segment escaped to {@code java%3Aglobal} went out as
 * {@code java%253Aglobal}, the server decoded it once, and the lookup failed
 * on a name that looked correct in every log.
 */
class RequestUriTest {

    private static final URI BASE = URI.create("http://host:8080/glassfish-services");

    @Test
    @DisplayName("an already encoded path is not encoded a second time")
    void anEncodedPathIsNotReEncoded() {
        URI uri = RequestUri.build(BASE, "/glassfish-services/naming/v1/lookup/caff%C3%A8", null);

        assertEquals("/glassfish-services/naming/v1/lookup/caff%C3%A8", uri.getRawPath());
        assertEquals("/glassfish-services/naming/v1/lookup/caffè", uri.getPath(),
                "decoding once must yield the original; twice would mean it went out double-encoded");
    }

    @Test
    @DisplayName("a percent sign survives as one percent sign")
    void percentIsNotDoubled() {
        URI uri = RequestUri.build(BASE, "/a/%2F/b", null);

        assertEquals("/a/%2F/b", uri.getRawPath());
        org.junit.jupiter.api.Assertions.assertFalse(uri.getRawPath().contains("%25"),
                "%25 is the signature of the double-encoding bug");
    }

    @Test
    void colonAndBangSurviveUnescaped() {
        // Both are legal pchar and both occur in every portable GlassFish JNDI
        // name: java:global/app/Bean!com.acme.View
        URI uri = RequestUri.build(BASE,
                "/glassfish-services/naming/v1/lookup/java:global/app/Bean!com.acme.View", null);

        assertEquals("/glassfish-services/naming/v1/lookup/java:global/app/Bean!com.acme.View",
                uri.getRawPath());
    }

    @Test
    void theQueryIsAppendedWhenPresentAndOmittedWhenNot() {
        assertEquals("new=b", RequestUri.build(BASE, "/x", "new=b").getRawQuery());
        assertEquals(null, RequestUri.build(BASE, "/x", null).getRawQuery());
        assertEquals(null, RequestUri.build(BASE, "/x", "").getRawQuery());
    }

    @Test
    void schemeHostAndPortComeFromTheBase() {
        URI uri = RequestUri.build(URI.create("https://example.test:8181/ctx"), "/ctx/a", null);

        assertEquals("https", uri.getScheme());
        assertEquals("example.test", uri.getHost());
        assertEquals(8181, uri.getPort());
    }

    @Test
    @DisplayName("a default port is not invented")
    void anAbsentPortStaysAbsent() {
        URI uri = RequestUri.build(URI.create("https://example.test/ctx"), "/ctx/a", null);

        assertEquals(-1, uri.getPort());
        assertEquals("https://example.test/ctx/a", uri.toString());
    }

    @Test
    void aPathThatCannotBeAUriIsRejectedRatherThanMangled() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestUri.build(BASE, "/a b|c", null));
    }
}
