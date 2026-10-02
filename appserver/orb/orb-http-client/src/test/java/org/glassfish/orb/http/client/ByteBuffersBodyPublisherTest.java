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

package org.glassfish.orb.http.client;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The publisher exists to keep the marshalled body from being copied on its
 * way to the socket, and to survive being published more than once.
 */
class ByteBuffersBodyPublisherTest {

    /** Collects everything a publisher emits. */
    private static byte[] drain(HttpRequest.BodyPublisher publisher) {
        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        List<Throwable> failures = new ArrayList<>();

        publisher.subscribe(new Flow.Subscriber<ByteBuffer>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                collected.writeBytes(chunk);
            }

            @Override
            public void onError(Throwable throwable) {
                failures.add(throwable);
            }

            @Override
            public void onComplete() {
            }
        });

        if (!failures.isEmpty()) {
            throw new AssertionError("publisher failed", failures.get(0));
        }
        return collected.toByteArray();
    }

    @Test
    void contentLengthIsTheSumOfWhatRemains() {
        HttpRequest.BodyPublisher publisher = ByteBuffersBodyPublisher.of(new ByteBuffer[] {
            ByteBuffer.wrap(new byte[] { 1, 2, 3 }),
            ByteBuffer.wrap(new byte[] { 4, 5 }),
        });

        assertEquals(5, publisher.contentLength());
    }

    @Test
    void everythingIsPublishedInOrder() {
        byte[] published = drain(ByteBuffersBodyPublisher.of(new ByteBuffer[] {
            ByteBuffer.wrap(new byte[] { 1, 2, 3 }),
            ByteBuffer.wrap(new byte[0]),
            ByteBuffer.wrap(new byte[] { 4, 5 }),
        }));

        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, published);
    }

    @Test
    @DisplayName("publishing twice yields the same bytes, so a retried request is not truncated")
    void aSecondSubscriptionSeesTheWholeBodyAgain() {
        HttpRequest.BodyPublisher publisher = ByteBuffersBodyPublisher.of(new ByteBuffer[] {
            ByteBuffer.wrap(new byte[] { 7, 8, 9 }),
        });

        assertArrayEquals(new byte[] { 7, 8, 9 }, drain(publisher));
        assertArrayEquals(new byte[] { 7, 8, 9 }, drain(publisher),
                "the JDK client may republish a body when it retries or redirects");
    }

    @Test
    @DisplayName("the source buffers are not consumed - they are duplicated, not copied and not drained")
    void theSourceBuffersAreLeftAlone() {
        ByteBuffer source = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
        int positionBefore = source.position();

        drain(ByteBuffersBodyPublisher.of(new ByteBuffer[] { source }));

        assertEquals(positionBefore, source.position(),
                "consuming the caller's buffer would make the second publish emit nothing");
        assertEquals(4, source.remaining());
    }

    @Test
    void anEmptyBodyBecomesNoBody() {
        assertEquals(0, ByteBuffersBodyPublisher.of(null).contentLength());
        assertEquals(0, ByteBuffersBodyPublisher.of(new ByteBuffer[0]).contentLength());
    }

    @Test
    void aZeroLengthBufferIsStillAValidBody() {
        HttpRequest.BodyPublisher publisher =
                ByteBuffersBodyPublisher.of(new ByteBuffer[] { ByteBuffer.wrap(new byte[0]) });

        assertEquals(0, publisher.contentLength());
        assertArrayEquals(new byte[0], drain(publisher));
    }

    @Test
    void theDescriptionNamesTheChunkCountAndSize() {
        String description = ByteBuffersBodyPublisher.of(new ByteBuffer[] {
            ByteBuffer.wrap(new byte[] { 1 }), ByteBuffer.wrap(new byte[] { 2, 3 }),
        }).toString();

        assertSame(true, description.contains("2 chunks"));
        assertSame(true, description.contains("3 bytes"));
    }
}
