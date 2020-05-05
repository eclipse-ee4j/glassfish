/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.basicjpa;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
//import javax.management.*;
import static jakarta.persistence.CascadeType.ALL;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityManagerFactory;

import jakarta.persistence.EntityTransaction;

import jakarta.persistence.Persistence;




import java.util.Iterator;

import java.util.List;
import jakarta.annotation.Resource;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author ishan.vishnoi@java.net
 */
public class BasicJPAPaaSServlet extends HttpServlet {

    @PersistenceUnit(unitName = "BasicJPAPU")
    private EntityManagerFactory emf;
    @Resource
    UserTransaction utx;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Animal firstAnimal = new Animal();
        firstAnimal.setName("Shera");
        firstAnimal.setCageNumber("A1");
        firstAnimal.setID(1);
        firstAnimal.setSpecies("Lion");
        firstAnimal.setYearOfBirth("2001");
        Boolean x = addAnimal(firstAnimal);
        Animal secondAnimal = new Animal();
        secondAnimal.setName("Bhola");
        secondAnimal.setCageNumber("A2");
        secondAnimal.setID(2);
        secondAnimal.setSpecies("Bear");
        secondAnimal.setYearOfBirth("2004");
        x = addAnimal(secondAnimal);
        Animal thirdAnimal = new Animal();
        thirdAnimal.setName("Ringa");
        thirdAnimal.setCageNumber("A3");
        thirdAnimal.setID(3);
        thirdAnimal.setSpecies("Rhino");
        thirdAnimal.setYearOfBirth("2007");
        x = addAnimal(thirdAnimal);

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<link rel='stylesheet' type='text/css' href='newcss.css' />");
            out.println("<title>Servlet NewServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Here is a list of animals in the zoo.");

            List dir = sortByName();
            Iterator dirIterator = dir.iterator();
            out.println("<table border='1'>");

            while (dirIterator.hasNext()) {
                out.println("<tr>");
                Animal animal = (Animal) dirIterator.next();

                out.println("<td> id:" + animal.getID() + "</td>");
                out.println("<td> name:" + animal.getName() + "</td>");
                out.println("<td> species:" + animal.getSpecies() + "</td>");
                out.println("<td> cage_number:" + animal.getCageNumber() + "</td>");
                out.println("<td> year_of_birth:" + animal.getYearOfBirth() + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");

            out.println("<br><a href='index.jsp'>Back</a>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    public List sortByName() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("SELECT x FROM Animal x order by x.name");
        List results = q.getResultList();
        return (results);
    }

    public boolean addAnimal(Animal animal) {
        EntityManager em = emf.createEntityManager();
        try {
            utx.begin();
            em.persist(animal);
            utx.commit();
        } finally {
            em.close();
            return false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
