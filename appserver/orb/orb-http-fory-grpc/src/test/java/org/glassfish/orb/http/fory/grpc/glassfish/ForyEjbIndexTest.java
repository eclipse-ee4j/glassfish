/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package org.glassfish.orb.http.fory.grpc.glassfish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.orb.http.fory.grpc.ForyGeneratedServiceRegistry;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What is published for the deployed views, and when it is rebuilt. The
 * deployed applications are a list here; the GlassFish registry they are read
 * from needs a running server.
 */
class ForyEjbIndexTest {

    public interface Calc {
        int twice(int value);

        void ping();

        String hello(String name);
    }

    public interface Greeter {
        String greet(String name);
    }

    /** Declares the same message and rpc twice once the first letter is upper-cased. */
    public interface CaseClash {
        String foo(String value);

        String Foo(String value);
    }

    public interface Overloaded {
        String say(String value);

        String say(int value);
    }

    public interface C {
        String run(String value);
    }

    public interface B_C {
        String run(String value);
    }

    public interface WithAHelper {
        String run(String value);

        static String help(String value) {
            return value;
        }
    }

    public interface Empty {
        String run(String value);
    }

    public interface RunRequest {
        String run(String value);
    }

    public interface One {
        interface Api {
            String call(String value);
        }
    }

    public interface Two {
        interface Api {
            String call(String value);
        }
    }

    private static final ClassLoader LOADER = ForyEjbIndexTest.class.getClassLoader();

    private final List<String> warnings = new CopyOnWriteArrayList<>();

    private final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    private final Logger logger = Logger.getLogger(ForyEjbIndex.class.getName());

    @BeforeEach
    void listenForWarnings() {
        logger.addHandler(handler);
    }

    @AfterEach
    void stopListening() {
        logger.removeHandler(handler);
    }

    static ForyEjbIndex.DeployedView view(String app, String module, String bean, Class<?> view) {
        return new ForyEjbIndex.DeployedView(app, module, bean, view.getName(), LOADER, false);
    }

    private static String wire(String app, String module, String bean, Class<?> view, String rpc) {
        return Protocol.CONTEXT_PATH + "/fory/" + ForyEjbIndex.serviceName(app, module, bean, view) + '/' + rpc;
    }

    private static ForyEjbIndex index(List<ForyEjbIndex.DeployedView> views) {
        ForyEjbIndex index = new ForyEjbIndex(() -> views, System::nanoTime);
        index.refresh();
        return index;
    }

    @Test
    void publishesEveryMethodOfAViewAndItsDocument() {
        ForyEjbIndex index = index(List.of(view("app", "module", "CalcBean", Calc.class)));

        for (String rpc : new String[] {"Twice", "Ping", "Hello"}) {
            ForyGeneratedServiceRegistry.Binding binding = index.foryRegistry()
                    .lookup(wire("app", "module", "CalcBean", Calc.class, rpc));
            assertNotNull(binding, rpc);
            assertEquals(new ForyEjbIndex.ForyRoute("app", "module", "CalcBean", Calc.class.getName()),
                    binding.attachment());
        }
        assertEquals(3, index.foryRegistry().size());
        assertEquals(1, index.catalog().size());
    }

