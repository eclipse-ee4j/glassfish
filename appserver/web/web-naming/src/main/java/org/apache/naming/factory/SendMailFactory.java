/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997-2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.naming.factory;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimePartDataSource;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Factory class that creates a JNDI named Jakarta Mail MimePartDataSource
 * object which can be used for sending email using SMTP.
 * <p>
 * Can be configured in the DefaultContext or Context scope
 * of your server.xml configuration file.
 * <p>
 * Example:
 * <p>
 * <pre>
 * &lt;Resource name="mail/send" auth="CONTAINER"
 *           type="jakarta.mail.internet.MimePartDataSource"/>
 * &lt;ResourceParams name="mail/send">
 *   &lt;parameter>&lt;name>factory&lt;/name>
 *     &lt;value>org.apache.naming.factory.SendMailFactory&lt;/value>
 *   &lt;/parameter>
 *   &lt;parameter>&lt;name>mail.smtp.host&lt;/name>
 *     &lt;value>your.smtp.host&lt;/value>
 *   &lt;/parameter>
 *   &lt;parameter>&lt;name>mail.smtp.user&lt;/name>
 *     &lt;value>someuser&lt;/value>
 *   &lt;/parameter>
 *   &lt;parameter>&lt;name>mail.from&lt;/name>
 *     &lt;value>someuser@some.host&lt;/value>
 *   &lt;/parameter>
 *   &lt;parameter>&lt;name>mail.smtp.sendpartial&lt;/name>
 *     &lt;value>true&lt;/value>
 *   &lt;/parameter>
 *  &lt;parameter>&lt;name>mail.smtp.dsn.notify&lt;/name>
 *     &lt;value>FAILURE&lt;/value>
 *   &lt;/parameter>
 *   &lt;parameter>&lt;name>mail.smtp.dsn.ret&lt;/name>
 *     &lt;value>FULL&lt;/value>
 *   &lt;/parameter>
 * &lt;/ResourceParams>
 * </pre>
 *
 * @author Glenn Nielsen Rich Catlett
 */

public class SendMailFactory implements ObjectFactory {

    // The class name for the javamail MimeMessageDataSource
    protected static final String DataSourceClassName = "jakarta.mail.internet.MimePartDataSource";

    @Override
    public Object getObjectInstance(Object RefObj, Name name, Context Ctx, Hashtable<?, ?> Env) throws Exception {
        final Reference reference = (Reference) RefObj;

        if (!reference.getClassName().equals(DataSourceClassName)) {
            // We can't create an instance of the DataSource
            return null;
        }

        // Set up the smtp session that will send the message
        Properties props = new Properties();

        // Enumeration of all refaddr
        Enumeration<RefAddr> list = reference.getAll();

        // Set transport to smtp
        props.put("mail.transport.protocol", "smtp");

        while (list.hasMoreElements()) {
            RefAddr refaddr = list.nextElement();

            // Set property
            props.put(refaddr.getType(), refaddr.getContent());
        }

        try {
            MimeMessage message = new MimeMessage(Session.getInstance(props));
            String from = (String) reference.get("mail.from").getContent();
            message.setFrom(new InternetAddress(from));
            message.setSubject("");

            return new MimePartDataSource(message);
        } catch (Exception e) {
            return null;
        }
    }
}
