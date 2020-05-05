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
package com.oracle.javaee7.samples.batch.twosteps;

import java.io.Externalizable;
import java.util.List;
@jakarta.inject.Named("SimpleItemWriter")
public class SimpleItemWriter
    extends jakarta.batch.api.AbstractItemWriter<String> {
    
    @Override
    public void open(Externalizable e) throws Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void writeItems(List<String> list) throws Exception {
        StringBuilder sb = new StringBuilder("SimpleItemWriter:");
        for (String s : list) {
            sb.append(" ").append(s);
        }
        System.out.println(sb.toString());
    }

    @Override
    public Externalizable checkpointInfo() throws Exception {
        return null;
    }
    
}
