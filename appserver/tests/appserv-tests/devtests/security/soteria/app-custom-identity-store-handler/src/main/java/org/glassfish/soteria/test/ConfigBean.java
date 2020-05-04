/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;

import javax.security.enterprise.identitystore.IdentityStore.ValidationType;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope;
import static javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope.ONE_LEVEL;
import static javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE;

import javax.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@RequestScoped
@Named
public class ConfigBean {
  private int priority300=300;
  private int priority100=100;
  private ValidationType[] useforBoth = {ValidationType.VALIDATE, ValidationType.PROVIDE_GROUPS};
  private ValidationType[] useforValidate = {ValidationType.VALIDATE};
  private ValidationType[] useforProvideGroup = {ValidationType.PROVIDE_GROUPS};
  private LdapSearchScope searchScopeOneLevel = ONE_LEVEL;
  private LdapSearchScope searchScopeSubTree = SUBTREE;

  public int getPriority300(){
    return priority300;
  }

  public int getPriority100(){
    return priority100;
  }

  public ValidationType[] getUseforBoth(){
    return useforBoth;
  }

  public ValidationType[] getUseforValidate(){
    return useforValidate;
  }

  public ValidationType[] getUseforProvideGroup(){
    return useforProvideGroup;
  }

  public LdapSearchScope getSearchScopeOneLevel(){
    return ONE_LEVEL;
  }

  public LdapSearchScope getSearchScopeSubTree(){
    return SUBTREE;
  }
}
