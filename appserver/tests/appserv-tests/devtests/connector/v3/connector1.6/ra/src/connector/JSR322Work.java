/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import jakarta.resource.spi.work.WorkContextProvider;
import jakarta.resource.spi.work.WorkContext;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import java.util.List;
import java.util.ArrayList;


public class JSR322Work extends DeliveryWork implements WorkContextProvider {

//    private WorkContexts ics = null;
    private List<WorkContext> contextsList = new ArrayList<WorkContext>();


    public JSR322Work(MessageEndpoint ep, int numOfMessages, String op){
        super(ep, numOfMessages, op);
    }

    public JSR322Work(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount){
        super(ep, numOfMessages, op, keepCount);
    }

    public List<WorkContext> getWorkContexts() {
        return contextsList;
    }


    public void addWorkContext(WorkContext ic){
        contextsList.add(ic);
    }

    public void run(){
        super.run();
        
    }
}
