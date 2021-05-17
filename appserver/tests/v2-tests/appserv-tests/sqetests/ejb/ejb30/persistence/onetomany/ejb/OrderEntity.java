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

@Entity(name="OrderBean")
@Table(name="CMP3_ORDER")
@NamedQuery(
    name="findAllOrdersByItem",
    query="SELECT OBJECT(theorder) FROM OrderBean theorder WHERE theorder.item.itemId = :id"
)
public class OrderEntity implements java.io.Serializable {

    private Integer orderId;
    private int version;
    private ItemEntity item;
    private int quantity;
    private String shippingAddress;
    private CustomerEntity customer;

    public OrderEntity(){}

    public OrderEntity(int id,int qty) {
        this.setOrderId(new Integer(id));
        this.setQuantity(qty);
    }

    @Id
    @Column(name="ORDER_ID")
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer id) {
        this.orderId = id;
    }

    @Version
    @Column(name="ORDER_VERSION")
    protected int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="ITEM_ID", referencedColumnName="ITEM_ID")
    public ItemEntity getItem() {
        return item;
    }

    public void setItem(ItemEntity item) {
        this.item = item;
    }


    @Column(name="QTY")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Column(name="SHIPADD")
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @ManyToOne()
    @JoinColumn(name="CUST_ID")
    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public String toString(){
        return "ID: "+orderId+": qty :"+quantity;
    }
}
