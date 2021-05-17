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

package test.beans;

import jakarta.enterprise.event.Observes;

public class TestEventObserver {
    public static int documentAfterBlogUpdate;
    public static int documentAfterDocumentUpdate;
    public static int documentOnAnyBlogEvent;
    public static int documentOnAnyDocumentEvent;

    public void afterBlogUpdate(
            @Observes @Updated @Blog Document d) {
        System.out.println("TestEventObserver:afterBlogUpdate");
        documentAfterBlogUpdate++;
    }


    public void afterDocumentUpdate(
            @Observes @Updated  Document d) {
        System.out.println("TestEventObserver:afterDocumentUpdate");
        documentAfterDocumentUpdate++;
    }

    public void onAnyBlogEvent(
            @Observes @Blog Document d) {
        System.out.println("TestEventObserver:onAnyBlogEvent");
        documentOnAnyBlogEvent++;
    }

    public void onAnyDocumentEvent(@Observes Document d){
        System.out.println("TestEventObserver:onAnyDocumentEvent");
        documentOnAnyDocumentEvent++;

    }

}
