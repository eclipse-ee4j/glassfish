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

import java.util.*;
import java.io.Serializable;
import jakarta.persistence.*;
import static jakarta.persistence.GenerationType.*;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

/**
 * Bean class: EmployeeBean
 * Remote interface: Employee
 * Primary key class: EmployeePK
 * Home interface: EmployeeHome
 *
 * Employees have a one-to-many relationship with Employees through the
 * managedEmployees attribute.
 * Addresses exist in one-to-one relationships with Employees through the
 * address attribute.
 * Employees have a many-to-many relationship with Projects through the
 * projects attribute.
 *
 * Employee now has invalid annotation fields and data. This is done so that
 * we may test the XML/Annotation merging. Employee has been defined in the
 * XML, therefore, most annotations should not be processed. If they are, then
 * they will force an error, which means something is wrong with our merging.
 *
 * The invalid annotations that should not be processed have _INVALID
 * appended to some annotation field member. Others will not have this,
 * which means they should be processed (their mappings are not defined in the
 * XML)
 */
@Entity
@EntityListeners(oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeeListener.class)
@Table(name="CMP3_EMPLOYEE")
@SecondaryTable(name="CMP3_SALARY")
@PrimaryKeyJoinColumn(name="EMP_ID", referencedColumnName="EMP_ID")
@NamedQueries({
@NamedQuery(
    name="findAllEmployeesByFirstName",
    query="SELECT OBJECT(employee) FROM Employee employee WHERE employee.firstName = :firstname"
),
@NamedQuery(
    name="constuctEmployees",
    query="SELECT new oracle.toplink.essentials.testing.models.cmp3.advanced.Employee(employee.firstName, employee.lastName) FROM Employee employee"
),
@NamedQuery(
    name="findEmployeeByPK",
    query="SELECT OBJECT(employee) FROM Employee employee WHERE employee.id = :id"
)
}
)
public class Employee implements Serializable {
    private Integer id;
    private Integer version;
    private String firstName;
    private String lastName;
    private Address address;
    private Collection<PhoneNumber> phoneNumbers;
    private Collection<Project> projects;
    private int salary;
    private EmploymentPeriod period;
    private Collection<Employee> managedEmployees;
    private Employee manager;

    public Employee () {
        this.phoneNumbers = new Vector<PhoneNumber>();
        this.projects = new Vector<Project>();
        this.managedEmployees = new Vector<Employee>();
    }

    public Employee(String firstName, String lastName){
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="EMPLOYEE_TABLE_GENERATOR")
    @TableGenerator(
        name="EMPLOYEE_TABLE_GENERATOR",
        table="CMP3_EMPLOYEE_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="EMPLOYEE_SEQ"
    )
    @Column(name="EMP_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    @Column(name="VERSION")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Column(name="F_NAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    // Not defined in the XML, this should get processed.
    @Column(name="L_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    @ManyToOne(cascade=PERSIST, fetch=LAZY)
    @JoinColumn(name="ADDR_ID")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @OneToMany(cascade=ALL, mappedBy="owner")
    public Collection<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Collection<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @OneToMany(cascade=ALL, mappedBy="manager")
    public Collection<Employee> getManagedEmployees() {
        return managedEmployees;
    }

    public void setManagedEmployees(Collection<Employee> managedEmployees) {
        this.managedEmployees = managedEmployees;
    }

    // Not defined in the XML, this should get processed.
    @ManyToOne(cascade=PERSIST, fetch=LAZY)
    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    @ManyToMany(cascade=PERSIST)
    @JoinTable(
        name="CMP3_EMP_PROJ",
        // Default for the project side and specify for the employee side
        // Will test both defaulting and set values.
        joinColumns=@JoinColumn(name="EMPLOYEES_EMP_ID", referencedColumnName="EMP_ID")
        //inverseJoinColumns=@JoinColumn(name="PROJECTS_PROJ_ID", referencedColumnName="PROJ_ID")
    )
    public Collection<Project> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Project> projects) {
        this.projects = projects;
    }

    @Column(table="CMP3_SALARY")
    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="startDate", column=@Column(name="START_DATE", nullable=false)),
        @AttributeOverride(name="endDate", column=@Column(name="END_DATE", nullable=true))
    })
    public EmploymentPeriod getPeriod() {
        return period;
    }

    public void setPeriod(EmploymentPeriod period) {
        this.period = period;
    }

    public void addManagedEmployee(Employee emp) {
        getManagedEmployees().add(emp);
        emp.setManager(this);
    }

    public void addPhoneNumber(PhoneNumber phone) {
        phone.setOwner(this);
        getPhoneNumbers().add(phone);
    }

    public void addProject(Project theProject) {
        getProjects().add(theProject);
    }

    public void removeManagedEmployee(Employee emp) {
        getManagedEmployees().remove(emp);
    }

    public void removePhoneNumber(PhoneNumber phone) {
        // Note that getPhoneNumbers() will not have a phone number identical to
        // "phone", (because it's serialized) and this will take advantage of
        // equals() in PhoneNumber to remove properly
        getPhoneNumbers().remove(phone);
    }

    public void removeProject(Project theProject) {
        getProjects().remove(theProject);
    }

    public String toString() {
        return "Employee: " + getId();
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("Employee ").append(getId()).append(": ").append(getLastName()).append(", ").append(getFirstName()).append(getSalary());

        return sbuff.toString();
    }

    // These methods were added for testing purpose only - BUG 4349991

    // Static method should be ignored
    static public void getAbsolutelyNothing() {}

    // Get methods with parameters should be ignored
    public String getYourStringBack(String str) {
        return str;
    }

    // Get methods with no corresponding set method, should be ignored.
    // logs a warning though.
    public String getAnEmptyString() {
        return "";
    }
}
