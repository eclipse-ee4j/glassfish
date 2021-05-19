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
import jakarta.jms.JMSException;
import jakarta.jms.TopicSubscriber;

import java.util.Iterator;

/**
 * This sample program shows a JMS message listener can use the MessageTransformer
 * utility to convert JMS messages back to SOAP messages.
 */
public class SOAPMessageWithJMSClient {

    /**
     * The main program to send SOAP messages with JMS and ReceiveSOAPMessageWithJMS.
     */
    public static void main (String[] args) {

        String topicName = JNDINames.TEST_MDB_TOPIC;
        String usage = "\nUsage: enter parameter Send or Receive (followed by optional Topic name). \n"+
                       "To Receive message: \"appclient -client SOAPtoJMSMessageSampleClient.jar Receive\" \n"+
                       "To Send message: \"appclient -client SOAPtoJMSMessageSampleClient.jar Send\"";

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Receive")){
                if (args.length > 1) {
                    topicName = args[1];
                }
                try {
                    ReceiveSOAPMessageWithJMS rsm = new ReceiveSOAPMessageWithJMS(topicName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("Send")){
                if (args.length > 1) {
                    topicName = args[1];
                }
                try {
                    SendSOAPMessageWithJMS ssm = new SendSOAPMessageWithJMS(topicName);
                    ssm.send();
                    ssm.close();
                    System.out.println("Finished");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
              System.out.println(usage);
            }
        } else {
            System.out.println(usage);
        }

    }
}
