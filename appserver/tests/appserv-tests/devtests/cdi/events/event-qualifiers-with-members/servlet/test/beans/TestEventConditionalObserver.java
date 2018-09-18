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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;

public class TestEventConditionalObserver {
    public static int documentAdminUpdatedEvent,
            documentFooUserUpdatedEvent = 0;

    public void afterAdminDocumentUpdate(
            @Observes @Updated(updatedBy = "admin") Document d) {
        System.out.println("TestEventObserver:afterAdminDocumentUpdate");
        documentAdminUpdatedEvent++;
    }

    public void afterFooUserDocumentUpdate(
            @Observes @Updated(updatedBy = "FooUser") Document d) {
        System.out.println("TestEventObserver:afterFooUserDocumentUpdate");
        documentFooUserUpdatedEvent++;
    }

}
