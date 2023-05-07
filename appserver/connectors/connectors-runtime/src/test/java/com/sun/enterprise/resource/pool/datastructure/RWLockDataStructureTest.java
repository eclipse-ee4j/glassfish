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

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.ResourceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.function.Executable;

import static org.easymock.EasyMock.createNiceMock;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class RWLockDataStructureTest {

    private static final int TASK_COUNT = 1000;


    @BeforeAll
    public static void init() {
        new ConnectorRuntime();
    }


    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES, threadMode = ThreadMode.SEPARATE_THREAD)
    public void raceConditions() throws Exception {

        ResourceHandler handler = createNiceMock(ResourceHandler.class);
        ResourceHandle handle = createNiceMock(ResourceHandle.class);
        EasyMock.expect(handle.isBusy()).andReturn(Boolean.FALSE);
        RWLockDataStructure dataStructure = new RWLockDataStructure(null, 100, handler, null);
        ResourceAllocator allocator = createNiceMock(ResourceAllocator.class);
        EasyMock.expect(handler.createResource(allocator)).andReturn(handle);
        EasyMock.expect(allocator.createResource()).andReturn(handle);
        EasyMock.replay(allocator, handler, handle);

        for (int i = 0; i < TASK_COUNT; i++) {
            // requires handler.createResource(allocator)
            dataStructure.addResource(allocator, 1);
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(500);
        List<Callable<Void>> tasks = new ArrayList<>(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT; i++) {
            tasks.add(() -> {
                ResourceHandle resource = dataStructure.getResource();
                dataStructure.removeResource(resource);
                return null;
            });
        }
        List<Future<Void>> futures = threadPool.invokeAll(tasks, 10, TimeUnit.SECONDS);
        assertAll(
            () -> assertTrue(futures.stream().allMatch(f -> f.isDone()), "All done."),
            () -> assertFalse(futures.stream().anyMatch(f -> f.isCancelled()), "None cancelled."),
            () -> assertAll(futures.stream().map(f -> (Executable) f::get).collect(Collectors.toList())),
            () -> assertThat("Resources Size", dataStructure.getResourcesSize(), equalTo(0)),
            () -> assertThat("Free list size", dataStructure.getFreeListSize(), equalTo(0))
        );
    }
}
