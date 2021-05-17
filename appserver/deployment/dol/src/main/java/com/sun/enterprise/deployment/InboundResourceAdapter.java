/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import java.util.Set;

/**
 * This class argueably could be rolled up to the
 * ConnectorDescriptor class.  However, it is easier
 * to keep track of the changes in inbound ra element
 * as well as encapsulate the concept of inbound ra.
 *
 * <!ELEMENT inbound-resourceadapter (messageadapter?)>
 * <!ELEMENT messageadapter (messagelistener+)>
 *
 * @author    Qingqing Ouyang
 */
public class InboundResourceAdapter extends Descriptor
{
    private Set messageListeners;

    public InboundResourceAdapter ()
    {
        messageListeners = new OrderedSet();
    }

    public Set
    getMessageListeners()
    {
        return messageListeners;
    }

    public void
    addMessageListener (MessageListener listener)
    {
        messageListeners.add(listener);
    }

    public void
    removeMessageListener (MessageListener listener)
    {
    messageListeners.remove(listener);
    }

    public boolean hasMessageListenerType(String msgListenerType){
        for(Object messageListenerObject : messageListeners){
            MessageListener ml = (MessageListener) messageListenerObject;
            if(ml.getMessageListenerType().equals(msgListenerType)){
                return true;
            }
        }
        return false;
    }

    public MessageListener getMessageListener(String msgListenerType){
        for(Object messageListenerObject : messageListeners){
            MessageListener ml = (MessageListener) messageListenerObject;
            if(ml.getMessageListenerType().equals(msgListenerType)){
                return ml;
            }
        }
        return null;
    }
}
