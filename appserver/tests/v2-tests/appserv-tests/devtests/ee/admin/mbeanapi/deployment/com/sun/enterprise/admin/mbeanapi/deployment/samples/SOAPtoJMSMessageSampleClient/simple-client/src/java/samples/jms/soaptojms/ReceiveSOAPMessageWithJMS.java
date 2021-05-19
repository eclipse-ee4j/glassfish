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

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.AttachmentPart;

import com.sun.messaging.xml.MessageTransformer;
import jakarta.jms.TopicConnectionFactory;

import jakarta.jms.MessageListener;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicSession;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicSubscriber;

import java.util.Iterator;

/**
 * This sample program shows a JMS message listener can use the MessageTransformer
 * utility to convert JMS messages back to SOAP messages.
 */
public class ReceiveSOAPMessageWithJMS implements MessageListener {

    TopicConnectionFactory tcf = null;
    TopicConnection tc = null;
    TopicSession session = null;
    Topic topic = null;
    TopicSubscriber subscriber = null;

    MessageFactory messageFactory = null;

    /**
     * Default constructor.
     *
     * @param topicName  a String that contains the name of a JMS Topic
     *
     */
    public ReceiveSOAPMessageWithJMS(String topicName) {
        init(topicName);
    }

    /**
     * JMS Connection/Session/Destination/MessageListener set ups.
     *
     * @param topicName a String that contains the name of a JMS Topic
     */
    public void init(String topicName) {
        try {

            /**
             * construct a default SOAP message factory.
             */
            messageFactory = MessageFactory.newInstance();

            /**
             * JMS set up.
             */
            ServiceLocator servicelocator = new ServiceLocator();
            tcf = servicelocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
            tc = tcf.createTopicConnection();
            session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = servicelocator.getTopic(topicName);
            subscriber = session.createSubscriber(topic);
            subscriber.setMessageListener( this );
            tc.start();

            System.out.println ("Ready to receive SOAP messages ...");
            Thread.currentThread().join();
        } catch (Exception jmse) {
            jmse.printStackTrace();
        }
    }


    /**
     * JMS Messages are delivered to this method. The body of the message
     * contains SOAP streams.
     *
     * 1.  The message conversion utility converts JMS message to SOAP
     * message type.
     * 2.  Get the attachment parts and print content information to the
     * standard output stream.
     *
     * @param message a delivered message
     *
     */
    public void onMessage (Message message) {

        try {

            /**
             * convert JMS to SOAP message.
             */
            System.out.println("Message received! Converting the JMS message to SOAP message");

            SOAPMessage soapMessage =
            MessageTransformer.SOAPMessageFromJMSMessage( message, messageFactory );

            /**
             * Print attachment counts.
             */
            System.out.println("Attachment counts: " + soapMessage.countAttachments());

            /**
             * Get attachment parts of the SOAP message.
             */
            Iterator iterator = soapMessage.getAttachments();
            while ( iterator.hasNext() ) {
                /**
                 * Get next attachment.
                 */
                AttachmentPart ap = (AttachmentPart) iterator.next();
                /**
                 * Get content type.
                 */
                String contentType = ap.getContentType();
                System.out.println("Content type: " + contentType);
                /**
                 * Get content Id.
                 */
                String contentId = ap.getContentId();
                System.out.println("Content Id: " + contentId);

                /**
                 * Check if this is a Text attachment.
                 */
                if ( contentType.indexOf("text") >=0 ) {
                    /**
                     * Get and print the string content if it is a text
                     * attachment.
                     */
                    String content = (String) ap.getContent();
                    System.out.println("Attachment content:\n\n" + content);
                    System.out.println("*** attachment content ends ***");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

}
