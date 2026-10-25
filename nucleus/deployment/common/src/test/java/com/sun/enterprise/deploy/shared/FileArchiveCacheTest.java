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

package com.sun.enterprise.deploy.shared;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileArchiveCacheTest {

    @TempDir
    Path directory;

    @Test
    void cachesEachPrefixUntilClearedOrClosed() throws Exception {
        Files.createDirectory(directory.resolve("nested"));
        Files.createFile(directory.resolve("nested/file.txt"));
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Cache miss for "": calls getListOfFiles() and caches the whole-archive listing.
            assertEquals(Set.of("nested", "nested/file.txt"), entries(archive));
            // Cache hit for "": reuses the listing without calling getListOfFiles().
            entries(archive);
            // Cache miss for "nested": calls getListOfFiles() and caches this prefix separately.
            archive.entries("nested");
            // Cache hit for "nested": reuses the listing without calling getListOfFiles().
            archive.entries("nested");
            assertEquals(2, archive.scans.get());

            archive.clearCaches();
            // Cache miss after clearCaches(): calls getListOfFiles() and caches the whole-archive listing again.
            entries(archive);
            // Cache miss after clearCaches(): calls getListOfFiles() and caches the "nested" listing again.
            archive.entries("nested");
            assertEquals(4, archive.scans.get());

            archive.close();
            // Cache miss after close(): calls getListOfFiles() and caches the whole-archive listing again.
            entries(archive);
            // Cache hit for "": reuses the new listing without calling getListOfFiles().
            entries(archive);
            assertEquals(5, archive.scans.get());
        }
    }

    @Test
    void overlappingEntriesStayDirtyWhenFirstOperationFinishesFirst() throws Exception {
        // The operation that originally disabled caching must not enable it while a later operation remains open.
        checkOverlappingEntries(true);
    }

    @Test
    void overlappingEntriesStayDirtyWhenLastOperationFinishesFirst() throws Exception {
        // Finishing the latest operation must leave caching disabled for the earlier operation still in progress.
        checkOverlappingEntries(false);
    }

    private void checkOverlappingEntries(boolean closeFirstEntryFirst) throws Exception {
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Verify clean-state caching and seed an empty listing to detect stale cache reads below.
            assertCached(archive);
            // Keep two entries open so their dirty-operation lifetimes overlap, even on this single thread.
            try (WritableArchiveEntry first = archive.putNextEntry("first.txt");
                WritableArchiveEntry second = archive.putNextEntry("second.txt")) {
                // Both new files must be visible instead of the previously cached empty listing.
                assertEquals(Set.of("first.txt", "second.txt"), entries(archive));
                // Neither enumeration may reuse or populate the cache while both operations are dirty.
                assertUncached(archive);
                // Exercise both completion orders: neither the first nor the latest starter owns re-enabling.
                WritableArchiveEntry finished = closeFirstEntryFirst ? first : second;
                // Finish one operation while deliberately leaving the other dirty.
                finished.close();
                // Repeated close must not decrement the dirty count again and prematurely enable caching.
                finished.close();
                // The remaining open entry must still force a fresh scan for every enumeration.
                assertUncached(archive);
                // Explicit invalidation must not reset the count or enable caching while an entry is open.
                archive.clearCaches();
                // Verify that clearing the cache preserved the remaining operation's dirty state.
                assertUncached(archive);
                // Automatic closure finishes the remaining operation and harmlessly re-closes the finished one.
            }
            // The last completion must leave an empty, usable cache: one fresh scan, then a cache hit.
            assertCached(archive);
        }
    }

    @Test
    void nestedDeleteDuringOverwriteDoesNotEnableCache() throws Exception {
        // Force putNextEntry() to perform a nested delete rather than simply create a new file.
        Files.createFile(directory.resolve("existing.txt"));
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Seed the cache so the overwrite must invalidate an existing listing.
            assertCached(archive);
            // The nested delete finishes before putNextEntry() returns, but the output entry remains dirty.
            try (WritableArchiveEntry entry = archive.putNextEntry("existing.txt")) {
                // The nested operation must not re-enable caching while its enclosing operation is still active.
                assertUncached(archive);
            }
            // Closing the output entry must restore caching: one fresh scan followed by a cache hit.
            assertCached(archive);
            // Also verify that a standalone delete invalidates the newly populated listing.
            assertTrue(archive.deleteEntry("existing.txt"));
            // A fresh scan must report an empty archive, not the cached name of the deleted file.
            assertEquals(Set.of(), entries(archive));
        }
    }

    @Test
    void concurrentWritersKeepCacheDisabledUntilBothFinish() throws Exception {
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Seed a clean cache before either writer starts.
            assertCached(archive);
            // Use separate threads and completion gates to control the writers' overlapping lifetimes.
            ExecutorService executor = Executors.newFixedThreadPool(2);
            // Do not inspect dirty-state caching until both entries have actually been opened.
            CountDownLatch opened = new CountDownLatch(2);
            // Release each writer independently to test the intermediate state with only one writer remaining.
            CountDownLatch finishFirst = new CountDownLatch(1);
            CountDownLatch finishSecond = new CountDownLatch(1);
            try {
                Future<Void> first = executor.submit(() -> {
                    try (WritableArchiveEntry entry = archive.putNextEntry("first.txt")) {
                        // Signal that this dirty operation is active, then keep its entry open until released.
                        opened.countDown();
                        assertTrue(finishFirst.await(10, TimeUnit.SECONDS));
                        // Perform a real write before automatic closure completes the dirty operation.
                        entry.write(1);
                    }
                    return null;
                });
                Future<Void> second = executor.submit(() -> {
                    try (WritableArchiveEntry entry = archive.putNextEntry("second.txt")) {
                        // Keep this operation active even after the first writer has been allowed to finish.
                        opened.countDown();
                        assertTrue(finishSecond.await(10, TimeUnit.SECONDS));
                        // Finish the second mutation before automatic closure makes the archive clean.
                        entry.write(2);
                    }
                    return null;
                });
                // Establish the overlap deterministically rather than relying on thread scheduling or sleeps.
                assertTrue(opened.await(10, TimeUnit.SECONDS));
                // Both enumerations must call getListOfFiles() while the two writers remain active.
                assertUncached(archive);
                // Allow only the first writer to finish; wait for closure and propagate any worker failure.
                finishFirst.countDown();
                first.get(10, TimeUnit.SECONDS);
                // A single remaining writer must still prevent cache reads and fills.
                assertUncached(archive);
                // Complete the final writer before checking that caching has been restored.
                finishSecond.countDown();
                second.get(10, TimeUnit.SECONDS);
                // The first clean enumeration must scan, and the next must reuse its listing.
                assertCached(archive);
                // Verify both names using the cache just populated by assertCached().
                assertEquals(Set.of("first.txt", "second.txt"), entries(archive));
            } finally {
                // Unblock both writers even if an assertion fails, then ensure no worker outlives the test.
                finishFirst.countDown();
                finishSecond.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void concurrentClosesFinishAnOperationOnlyOnce() throws Exception {
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Leave a second entry open so double-decrementing the first entry's count becomes observable.
            try (WritableArchiveEntry first = archive.putNextEntry("first.txt");
                WritableArchiveEntry second = archive.putNextEntry("second.txt")) {
                // Have two threads attempt to close the same entry from a shared starting gate.
                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch start = new CountDownLatch(1);
                try {
                    Future<Void> firstClose = executor.submit(() -> {
                        // Wait for the common release before attempting the first close.
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        first.close();
                        return null;
                    });
                    Future<Void> secondClose = executor.submit(() -> {
                        // Deliberately close first again, not second: completion must be idempotent across threads.
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        first.close();
                        return null;
                    });
                    // Release both attempts and observe their completion, including any exceptions.
                    start.countDown();
                    firstClose.get(10, TimeUnit.SECONDS);
                    secondClose.get(10, TimeUnit.SECONDS);
                    // Only one operation may have finished; second must still keep the cache disabled.
                    assertUncached(archive);
                } finally {
                    // Release waiting tasks on failure and ensure they finish before resources are closed.
                    start.countDown();
                    executor.shutdownNow();
                    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                }
            }
            // Automatic closure of second must restore an empty cache without repeated closes corrupting the count.
            assertCached(archive);
        }
    }

    @Test
    void failedEntryCreationBalancesDirtyCount() throws Exception {
        // A regular file cannot contain another file, ensuring putNextEntry() fails after starting a dirty operation.
        Files.createFile(directory.resolve("not-a-directory"));
        try (CountingArchive archive = new CountingArchive(directory)) {
            // Establish normal caching before exercising failure cleanup.
            assertCached(archive);
            // Keep one successful operation active to check that failure cleanup preserves another operation's count.
            try (WritableArchiveEntry entry = archive.putNextEntry("open.txt")) {
                // The failed operation must release its own dirty count despite never returning an output entry.
                assertThrows(IOException.class, () -> archive.putNextEntry("not-a-directory/file.txt"));
                // Failure must not enable caching while the successful entry is still open.
                assertUncached(archive);
            }
            // If the failed operation leaked a count, closing the successful entry would not restore caching.
            assertCached(archive);
            // Repeat without another active operation to test cleanup of the sole dirty operation.
            assertThrows(IOException.class, () -> archive.putNextEntry("not-a-directory/file.txt"));
            // Failure must leave a clean, invalidated cache: one fresh scan, then a hit.
            assertCached(archive);
        }
    }

    @Test
    void siblingMutationsKeepAllAncestorsDirty() throws Exception {
        // Two sibling archives beneath an intermediate archive exercise propagation through multiple ancestor levels.
        Path middlePath = Files.createDirectory(directory.resolve("middle"));
        Path leftPath = Files.createDirectory(middlePath.resolve("left"));
        Path rightPath = Files.createDirectory(middlePath.resolve("right"));
        try (CountingArchive root = new CountingArchive(directory);
            CountingArchive middle = new CountingArchive(middlePath);
            CountingArchive left = new CountingArchive(leftPath);
            CountingArchive right = new CountingArchive(rightPath)) {
            // Establish the logical archive hierarchy explicitly; filesystem nesting alone does not set parent links.
            middle.setParentArchive(root);
            left.setParentArchive(middle);
            right.setParentArchive(middle);
            // Seed both ancestor caches before descendant mutations make those listings stale.
            assertCached(root);
            assertCached(middle);
            // Each sibling contributes a separate dirty operation to both shared ancestors.
            try (WritableArchiveEntry first = left.putNextEntry("first.txt");
                WritableArchiveEntry second = right.putNextEntry("second.txt")) {
                // Ancestor enumerations must scan and see new descendant files rather than use their old listings.
                assertTrue(entries(root).contains("middle/left/first.txt"));
                assertTrue(entries(middle).contains("right/second.txt"));
                // Repeated reads must remain uncached at both ancestor levels.
                assertUncached(root);
                assertUncached(middle);
                // Finish only the left mutation to separate local cleanliness from shared-ancestor cleanliness.
                first.close();
                // The right mutation still keeps both shared ancestors dirty.
                assertUncached(root);
                assertUncached(middle);
                // Left has no remaining local mutation, so it can scan once and then use its own cache.
                assertCached(left);
                // Right's still-open entry must continue to bypass caching.
                assertUncached(right);
            }
            // Closing the last sibling mutation must restore caching in that sibling and every shared ancestor.
            assertCached(root);
            assertCached(middle);
            assertCached(right);
        }
    }

    @Test
    void completedMutationPreventsOlderScanFromRepopulatingCache() throws Exception {
        try (CountingArchive archive = new CountingArchive(directory)) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            // Pause a reader after it scans but before it returns the listing for potential cache publication.
            try (ScanBarrier barrier = archive.pauseNextScan()) {
                // Start a cache-miss scan of the initially empty archive on a separate thread.
                Future<Set<String>> oldScan = executor.submit(() -> entries(archive));
                // Ensure the old listing has been captured before changing the filesystem.
                barrier.awaitScan();
                // Start and finish a whole mutation while that old scan is still suspended.
                try (WritableArchiveEntry entry = archive.putNextEntry("new.txt")) {
                    entry.write(1);
                }
                // The new cache must be usable even while a reader still holds the detached cache.
                assertEquals(Set.of("new.txt"), entries(archive));
                // Let the obsolete scan finish only after the fresh listing has been cached.
                barrier.close();
                // The old scan must neither overwrite nor invalidate the freshly populated cache.
                assertEquals(Set.of(), oldScan.get(10, TimeUnit.SECONDS));
                // Both reads must hit the fresh cache, not the obsolete empty listing.
                assertEquals(Set.of("new.txt"), entries(archive));
                entries(archive);
                // Only the suspended old scan and the fresh scan were needed; no repair scan should occur.
                assertEquals(2, archive.scans.get());
            } finally {
                // The barrier is released by try-with-resources on failure; ensure the reader thread also terminates.
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void scanStartedWhileDirtyIsNotCachedAfterLastWriterFinishes() throws Exception {
        // Keep an output entry open so the reader starts in dirty state rather than using a clean cache.
        try (CountingArchive archive = new CountingArchive(directory);
            WritableArchiveEntry entry = archive.putNextEntry("new.txt")) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            // Suspend the uncached scan after collecting names but before it can return them.
            try (ScanBarrier barrier = archive.pauseNextScan()) {
                Future<Set<String>> dirtyScan = executor.submit(() -> entries(archive));
                // Ensure the scan has run while the output entry is still dirty.
                barrier.awaitScan();
                // Make the archive clean before the dirty scan resumes, exercising a late-publication race.
                entry.close();
                // Allow the scan to return now that caching has been re-enabled.
                barrier.close();
                // Its caller may receive the scanned names, but they must not populate the new clean cache.
                assertEquals(Set.of("new.txt"), dirtyScan.get(10, TimeUnit.SECONDS));
                // Require a fresh scan followed by a hit; zero scans here would mean the dirty scan was cached.
                assertCached(archive);
            } finally {
                // Ensure the reader cannot outlive the test, including after a failed assertion.
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void explicitClearPreventsInFlightScanFromRepopulatingCache() throws Exception {
        try (CountingArchive archive = new CountingArchive(directory)) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            // Hold an empty listing in flight so an explicit clear must also invalidate pending cache publication.
            try (ScanBarrier barrier = archive.pauseNextScan()) {
                Future<Set<String>> oldScan = executor.submit(() -> entries(archive));
                // Wait until the old scan has captured the empty directory before making an external change.
                barrier.awaitScan();
                // Bypass FileArchive mutation tracking to exercise the explicit invalidation contract.
                Files.createFile(directory.resolve("external.txt"));
                // Replace the old cache even though no dirty operation was registered.
                archive.clearCaches();
                // A fresh scan must populate the new cache without waiting for the old reader to finish.
                assertEquals(Set.of("external.txt"), entries(archive));
                // Resume the obsolete scan after the new cache has been populated.
                barrier.close();
                // The overlapping caller may receive its old snapshot; it must not corrupt the current cache.
                assertEquals(Set.of(), oldScan.get(10, TimeUnit.SECONDS));
                // Both enumerations must hit the new cache and retain the externally added name.
                assertEquals(Set.of("external.txt"), entries(archive));
                entries(archive);
                // No additional scan should be needed to repair damage from the obsolete reader.
                assertEquals(2, archive.scans.get());
            } finally {
                // Release of the barrier is automatic on failure; also terminate the reader executor.
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    private static Set<String> entries(FileArchive archive) {
        return new HashSet<>(Collections.list(archive.entries()));
    }

    private static void assertCached(CountingArchive archive) {
        int before = archive.scans.get();
        // Call this helper with a clean, empty cache: the first enumeration must invoke getListOfFiles().
        entries(archive);
        // The second enumeration must hit the newly populated cache without invoking getListOfFiles().
        entries(archive);
        assertEquals(before + 1, archive.scans.get(), "The first clean scan should be cached");
    }

    private static void assertUncached(CountingArchive archive) {
        int before = archive.scans.get();
        // Dirty-state enumeration must invoke getListOfFiles() rather than reuse an existing cached listing.
        entries(archive);
        // The previous dirty scan must not have populated the cache: this call must scan again.
        entries(archive);
        assertEquals(before + 2, archive.scans.get(), "Every dirty enumeration should scan without using the cache");
    }

    private static final class CountingArchive extends FileArchive {
        private final AtomicInteger scans = new AtomicInteger();
        private final AtomicReference<ScanBarrier> nextScan = new AtomicReference<>();

        private CountingArchive(Path path) throws IOException {
            open(path.toUri());
        }

        private ScanBarrier pauseNextScan() {
            ScanBarrier barrier = new ScanBarrier();
            nextScan.set(barrier);
            return barrier;
        }

        @Override
        List<String> getListOfFiles(File directory, Logger logger) {
            scans.incrementAndGet();
            List<String> names = super.getListOfFiles(directory, logger);
            ScanBarrier barrier = nextScan.getAndSet(null);
            if (barrier != null) {
                barrier.scanned.countDown();
                try {
                    assertTrue(barrier.resume.await(10, TimeUnit.SECONDS), "Scan was not released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            }
            return names;
        }
    }

    private static final class ScanBarrier implements AutoCloseable {
        private final CountDownLatch scanned = new CountDownLatch(1);
        private final CountDownLatch resume = new CountDownLatch(1);

        private void awaitScan() throws InterruptedException {
            assertTrue(scanned.await(10, TimeUnit.SECONDS), "Reader did not reach the scan barrier");
        }

        @Override
        public void close() {
            resume.countDown();
        }
    }
}