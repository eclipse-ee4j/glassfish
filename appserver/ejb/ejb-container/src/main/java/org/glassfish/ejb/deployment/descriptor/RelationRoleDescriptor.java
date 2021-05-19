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
 * This class contains information about one of the partners in
 * a relationship between EJB2.0 CMP EntityBeans.
 * It represents information in the <ejb-relation-role> XML element.
 *
 * @author Sanjeev Krishnan
 */
public final class RelationRoleDescriptor extends Descriptor {

    // Bean for owner (role-source) of this side of
    // the relationship
    private EjbCMPEntityDescriptor owner;
    private PersistenceDescriptor pers;
    private RelationshipDescriptor relnDesc;

    private String roleSourceDescription;
    private String cmrField; // CMR field name in owner's class
    private String cmrFieldDescription;
    private String cmrFieldType; // Java type of cmr-field
    private boolean isMany;
    private RelationRoleDescriptor partner;
    private boolean cascadeDelete;
    private CMRFieldInfo cmrFieldInfo;
    private String relationRoleName;

    public RelationRoleDescriptor() {
    }


    /**
     * May return null if the role-source for this relationship role
     * is a remote-ejb-name
     */
    public PersistenceDescriptor getPersistenceDescriptor() {
        return pers;
    }


    /**
     * Can be set to null if there is no associated persistence descriptor.
     */
    public void setPersistenceDescriptor(PersistenceDescriptor newPers) {
        if (this.pers != null) {
            // first invalidate cmr stuff in original persistence descriptor
            this.pers.invalidate();
        }

        this.pers = newPers;
        if (newPers != null) {
            this.owner = (EjbCMPEntityDescriptor) newPers.getParentDescriptor();
        }
        invalidateCMRFieldStuff();
    }


    private void invalidateCMRFieldStuff() {
        cmrFieldInfo = null;
        if (pers != null) {
            pers.invalidate();
        }
    }


    public RelationshipDescriptor getRelationshipDescriptor() {
        return relnDesc;
    }


    public void setRelationshipDescriptor(RelationshipDescriptor relnDesc) {
        this.relnDesc = relnDesc;
    }


    public void setOwner(EjbCMPEntityDescriptor owner) {
        this.owner = owner;
        invalidateCMRFieldStuff();
    }


    public EjbCMPEntityDescriptor getOwner() {
        return owner;
    }


    /**
     * The other role in the relationship I participate in.
     */
    public RelationRoleDescriptor getPartner() {
        return partner;
    }


    public void setPartner(RelationRoleDescriptor partner) {
        this.partner = partner;
    }


    public String getRelationRoleName() {
        return relationRoleName;
    }


    public void setRelationRoleName(String relationRoleName) {
        this.relationRoleName = relationRoleName;
    }


    public void setRoleSourceDescription(String roleSourceDescription) {
        this.roleSourceDescription = roleSourceDescription;
    }


    public String getRoleSourceDescription() {
        if (roleSourceDescription == null) {
            roleSourceDescription = "";
        }
        return roleSourceDescription;
    }


    /**
     * Set to NULL to indicate no cmr field
     */
    public void setCMRField(String cmrField) {
        this.cmrField = cmrField;
        invalidateCMRFieldStuff();
    }


    public String getCMRField() {
        return cmrField;
    }


    public void setCMRFieldDescription(String cmrFieldDescription) {
        this.cmrFieldDescription = cmrFieldDescription;
    }


    public String getCMRFieldDescription() {
        if (cmrFieldDescription == null) {
            cmrFieldDescription = "";
        }
        return cmrFieldDescription;
    }


    /**
     * Only applicable when partner is collection-valued.
     * Set to NULL to indicate no field type is not applicable.
     */
    public void setCMRFieldType(String newCmrFieldType) {
        if (newCmrFieldType == null) {
            this.cmrFieldType = null;
            invalidateCMRFieldStuff();
        } else if (newCmrFieldType.equals("java.util.Collection") || newCmrFieldType.equals("java.util.Set")) {
            this.cmrFieldType = newCmrFieldType;
            invalidateCMRFieldStuff();
        } else {
            throw new IllegalArgumentException(
                "cmr-field-type is " + newCmrFieldType + ", must be java.util.Collection or java.util.Set");
        }
    }


    public String getCMRFieldType() {
        return cmrFieldType;
    }


    public void setIsMany(boolean isMany) {
        this.isMany = isMany;
        invalidateCMRFieldStuff();
    }


    public boolean getIsMany() {
        return isMany;
    }


    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }


    public boolean getCascadeDelete() {
        return cascadeDelete;
    }


    public void setCMRFieldInfo(CMRFieldInfo cmrFieldInfo) {
        this.cmrFieldInfo = cmrFieldInfo;
    }


    public CMRFieldInfo getCMRFieldInfo() {
        if (cmrFieldInfo == null && pers != null) {
            pers.getCMRFieldInfo(); // tell pers to initialize its CMRFieldInfos
        }
        return cmrFieldInfo;
    }


    public String composeReverseCmrFieldName() {
        return "_" + getPartner().getOwner().getName() + "_" + getPartner().getCMRField();
    }
}

