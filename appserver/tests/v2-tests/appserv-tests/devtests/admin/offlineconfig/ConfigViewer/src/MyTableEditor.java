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

/*
 * MyTableEditor.java
 *
 * Created on April 28, 2006, 5:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import javax.swing.JTextField;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;

/**
 *
 * @author kravtch
 */
public class MyTableEditor{

    /**
     * Creates a new instance of MyTableEditor
     */
    DefaultCellEditor textEditor  = new DefaultCellEditor(new JTextField());
    public MyTableEditor() {
    }

    /*public Component getTableCellEditorComponent(JTable table, Object value,
              boolean isSelected, int row, int column) {
        return textEditor.getTableCellEditorComponent(
                       table, value, isSelected, row, column);
    }*/
}
