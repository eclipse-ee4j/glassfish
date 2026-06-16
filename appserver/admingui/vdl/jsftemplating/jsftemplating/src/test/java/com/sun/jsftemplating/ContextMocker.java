/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;

/**
 * Provide a dummy FacesContext for unit tests to pass.
 *
 * @author Romain Grecourt
 */
public class ContextMocker extends FacesContext {
  public ExternalContext _extCtx = new ExternalContextMocker();
  public UIViewRoot _viewRoot = Mockito.mock(UIViewRoot.class);

  public ContextMocker() {
  }

  static ContextMocker _ctx = new ContextMocker();
  public static void init(){
    setCurrentInstance(_ctx);
  }

  @Override
  public Lifecycle getLifecycle() {
    return Mockito.mock(Lifecycle.class);
  }

  @Override
  public ExternalContext getExternalContext() {
    return _extCtx;
  }

  @Override
  public Application getApplication() {
    return Mockito.mock(Application.class);
  }

  @Override
  public Iterator<String> getClientIdsWithMessages() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public FacesMessage.Severity getMaximumSeverity() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public Iterator<FacesMessage> getMessages() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public Iterator<FacesMessage> getMessages(String clientId) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public RenderKit getRenderKit() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public boolean getRenderResponse() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public boolean getResponseComplete() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public ResponseStream getResponseStream() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void setResponseStream(ResponseStream responseStream) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public ResponseWriter getResponseWriter() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void setResponseWriter(ResponseWriter responseWriter) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public UIViewRoot getViewRoot() {
    return _viewRoot;
  }

  @Override
  public void setViewRoot(UIViewRoot root) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void addMessage(String clientId, FacesMessage message) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void release() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void renderResponse() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void responseComplete() {
    throw new UnsupportedOperationException("Not supported.");
  }

  public static class ExternalContextMocker extends ExternalContext {

    public ExternalContextMocker() {
    }

    public Map<String,Object> _appMap = new HashMap<>();
    public Map _initParamMap = new HashMap();
    public Map<String, Object> _requestMap = new HashMap<>();

    @Override
    public Map<String, Object> getApplicationMap() {
      return _appMap;
    }
    @Override
    public void dispatch(String path) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String encodeActionURL(String url) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String encodeNamespace(String name) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String encodeResourceURL(String url) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getAuthType() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getContext() {
      return this;
    }

    @Override
    public String getInitParameter(String name) {
      return (String) _initParamMap.get(name);
    }

    @Override
    public Map getInitParameterMap() {
      return _initParamMap;
    }

    @Override
    public String getRemoteUser() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getRequest() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getRequestContextPath() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, Object> getRequestCookieMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Locale getRequestLocale() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Iterator<Locale> getRequestLocales() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, Object> getRequestMap() {
      return _requestMap;
    }

    @Override
    public Map<String, String> getRequestParameterMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Iterator<String> getRequestParameterNames() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getRequestPathInfo() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getRequestServletPath() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public URL getResource(String path) {
      return this.getClass().getClassLoader().getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<String> getResourcePaths(String path) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getResponse() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getSession(boolean create) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<String, Object> getSessionMap() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Principal getUserPrincipal() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isUserInRole(String role) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void log(String message) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void log(String message, Throwable exception) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void redirect(String url) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String encodeWebsocketURL(String string) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException("Not supported");
    }
  }
}
