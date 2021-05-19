/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package simple_bv_servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimpleBVServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("Context Path: " + req.getContextPath());
        System.out.println("Path info: " + req.getPathInfo());
        System.out.println("Path translated: " + req.getPathTranslated());

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            if (pathInfo.endsWith("bv_10")) {
                handleBV10(resp);
            } else if (pathInfo.endsWith("bv_20")) {
                handleBV20(resp);
            } else {
                emptyOutput(resp);
            }
        } else {
            emptyOutput(resp);
        }
    }

    private void emptyOutput(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        out.print("<html><head><title>SimpleBVServlet</title></head><body>");
        out.print("</body></html>");
    }

    private void handleBV10(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        out.print("<html><head><title>SimpleBVServlet</title></head><body>");

        ValidatorFactory validatorFactory = null;

        validatorFactory = Validation.buildDefaultValidatorFactory();

        out.print("<p>");
        out.print("Obtained ValidatorFactory: " + validatorFactory + ".");
        out.print("</p>");

        ValidatorContext validatorContext = validatorFactory.usingContext();
        jakarta.validation.Validator beanValidator = validatorContext.getValidator();

        out.print("<h1>");
        out.print("Validating person class using validateValue with valid property");
        out.print("</h1>");

        List<String> listOfString = new ArrayList<String>();
        listOfString.add("one");
        listOfString.add("two");
        listOfString.add("three");

        Set<ConstraintViolation<Person>> violations =
                beanValidator.validateValue(Person.class, "listOfString", listOfString);

        printConstraintViolations(out, violations, "case1");

        out.print("<h1>");
        out.print("Validating person class using validateValue with invalid property");
        out.print("</h1>");

        try {
            violations =
                    beanValidator.validateValue(Person.class, "nonExistentProperty", listOfString);
            System.out.println("Check: should not print this");
        } catch (IllegalArgumentException iae) {
            out.print("<p>");
            out.print("case2: caught IllegalArgumentException.  Message: " +
                    iae.getMessage());
            out.print("</p>");
        System.out.println("Caught IllegalArgumentException: must pass test");
        }catch (Exception ex) {
            System.out.println("check: if any other exception?");
        ex.printStackTrace();
        }
        Person person = new Person();

        out.print("<h1>");
        out.print("Validating invalid person instance using validate.");
        out.print("</h1>");

        violations = beanValidator.validate(person);

        printConstraintViolations(out, violations, "case3");

        out.print("<h1>");
        out.print("Validating valid person.");
        out.print("</h1>");

        person.setFirstName("John");
        person.setLastName("Yaya");
        person.setListOfString(listOfString);

        violations = beanValidator.validate(person);
        printConstraintViolations(out, violations, "case4");

        out.print("</body></html>");
    }

    private void handleBV20(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        out.print("<html><head><title>SimpleBVServlet</title></head><body>");

        ValidatorFactory validatorFactory = null;

        validatorFactory = Validation.buildDefaultValidatorFactory();

        out.print("<p>");
        out.print("Obtained ValidatorFactory: " + validatorFactory + ".");
        out.print("</p>");

        ValidatorContext validatorContext = validatorFactory.usingContext();
        jakarta.validation.Validator beanValidator = validatorContext.getValidator();

        out.print("<h1>");
        out.print("Validating person class using validateValue with valid property");
        out.print("</h1>");


        // Validation: Null entry in the list
        // List<@NotNull String> listOfString in Employee.
        List<String> listOfString = new ArrayList<String>();
        listOfString.add("one");
        listOfString.add("two");
        listOfString.add(null);
        Set<ConstraintViolation<Employee>> violations =
            beanValidator.validateValue(Employee.class, "listOfString", listOfString);

        printConstraintViolationsOfBV2(out, violations, "case0");


        //Validation: Not null listOfString
        listOfString = new ArrayList<String>();
        listOfString.add("one");
        listOfString.add("two");
        listOfString.add("three");

        violations =
            beanValidator.validateValue(Employee.class, "listOfString", listOfString);

        printConstraintViolationsOfBV2(out, violations, "case1");


        out.print("<h1>");
        out.print("Validating person class using validateValue with invalid property");
        out.print("</h1>");

        try {
            violations =
                beanValidator.validateValue(simple_bv_servlet.Employee.class, "nonExistentProperty", listOfString);
            System.out.println("Check: should not print this");
        } catch (IllegalArgumentException iae) {
            out.print("<p>");
            out.print("case2: caught IllegalArgumentException.  Message: " +
                iae.getMessage());
            out.print("</p>");
            System.out.println("Caught IllegalArgumentException: must pass test");
        }catch (Exception ex) {
            System.out.println("check: if any other exception?");
            ex.printStackTrace();
        }
        simple_bv_servlet.Employee person = new Employee();

        out.print("<h1>");
        out.print("Validating invalid person instance using validate.");
        out.print("</h1>");

        violations = beanValidator.validate(person);

        printConstraintViolationsOfBV2(out, violations, "case3");

        out.print("<h1>");
        out.print("Validating valid person.");
        out.print("</h1>");

        //validate the person. Invalid email format. (@Email)
        person.setFirstName("John");
        person.setLastName("Yaya");
        person.setAge(30);
        person.setEmail("johy.yaya-abc.com");
        person.setListOfString(listOfString);

        violations = beanValidator.validate(person);
        printConstraintViolationsOfBV2(out, violations, "case4");

        //validate email (@Email)
        person.setEmail("johy.yaha@abc.com");
        violations = beanValidator.validate(person);
        printConstraintViolationsOfBV2(out, violations, "case5");

        ///validate that the age is greater than 25 (@Min 20, @Min 25)
        person.setAge(22);
        violations = beanValidator.validate(person);
        printConstraintViolationsOfBV2(out, violations, "case6");


        //validate that the age is within 50. (@Max 50 and @Max 60)
        person.setAge(52);
        violations = beanValidator.validate(person);
        printConstraintViolationsOfBV2(out, violations, "case7");



        out.print("</body></html>");
    }

    private void printConstraintViolationsOfBV2(PrintWriter out,
        Set<ConstraintViolation<Employee>> violations, String caseId) {
        if (violations.isEmpty()) {
            out.print("<p>");
            out.print(caseId + ": No ConstraintViolations found.");
            out.print("</p>");
        } else {
            for (ConstraintViolation<Employee> curViolation : violations) {
                out.print("<p>");
                out.print(caseId + ": ConstraintViolation: message: " + curViolation.getMessage() +
                    " propertyPath: " + curViolation.getPropertyPath());
                out.print("</p>");
            }
        }
    }
    private void printConstraintViolations(PrintWriter out,
            Set<ConstraintViolation<Person>> violations, String caseId) {
        if (violations.isEmpty()) {
            out.print("<p>");
            out.print(caseId + ": No ConstraintViolations found.");
            out.print("</p>");
        } else {
            for (ConstraintViolation<Person> curViolation : violations) {
                out.print("<p>");
                out.print(caseId + ": ConstraintViolation: message: " + curViolation.getMessage() +
                        " propertyPath: " + curViolation.getPropertyPath());
                out.print("</p>");
            }
        }
    }
}
