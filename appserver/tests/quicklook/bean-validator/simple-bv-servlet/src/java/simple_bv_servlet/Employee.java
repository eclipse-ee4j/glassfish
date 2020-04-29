/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package simple_bv_servlet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class Employee implements Serializable {

  //Repeatable Annotation Usage
  @Max(60)
  @Max(50) //So, effectively the max value is 50
  @Min(20)
  @Min(25) //So, effective the min value is 25
  private int age;

  public void setAge(int age){
    this.age = age;
  }

  public int getAge(){
    return age;
  }

  @NotNull
  @Email //BV 2.0 annotation
  private String email;

  public void setEmail(String email){
    this.email = email;
  }

  public String getEmail(){
    return email;
  }

  @NotNull
  private String firstName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @NotNull
  private String lastName;

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  private List<String> listOfString;

  @NotNull
  /*Type_Use target support for all in-built constraint annotations: List<@NotNull String> */
  public List<@NotNull String > getListOfString() {
    return listOfString;
  }

  public void setListOfString(List<String> listOfString) {
    this.listOfString = listOfString;
  }


}