    @Test
    void aViewWithAMethodItCannotPublishPublishesNothing() {
        // foo and Foo pass a check on the raw names, and used to register one
        // of them and then fail on the other.
        ForyEjbIndex index = index(List.of(
                view("app", "module", "ClashBean", CaseClash.class),
                view("app", "module", "OverloadBean", Overloaded.class),
                view("app", "module", "GreeterBean", Greeter.class)));

        assertEquals(1, index.foryRegistry().size(), "only the greeter's one method");
        assertNotNull(index.foryRegistry().lookup(wire("app", "module", "GreeterBean", Greeter.class, "Greet")));
        assertEquals(1, index.catalog().size());
        assertEquals(2, warnings.size(), warnings.toString());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("ClashBean")), warnings.toString());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("OverloadBean")), warnings.toString());
    }

    @Test
    void namesThatAreNotJavaNamesArePublishedUnderTheirEscapedNames() {
        // A leading digit and a keyword are escaped, so that the application
        // 2fa-app and a module named default are served like any other.
        ForyEjbIndex index = index(List.of(
                view("2fa-app", "module", "GreeterBean", Greeter.class),
                view("app", "default", "GreeterBean", Greeter.class),
                view("app", "module", "GreeterBean", Greeter.class)));

        assertEquals(3, index.foryRegistry().size());
        assertEquals(3, index.catalog().size());
        assertEquals(List.of(), warnings);
        assertNotNull(index.foryRegistry().lookup(wire("2fa-app", "module", "GreeterBean", Greeter.class, "Greet")));
        assertNotNull(index.foryRegistry().lookup(wire("app", "default", "GreeterBean", Greeter.class, "Greet")));
    }

    @Test
    void theEscapedNameOfAnythingIsAJavaName() {
        for (String name : new String[] {"2fa-app", "default", "class", "true", "9", "-", "a.b", "\u00e8", "_"}) {
            String escaped = ForyEjbIndex.escape(name);
            assertTrue(javax.lang.model.SourceVersion.isIdentifier(escaped)
                    && !javax.lang.model.SourceVersion.isKeyword(escaped), name + " -> " + escaped);
        }
        assertNotEquals(ForyEjbIndex.escape("2"), ForyEjbIndex.escape("_32"));
        assertNotEquals(ForyEjbIndex.escape("default"), ForyEjbIndex.escape("_64efault"));
    }

    @Test
    void aViewThatCannotBeLoadedPublishesNothingAndLogsAWarning() {
        ForyEjbIndex index = index(List.of(
                new ForyEjbIndex.DeployedView("app", "module", "Gone", "no.such.View", LOADER, false),
                view("app", "module", "GreeterBean", Greeter.class)));

        assertEquals(1, index.foryRegistry().size());
        assertEquals(1, warnings.size(), warnings.toString());
    }

    @Test
    void anErrorWhileLoadingAViewDoesNotStopTheOthers() {
        ClassLoader broken = new ClassLoader(LOADER) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(Calc.class.getName())) {
                    throw new NoClassDefFoundError("a class the view needs is gone");
                }
                return super.loadClass(name);
            }
        };

        ForyEjbIndex index = index(List.of(
                new ForyEjbIndex.DeployedView("app", "module", "CalcBean", Calc.class.getName(), broken, false),
                view("app", "module", "GreeterBean", Greeter.class)));

        assertEquals(1, index.foryRegistry().size());
        assertNotNull(index.foryRegistry().lookup(wire("app", "module", "GreeterBean", Greeter.class, "Greet")));
        assertEquals(1, warnings.size(), warnings.toString());
    }

    @Test
    void doesNotPublishAStatefulBean() {
        ForyEjbIndex index = index(List.of(
                new ForyEjbIndex.DeployedView("app", "module", "Cart", Greeter.class.getName(), LOADER, true)));

        assertEquals(0, index.foryRegistry().size());
        assertEquals(0, index.catalog().size());
        assertEquals(List.of(), warnings);
    }

    @Test
    void differentNamesNeverBecomeTheSameService() {
        assertNotEquals(ForyEjbIndex.serviceName("a-b", "m", "B", Greeter.class),
                ForyEjbIndex.serviceName("a_b", "m", "B", Greeter.class));
        assertNotEquals(ForyEjbIndex.serviceName("a", "m", "A_B", C.class),
                ForyEjbIndex.serviceName("a", "m", "A", B_C.class));
        assertNotEquals(ForyEjbIndex.serviceName("a.b", "m", "B", Greeter.class),
                ForyEjbIndex.serviceName("a_2Eb", "m", "B", Greeter.class));
        assertNotEquals(ForyEjbIndex.escape("a-b"), ForyEjbIndex.escape("a_2Db"));
        assertNotEquals(ForyEjbIndex.escape("\u00e8"), ForyEjbIndex.escape("_E8"));
        assertEquals("my_2Dapp", ForyEjbIndex.escape("my-app"));
    }

    @Test
    void twoViewsOfTheSameBeanAreTwoServices() {
        // Both declare PingRequest and PingResponse; in one package they would
        // be the same two messages with the same type ids.
        ForyEjbIndex index = index(List.of(
                view("app", "module", "Bean", Calc.class),
                view("app", "module", "Bean", Greeter.class)));

        assertEquals(4, index.foryRegistry().size());
        assertEquals(2, index.catalog().size());
        assertNotEquals(ForyEjbIndex.packageName("app", "module", "Bean", Calc.class),
                ForyEjbIndex.packageName("app", "module", "Bean", Greeter.class));
    }

    @Test
    void refusesBothViewsThatWouldBeTheSameService() {
        ForyEjbIndex index = index(List.of(
                view("app", "module", "Bean", One.Api.class),
                view("app", "module", "Bean", Two.Api.class),
                view("app", "module", "Other", Greeter.class)));

        assertEquals(1, index.foryRegistry().size());
        assertEquals(1, index.catalog().size());
        assertEquals(1, warnings.size(), warnings.toString());
    }

    @Test
    void unknownPathsDoNotRebuildEachTime() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        AtomicLong now = new AtomicLong(1_000_000_000L);
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            reads.incrementAndGet();
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, now::get);

        for (int i = 0; i < 100; i++) {
            assertNull(index.foryRegistry().lookup("/glassfish-services/fory/unknown/" + i));
        }
        assertEquals(1, reads.get(), "100 unknown paths read the deployed applications " + reads.get() + " times");

        // The index too, and an unknown document.
        for (int i = 0; i < 20; i++) {
            index.catalog().dispatch(new Exchange("GET", Protocol.CONTEXT_PATH + "/.well-known/ejb/x" + i + ".fdl"));
            index.catalog().dispatch(new Exchange("GET", Protocol.CONTEXT_PATH + "/.well-known/ejb/"));
        }
        assertEquals(1, reads.get());

        now.addAndGet(ForyEjbIndex.MIN_REBUILD_INTERVAL_NANOS + 1);
        assertNull(index.foryRegistry().lookup("/glassfish-services/fory/unknown/again"));
        assertEquals(2, reads.get(), "after the interval a miss looks again");
    }

    @Test
    void aDeploymentThatChangedIsLookedAtAtOnce() {
        AtomicInteger reads = new AtomicInteger();
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            reads.incrementAndGet();
            return List.copyOf(deployed);
        }, () -> 1_000_000_000L);
        index.refresh();
        assertEquals(1, reads.get());
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        assertNull(index.foryRegistry().lookup(path));
        assertEquals(1, reads.get());

        deployed.add(view("app", "module", "GreeterBean", Greeter.class));
        index.markStale();

        assertNotNull(index.foryRegistry().lookup(path), "the bean deployed after the last rebuild");
        assertEquals(2, reads.get());
    }

    @Test
    void aRegisteredPathIsServedWhileTheIndexIsRebuilt() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() == 2) {
                entered.countDown();
                try {
                    release.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, System::nanoTime);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        assertNotNull(index.foryRegistry().lookup(path));
        index.markStale();

        AtomicReference<Object> other = new AtomicReference<>("not yet");
        Thread rebuilder = new Thread(() -> other.set(index.foryRegistry().lookup("/missing")));
        rebuilder.start();
        try {
            assertTrue(entered.await(30, TimeUnit.SECONDS), "the rebuild did not start");

            // A rebuild is in progress, between reading what is deployed and
            // publishing it: this is the window a clear-then-refill leaves open.
            // On a thread of its own and with a deadline: a lookup that has to
            // wait for the rebuild would otherwise just wait for it.
            AtomicReference<Object> served = new AtomicReference<>();
            Thread reader = new Thread(() -> served.set(index.foryRegistry().lookup(path)));
            reader.start();
            reader.join(5_000);
            assertNotNull(served.get(), "the registered path was not served during the rebuild");
            assertEquals(2, reads.get(), "serving a registered path started another rebuild");
            assertEquals(1, index.catalog().size());
        } finally {
            release.countDown();
            rebuilder.join(30_000);
        }
        assertNull(other.get());
        assertNotNull(index.foryRegistry().lookup(path));
    }

    @Test
    void concurrentMissesAndRebuildsAlwaysFindARegisteredPath() throws Exception {
        AtomicLong now = new AtomicLong(1_000_000_000L);
        ForyEjbIndex index = new ForyEjbIndex(
                () -> List.of(view("app", "module", "GreeterBean", Greeter.class)), now::get);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        AtomicReference<String> failure = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 6; t++) {
            int id = t;
            Thread thread = new Thread(() -> {
                try {
                    for (int i = 0; i < 300 && failure.get() == null; i++) {
                        if (index.foryRegistry().lookup(path) == null) {
                            failure.compareAndSet(null, "the registered path was missing");
                        }
                        index.foryRegistry().lookup("/missing/" + id + '/' + i);
                        if (i % 7 == 0) {
                            index.markStale();
                            now.addAndGet(ForyEjbIndex.MIN_REBUILD_INTERVAL_NANOS);
                        }
                    }
                } catch (RuntimeException e) {
                    failure.compareAndSet(null, e.toString());
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join(60_000);
        }

        assertNull(failure.get());
        assertEquals(1, index.foryRegistry().size());
    }

    @Test
    void aViewIsBuiltOnceForAsLongAsItsApplicationStaysDeployed() {
        AtomicLong now = new AtomicLong(1_000_000_000L);
        ForyEjbIndex index = new ForyEjbIndex(
                () -> List.of(view("app", "module", "GreeterBean", Greeter.class)), now::get);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        ForyGeneratedServiceRegistry.Binding first = index.foryRegistry().lookup(path);

        now.addAndGet(ForyEjbIndex.MIN_REBUILD_INTERVAL_NANOS + 1);
        index.foryRegistry().lookup("/missing");
        index.markStale();
        index.foryRegistry().lookup("/missing");

        assertSame(first, index.foryRegistry().lookup(path), "an unchanged view was compiled again");
    }

    @Test
    void aRedeployedViewIsBuiltAgain() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(view("app", "module", "GreeterBean", Greeter.class));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        ForyGeneratedServiceRegistry.Binding first = index.foryRegistry().lookup(path);

        // The same view under the class loader of the new deployment.
        deployed.set(0, new ForyEjbIndex.DeployedView("app", "module", "GreeterBean", Greeter.class.getName(),
                new ClassLoader(LOADER) { }, false));
        index.markStale();

        // A hit: the path is published, under the old loader's binding.
        assertNotSame(first, index.foryRegistry().lookup(path), "the old binding survived the redeployment");
    }

    @Test
    void dropForgetsWhatWasUndeployed() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(view("app", "module", "GreeterBean", Greeter.class));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        assertNotNull(index.foryRegistry().lookup(path));

        deployed.clear();
        index.dropAll();

        assertEquals(0, index.foryRegistry().size());
        assertEquals(0, index.catalog().size());
        assertNull(index.foryRegistry().lookup(path));
    }

    @Test
    void aFailureReadingTheDeploymentKeepsWhatWasPublishedAndIsRetriedNoSoonerThanTheInterval() {
        AtomicInteger reads = new AtomicInteger();
        AtomicLong now = new AtomicLong(1_000_000_000L);
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() > 1) {
                throw new IllegalStateException("registry unavailable");
            }
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, now::get);
        index.refresh();
        index.markStale();

        index.refresh();

        assertEquals(1, index.foryRegistry().size());
        assertEquals(1, warnings.size(), warnings.toString());

        // A registry that keeps failing is not asked again for every request.
        for (int i = 0; i < 50; i++) {
            index.foryRegistry().lookup("/missing/" + i);
        }
        assertEquals(2, reads.get());

        // After the interval it is, and a failure that goes on is not said again.
        now.addAndGet(ForyEjbIndex.MIN_REBUILD_INTERVAL_NANOS + 1);
        index.foryRegistry().lookup("/missing");
        assertEquals(3, reads.get());
        assertEquals(1, warnings.size(), warnings.toString());
        assertEquals(1, index.foryRegistry().size());
    }

    @Test
    void anErrorReadingTheDeploymentIsCaughtToo() {
        AtomicInteger reads = new AtomicInteger();
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() > 1) {
                throw new NoClassDefFoundError("a class the registry needs");
            }
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, System::nanoTime);
        index.refresh();
        index.markStale();

        index.refresh();

        assertEquals(1, index.foryRegistry().size());
    }

    @Test
    void anUnloadingApplicationIsNotPublishedAgain() {
        // The registry reader leaves out an application that is not loaded
        // (see ForyEjbIndexRegistryTest), which is what the supplier does here
        // from the moment the application starts to unload.
        AtomicBoolean unloading = new AtomicBoolean();
        ForyEjbIndex index = new ForyEjbIndex(() -> unloading.get()
                ? List.of() : List.of(view("app", "module", "GreeterBean", Greeter.class)), System::nanoTime);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        assertNotNull(index.foryRegistry().lookup(path));

        unloading.set(true);
        index.drop("app");
        // A request in the window between the unload and the registry's letting go of it.
        assertNull(index.foryRegistry().lookup(path));

        assertEquals(0, index.foryRegistry().size());
        assertEquals(0, index.catalog().size());
    }

    @Test
    void dropGoesByTheNameTheApplicationIsRegisteredUnder() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(new ForyEjbIndex.DeployedView("display-name", "module", "GreeterBean", Greeter.class.getName(),
                LOADER, false, "registered-as"));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        index.refresh();
        assertEquals(1, index.foryRegistry().size());

        index.drop("display-name");
        assertEquals(1, index.foryRegistry().size(), "dropped by a name the application is not registered under");

        deployed.clear();
        index.drop("registered-as");
        assertEquals(0, index.foryRegistry().size());
    }

    @Test
    void dropForgetsOnlyTheNamedApplication() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(new ForyEjbIndex.DeployedView("a", "module", "GreeterBean", Greeter.class.getName(), LOADER,
                false, "a"));
        deployed.add(new ForyEjbIndex.DeployedView("b", "module", "GreeterBean", Greeter.class.getName(), LOADER,
                false, "b"));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        index.refresh();
        String pathA = wire("a", "module", "GreeterBean", Greeter.class, "Greet");
        String pathB = wire("b", "module", "GreeterBean", Greeter.class, "Greet");
        ForyGeneratedServiceRegistry.Binding b = index.foryRegistry().lookup(pathB);
        assertEquals(2, index.foryRegistry().size());

        deployed.remove(0);
        index.drop("a");

        assertEquals(1, index.foryRegistry().size());
        assertEquals(1, index.catalog().size());
        assertNull(index.foryRegistry().lookup(pathA));
        assertSame(b, index.foryRegistry().lookup(pathB), "the other application's view was built again");
    }

    @Test
    void requestsAndDeploymentEventsDoNotWaitForARebuildThatIsRunning() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() == 2) {
                entered.countDown();
                try {
                    release.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, System::nanoTime);
        index.refresh();
        index.markStale();
        Thread rebuilder = new Thread(() -> index.foryRegistry().lookup("/missing"));
        rebuilder.start();
        try {
            assertTrue(entered.await(30, TimeUnit.SECONDS), "the rebuild did not start");
            index.markStale();

            List<Thread> misses = new ArrayList<>();
            AtomicInteger returned = new AtomicInteger();
            for (int i = 0; i < 8; i++) {
                int id = i;
                Thread miss = new Thread(() -> {
                    index.foryRegistry().lookup("/missing/" + id);
                    returned.incrementAndGet();
                });
                misses.add(miss);
                miss.start();
            }
            Thread event = new Thread(() -> {
                index.drop("app");
                returned.incrementAndGet();
            });
            event.start();
            for (Thread t : misses) {
                t.join(10_000);
            }
            event.join(10_000);

            assertEquals(9, returned.get(), "requests or a deployment event were parked behind the rebuild");
        } finally {
            release.countDown();
            rebuilder.join(30_000);
        }
        // What the event asked for is done once the rebuild is over, whatever it published.
        assertEquals(0, index.foryRegistry().size());
        assertEquals(0, index.catalog().size());
    }

    @Test
    void aDropThatFoundTheLockTakenIsAppliedWhenTheHolderTakesAnEarlyReturn() throws Exception {
        AtomicBoolean armed = new AtomicBoolean();
        AtomicReference<ForyEjbIndex> holder = new AtomicReference<>();
        ForyEjbIndex index = new ForyEjbIndex(
                () -> List.of(view("app", "module", "GreeterBean", Greeter.class)), () -> {
                    if (armed.compareAndSet(true, false)) {
                        // The request that holds the lock is checking how old the last
                        // rebuild is; a deployment event arrives now, on its own thread.
                        Thread event = new Thread(() -> holder.get().drop("app"));
                        event.start();
                        try {
                            event.join(10_000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    return 1_000_000_000L;
                });
        holder.set(index);
        index.refresh();
        assertEquals(1, index.foryRegistry().size());

        armed.set(true);
        // A miss that is too soon after the last rebuild to look again.
        index.foryRegistry().lookup("/missing");

        assertEquals(0, index.foryRegistry().size(), "the queued drop was left undone");
    }

    @Test
    void aRequestForAPublishedPathWaitsAShortTimeForARebuildThatIsRunning() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(view("app", "module", "GreeterBean", Greeter.class));
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() == 2) {
                entered.countDown();
                try {
                    release.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return List.copyOf(deployed);
        }, System::nanoTime);
        index.refresh();
        String path = wire("app", "module", "GreeterBean", Greeter.class, "Greet");
        ForyGeneratedServiceRegistry.Binding old = index.foryRegistry().lookup(path);
        index.markStale();
        Thread rebuilder = new Thread(() -> index.foryRegistry().lookup("/missing"));
        rebuilder.start();
        assertTrue(entered.await(30, TimeUnit.SECONDS));
        // The redeployment is read by the rebuild that is running.
        deployed.set(0, new ForyEjbIndex.DeployedView("app", "module", "GreeterBean", Greeter.class.getName(),
                new ClassLoader(LOADER) { }, false));
        index.markStale();

        AtomicReference<ForyGeneratedServiceRegistry.Binding> served = new AtomicReference<>();
        Thread hit = new Thread(() -> served.set(index.foryRegistry().lookup(path)));
        hit.start();
        Thread.sleep(50);
        release.countDown();
        hit.join(30_000);
        rebuilder.join(30_000);

        assertNotNull(served.get());
        assertNotSame(old, served.get(), "answered with the old binding without waiting for the rebuild");
    }

    @Test
    void aMissDoesNotWaitForARebuildThatIsRunningEvenWhenTheDeploymentChanged() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        ForyEjbIndex index = new ForyEjbIndex(() -> {
            if (reads.incrementAndGet() == 2) {
                entered.countDown();
                try {
                    release.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return List.of(view("app", "module", "GreeterBean", Greeter.class));
        }, System::nanoTime);
        index.refresh();
        index.markStale();
        Thread rebuilder = new Thread(() -> index.foryRegistry().lookup("/missing"));
        rebuilder.start();
        try {
            assertTrue(entered.await(30, TimeUnit.SECONDS));
            index.markStale();

            long started = System.nanoTime();
            index.foryRegistry().lookup("/missing/again");

            long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            assertTrue(millis < ForyEjbIndex.STALE_WAIT_MILLIS, "a miss waited " + millis + " ms");
        } finally {
            release.countDown();
            rebuilder.join(30_000);
        }
    }

    @Test
    void aViewWhoseClassLoaderIsGoneIsLeftOutWithoutAWarning() {
        ForyEjbIndex index = index(List.of(
                new ForyEjbIndex.DeployedView("app", "module", "Gone", Greeter.class.getName(), null, false)));

        assertEquals(0, index.foryRegistry().size());
        assertEquals(List.of(), warnings);
    }

    @Test
    void aStaticHelperOfAViewDoesNotUnpublishIt() {
        ForyEjbIndex index = index(List.of(view("app", "module", "Bean", WithAHelper.class)));

        assertEquals(1, index.foryRegistry().size());
        assertEquals(List.of(), warnings);
    }

    @Test
    void aViewNamedLikeAMessageIsRefusedAndOneNamedEmptyIsFine() {
        ForyEjbIndex index = index(List.of(
                view("app", "module", "Bean", Empty.class),
                view("app", "module", "Bean", RunRequest.class)));

        assertEquals(1, index.foryRegistry().size());
        assertNotNull(index.foryRegistry().lookup(wire("app", "module", "Bean", Empty.class, "Run")));
        assertEquals(1, warnings.size(), warnings.toString());
    }

    /** Enough of an exchange to read the catalog's answer. */
    static final class Exchange implements org.glassfish.orb.http.server.ServerExchange {
        private final String method;
        private final String path;

        Exchange(String method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return pathBytes().length;
        }

        @Override
        public String requestHeader(String name) {
            return null;
        }

        @Override
        public String queryParameter(String name) {
            return null;
        }

        @Override
        public java.io.InputStream requestBody() {
            return java.io.InputStream.nullInputStream();
        }

        @Override
        public void setStatus(int status) {
        }

        @Override
        public void setResponseHeader(String name, String value) {
        }

        @Override
        public void writeBody(java.nio.ByteBuffer[] body) {
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
