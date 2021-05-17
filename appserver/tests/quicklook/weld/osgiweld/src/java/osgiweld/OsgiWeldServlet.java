/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package osgiweld;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Test integrity of OSGi Weld Module.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class OsgiWeldServlet extends HttpServlet {

    private static List<Attributes.Name> ATTRS =
            Arrays.asList(new Attributes.Name("Export-Package"),
                          new Attributes.Name("Import-Package"));
                          //new Attributes.Name("Private-Package"));
            //From Weld 1.1, Private-Package is not part of the OSGi headers

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String result = "OK";
        try {
            String gfhome = System.getProperty("com.sun.aas.instanceRoot");
            // Soft test, ignore if can't find module
            if (gfhome != null) {
                String jarFile = gfhome + File.separator + ".."
                        + File.separator + ".." + File.separator
                        + "modules" + File.separator + "weld-osgi-bundle.jar";
                //System.out.println("Weld Osgi module = " + jarFile);
                JarFile jar = new JarFile(jarFile);
                Manifest manifest = jar.getManifest();

                String command = request.getParameter("command");
                //System.out.println("Command: " + command);
                if (command.equals("manifest")) {
                    // Make sure all manifest attrs are there
                    Set<Object> keys = manifest.getMainAttributes().keySet();
                    //System.out.println("Keys: " + keys);
                    if (!keys.containsAll(ATTRS) || !checkBundleSymbolicName(manifest.getMainAttributes())) {
                        result = "ERROR";
                    }
                } else if (command.equals("exports")) {
                    // Make sure package exports are present and return them
                    String exportedValues = manifest.getMainAttributes().getValue(new Attributes.Name("Export-Package"));
                    //System.out.println("Exported Values: " + exportedValues);
                    if (null != exportedValues) {
                        result = exportedValues;
                    } else {
                        result = "ERROR";
                    }
                } else if (command.equals("imports")) {
                    //Make sure package imports are present and return them
                    String importedValues = manifest.getMainAttributes().getValue(new Attributes.Name("Import-Package"));
                    //System.out.println("Imported Values: " + importedValues);
                    if (null != importedValues) {
                        result = importedValues;
                    } else {
                        result = "ERROR";
                    }
                }
            } else {
                System.out.println("Unable to find Weld module");
            }
        } catch (Exception e) {
            result = "ERROR";
        }

        out.println(result);
        out.close();
    }

    private boolean checkBundleSymbolicName(Attributes attrs){
        String name = attrs.getValue("Bundle-SymbolicName");
        System.out.println("Bundle-SymbolicName:"+ name);
        return name.equals("org.jboss.weld.osgi-bundle");
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
