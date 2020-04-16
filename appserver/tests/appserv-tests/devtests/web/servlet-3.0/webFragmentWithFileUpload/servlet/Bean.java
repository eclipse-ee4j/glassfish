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

package fileupload;

import java.io.IOException;
import java.util.Scanner;
import jakarta.faces.bean.ManagedBean;
import jakarta.servlet.http.Part;

@ManagedBean
public class Bean {

    private Part file;
    private String fileContent;

    public void upload() {
        try {
            fileContent = new Scanner(file.getInputStream()).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Upload completed" + fileContent);
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

}
