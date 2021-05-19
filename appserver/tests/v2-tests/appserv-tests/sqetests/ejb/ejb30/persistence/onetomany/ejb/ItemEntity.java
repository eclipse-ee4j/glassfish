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

package pe.ejb.ejb30.persistence.toplinksample.ejb;

import jakarta.persistence.*;

@Entity
@Table(name = "CMP3_ITEM")
@NamedQuery(name = "findAllItemsByName", query =
    "SELECT OBJECT(item) FROM ItemEntity item WHERE item.name = ?1")
public class ItemEntity implements java.io.Serializable {

    private Integer itemId;
    private int version;
    private String name;
    private String description;

    public ItemEntity() { }

    public ItemEntity(int id, String name) {
        this.setItemId(new Integer(id));
        this.setName(name);
    }

    @Id
    @Column(name = "ITEM_ID")
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer id) {
        this.itemId = id;
    }

    @Version@Column(name = "ITEM_VERSION")
    protected int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }


    @Column(name="DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }


    @Column(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "ID: "+itemId+": name :"+name;
    }
}
