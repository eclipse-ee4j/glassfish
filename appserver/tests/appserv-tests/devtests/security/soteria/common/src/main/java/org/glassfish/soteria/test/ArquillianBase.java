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

import static java.util.logging.Level.SEVERE;
import static org.apache.http.HttpStatus.SC_MULTIPLE_CHOICES;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

public class ArquillianBase {
    
    private static final Logger logger = Logger.getLogger(ArquillianBase.class.getName());
    
    private WebClient webClient;
    private String response;

	@ArquillianResource
    private URL base;
	
    @Rule
    public TestWatcher ruleExample = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);
            
            logger.log(SEVERE, 
                "\n\nTest failed: " + 
                description.getClassName() + "." + description.getMethodName() +
                
                "\nMessage: " + e.getMessage() +
                
                "\nLast response: " +
                
                "\n\n"  + response + "\n\n");
            
        }
    };

    @Before
    public void setUp() {
        response = null;
        webClient = new WebClient() {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void printContentIfNecessary(WebResponse webResponse) {
                int statusCode = webResponse.getStatusCode();
                if (getOptions().getPrintContentOnFailingStatusCode() && !(statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES)) {
                    logger.log(SEVERE, webResponse.getWebRequest().getUrl().toExternalForm());
                }
                super.printContentIfNecessary(webResponse);
            }
        };
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @After
    public void tearDown() {
        webClient.getCookieManager().clearCookies();
        webClient.close();
    }
    
    protected String readFromServer(String path) {
        response = "";
        WebResponse localResponse = responseFromServer(path);
        if (localResponse != null) {
            response = localResponse.getContentAsString();
        }
        
    	return response;
    }
    
    protected WebResponse responseFromServer(String path) {
        
        WebResponse webResponse = null;
        
        Page page = pageFromServer(path);
        if (page != null) {
            webResponse = page.getWebResponse();
            if (webResponse != null) {
                response = webResponse.getContentAsString();
            }
        }
        
        return webResponse;
    }
    
    protected <P extends Page> P pageFromServer(String path) {
    	
    	if (base.toString().endsWith("/") && path.startsWith("/")) {
    		path = path.substring(1);
    	}
    	
        try {
            response = "";
            
            P page = webClient.getPage(base + path);
            
            if (page != null) {
                WebResponse localResponse = page.getWebResponse();
                if (localResponse != null) {
                    response = localResponse.getContentAsString();
                }
            }
            
            return page;
            
        } catch (FailingHttpStatusCodeException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    protected WebClient getWebClient() {
 		return webClient;
 	}
    
}
