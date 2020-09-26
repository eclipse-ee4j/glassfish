/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package dataregistry;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import jakarta.ejb.*;


public abstract class PartBean implements EntityBean {

    private EntityContext context;


    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context=aContext;
    }


    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {

    }


    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }


    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {

    }


    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }


    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {

    }


    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {

    }

    public abstract String getPartNumber();
    public abstract void setPartNumber(String partNumber);

    public abstract int getRevision();
    public abstract void setRevision(int revision);

    public abstract String getDescription();
    public abstract void setDescription(String description);

    public abstract Date getRevisionDate();
    public abstract void setRevisionDate(Date revisionDate);

    public abstract Serializable getDrawing();
    public abstract void setDrawing(Serializable drawing);

    public abstract String getSpecification();
    public abstract void setSpecification(String specification);

    public abstract LocalPart getBomPart();
    public abstract void setBomPart(LocalPart bomPart);

    public abstract Collection getParts();
    public abstract void setParts(Collection parts);

    public abstract LocalVendorPart getVendorPart();
    public abstract void setVendorPart(LocalVendorPart vendorPart);

    public PartKey ejbCreate(String partNumber, int revision, String description,
            Date revisionDate, String specification, Serializable drawing)
            throws CreateException {

        setPartNumber(partNumber);
        setRevision(revision);
        setDescription(description);
        setRevisionDate(revisionDate);
        setSpecification(specification);
        setDrawing(drawing);

        return null;
    }

    public void ejbPostCreate(String partNumber, int revision, String description,
            Date revisionDate, String specification, Serializable drawing)
            throws CreateException {
    }

}
