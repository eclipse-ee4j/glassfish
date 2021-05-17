/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package mypack;

import java.io.IOException;
import java.io.StringWriter;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.JspFragment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


public class MySimpleTag extends SimpleTagSupport {

  private boolean pConstructed = false;
  private PageContext pageContext;

  public void doTag() {

    this.pageContext = (PageContext) getJspContext();
    if (pConstructed) {
        pageContext.setAttribute("InjectMessage", "==PostConstruct==");
    }
    JspWriter out = pageContext.getOut();
    StringWriter sw = new StringWriter();
    try {
      JspFragment body = getJspBody();
      body.invoke(sw);
      out.println(sw.toString());
    } catch (IOException | JspException e) {
      e.printStackTrace();
    }
  }

  @PostConstruct
  public void myPostConstruct () {
     pConstructed = true;
  }

  @PreDestroy
  public void myPreDestroy () {
      pageContext.setAttribute("InjectMessage", "==PreDestroy==");
  }
}

