/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.IOException;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

public class AsyncListenerImpl implements AsyncListener {
    private String path = null;

    public AsyncListenerImpl(String path) {
        System.out.println("Construct AsyncLisetnerImpl: " + path);
        this.path = path;
    }

    public void onComplete(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onTimeout(AsyncEvent event) throws IOException {
        AsyncContext ac = event.getAsyncContext();
        System.out.println("Async dispatch to " + path);
        ac.dispatch(path);
    }

    public void onError(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
        // do nothing
    }
}
