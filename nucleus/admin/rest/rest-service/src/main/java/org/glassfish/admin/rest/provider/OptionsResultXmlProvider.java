/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.provider;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.util.Iterator;
import java.util.Set;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.results.OptionsResult;

import static org.glassfish.admin.rest.provider.ProviderUtil.getEndXmlElement;
import static org.glassfish.admin.rest.provider.ProviderUtil.quote;

/**
 * XML provider for OptionsResult.
 *
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class OptionsResultXmlProvider extends BaseProvider<OptionsResult> {
    private final static String QUERY_PARAMETERS = "queryParameters";
    private final static String MESSAGE_PARAMETERS = "messageParameters";
    private final static String METHOD = "method";

    public OptionsResultXmlProvider() {
        super(OptionsResult.class, MediaType.APPLICATION_XML_TYPE);
    }

    //get json representation for the given OptionsResult object
    @Override
    public String getContent(OptionsResult proxy) {
        String result;
        String indent = Constants.INDENT;
        result = "<" + proxy.getName() + ">";

        result = result + getRespresenationForMethodMetaData(proxy, indent);

        result = result + "\n" + getEndXmlElement(proxy.getName());
        return result;
    }

    String getRespresenationForMethodMetaData(OptionsResult proxy, String indent) {
        String result = "";
        Set<String> methods = proxy.methods();
        Iterator<String> iterator = methods.iterator();
        String method;

        while (iterator.hasNext()) {
            method = iterator.next();

            //method
            result = result + getMethod(method, indent);

            MethodMetaData methodMetaData = proxy.getMethodMetaData(method);

            //           //query params
            //           result = result + getQueryParams(methodMetaData,
            //               indent + Constants.INDENT);

            //parameters (message parameters)
            result = result + getMessageParams(methodMetaData, indent + Constants.INDENT);

            result = result + "\n" + indent;
            result = result + getEndXmlElement(METHOD);
        }
        return result;
    }

    //get xml representation for the given method name
    private String getMethod(String method, String indent) {
        String result = "\n" + indent + "<";
        result = result + METHOD + " name=";
        result = result + quote(method);
        result = result + ">";
        return result;
    }

    //    //get xml representation for the method query parameters
    //    private String getQueryParams(MethodMetaData methodMetaData,
    //            String indent) {
    //        //TODO too many string concatenations happening here. Change this and other methods in this class to use StringBuffer
    //        String result = "";
    //        if (methodMetaData.sizeQueryParamMetaData() > 0) {
    //            result = result + "\n" + indent;
    //            result = result + "<" + QUERY_PARAMETERS + ">";
    //
    //            Set<String> queryParams = methodMetaData.queryParams();
    //            Iterator<String> iterator = queryParams.iterator();
    //            String queryParam;
    //            while (iterator.hasNext()) {
    //                queryParam = iterator.next();
    //                ParameterMetaData parameterMetaData =
    //                    methodMetaData.getQueryParamMetaData(queryParam);
    //                result = result + getParameter(queryParam, parameterMetaData,
    //                    indent + Constants.INDENT);
    //            }
    //            result = result + "\n" + indent;
    //            result = result +  getEndXmlElement(QUERY_PARAMETERS);
    //        }
    //        return result;
    //    }

    //get xml representation for the method message parameters
    private String getMessageParams(MethodMetaData methodMetaData, String indent) {
        String result = "";
        if (methodMetaData.sizeParameterMetaData() > 0) {
            result = result + "\n" + indent;
            result = result + "<" + MESSAGE_PARAMETERS + ">";

            Set<String> parameters = methodMetaData.parameters();
            Iterator<String> iterator = parameters.iterator();
            String parameter;
            while (iterator.hasNext()) {
                parameter = iterator.next();
                ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData(parameter);
                result = result + getParameter(parameter, parameterMetaData, indent + Constants.INDENT);
            }
            result = result + "\n" + indent;
            result = result + getEndXmlElement(MESSAGE_PARAMETERS);
        }
        return result;
    }

    //get xml representation for the given parameter
    private String getParameter(String parameter, ParameterMetaData parameterMetaData, String indent) {
        StringBuilder result = new StringBuilder("\n" + indent);

        result.append("<").append(parameter);

        Set<String> attributes = parameterMetaData.attributes();
        Iterator<String> iterator = attributes.iterator();
        String attributeName;
        while (iterator.hasNext()) {
            attributeName = iterator.next();
            String attributeValue = parameterMetaData.getAttributeValue(attributeName);
            result.append(getAttribute(attributeName, attributeValue));
        }
        result.append("/>");
        return result.toString();
    }

    //get xml representation for a give attribute of parameter
    private String getAttribute(String name, String value) {
        String result = " ";
        name = name.replace(' ', '-');
        result = result + name + "=" + quote(value);
        return result;
    }

}
