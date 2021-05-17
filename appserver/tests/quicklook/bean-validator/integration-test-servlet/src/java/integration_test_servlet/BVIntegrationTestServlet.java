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

package integration_test_servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.bootstrap.GenericBootstrap;

public class BVIntegrationTestServlet extends HttpServlet {

    public static String validationXml =
"<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n" +
"<constraint-mappings xmlns=\"http://jboss.org/xml/ns/javax/validation/mapping\"\n" +
"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
"  xsi:schemaLocation=\"http://jboss.org/xml/ns/javax/validation/mapping validation-mapping-1.0.xsd\">\n" +
"      <default-package>integration_test_servlet</default-package>\n" +
"      <bean class=\"Person\" ignore-annotations=\"true\">\n" +
"          <field name=\"firstName\" ignore-annotations=\"true\">\n" +
"              <constraint annotation=\"jakarta.validation.constraints.NotNull\" />\n" +
"          </field>\n" +
"          <field name=\"lastName\" ignore-annotations=\"true\">\n" +
"              <constraint annotation=\"jakarta.validation.constraints.NotNull\" />\n" +
"          </field>\n" +
"          <getter name=\"listOfString\"  ignore-annotations=\"true\">\n" +
"              <constraint annotation=\"jakarta.validation.constraints.NotNull\" />\n" +
"          </getter>\n" +
"      </bean>\n" +
"</constraint-mappings>\n";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        out.print("<html><head><title>SimpleBVServlet</title></head><body>");

        jakarta.validation.Validator beanValidator = configureValidation(req, resp);

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
        } catch (IllegalArgumentException iae) {
            out.print("<p>");
            out.print("case2: caught IllegalArgumentException.  Message: " +
                    iae.getMessage());
            out.print("</p>");
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


    private Validator configureValidation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();

        GenericBootstrap bootstrap = Validation.byDefaultProvider();
        Configuration config = bootstrap.configure();
        InputStream mappingsXml = new ByteArrayInputStream(validationXml.getBytes());
        config.addMapping(mappingsXml);
        ValidatorFactory factory = config.buildValidatorFactory();
        ValidatorContext validatorContext = factory.usingContext();
        Validator validator = validatorContext.getValidator();

        if (null == validator) {
            factory = Validation.byDefaultProvider().configure().buildValidatorFactory();
            validator = factory.getValidator();
        }

        out.print("<p>");
        out.print("Obtained ValidatorFactory: " + factory + ".");
        out.print("</p>");



        return validator;
    }
}
