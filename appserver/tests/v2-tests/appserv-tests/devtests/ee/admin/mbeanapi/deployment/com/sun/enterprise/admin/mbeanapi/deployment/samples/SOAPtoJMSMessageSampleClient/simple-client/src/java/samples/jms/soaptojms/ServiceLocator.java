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


import java.net.URL;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import samples.jms.soaptojms.ServiceLocatorException;

/**
 *  This class is an implementation of the Service Locator pattern. It is
 *  used to looukup resources such as JMS Destinations, etc.
 */
public class ServiceLocator {

    private InitialContext ic;

    public ServiceLocator() throws ServiceLocatorException  {
      try {
        ic = new InitialContext();
      } catch (NamingException ne) {
            throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      }
    }


   /**
     * This method helps in obtaining the topic factory
     * @return the factory for the factory to get topic connections from
     */
    public  TopicConnectionFactory getTopicConnectionFactory(String topicConnFactoryName) throws ServiceLocatorException {
      TopicConnectionFactory factory = null;
      try {
        factory = (TopicConnectionFactory) ic.lookup(topicConnFactoryName);
      } catch (NamingException ne) {
          ne.printStackTrace();
          throw new ServiceLocatorException(ne);
      } catch (Exception e) {
          e.printStackTrace();
          throw new ServiceLocatorException(e);
      }
      return factory;
    }


    /**
     * This method obtains the topc itself for a caller
     * @return the Topic Destination to send messages to
     */
    public  Topic getTopic(String topicName) throws ServiceLocatorException {
      Topic topic = null;
      try {
          topic = (Topic)ic.lookup(topicName);
      } catch (NamingException ne) {
         throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      }
      return topic;
    }

}

