/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.util;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import com.sun.enterprise.deployment.util.TracerVisitor;

import java.lang.System.Logger;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;

import static java.lang.System.Logger.Level.INFO;

public class EjbBundleTracerVisitor extends TracerVisitor implements EjbBundleVisitor {

    private static final Logger LOG = System.getLogger(EjbBundleTracerVisitor.class.getName());

    @Override
    public void accept(BundleDescriptor bundle) {
        LOG.log(INFO, "accept(bundle.name={0})", bundle.getName());
        if (bundle instanceof EjbBundleDescriptorImpl) {
            EjbBundleDescriptorImpl ejbBundle = (EjbBundleDescriptorImpl) bundle;
            accept(ejbBundle);

            for (EjbDescriptor anEjb : ejbBundle.getEjbs()) {
                anEjb.visit(getSubDescriptorVisitor(anEjb));
            }
            if (ejbBundle.hasRelationships()) {
                for (RelationshipDescriptor rd : ejbBundle.getRelationships()) {
                    accept(rd);
                }
            }
            for (WebService ws : ejbBundle.getWebServices().getWebServices()) {
                accept(ws);
            }
        }
        super.accept(bundle);
    }

    @Override
    public void accept(com.sun.enterprise.deployment.EjbBundleDescriptor bundle) {
        LOG.log(INFO, "accept(bundle.name={0})", bundle.getName());
    }

    protected void accept(EjbDescriptor ejb) {
        LOG.log(INFO, "accept ejb:\n{0}", ejb);

        for (EjbReference aRef : ejb.getEjbReferenceDescriptors()) {
            accept(aRef);
        }

        for (MethodPermission mp : ejb.getPermissionedMethodsByPermission().keySet()) {
            Set<MethodDescriptor> methods = ejb.getPermissionedMethodsByPermission().get(mp);
            accept(mp, methods);
        }

        {
            Map<MethodPermission, Set<MethodDescriptor>> methodPerms = ejb.getStyledPermissionedMethodsByPermission();
            if (methodPerms != null) {
                for (Entry<MethodPermission, Set<MethodDescriptor>> mp : methodPerms.entrySet()) {
                    accept(mp.getKey(), mp.getValue());
                }
            }
        }

        for (RoleReference roleRef : ejb.getRoleReferences()) {
            accept(roleRef);
        }

        for (Entry<MethodDescriptor, ContainerTransaction> md : ejb.getMethodContainerTransactions().entrySet()) {
            accept(md.getKey(), md.getValue());
        }

        for (EnvironmentProperty envProp : ejb.getEnvironmentProperties()) {
            accept(envProp);
        }

        for (ResourceReferenceDescriptor next : ejb.getResourceReferenceDescriptors()) {
            accept(next);
        }

        for (ResourceEnvReferenceDescriptor next : ejb.getResourceEnvReferenceDescriptors()) {
            accept(next);
        }

        for (MessageDestinationReferencer next : ejb.getMessageDestinationReferenceDescriptors()) {
            accept(next);
        }

        // If this is a message bean, it can be a message destination
        // referencer as well.
        if (EjbMessageBeanDescriptor.TYPE.equals(ejb.getType())) {
            MessageDestinationReferencer msgDestReferencer = (MessageDestinationReferencer) ejb;
            if (msgDestReferencer.getMessageDestinationLinkName() != null) {
                accept(msgDestReferencer);
            }
        }

        for (ServiceReferenceDescriptor sref : ejb.getServiceReferenceDescriptors()) {
            accept(sref);
        }

        if (ejb instanceof EjbCMPEntityDescriptor) {
            EjbCMPEntityDescriptor cmp = (EjbCMPEntityDescriptor) ejb;
            PersistenceDescriptor persistenceDesc = cmp.getPersistenceDescriptor();
            for (FieldDescriptor field : persistenceDesc.getCMPFields()) {
                accept(field);
            }
            for (MethodDescriptor method : persistenceDesc.getQueriedMethods()) {
                QueryDescriptor qd = persistenceDesc.getQueryFor(method);
                accept(qd);
            }
        }
    }

    protected void accept(MethodPermission methodPermission, Collection<MethodDescriptor> methodDescriptors) {
        LOG.log(INFO, "accept(methodPermission={0}, methods)", methodPermission);
    }

    protected void accept(RoleReference reference) {
        LOG.log(INFO, "accept(reference={0})", reference);
    }

    protected void accept(MethodDescriptor method, ContainerTransaction transaction) {
        LOG.log(INFO, "accept(method={0}, transaction={1})", method, transaction);
    }

    protected void accept(FieldDescriptor field) {
        LOG.log(INFO, "accept(field={0})", field);
    }

    protected void accept(QueryDescriptor query) {
        LOG.log(INFO, "accept(query={0})", query);
    }

    protected void accept(RelationshipDescriptor rd) {
        LOG.log(INFO, () -> "accept relationship:\n"
            + "From EJB " + rd.getSource().getName() + " cmr field: "
            + rd.getSource().getCMRField() + "("
            + rd.getSource().getCMRFieldType() + ")  to EJB "
            + rd.getSink().getName() + " isMany "
            + rd.getSource().getIsMany() + " cascade-delete "
            + rd.getSource().getCascadeDelete() + "\n"
            + "To  EJB " + rd.getSink().getName() + " isMany "
            + rd.getSink().getIsMany() + " cascade-delete "
            + rd.getSink().getCascadeDelete() + "\n"
            + "Bidirectional cmr field: " + (rd.getIsBidirectional() ? rd.getSink().getCMRField()
                + "(" + rd.getSink().getCMRFieldType() + ")" : "false"));
    }
}
