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

import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

public class TestEventProducer {
    @Inject Event<Document> docEvent;
    
    @Inject @Updated(updatedBy="admin") Event<Document> updatedByAdminEvent;
    
    public void fireEvents(){
        Document d = new Document("Test");
        docEvent.fire(d); //general fire of a Document related event
        
        //send a created event
        docEvent.select(
                new AnnotationLiteral<Created>(){}).fire(d);
        
        
        d.update();
        //send an updated by admin event
        updatedByAdminEvent.fire(d);
        
        //send an updated by FooUser event
        docEvent.select(new UserBinding("FooUser")).fire(d);
        
    }
    

}

class UserBinding extends AnnotationLiteral<Updated> implements Updated{
    private String userName;

    public UserBinding(String userName){
        this.userName = userName;
    }

    @Override
    public String updatedBy() {
        return this.userName;
    }
    
}
