/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ScheduledTimerDescriptor;
import com.sun.enterprise.deployment.node.DataSourceDefinitionNode;
import com.sun.enterprise.deployment.node.LifecycleCallbackNode;
import com.sun.enterprise.deployment.node.MailSessionNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.glassfish.ejb.deployment.descriptor.ConcurrentMethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbInitInfo;
import org.glassfish.ejb.deployment.descriptor.EjbRemovalInfo;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor.ConcurrencyManagementType;
import org.glassfish.ejb.deployment.descriptor.TimeoutValueDescriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import static org.glassfish.ejb.deployment.EjbTagNames.AFTER_BEGIN_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.AFTER_COMPLETION_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.AROUND_INVOKE_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.AROUND_TIMEOUT_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.ASYNC_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.BEFORE_COMPLETION_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.CONCURRENCY_MANAGEMENT_TYPE;
import static org.glassfish.ejb.deployment.EjbTagNames.CONCURRENT_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.DEPENDS_ON;
import static org.glassfish.ejb.deployment.EjbTagNames.INIT_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.INIT_ON_STARTUP;
import static org.glassfish.ejb.deployment.EjbTagNames.LOCAL_BEAN;
import static org.glassfish.ejb.deployment.EjbTagNames.PASSIVATION_CAPABLE;
import static org.glassfish.ejb.deployment.EjbTagNames.POST_ACTIVATE_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.PRE_PASSIVATE_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.REMOVE_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.SESSION_TYPE;
import static org.glassfish.ejb.deployment.EjbTagNames.STATEFUL_TIMEOUT;
import static org.glassfish.ejb.deployment.EjbTagNames.TIMEOUT_METHOD;
import static org.glassfish.ejb.deployment.EjbTagNames.TIMER;
import static org.glassfish.ejb.deployment.EjbTagNames.TRANSACTION_TYPE;

/**
 * This class handles all information pertinent to session beans
 *
 * @author Jerome Dochez
 */
public class EjbSessionNode extends InterfaceBasedEjbNode<EjbSessionDescriptor> {

    private EjbSessionDescriptor descriptor;

    private boolean inDependsOn;
    private List<String> dependsOn;

    public EjbSessionNode() {
        registerElementHandler(new XMLElement(AROUND_INVOKE_METHOD), AroundInvokeNode.class,
            "addAroundInvokeDescriptor");

        registerElementHandler(new XMLElement(AROUND_TIMEOUT_METHOD), AroundTimeoutNode.class,
            "addAroundTimeoutDescriptor");

        registerElementHandler(new XMLElement(TagNames.POST_CONSTRUCT), LifecycleCallbackNode.class,
            "addPostConstructDescriptor");

        registerElementHandler(new XMLElement(TagNames.PRE_DESTROY), LifecycleCallbackNode.class,
            "addPreDestroyDescriptor");

        registerElementHandler(new XMLElement(TagNames.DATA_SOURCE), DataSourceDefinitionNode.class,
            "addResourceDescriptor");

        registerElementHandler(new XMLElement(TagNames.MAIL_SESSION), MailSessionNode.class,
            "addResourceDescriptor");

        registerElementHandler(new XMLElement(POST_ACTIVATE_METHOD), LifecycleCallbackNode.class,
            "addPostActivateDescriptor");

        registerElementHandler(new XMLElement(PRE_PASSIVATE_METHOD), LifecycleCallbackNode.class,
            "addPrePassivateDescriptor");

        registerElementHandler(new XMLElement(TIMEOUT_METHOD), MethodNode.class, "setEjbTimeoutMethod");

        registerElementHandler(new XMLElement(INIT_METHOD), EjbInitNode.class, "addInitMethod");

        registerElementHandler(new XMLElement(REMOVE_METHOD), EjbRemoveNode.class, "addRemoveMethod");

        registerElementHandler(new XMLElement(STATEFUL_TIMEOUT), TimeoutValueNode.class,
            "addStatefulTimeoutDescriptor");

        registerElementHandler(new XMLElement(AFTER_BEGIN_METHOD), MethodNode.class,
            "addAfterBeginDescriptor");

        registerElementHandler(new XMLElement(BEFORE_COMPLETION_METHOD), MethodNode.class,
            "addBeforeCompletionDescriptor");

        registerElementHandler(new XMLElement(AFTER_COMPLETION_METHOD), MethodNode.class,
            "addAfterCompletionDescriptor");

        registerElementHandler(new XMLElement(ASYNC_METHOD), MethodNode.class,
            "addAsynchronousMethod");

        registerElementHandler(new XMLElement(CONCURRENT_METHOD), ConcurrentMethodNode.class,
            "addConcurrentMethodFromXml");
    }


