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

import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

public class TestEventProducer {
    @Inject Event<Document> docEvent;
    
    public void fireEvents(){
        Document d = new Document("Test");
        docEvent.fire(d); //general fire of a Document related event
        
        //send a created event
        docEvent.select(
                new AnnotationLiteral<Created>(){}).fire(d);
        
        
        d.update();
        //send an updated event
        docEvent.select(
                new AnnotationLiteral<Updated>(){}).fire(d);
        
        //send an updated and approved event
        docEvent.select(new AnnotationLiteral<Updated>(){}, new AnnotationLiteral<Approved>(){}).fire(d);
        
    }

}
