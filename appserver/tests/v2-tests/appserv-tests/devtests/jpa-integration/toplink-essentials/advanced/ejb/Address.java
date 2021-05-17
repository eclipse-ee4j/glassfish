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



package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.io.Serializable;
import jakarta.persistence.*;
import static jakarta.persistence.GenerationType.*;
import java.util.*;
import static jakarta.persistence.CascadeType.*;

/**
 * <p><b>Purpose</b>: Represents the mailing address on an Employee
 * <p><b>Description</b>: Held in a private 1:1 relationship from Employee
 * @see Employee
 */
@Entity
@Table(name="CMP3_ADDRESS")
@NamedNativeQuery(
    name="findAllSQLAddresses",
    query="select * from CMP3_ADDRESS",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
)
@NamedQuery(
    name="findAllAddressesByPostalCode",
    query="SELECT OBJECT(address) FROM Address address WHERE address.postalCode = :postalcode"
)
public class Address implements Serializable {
    private Integer id;
    private String street;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private Collection<Employee> employees;

    public Address() {
        city = "";
        province = "";
        postalCode = "";
        street = "";
        country = "";
        this.employees = new Vector<Employee>();
    }

    public Address(String street, String city, String province, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.employees = new Vector<Employee>();
    }

    @Id
    @GeneratedValue(strategy=SEQUENCE, generator="ADDRESS_SEQUENCE_GENERATOR")
    @SequenceGenerator(name="ADDRESS_SEQUENCE_GENERATOR", sequenceName="ADDRESS_SEQ", allocationSize=25)
    @Column(name="ADDRESS_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Column(name="P_CODE")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @OneToMany(cascade=ALL, mappedBy="address")
    public Collection<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Collection<Employee> employees) {
        this.employees = employees;
    }
}
