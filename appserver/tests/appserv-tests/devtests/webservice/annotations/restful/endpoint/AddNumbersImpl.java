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

package endpoint;

import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;

import jakarta.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.http.HTTPException;
import jakarta.xml.ws.http.HTTPBinding;
import jakarta.xml.ws.BindingType;

@WebServiceProvider
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class AddNumbersImpl implements Provider<Source> {

    @Resource
    protected WebServiceContext wsContext;

    public Source invoke(Source source) {
        try {
            MessageContext mc = wsContext.getMessageContext();
            String query = (String)mc.get(MessageContext.QUERY_STRING);
            String path = (String)mc.get(MessageContext.PATH_INFO);
            System.out.println("Query String = "+query);
            System.out.println("PathInfo = "+path);
            if (query != null && query.contains("num1=") &&
                query.contains("num2=")) {
                return createSource(query);
            } else if (path != null && path.contains("/num1") &&
                       path.contains("/num2")) {
                return createSource(path);
            } else {
                throw new HTTPException(404);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new HTTPException(500);
        }
    }

    private Source createSource(String str) {
        StringTokenizer st = new StringTokenizer(str, "=&/");
        String token = st.nextToken();
        int number1 = Integer.parseInt(st.nextToken());
        st.nextToken();
        int number2 = Integer.parseInt(st.nextToken());
        int sum = number1+number2;
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.org\"><ns:return>"
            +sum
            +"</ns:return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }

}
