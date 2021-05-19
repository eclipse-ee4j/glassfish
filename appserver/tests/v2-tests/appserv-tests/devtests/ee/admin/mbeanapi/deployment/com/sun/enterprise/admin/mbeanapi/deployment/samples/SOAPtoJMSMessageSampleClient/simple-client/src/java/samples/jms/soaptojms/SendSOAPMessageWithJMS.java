/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.jms.soaptojms;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.Name;

import java.net.URL;
import javax.activation.DataHandler;

import com.sun.messaging.xml.MessageTransformer;
import jakarta.jms.TopicConnectionFactory;

import jakarta.jms.TopicConnection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Message;
import jakarta.jms.TopicSession;
import jakarta.jms.Topic;
import jakarta.jms.TopicPublisher;

import java.util.*;
import java.io.*;

/**
 * This example shows how to use the MessageTransformer utility to send SOAP
 * messages with JMS.
 * <p>
 * SOAP messages are constructed with jakarta.xml.soap API.  The messages
 * are converted with MessageTransformer utility to convert SOAP to JMS
 * message types.  The JMS messages are then published to the JMS topics.
 */
public class SendSOAPMessageWithJMS {

    TopicConnectionFactory tcf = null;
    TopicConnection tc = null;
    TopicSession session = null;
    Topic topic = null;

    TopicPublisher publisher = null;

    /**
     * default constructor.
     *
     * @param topicName a String that contains the name of a JMS Topic
     *
     */
    public SendSOAPMessageWithJMS(String topicName) {
        init(topicName);
    }

    /**
     * Initialize JMS Connection/Session/Topic and Publisher.
     *
     * @param topicName a String that contains the name of a JMS Topic
     *
     */
    public void init(String topicName) {
        try {
            ServiceLocator servicelocator = new ServiceLocator();
            tcf = servicelocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
            tc = tcf.createTopicConnection();
            session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = servicelocator.getTopic(topicName);
            publisher = session.createPublisher(topic);
        } catch (JMSException jmse) {
            jmse.printStackTrace();
        } catch(ServiceLocatorException se) {
            se.printStackTrace();
        }
    }

    /**
     * Send SOAP message with JMS API.
     */
    public void send () throws Exception {

        /**
         * Construct a default SOAP message factory.
         */
        MessageFactory mf = MessageFactory.newInstance();
        /**
         * Create a SOAP message object.
         */
        System.out.println ("Create a SOAP message");
        SOAPMessage soapMessage = mf.createMessage();
        /**
         * Get SOAP part.
         */
        SOAPPart soapPart = soapMessage.getSOAPPart();
        /**
         * Get SOAP envelope.
         */
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

        /**
         * Get SOAP body.
         */
        SOAPBody soapBody = soapEnvelope.getBody();
        /**
         * Create a name object. with name space http://www.sun.com/imq.
         */
        Name name = soapEnvelope.createName("HelloWorld", "hw", "http://www.sun.com/imq");
        /**
         * Add child element with the above name.
         */
        SOAPElement element = soapBody.addChildElement(name);

        /**
         * Add another child element.
         */
        element.addTextNode( "Welcome to Sun Web Services." );

        /**
         * Create an atachment with activation API.
         */

        URL url = getUrlFromPropsFile();
        System.out.println ("Attaching the file from URL: " + url);

        DataHandler dh = new DataHandler (url);
        AttachmentPart ap = soapMessage.createAttachmentPart(dh);
        /**
         * set content type/ID.
         */
        ap.setContentType("text/html");
        ap.setContentId("cid-001");

        /**
         *  add the attachment to the SOAP message.
         */
        soapMessage.addAttachmentPart(ap);
        soapMessage.saveChanges();

        /**
         * Convert SOAP to JMS message.
         */
        System.out.println ("Convert the message to JMS message");
        Message m = MessageTransformer.SOAPMessageIntoJMSMessage( soapMessage, session );

        /**
         * publish JMS message.
         */
        System.out.println ("Publish the message");
        publisher.publish( m );
    }


    /** Read server and port from soaptojms.properties file
     *
     */

    public URL getUrlFromPropsFile() throws Exception {
        InputStream props = SendSOAPMessageWithJMS.class.getResourceAsStream("soaptojms.properties");

        Properties P = new Properties();
        P.load(props);

        return (new URL(P.getProperty("url")));
    }


    /**
     * Close JMS connection.
     *
     * @exception JMSException
     */
    public void close() throws JMSException {
        tc.close();
    }

}
