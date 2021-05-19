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

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;

@RequestScoped
public class TestEventConditionalObserver {
    public static int documentCreatedEvent = 0;
    public static int documentUpdatedEvent = 0;
    public static int documentAnyEvents = 0;
    public static int documentApprovedEvents = 0;

    public void onDocumentCreate(@Observes(notifyObserver=Reception.IF_EXISTS) @Created Document d) {
        System.out.println("TestEventObserver:onDocumentCreate");
        documentCreatedEvent++;
    }

    public void onAnyDocumentEvent(@Observes Document d) {
        System.out.println("TestEventObserver:onAnyDocumentUpdatedEvent");
        documentAnyEvents++;
    }

    public void afterDocumentUpdate(@Observes @Updated Document d) {
        System.out.println("TestEventObserver:afterDocumentUpdate");
        documentUpdatedEvent++;
    }

    public void onDocumentUpdatedAndApproved(
            @Observes @Updated @Approved Document d){
        System.out.println("TestEventObserver:updated and approved");
        documentApprovedEvents++;

    }

}
