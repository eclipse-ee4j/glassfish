/*
 * Copyright (c) 2023, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.datastructure;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.ResourceHandler;

import jakarta.resource.spi.ManagedConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.easymock.IExpectationSetters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.function.Executable;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class RWLockDataStructureTest {

    private static final int TASK_COUNT = 1000;

    private static final int THREAD_COUNT = 500;

    private static final int RESOURCE_COUNT = TASK_COUNT;

    private volatile ResourceHandler handler;
    private volatile ResourceAllocator allocator;

    @BeforeEach
    public void createAndPopulateMocks() throws PoolingException {

        ResourceHandler localHandler = createNiceMock(ResourceHandler.class);
        ResourceAllocator localAllocator = createNiceMock(ResourceAllocator.class);

        List<Object> mocks = new ArrayList<>(RESOURCE_COUNT);
        for (int i = 0; i < RESOURCE_COUNT; i++) {
            mocks.add(
                // We use constructor to generate ResourceHandle mock
                // because we depend on an internal state of this object.
                createMockBuilder(ResourceHandle.class)
                            .withConstructor(ManagedConnection.class, ResourceSpec.class, ResourceAllocator.class)
                    // Actual constructor arguments does not matter
                            .withArgs(null, null, null)
                    .createNiceMock());
        }

        IExpectationSetters<ResourceHandle> handlerExpectation = expect(localHandler.createResource(localAllocator));
        IExpectationSetters<ResourceHandle> allocatorExpectation = expect(localAllocator.createResource());
        for (Object resource : mocks) {
            handlerExpectation.andReturn((ResourceHandle) resource);
            allocatorExpectation.andReturn((ResourceHandle) resource);
        }
        mocks.add(localHandler);
        mocks.add(localAllocator);

        replay(mocks.toArray());

        handler = localHandler;
        allocator = localAllocator;
    }

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testAddResource() throws Exception {
        int resourceCount = RESOURCE_COUNT / 2;
        int taskCount = TASK_COUNT / 2;

        DataStructure dataStructure = new RWLockDataStructure(null, resourceCount, handler);

        List<Callable<Integer>> tasks = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> dataStructure.addResource(allocator, 1));
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<Integer>> futures = threadPool.invokeAll(tasks);
        assertAll(
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertThat(futures.stream().mapToInt(this::getResult).sum(), equalTo(taskCount)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(taskCount)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(taskCount))
        );

        assertThat(dataStructure.addResource(allocator, 1), equalTo(0));

        // Increase max pool size
        dataStructure.setMaxSize(resourceCount + 100);
        assertAll(
            () -> assertThat("Add Resources", dataStructure.addResource(allocator, 100), equalTo(100)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(resourceCount + 100)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(resourceCount + 100))
        );

        // Decrease max pool size
        dataStructure.setMaxSize(resourceCount);
        assertAll(
            () -> assertThat("Add Resource", dataStructure.addResource(allocator, 1), equalTo(0)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(resourceCount + 100)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(resourceCount + 100))
        );

        List<ResourceHandle> allResources = dataStructure.getAllResources();
        assertThat("Resources Size", allResources, hasSize(dataStructure.getResourcesSize()));
        for (ResourceHandle resource : allResources) {
            assertThat(Collections.frequency(allResources, resource), equalTo(1));
        }

        threadPool.shutdownNow();
    }

    @Test
    public void testAddResourceWithException() throws Exception {

        handler = createNiceMock(ResourceHandler.class);
        allocator = createNiceMock(ResourceAllocator.class);

        ResourceHandle resource = createMockBuilder(ResourceHandle.class)
                .withConstructor(ManagedConnection.class, ResourceSpec.class, ResourceAllocator.class)
                .withArgs(null, null, null)
                .createNiceMock();

        expect(handler.createResource(allocator)).andThrow(new PoolingException());
        expect(allocator.createResource()).andThrow(new PoolingException());
        expect(handler.createResource(allocator)).andReturn(resource);
        expect(allocator.createResource()).andReturn(resource);

        replay(resource, handler, allocator);

        DataStructure dataStructure = new RWLockDataStructure(null, 1, handler);

        assertAll(
            () -> assertThrows(PoolingException.class, () -> dataStructure.addResource(allocator, 1)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(0)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(0))
         );

        assertAll(
            () -> assertThat("Add Resource", dataStructure.addResource(allocator, 1), equalTo(1)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(1)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(1))
        );
    }

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testGetResource() throws Exception {

        DataStructure dataStructure = new RWLockDataStructure(null, RESOURCE_COUNT, handler);

        assertThat("Add Resources", dataStructure.addResource(allocator, RESOURCE_COUNT), equalTo(RESOURCE_COUNT));

        List<Callable<ResourceHandle>> tasks = new ArrayList<>(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT; i++) {
            tasks.add(dataStructure::getResource);
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<ResourceHandle>> futures = threadPool.invokeAll(tasks);
        assertAll(
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(RESOURCE_COUNT)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(0))
        );

        List<ResourceHandle> resources = futures.stream().map(this::getResult).collect(Collectors.toList());
        assertThat(resources, hasSize(RESOURCE_COUNT));

        List<ResourceHandle> allResources = dataStructure.getAllResources();
        for (ResourceHandle resource : allResources) {
            assertThat(Collections.frequency(allResources, resource), equalTo(1));
        }

        assertThat("Get Resource", dataStructure.getResource(), nullValue());

        threadPool.shutdownNow();
    }

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testReturnResource() throws Exception {

        DataStructure dataStructure = new RWLockDataStructure(null, RESOURCE_COUNT, handler);

        assertThat("Add Resources", dataStructure.addResource(allocator, RESOURCE_COUNT), equalTo(RESOURCE_COUNT));
        assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(RESOURCE_COUNT));

        List<ResourceHandle> resources = new CopyOnWriteArrayList<>();
        for (int i = 0; i < RESOURCE_COUNT; i++) {
            resources.add(dataStructure.getResource());
        }
        assertAll(
            () -> assertThat(resources, hasSize(RESOURCE_COUNT)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(0))
        );

        List<Callable<Void>> tasks = new ArrayList<>(TASK_COUNT);
        for (ResourceHandle resource : resources) {
            tasks.add(() -> {
                dataStructure.returnResource(resource);
                return null;
            });
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<Void>> futures = threadPool.invokeAll(tasks);
        assertAll(
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(RESOURCE_COUNT)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(RESOURCE_COUNT))
        );

        threadPool.shutdownNow();
    }

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testRemoveResource() throws Exception {

        DataStructure dataStructure = new RWLockDataStructure(null, RESOURCE_COUNT, handler);

        assertThat("Add Resources", dataStructure.addResource(allocator, RESOURCE_COUNT), equalTo(RESOURCE_COUNT));

        List<ResourceHandle> resources = new CopyOnWriteArrayList<>();
        for (int i = 0; i < RESOURCE_COUNT; i++) {
            resources.add(dataStructure.getResource());
        }
        assertThat(resources, hasSize(RESOURCE_COUNT));

        List<Callable<Void>> tasks = new ArrayList<>(TASK_COUNT);
        for (ResourceHandle resource : resources) {
            tasks.add(() -> {
                dataStructure.removeResource(resource);
                return null;
            });
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<Void>> futures = threadPool.invokeAll(tasks);
        assertAll(
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(0)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(0)),
            () -> assertThat("All Resources", dataStructure.getAllResources(), hasSize(0))
        );

        threadPool.shutdownNow();
    }

    @Test
    public void testRemoveAll() throws PoolingException {

        DataStructure dataStructure = new RWLockDataStructure(null, RESOURCE_COUNT, handler);

        dataStructure.addResource(allocator, RESOURCE_COUNT);
        assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(RESOURCE_COUNT));

        dataStructure.removeAll();

        assertAll(
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(0)),
            () -> assertThat("Free List Size", dataStructure.getFreeListSize(), equalTo(0)),
            () -> assertThat("Get Resource", dataStructure.getResource(), nullValue())
        );
    }

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testRaceConditions() throws Exception {

        RWLockDataStructure dataStructure = new RWLockDataStructure(null, RESOURCE_COUNT, handler);

        for (int i = 0; i < RESOURCE_COUNT; i++) {
            // requires handler.createResource(allocator)
            dataStructure.addResource(allocator, 1);
        }

        List<Callable<ResourceHandle>> tasks = new ArrayList<>(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT; i++) {
            tasks.add(() -> {
                ResourceHandle resource = dataStructure.getResource();
                dataStructure.removeResource(resource);
                return resource;
            });
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

        List<Future<ResourceHandle>> futures = threadPool.invokeAll(tasks);
        // When executed without races, all returned ResourceHandles is not null
        // and Resources List always empty. This is because we do pair getResource and
        // removeResource calls.
        // When race condition present, then in some cases we can meet some returned
        // ResourceHandles is null AND Resources List is not empty.
        assertAll(
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertTrue(futures.stream().allMatch(this::notNull)),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(0)),
            () -> assertThat("Free list size", dataStructure.getFreeListSize(), equalTo(0))
        );

        threadPool.shutdownNow();
    }

    private <T> boolean notNull(Future<T> future) {
        return getResult(future) != null;
    }

    private <T> T getResult(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
