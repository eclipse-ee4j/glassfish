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

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import static javax.security.enterprise.AuthenticationStatus.*;

@RequestScoped
public class TestAuthenticationMechanism implements HttpAuthenticationMechanism {
    
    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {

        // Get the (caller) name and password from the request
        // NOTE: This is for the smallest possible example only. In practice
        // putting the password in a request query parameter is highly
        // insecure
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        tagHttpRequest(request,"validateRequest");
        if (name != null && password != null) {

            // Delegate the {credentials in -> identity data out} function to
            // the Identity Store
            CredentialValidationResult result = identityStoreHandler.validate(
                new UsernamePasswordCredential(name, password));

            if (result.getStatus() == VALID) {
                // Communicate the details of the authenticated user to the
                // container. In many cases the underlying handler will just store the details 
                // and the container will actually handle the login after we return from 
                // this method.
                return httpMessageContext.notifyContainerAboutLogin(
                    result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                return httpMessageContext.responseUnauthorized();
            }
        } 

        return httpMessageContext.doNothing();
    }

    @Override
    public AuthenticationStatus secureResponse(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext)  throws AuthenticationException {
        try {
            response.getWriter().write(",secureResponse");
        }catch (IOException e){
            e.printStackTrace();
        }
        tagHttpRequest(request,"secureResponse");
        return SUCCESS;
    }

    @Override
    public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        try {
            response.getWriter().write(",cleanSubject");
        }catch (IOException e){
            e.printStackTrace();
        }
        tagHttpRequest(request,"cleanSubject");
    }

    private void tagHttpRequest(HttpServletRequest request,String methodName){

        if(request.getAttribute("methodInvList") == null){
            List<String> invList= new ArrayList<>();
            invList.add(methodName);
            request.setAttribute("methodInvList", invList.stream().collect(Collectors.joining(",")));
        }else{
            String invListStr = (String)request.getAttribute("methodInvList");
            List<String> invList = new ArrayList(Arrays.asList(invListStr.split(",")));
            invList.add(methodName);
            request.setAttribute("methodInvList", invList.stream().collect(Collectors.joining(",")));
        }
    }
    
}
