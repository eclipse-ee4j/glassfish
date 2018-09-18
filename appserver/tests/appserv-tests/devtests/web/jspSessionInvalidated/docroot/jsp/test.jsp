<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%><%

    Object obj;
    
    // Invalidate the session to make sure this does not disturb the 
    // behavior of the following methods on PageContext:
    //     private int doGetAttributeScope(String name);
    //     private Object doFindAttribute(String name){
    //     private void doRemoveAttribute(String name){
    session.invalidate();
    
    try {
        // attr1 does not exist. findAttribute() invoked on all scopes and
        // must return a null value.
        obj = pageContext.findAttribute("attr1");
        if (obj != null) {
            out.println("ERROR: attr1 is not null on findAttribute()");            
        }  
        
        // attr1 does not exist. getAttributeScope() invoked on all scopes and
        // must return 0.
        int scope = pageContext.getAttributesScope("attr1");
       if (scope != 0) {
            out.println("ERROR: scope is not 0 on getAttributesScope()");            
        }                
        
        // set attr2 in application scope.
        // Make sure it gets removed properly.
        pageContext.setAttribute("attr2", "attr2", PageContext.APPLICATION_SCOPE);
        pageContext.removeAttribute("attr2");
        obj = pageContext.findAttribute("attr2");
        if (obj != null) {
            out.println("ERROR: attr2 is not null");            
        }        

        // Success!
        out.println("SUCCESS");
        
    } catch (IllegalStateException ex) {
        out.println("ERROR: invalidated session throws IllegalStateException when trying to find/remove attribute");
    }
%>
