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

/*
 * FilterMapping.java
 *
 * Created on November 29, 2000, 2:59 PM
 */

package com.sun.enterprise.tools.verifier.tests.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;
import org.glassfish.web.deployment.descriptor.ServletFilterMappingDescriptor;

/**
 * Check that all the mappings for the declated filters are correct.
 * 
 * @author  Jerome Dochez
 * @version 1.0
 */
public class FilterMapping extends WebTest {
    Result result;
    ComponentNameConstructor compName;

    /**
     * Check that the mappings for all filters are correct
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        Enumeration filterEnum = descriptor.getServletFilterDescriptors().elements();
        if (filterEnum.hasMoreElements()) {
            while (filterEnum.hasMoreElements()) {
                ServletFilterDescriptor filter = (ServletFilterDescriptor) filterEnum.nextElement();
                hasValidMapping(descriptor, filter.getName());
            }
        }
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "All filter mappings are correct"));
        }
        return result;
    }

    private void hasValidMapping(WebBundleDescriptor descriptor, String filterName) {
        Enumeration filtermapperEnum = descriptor.getServletFilterMappingDescriptors().elements();
        if (filtermapperEnum.hasMoreElements()) {
            ServletFilterMappingDescriptor filterMapper = null;
            boolean mappingFound = false;
            do {
                filterMapper = (ServletFilterMappingDescriptor)filtermapperEnum.nextElement();
                String filterMapping = filterMapper.getName();
                mappingFound = filterName.equals(filterMapping);
            } while (!mappingFound && filtermapperEnum.hasMoreElements());

            if (mappingFound) {
                List<String> urlPatterns = filterMapper.getUrlPatterns();
                for(String url : urlPatterns) {
                    if (!((url.startsWith("/")) ||
                            ((url.startsWith("/")) && (url.endsWith("/*"))) ||
                            (url.startsWith("*.")))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                "Filter Mapping for [ {0} ] has invalid " +
                                "url-mapping [ {1} ] ",
                                new Object[] {filterName, url} ));
                    }
                }
                List<String> servletsInFilter = filterMapper.getServletNames();
                // Section SRV.18.0.2 Filter All Dispatches of Servlet 2.5 spec allows "*"
                // to be specified as Servlet name to allow filtering of requests for all servlets.
                final String ALL_SERVLETS_NAME = "*";
                servletsInFilter.remove(ALL_SERVLETS_NAME);
                List<String> servletsInWAR = new ArrayList<String>();

                if(servletsInFilter.size() > 0) {
                    Set servletDescriptor = descriptor.getServletDescriptors();
                    Iterator itr = servletDescriptor.iterator();
                    // test the servlets in this .war
                    while (itr.hasNext()) {
                        WebComponentDescriptor servlet = (WebComponentDescriptor) itr.next();
                        servletsInWAR.add(servlet.getCanonicalName());
                    }
                    if (!(servletsInWAR != null && servletsInWAR.containsAll(servletsInFilter))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed1",
                                "Filter Mapping for [ {0} ] has invalid servlet-name",
                                new Object[] {filterName}));
                    }
                }
            }
        }
    }
}
