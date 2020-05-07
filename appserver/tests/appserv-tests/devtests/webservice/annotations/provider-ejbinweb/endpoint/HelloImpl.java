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

import java.io.*;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.ejb.Stateless;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.ws.WebServiceException;


import endpoint.jaxws.*;

@WebServiceProvider(serviceName = "HelloImplService", portName = "HelloImpl", targetNamespace = "http://endpoint/jaxws", wsdlLocation = "HelloImplService.wsdl")
@Stateless
public class HelloImpl implements Provider<Source> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int combo;
    private int bodyIndex;

    public jakarta.xml.bind.JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    private static jakarta.xml.bind.JAXBContext createJAXBContext() {
        try {
            return jakarta.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        } catch (jakarta.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    public Source invoke(Source request) {
        try {
            recvBean(request);
            return sendBean();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);
        }
    }

    private void recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        JAXBElement element = (JAXBElement) jaxbContext.createUnmarshaller().unmarshal(source);
        System.out.println("name=" + element.getName() + " value=" + element.getValue());
        if (element.getValue() instanceof SayHello) {
            SayHello hello = (SayHello) element.getValue();
            System.out.println("Say Hello from " + hello.getArg0());
        }

    }

    private Source sendBean() throws Exception {
        System.out.println("**** sendBean ******");
        SayHelloResponse resp = new SayHelloResponse();
        resp.setReturn("WebSvcTest-Hello");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectFactory factory = new ObjectFactory();
        jaxbContext.createMarshaller().marshal(factory.createSayHelloResponse(resp), bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }
}
