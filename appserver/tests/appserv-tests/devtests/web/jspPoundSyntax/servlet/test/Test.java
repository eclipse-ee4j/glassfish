/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.el.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;

public class Test extends SimpleTagSupport {

    private String val1, val2;
    ValueExpression expr, exprString;

    public void setLit(String val) {
        val1 = val;
    }

    public void setExpr(String val) {
        val2 = val;
    }

    public void setDeferred(ValueExpression expr) {
        this.expr = expr;
    }

    public void setExprString(ValueExpression expr) {
        this.exprString = expr;
    }

    public void doTag() throws JspException {

        PageContext pc = (PageContext) getJspContext();
        JspWriter out = pc.getOut();

        try {
            out.println("val1\n" + val1);
            out.println("val2\n" + val2);
            if (expr != null) {
                out.println("expr\n" + expr.getValue(pc.getELContext()));
            }
            if (exprString != null) {
                out.println("exprString\n" + exprString.getExpressionString());
            }
/*
            if (getJspBody() != null) {
                out.write("[<");
                getJspBody().invoke(out);
                out.write(">]");
            }
*/
        } catch (IOException ex) {
            System.out.println("Exception " + ex);
        }
    }
}
