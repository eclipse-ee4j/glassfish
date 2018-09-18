/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;

/** 
 * All web modules in the application must have unique context-root. 
 */

public class AppWebContextUnique extends ApplicationTest implements AppCheck { 


    /** 
     * All web modules in the application must have unique context-root.
     * Applicable for j2ee 1.3 or below. For 1.4 and above xml schema takes care of this.
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();
        Set webs=descriptor.getBundleDescriptors(WebBundleDescriptor.class);
        if(webs.size()<=1){
            result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There is one or less web component in application [ {0} ]",
				  new Object[] {descriptor.getName()}));        
            return result;
        }
        Set<String> contexts=new HashSet<String>();
        Iterator itr=webs.iterator();
        boolean oneFailed=false;
	while (itr.hasNext()) {
            WebBundleDescriptor wbd = (WebBundleDescriptor) itr.next();
            String ctx=wbd.getContextRoot();
            if(!contexts.add(ctx)){
                oneFailed=true;
                result.failed(
                    (smh.getLocalString
                     (getClass().getName() + ".failed",
                      "Error: There is already a web module with context-root [ {0} ] within application [ {1} ]",
                      new Object[] {ctx, descriptor.getName()})));
            }
        }
	if(!oneFailed){
            result.passed(
                (smh.getLocalString
                 (getClass().getName() + ".passed",
                  "All the context-root values are unique within application [ {0} ]",
                  new Object[] {descriptor.getName()})));
        }
	return result;
    }
}
