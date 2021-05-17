/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.jms.soaptojms;


/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public class JNDINames {

  private JNDINames() { } //Prevents instantiation

  // JNDI names of topic resources
  public static final String TOPIC_CONNECTION_FACTORY =
                   "jms/TopicConnectionFactory";

  public static final String TEST_MDB_TOPIC =
                         "jms/TestTopic";

}

