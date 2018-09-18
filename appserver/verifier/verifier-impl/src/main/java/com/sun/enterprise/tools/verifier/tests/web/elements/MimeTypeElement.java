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

package com.sun.enterprise.tools.verifier.tests.web.elements;

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import java.util.logging.Level;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.descriptor.MimeMappingDescriptor;


/** 
 * Servlet mime-type element contains a defined mime type.  i.e. "text/plain"
 */
public class MimeTypeElement extends WebTest implements WebCheck, MimeTypes { 

    /**
     * Servlet mime-type element contains a defined mime type.  i.e. "text/plain"
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getMimeMappings().hasMoreElements()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
	    // get the mimeType's in this .war
	    for (Enumeration e = descriptor.getMimeMappings() ; e.hasMoreElements() ;) {
		foundIt = false;
		MimeMappingDescriptor mimemapping = (MimeMappingDescriptor)e.nextElement();
		String mimeType = mimemapping.getMimeType();
		logger.log(Level.FINE, "servlet mimeType: " + mimeType);
		int pos = mimeType.indexOf("/");
		// user defined
		// see http://www-dos.uniinc.msk.ru/tech1/1995/mime/m_tech.htm#Type

		if (mimeType.substring(pos+1).startsWith("X-") || mimeType.substring(pos+1).startsWith("x-")) {
		    foundIt = true;
		} else if (mimeType.startsWith("X-")) {
                    foundIt = true;
                } else if (mimeType.substring(0,pos).equals("text")) {
		    if (Arrays.asList(text).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("multipart")) {
		    if (Arrays.asList(multipart).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("message")) {
		    if (Arrays.asList(message).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("application")) {
		    if (Arrays.asList(application).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("image")) {
		    if (Arrays.asList(image).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("audio")) {
		    if (Arrays.asList(audio).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("video")) {
		    if (Arrays.asList(video).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		} else if (mimeType.substring(0,pos).equals("model")) {
		    if (Arrays.asList(model).contains(mimeType.substring(pos+1,mimeType.length()))) {
			foundIt = true;
		    } 
		}
   
		if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Servlet mime-type [ {0} ] defined for this web application [ {1} ]",
					   new Object[] {mimeType, descriptor.getName()}));
		} else {
		    if (!oneFailed) {
			oneFailed = true;
		    }
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: Servlet mime-type [ {0} ] not defined for this web application [ {1} ]",
					    new Object[] {mimeType, descriptor.getName()}));
		}
	    }
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no mimemappings within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }
}
