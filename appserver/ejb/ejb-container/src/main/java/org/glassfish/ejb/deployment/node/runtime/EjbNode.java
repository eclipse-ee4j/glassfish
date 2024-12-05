/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.EjbRefNode;
import com.sun.enterprise.deployment.node.runtime.MessageDestinationRefNode;
import com.sun.enterprise.deployment.node.runtime.ResourceEnvRefNode;
import com.sun.enterprise.deployment.node.runtime.ResourceRefNode;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.node.runtime.ServiceRefNode;
import com.sun.enterprise.deployment.node.runtime.WebServiceEndpointRuntimeNode;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.Map;
import java.util.logging.Level;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.BeanCacheDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.CheckpointAtEndOfMethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.FlushAtEndOfMethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;
import org.glassfish.ejb.deployment.descriptor.runtime.MdbConnectionFactoryDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles all runtime information for ejbs
 *
 * @author Jerome Dochez
 */
public class EjbNode extends DeploymentDescriptorNode<EjbDescriptor> {

    private EjbDescriptor descriptor;
    private String availEnabled;

    public EjbNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.RESOURCE_REFERENCE), ResourceRefNode.class);
        registerElementHandler(new XMLElement(TagNames.EJB_REFERENCE), EjbRefNode.class);
        registerElementHandler(new XMLElement(TagNames.RESOURCE_ENV_REFERENCE), ResourceEnvRefNode.class);
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION_REFERENCE), MessageDestinationRefNode.class);
        registerElementHandler(new XMLElement(WebServicesTagNames.SERVICE_REF), ServiceRefNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.CMP), CmpNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.MDB_CONNECTION_FACTORY), MDBConnectionFactoryNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.IOR_CONFIG), IORConfigurationNode.class,
            "addIORConfigurationDescriptor");
        registerElementHandler(new XMLElement(RuntimeTagNames.BEAN_POOL), BeanPoolNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.BEAN_CACHE), BeanCacheNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.MDB_RESOURCE_ADAPTER), MDBResourceAdapterNode.class);
        registerElementHandler(new XMLElement(WebServicesTagNames.WEB_SERVICE_ENDPOINT),
            WebServiceEndpointRuntimeNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.FLUSH_AT_END_OF_METHOD), FlushAtEndOfMethodNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.CHECKPOINT_AT_END_OF_METHOD),
            CheckpointAtEndOfMethodNode.class);
    }


    @Override
    public EjbDescriptor getDescriptor() {
        return descriptor;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.EJB_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc != null) {
                if (parentDesc instanceof EjbBundleDescriptorImpl) {
                    descriptor = ((EjbBundleDescriptorImpl) parentDesc).getEjbByName(value);
                }
            }
            if (descriptor == null) {
                DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.ADD_DESCRIPTOR_FAILURE,
                    new Object[] {element, value});
            } else {
                if (availEnabled != null) {
                    descriptor.getIASEjbExtraDescriptors()
                        .setAttributeValue(IASEjbExtraDescriptors.AVAILABILITY_ENABLED, availEnabled);
                }
            }
            return;
        } else if (descriptor == null && !RuntimeTagNames.AVAILABILITY_ENABLED.equals(element.getQName())) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.ADD_DESCRIPTOR_FAILURE,
                new Object[] {element.getQName(), value});
        }
        // if this is the availability-enabled attribute, save the value
        // and set it later
        if (RuntimeTagNames.AVAILABILITY_ENABLED.equals(element.getQName())) {
            availEnabled = value;
        } else if (RuntimeTagNames.NAME.equals(element.getQName())) {
            // principal
            if (Boolean.FALSE.equals(descriptor.getUsesCallerIdentity())
                && descriptor.getRunAsIdentity() != null) {
                descriptor.getRunAsIdentity().setPrincipal(value);
            }
        } else if (RuntimeTagNames.PASS_BY_REFERENCE.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setPassByReference(Boolean.valueOf(value));
        } else if (RuntimeTagNames.JMS_MAX_MESSAGES_LOAD.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setJmsMaxMessagesLoad(Integer.parseInt(value));
        } else if (RuntimeTagNames.IS_READ_ONLY_BEAN.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setIsReadOnlyBean((Boolean.valueOf(value)).booleanValue());
        } else if (RuntimeTagNames.REFRESH_PERIOD_IN_SECONDS.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setRefreshPeriodInSeconds(Integer.parseInt(value));
        } else if (RuntimeTagNames.COMMIT_OPTION.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setCommitOption(value);
        } else if (RuntimeTagNames.CMT_TIMEOUT_IN_SECONDS.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setCmtTimeoutInSeconds(Integer.parseInt(value));
        } else if (RuntimeTagNames.USE_THREAD_POOL_ID.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setUseThreadPoolId(value);
        } else if (RuntimeTagNames.CHECKPOINTED_METHODS.equals(
            element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setCheckpointedMethods(
                value);
        } else if(RuntimeTagNames.PER_REQUEST_LOAD_BALANCING.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().setPerRequestLoadBalancing(Boolean.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();

        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");

        // gen-classes
        table.put(RuntimeTagNames.REMOTE_IMPL, "setEJBObjectImplClassName");
        table.put(RuntimeTagNames.LOCAL_IMPL, "setEJBLocalObjectImplClassName");
        table.put(RuntimeTagNames.REMOTE_HOME_IMPL, "setRemoteHomeImplClassName");
        table.put(RuntimeTagNames.LOCAL_HOME_IMPL, "setLocalHomeImplClassName");

        // for mdbs...
        table.put(RuntimeTagNames.DURABLE_SUBSCRIPTION, "setDurableSubscriptionName");
        table.put(RuntimeTagNames.MDB_CONNECTION_FACTORY, "setConnectionFactoryName");
        table.put(RuntimeTagNames.RESOURCE_ADAPTER_MID, "setResourceAdapterMid");
        return table;
    }


    @Override
    public boolean endElement(XMLElement element) {
        if(RuntimeTagNames.EJB.equals(element.getQName())) {
            descriptor.getIASEjbExtraDescriptors().parseCheckpointedMethods(descriptor);
        }
        return super.endElement(element);
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof MdbConnectionFactoryDescriptor) {
            descriptor.getIASEjbExtraDescriptors()
                .setMdbConnectionFactory((MdbConnectionFactoryDescriptor) newDescriptor);
        } else if (newDescriptor instanceof BeanPoolDescriptor) {
            descriptor.getIASEjbExtraDescriptors().setBeanPool((BeanPoolDescriptor) newDescriptor);
        } else if (newDescriptor instanceof BeanCacheDescriptor) {
            descriptor.getIASEjbExtraDescriptors().setBeanCache((BeanCacheDescriptor) newDescriptor);
        } else if (newDescriptor instanceof FlushAtEndOfMethodDescriptor) {
            descriptor.getIASEjbExtraDescriptors()
                .setFlushAtEndOfMethodDescriptor((FlushAtEndOfMethodDescriptor) newDescriptor);
        } else if (newDescriptor instanceof CheckpointAtEndOfMethodDescriptor) {
            descriptor.getIASEjbExtraDescriptors()
                .setCheckpointAtEndOfMethodDescriptor((CheckpointAtEndOfMethodDescriptor) newDescriptor);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbDescriptor ejbDescriptor) {
        Element ejbNode = (Element)super.writeDescriptor(parent, nodeName, ejbDescriptor);
        appendTextChild(ejbNode, RuntimeTagNames.EJB_NAME, ejbDescriptor.getName());
        appendTextChild(ejbNode, RuntimeTagNames.JNDI_NAME, ejbDescriptor.getJndiName());

        RuntimeDescriptorNode.writeCommonComponentInfo(ejbNode, ejbDescriptor);

        appendTextChild(ejbNode, RuntimeTagNames.PASS_BY_REFERENCE,
            String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().getPassByReference()));

        if (ejbDescriptor instanceof IASEjbCMPEntityDescriptor) {
            CmpNode cmpNode = new CmpNode();
            cmpNode.writeDescriptor(ejbNode, RuntimeTagNames.CMP, (IASEjbCMPEntityDescriptor) ejbDescriptor);
        }

        // principal
        if (Boolean.FALSE.equals(ejbDescriptor.getUsesCallerIdentity())) {
            RunAsIdentityDescriptor raid = ejbDescriptor.getRunAsIdentity();
            if ( raid != null && raid.getPrincipal() != null ) {
                Node principalNode = appendChild(ejbNode, RuntimeTagNames.PRINCIPAL);
                appendTextChild(principalNode, RuntimeTagNames.NAME,raid.getPrincipal());
            }
        }

        if (ejbDescriptor instanceof EjbMessageBeanDescriptor) {
            EjbMessageBeanDescriptor msgBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;

            // mdb-connection-factory?
            if (ejbDescriptor.getIASEjbExtraDescriptors().getMdbConnectionFactory()!=null) {
                MDBConnectionFactoryNode mcfNode = new MDBConnectionFactoryNode();
                mcfNode.writeDescriptor(ejbNode, RuntimeTagNames.MDB_CONNECTION_FACTORY,
                    ejbDescriptor.getIASEjbExtraDescriptors().getMdbConnectionFactory());
            }

            // jms-durable-subscription-name
            if (msgBeanDesc.hasDurableSubscription()) {
                appendTextChild(ejbNode, RuntimeTagNames.DURABLE_SUBSCRIPTION,
                    msgBeanDesc.getDurableSubscriptionName());
            }
            appendTextChild(ejbNode, RuntimeTagNames.JMS_MAX_MESSAGES_LOAD,
                String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().getJmsMaxMessagesLoad()));
        }

        // ior-configuration
        IORConfigurationNode iorNode = new IORConfigurationNode();
        for (EjbIORConfigurationDescriptor iorConf : ejbDescriptor.getIORConfigurationDescriptors()) {
            iorNode.writeDescriptor(ejbNode,RuntimeTagNames.IOR_CONFIG, iorConf);
        }

        appendTextChild(ejbNode, RuntimeTagNames.IS_READ_ONLY_BEAN,
            String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().isIsReadOnlyBean()));
        appendTextChild(ejbNode, RuntimeTagNames.REFRESH_PERIOD_IN_SECONDS,
            String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().getRefreshPeriodInSeconds()));
        appendTextChild(ejbNode, RuntimeTagNames.COMMIT_OPTION,
            ejbDescriptor.getIASEjbExtraDescriptors().getCommitOption());
        appendTextChild(ejbNode, RuntimeTagNames.CMT_TIMEOUT_IN_SECONDS,
            String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().getCmtTimeoutInSeconds()));
        appendTextChild(ejbNode, RuntimeTagNames.USE_THREAD_POOL_ID,
            ejbDescriptor.getIASEjbExtraDescriptors().getUseThreadPoolId());

        // gen-classes
        writeGenClasses(ejbNode, ejbDescriptor);

        // bean-pool
        BeanPoolDescriptor beanPoolDesc = ejbDescriptor.getIASEjbExtraDescriptors().getBeanPool();
        if (beanPoolDesc!=null) {
            BeanPoolNode bpNode = new BeanPoolNode();
            bpNode.writeDescriptor(ejbNode, RuntimeTagNames.BEAN_POOL, beanPoolDesc);
        }

        // bean-cache
        BeanCacheDescriptor beanCacheDesc = ejbDescriptor.getIASEjbExtraDescriptors().getBeanCache();
        if (beanCacheDesc!=null) {
            BeanCacheNode bcNode = new BeanCacheNode();
            bcNode.writeDescriptor(ejbNode, RuntimeTagNames.BEAN_CACHE, beanCacheDesc);
        }

        if (ejbDescriptor instanceof EjbMessageBeanDescriptor) {
            EjbMessageBeanDescriptor msgBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;
            if (msgBeanDesc.hasResourceAdapterMid()) {
                MDBResourceAdapterNode mdb = new MDBResourceAdapterNode();
                mdb.writeDescriptor(ejbNode, RuntimeTagNames.MDB_RESOURCE_ADAPTER, msgBeanDesc);
            }
        } else if( ejbDescriptor instanceof EjbSessionDescriptor ) {

            // web-services
            WebServiceEndpointRuntimeNode wsRuntime = new WebServiceEndpointRuntimeNode();
            wsRuntime.writeWebServiceEndpointInfo(ejbNode, ejbDescriptor);
        }

        // flush-at-end-of-method
        FlushAtEndOfMethodDescriptor flushMethodDesc = ejbDescriptor.getIASEjbExtraDescriptors()
            .getFlushAtEndOfMethodDescriptor();
        if (flushMethodDesc != null) {
            FlushAtEndOfMethodNode flushNode = new FlushAtEndOfMethodNode();
            flushNode.writeDescriptor(ejbNode, RuntimeTagNames.FLUSH_AT_END_OF_METHOD, flushMethodDesc);
        }

        // checkpointed-methods
        // checkpoint-at-end-of-method
        CheckpointAtEndOfMethodDescriptor checkpointMethodDesc = ejbDescriptor.getIASEjbExtraDescriptors()
            .getCheckpointAtEndOfMethodDescriptor();
        if (checkpointMethodDesc != null) {
            CheckpointAtEndOfMethodNode checkpointNode = new CheckpointAtEndOfMethodNode();
            checkpointNode.writeDescriptor(ejbNode, RuntimeTagNames.CHECKPOINT_AT_END_OF_METHOD, checkpointMethodDesc);
        }
        if (ejbDescriptor.getIASEjbExtraDescriptors().getPerRequestLoadBalancing() != null) {
            appendTextChild(ejbNode, RuntimeTagNames.PER_REQUEST_LOAD_BALANCING,
                String.valueOf(ejbDescriptor.getIASEjbExtraDescriptors().getPerRequestLoadBalancing()));
        }
        // availability-enabled
        setAttribute(ejbNode, RuntimeTagNames.AVAILABILITY_ENABLED,
            ejbDescriptor.getIASEjbExtraDescriptors().getValue(IASEjbExtraDescriptors.AVAILABILITY_ENABLED));

        return ejbNode;
    }


    /**
     * Write all the classes info generated at deployment
     *
     * @param parent node for the information
     * @param descriptor the descriptor containing the generated info
     */
    private void writeGenClasses(Node parent, EjbDescriptor ejbDescriptor) {
        // common to all ejbs but mdb...
        Node genClasses = appendChild(parent, RuntimeTagNames.GEN_CLASSES);
        appendTextChild(genClasses, RuntimeTagNames.REMOTE_IMPL, ejbDescriptor.getEJBObjectImplClassName());
        appendTextChild(genClasses, RuntimeTagNames.LOCAL_IMPL, ejbDescriptor.getEJBLocalObjectImplClassName());
        appendTextChild(genClasses, RuntimeTagNames.REMOTE_HOME_IMPL, ejbDescriptor.getRemoteHomeImplClassName());
        appendTextChild(genClasses, RuntimeTagNames.LOCAL_HOME_IMPL, ejbDescriptor.getLocalHomeImplClassName());
    }
}
