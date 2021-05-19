/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package myfilter;

import java.io.*;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.servlet.*;
import javax.naming.*;
import jakarta.annotation.*;

public class MyFilter implements Filter {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage("Hello Filter");
            producer.send(queue, msg);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