    @Override
    public EjbSessionDescriptor getEjbDescriptor() {
        if (descriptor == null) {
            descriptor = new EjbSessionDescriptor();
            descriptor.setEjbBundleDescriptor((EjbBundleDescriptorImpl) getParentNode().getDescriptor());
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(SESSION_TYPE, "setSessionType");
        table.put(TRANSACTION_TYPE, "setTransactionType");
        table.put(INIT_ON_STARTUP, "setInitOnStartup");
        table.put(PASSIVATION_CAPABLE, "setPassivationCapable");
        return table;
    }


    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (LOCAL_BEAN.equals(element.getQName())) {
            descriptor.setLocalBean(true);
        } else if (DEPENDS_ON.equals(element.getQName())) {
            inDependsOn = true;
            dependsOn = new ArrayList<>();
        }
        super.startElement(element, attributes);
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (inDependsOn && TagNames.EJB_NAME.equals(element.getQName())) {
            dependsOn.add(value);
        } else if (CONCURRENCY_MANAGEMENT_TYPE.equals(element.getQName())) {
            descriptor.setConcurrencyManagementType(ConcurrencyManagementType.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public boolean endElement(XMLElement element) {
        if (DEPENDS_ON.equals(element.getQName())) {
            inDependsOn = false;
            descriptor.setDependsOn(dependsOn.toArray(new String[dependsOn.size()]));
            dependsOn = null;
        }
        return super.endElement(element);
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbSessionDescriptor ejbDesc) {
        Node ejbNode = super.writeDescriptor(parent, nodeName, ejbDesc);
        writeDisplayableComponentInfo(ejbNode, ejbDesc);
        writeCommonHeaderEjbDescriptor(ejbNode, ejbDesc);
        appendTextChild(ejbNode, SESSION_TYPE, ejbDesc.getSessionType());

        if (ejbDesc.hasStatefulTimeout()) {
            TimeoutValueNode timeoutValueNode = new TimeoutValueNode();
            TimeoutValueDescriptor timeoutDesc = new TimeoutValueDescriptor();
            timeoutDesc.setValue(ejbDesc.getStatefulTimeoutValue());
            timeoutDesc.setUnit(ejbDesc.getStatefulTimeoutUnit());
            timeoutValueNode.writeDescriptor(ejbNode, STATEFUL_TIMEOUT, timeoutDesc);
        }

        MethodNode methodNode = new MethodNode();
        if (ejbDesc.isTimedObject()) {
            if (ejbDesc.getEjbTimeoutMethod() != null) {
                methodNode.writeJavaMethodDescriptor(ejbNode, TIMEOUT_METHOD, ejbDesc.getEjbTimeoutMethod());
            }
            for (ScheduledTimerDescriptor timerDesc : ejbDesc.getScheduledTimerDescriptors()) {
                ScheduledTimerNode timerNode = new ScheduledTimerNode();
                timerNode.writeDescriptor(ejbNode, TIMER, timerDesc);
            }
        }

        if (ejbDesc.isSingleton()) {
            appendTextChild(ejbNode, INIT_ON_STARTUP, Boolean.toString(ejbDesc.getInitOnStartup()));
        }

        if (!ejbDesc.isStateless()) {
            appendTextChild(ejbNode, CONCURRENCY_MANAGEMENT_TYPE, ejbDesc.getConcurrencyManagementType().toString());
        }

        for (EjbSessionDescriptor.AccessTimeoutHolder next : ejbDesc.getAccessTimeoutInfo()) {
            ConcurrentMethodDescriptor cDesc = new ConcurrentMethodDescriptor();
            cDesc.setConcurrentMethod(next.method);
            TimeoutValueDescriptor timeoutDesc = new TimeoutValueDescriptor();
            timeoutDesc.setValue(next.value);
            timeoutDesc.setUnit(next.unit);
            cDesc.setAccessTimeout(timeoutDesc);

            ConcurrentMethodNode cNode = new ConcurrentMethodNode();
            cNode.writeDescriptor(ejbNode, CONCURRENT_METHOD, cDesc);
        }

        for (MethodDescriptor nextReadLock : ejbDesc.getReadLockMethods()) {
            ConcurrentMethodDescriptor cDesc = new ConcurrentMethodDescriptor();
            cDesc.setConcurrentMethod(nextReadLock);
            cDesc.setWriteLock(false);
            ConcurrentMethodNode cNode = new ConcurrentMethodNode();
            cNode.writeDescriptor(ejbNode, CONCURRENT_METHOD, cDesc);
        }

        if (ejbDesc.hasDependsOn()) {
            Node dependsOnNode = appendChild(ejbNode, DEPENDS_ON);
            for (String depend : ejbDesc.getDependsOn()) {
                appendTextChild(dependsOnNode, TagNames.EJB_NAME, depend);
            }
        }

        if (ejbDesc.hasInitMethods()) {
            EjbInitNode initNode = new EjbInitNode();
            for (EjbInitInfo next : ejbDesc.getInitMethods()) {
                initNode.writeDescriptor(ejbNode, INIT_METHOD, next);
            }
        }

        if (ejbDesc.hasRemoveMethods()) {
            EjbRemoveNode removeNode = new EjbRemoveNode();
            for (EjbRemovalInfo next : ejbDesc.getAllRemovalInfo()) {
                removeNode.writeDescriptor(ejbNode, REMOVE_METHOD, next);
            }
        }

        for (MethodDescriptor nextDesc : ejbDesc.getAsynchronousMethods()) {
            methodNode.writeDescriptor(ejbNode, ASYNC_METHOD, nextDesc, ejbDesc.getName());
        }

        appendTextChild(ejbNode, TRANSACTION_TYPE, ejbDesc.getTransactionType());

        MethodDescriptor afterBeginMethod = ejbDesc.getAfterBeginMethod();
        if (afterBeginMethod != null) {
            methodNode.writeJavaMethodDescriptor(ejbNode, AFTER_BEGIN_METHOD, afterBeginMethod);
        }

        MethodDescriptor beforeCompletionMethod = ejbDesc.getBeforeCompletionMethod();
        if (beforeCompletionMethod != null) {
            methodNode.writeJavaMethodDescriptor(ejbNode, BEFORE_COMPLETION_METHOD, beforeCompletionMethod);
        }

        MethodDescriptor afterCompletionMethod = ejbDesc.getAfterCompletionMethod();
        if (afterCompletionMethod != null) {
            methodNode.writeJavaMethodDescriptor(ejbNode, AFTER_COMPLETION_METHOD, afterCompletionMethod);
        }

        //around-invoke-method
        writeAroundInvokeDescriptors(ejbNode, ejbDesc.getAroundInvokeDescriptors().iterator());

        //around-timeout-method
        writeAroundTimeoutDescriptors(ejbNode, ejbDesc.getAroundTimeoutDescriptors().iterator());

        // env-entry*
        writeEnvEntryDescriptors(ejbNode, ejbDesc.getEnvironmentProperties().iterator());

        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(ejbNode, ejbDesc.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(ejbNode, ejbDesc.getServiceReferenceDescriptors().iterator());

        // resource-ref*
        writeResourceRefDescriptors(ejbNode, ejbDesc.getResourceReferenceDescriptors().iterator());

        // resource-env-ref*
        writeResourceEnvRefDescriptors(ejbNode, ejbDesc.getResourceEnvReferenceDescriptors().iterator());

        // message-destination-ref*
        writeMessageDestinationRefDescriptors(ejbNode, ejbDesc.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-context-ref*
        writeEntityManagerReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerReferenceDescriptors().iterator());

        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerFactoryReferenceDescriptors().iterator());

        // post-construct
        writeLifeCycleCallbackDescriptors(ejbNode, TagNames.POST_CONSTRUCT, ejbDesc.getPostConstructDescriptors());

        // pre-destroy
        writeLifeCycleCallbackDescriptors(ejbNode, TagNames.PRE_DESTROY, ejbDesc.getPreDestroyDescriptors());

        // all descriptors (includes DSD, MSD, JMSCFD, JMSDD,AOD, CFD)*
        writeResourceDescriptors(ejbNode, ejbDesc.getAllResourcesDescriptors().iterator());

        // post-activate-method
        writeLifeCycleCallbackDescriptors(ejbNode, POST_ACTIVATE_METHOD, ejbDesc.getPostActivateDescriptors());

        // pre-passivate-method
        writeLifeCycleCallbackDescriptors(ejbNode, PRE_PASSIVATE_METHOD, ejbDesc.getPrePassivateDescriptors());

        // security-role-ref*
        writeRoleReferenceDescriptors(ejbNode, ejbDesc.getRoleReferences().iterator());

        // security-identity
        writeSecurityIdentityDescriptor(ejbNode, ejbDesc);

        // passivation-capable
        if (ejbDesc.isStateful()) {
            appendTextChild(ejbNode, PASSIVATION_CAPABLE, Boolean.toString(ejbDesc.isPassivationCapable()));
        }

        return ejbNode;
    }
}
