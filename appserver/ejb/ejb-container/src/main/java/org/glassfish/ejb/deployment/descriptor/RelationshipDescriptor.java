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

package org.glassfish.ejb.deployment.descriptor;

import org.glassfish.deployment.common.Descriptor;


/**
 * This class contains information about relationships between
 * EJB2.0 CMP EntityBeans.
 * It represents information in the <ejb-relation> XML element.
 *
 * @author Sanjeev Krishnan
 */
public final class RelationshipDescriptor extends Descriptor {

    private RelationRoleDescriptor source; // descriptor for source role
    private RelationRoleDescriptor sink; // descriptor for sink role

    private boolean isBidirectional = true;

    public boolean isOneOne() {
        return (!source.getIsMany() && !sink.getIsMany());
    }


    public boolean isOneMany() {
        return (!source.getIsMany() && sink.getIsMany());
    }


    public boolean isManyOne() {
        return (source.getIsMany() && !sink.getIsMany());
    }


    public boolean isManyMany() {
        return (source.getIsMany() && sink.getIsMany());
    }


    /**
     * Checks whether an EjbCMPEntityDescriptor
     * is a participant in this relationship.
     */
    public boolean hasParticipant(Descriptor desc) {
        return ((source.getOwner() == desc) || (sink.getOwner() == desc));
    }


    public RelationRoleDescriptor getSource() {
        return source;
    }


    public void setSource(RelationRoleDescriptor source) {
        this.source = source;
    }


    public void setSink(RelationRoleDescriptor sink) {
        this.sink = sink;
    }


    public RelationRoleDescriptor getSink() {
        return sink;
    }


    public void setIsBidirectional(boolean isBidirectional) {
        this.isBidirectional = isBidirectional;
    }


    public boolean getIsBidirectional() {
        return isBidirectional;
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("From EJB ").append(getSource().getName()
           ).append(" cmr field : ").append(getSource().getCMRField()
           ).append("(").append(getSource().getCMRFieldType()).append(")  to EJB ").append(getSink().getName()
           ).append(" isMany ").append(getSource().getIsMany()
           ).append(" cascade-delete ").append(getSource().getCascadeDelete());
    }
}
