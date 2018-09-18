/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.cmt.chunk;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author makannan
 */
public class SimpleIdGenerator
    implements IdGenerator {
    
    private AtomicInteger counter = new AtomicInteger(0);
    
    public String nextId() {
        return "" + counter.incrementAndGet();
    }
}
