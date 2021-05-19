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

import com.sun.enterprise.deployment.types.MessageDestinationReferencer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * Shared implementation for deployment descriptor entities that can refer
 * to a message destination.  Each MessageDestinationReferencer has an
 * owner.  The owner can either be a MessageDestinationReference
 * or a MessageDrivenBean.
 *
 * @author Kenneth Saks
 *
*/
public class MessageDestinationReferencerImpl implements
    MessageDestinationReferencer, Serializable {

    // holds the name of the message destination link.
    private String messageDestinationLinkName=null;

    // In case the reference has been resolved, this points to the
    // referenced message destination.
    private MessageDestinationDescriptor messageDestination=null;

    private MessageDestinationReferenceDescriptor ownerMsgDestRef = null;
    private EjbMessageBeanDescriptor ownerMsgBean = null;

    public MessageDestinationReferencerImpl(MessageDestinationReferencerImpl other) {
        //super(other);
        messageDestinationLinkName = other.messageDestinationLinkName; // immutable String
        messageDestination = other.messageDestination; // copy as-is
        ownerMsgDestRef = other.ownerMsgDestRef; // copy as-is
        ownerMsgBean = other.ownerMsgBean; // copy as-is
    }

    public MessageDestinationReferencerImpl(Descriptor desc) {
        if( desc instanceof MessageDestinationReferenceDescriptor ) {
            ownerMsgDestRef = (MessageDestinationReferenceDescriptor) desc;
        } else if( desc instanceof EjbMessageBeanDescriptor ) {
            ownerMsgBean = (EjbMessageBeanDescriptor) desc;
        } else {
            throw new IllegalArgumentException("Invalid desc = " + desc);
        }
    }

    /**
     * True if the owner is a message destination reference.
     */
    public boolean ownedByMessageDestinationRef() {
        return (ownerMsgDestRef != null);
    }

    /**
     * Get the descriptor for the message destination reference owner.
     */
    public MessageDestinationReferenceDescriptor getMessageDestinationRefOwner() {
        return ownerMsgDestRef;
    }

    /**
     * True if the owner is a message-driven bean.
     */
    public boolean ownedByMessageBean() {
        return (ownerMsgBean != null);
    }


    /**
     * Get the descriptor for the message-driven bean owner.
     */
    public EjbMessageBeanDescriptor getMessageBeanOwner() {
        return ownerMsgBean;
    }

    /**
     * True if this reference has been resolved to a valid MessageDestination
     * object.
     */
    public boolean isLinkedToMessageDestination() {
        return (messageDestination != null);
    }

    private BundleDescriptor getBundleDescriptor() {
        return ownedByMessageDestinationRef() ?
            ownerMsgDestRef.getReferringBundleDescriptor() :
            ownerMsgBean.getEjbBundleDescriptor();
    }

    /**
     * @return the link name of the message destination to which I refer
     * NOTE that this "link name" is potentially different from the actual
     * name of the target message destination, since the message destination
     * could be defined in a different module.
     */
    public String getMessageDestinationLinkName() {
        return messageDestinationLinkName;
    }

    /**
     * Sets the name of the message destination to which I refer.
     * NOTE : Does *NOT* attempt to resolve link name.  Use
     * alternate version of setMessageDestinationLinkName or resolveLink
     * if link resolution is required.
     */
    public void setMessageDestinationLinkName(String linkName) {
        setMessageDestinationLinkName(linkName, false);
    }


    /**
     * Sets the name of the message destination to which I refer.
     * @param resolve if true,  *try* to resolve link to the target message
     * destination.
     *
     * @return MessageDestination to which link was resolved, or null if
     * link name resolution failed.
     */
    public MessageDestinationDescriptor setMessageDestinationLinkName(String linkName, boolean resolve) {
        messageDestinationLinkName = linkName;
        MessageDestinationDescriptor msgDest = null;

        if (resolve) {
            msgDest = resolveLinkName();
        }
        return msgDest;
    }

    /**
     * Try to resolve the current link name value to a MessageDestination
     * object.
     *
     * @return MessageDestination to which link was resolved, or null if
     * link name resolution failed.
     */
    public MessageDestinationDescriptor resolveLinkName() {
        MessageDestinationDescriptor msgDest = null;

        String linkName = messageDestinationLinkName;

        if( (linkName != null) && (linkName.length() > 0) ) {
            int hashIndex = linkName.indexOf('#');

            BundleDescriptor bundleDescriptor = getBundleDescriptor();
            Application app = bundleDescriptor.getApplication();
            if (app == null && bundleDescriptor instanceof Application) {
                app = (Application)bundleDescriptor;
            }
            BundleDescriptor targetBundle = null;
            String msgDestName = linkName;

            if (app != null) {

                // explicit reference to another module
                if (hashIndex != -1) {
                    String relativeModuleUri = linkName.substring(0, hashIndex);
                    msgDestName = linkName.substring(hashIndex + 1);
                    targetBundle = app.getRelativeBundle(bundleDescriptor, relativeModuleUri);
                } else {
                    // Default is to find message destination within this
                    // module.  If it's not there, try searching the other
                    // modules.  NOTE that it's up to the deployer to ensure
                    // that any message destinations that are referred to
                    // from outside the defining module without an explicit
                    // reference have names that are unique within all the
                    // message destinations in the .ear.  There is no
                    // required search ordering.
                    if (!bundleDescriptor.hasMessageDestinationByName(msgDestName)) {
                        Set modules = app.getBundleDescriptors();
                        for (Iterator iter = modules.iterator(); iter.hasNext();) {
                            BundleDescriptor next = (BundleDescriptor) iter.next();
                            if (next.hasMessageDestinationByName(msgDestName)) {
                                targetBundle = next;
                                break;
                            }
                        }

                        // also look in extension bundle descriptors
                        // for ejb in war case
                        if (targetBundle == null) {
                            Collection<RootDeploymentDescriptor> extensionBundles = bundleDescriptor.getExtensionsDescriptors();
                            for(Iterator<RootDeploymentDescriptor> itr = extensionBundles.iterator(); itr.hasNext();) {
                                RootDeploymentDescriptor next = itr.next();
                                if (next instanceof BundleDescriptor) {
                                    if (((BundleDescriptor)next).hasMessageDestinationByName(msgDestName) ) {
                                        targetBundle = (BundleDescriptor)next;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        targetBundle = bundleDescriptor;
                    }
                }
            }
            try {
                if (targetBundle != null) {
                    msgDest = targetBundle.getMessageDestinationByName(msgDestName);
                }
            } catch (IllegalArgumentException iae) {
            }
        }
        if (msgDest != null) {
            setMessageDestination(msgDest);
        }

        return msgDest;
    }

    /**
     * @return the message destination to which I refer. Can be NULL.
    */
    public MessageDestinationDescriptor getMessageDestination() {
        return messageDestination;
    }

    /**
     * @param newMsgDest the message destination to which I refer.
     */
    public void setMessageDestination(MessageDestinationDescriptor newMsgDest) {
        if (messageDestination != null) {
            messageDestination.removeReferencer(this);
        }
        if (newMsgDest != null) {
            newMsgDest.addReferencer(this);

            // Keep message destination link name in synch with message
            // destination object.
            BundleDescriptor bundleDescriptor = getBundleDescriptor();
            BundleDescriptor targetBundleDescriptor = newMsgDest.getBundleDescriptor();
            String linkName = newMsgDest.getName();
            if (bundleDescriptor != targetBundleDescriptor) {
                Application app = bundleDescriptor.getApplication();
                String relativeUri = app.getRelativeUri(bundleDescriptor, targetBundleDescriptor);
                linkName = relativeUri + "#" + linkName;
            }
            messageDestinationLinkName = linkName;
        }
        messageDestination = newMsgDest;
    }

}
