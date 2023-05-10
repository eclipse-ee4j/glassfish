/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.ResourceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.easymock.IExpectationSetters;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.function.Executable;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class RWLockDataStructureTest {

    private static final int TASK_COUNT = 1000;

    private static final int THREAD_COUNT = 500;

    private static final int MAX_SIZE = TASK_COUNT;

    @RepeatedTest(20)
    @Timeout(value = 10, threadMode = ThreadMode.SEPARATE_THREAD)
    public void raceConditions() throws Exception {

        ResourceHandler handler = createNiceMock(ResourceHandler.class);
        ResourceAllocator allocator = createNiceMock(ResourceAllocator.class);

        List<Object> mocks = new ArrayList<>(MAX_SIZE);
        for (int i = 0; i < MAX_SIZE; i++) {
            mocks.add(
                // We use constructor to generate ResourceHandle mock
                // because we depend on an internal state of this object.
                createMockBuilder(ResourceHandle.class)
                    .withConstructor(Object.class, ResourceSpec.class, ResourceAllocator.class, ClientSecurityInfo.class)
                    // Actual constructor arguments does not matter
                    .withArgs(null, null, null, null)
                    .createNiceMock());
        }

        IExpectationSetters<ResourceHandle> handlerExpectation = expect(handler.createResource(allocator));
        IExpectationSetters<ResourceHandle> allocatorExpectation = expect(allocator.createResource());
        for (Object resource : mocks) {
            handlerExpectation.andReturn((ResourceHandle) resource);
            allocatorExpectation.andReturn((ResourceHandle) resource);
        }
        mocks.add(handler);
        mocks.add(allocator);

        replay(mocks.toArray());

        RWLockDataStructure dataStructure = new RWLockDataStructure(null, MAX_SIZE, handler, null);

        for (int i = 0; i < MAX_SIZE; i++) {
            // requires handler.createResource(allocator)
            dataStructure.addResource(allocator, 1);
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<ResourceHandle>> tasks = new ArrayList<>(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT; i++) {
            tasks.add(() -> {
                ResourceHandle resource = dataStructure.getResource();
                dataStructure.removeResource(resource);
                return resource;
            });
        }
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

    private boolean notNull(Future<?> future) {
        try {
            return future.get() != null;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
