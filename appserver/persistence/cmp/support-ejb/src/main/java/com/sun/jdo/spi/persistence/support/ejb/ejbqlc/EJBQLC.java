/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.spi.persistence.support.ejb.model.util.NameMapper;
import com.sun.jdo.spi.persistence.utility.generator.JavaClassWriterHelper;

import java.io.Reader;
import java.io.StringReader;
import java.lang.System.Logger;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

import antlr.TokenBuffer;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * This class is the driver of the EJBQL compiler. It controls the compiler
 * passes: syntax analysis, semantic analysis and generation of the JDOQL query.
 * <p>
 * A EJBQLC instance is able to compile multiple EJBQL queries as long as they
 * come from the same deployement descriptor. The class uses the model instance
 * passed to the constructor to access any meta data from the deployement
 * descriptor. Method {@link #compile} compiles a single EJBQL query string
 * together with the java.lang.reflect.Method instance of the corresponding
 * finder/selector method. The result is a JDOQLElements instance, that can be
 * used to construct a JDOQL query instance.
 *
 * @author  Michael Bouschen 2001
 * @author  Shing Wai Chan 2001
 */
public class EJBQLC
{
    /** Meta data access. */
    protected Model model;

    /** Name mapping EJB <-> JDO. */
    protected NameMapper nameMapper;

    /** The intermediate form of the EJBQL query string. */
    protected EJBQLAST ast;

    /** The logger */
    private static final Logger LOG = System.getLogger(EJBQLC.class.getName());

    /** I18N support. */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
        EJBQLC.class);

    /**
     * Signature with CVS keyword substitution for identifying the generated code
     */
    public static final String SIGNATURE = "$RCSfile: EJBQLC.java,v $ $Revision: 1.3 $";

    /**
     * Constructor.
     *
     * @param model meta data access.
     * @param nameMapper name mapping EJB <-> JDO.
     */
    public EJBQLC(Model model, NameMapper nameMapper)
    {
        this.model = model;
        this.nameMapper = nameMapper;
    }

    /**
     * Compiles the specified query string for the specified
     * finder/selector method.
     * @param ejbqlQuery the EJBQL query text
     * @param method the Method instance of the finder or selector
     * @param resultTypeMapping result-type-mapping element from the DD
     * @param finderNotSelector <code>true</code> indicates a finder,
     * <code>false</code> a selector
     * @param ejbName the ejb name of the entity bean
     */
    public JDOQLElements compile(String ejbqlQuery, Method method,
                                 int resultTypeMapping,
                                 boolean finderNotSelector, String ejbName)
        throws EJBQLException
    {
        if (method == null) {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs,
                "ERR_MissingMethodInstance"));
        }
        if ((ejbqlQuery == null) || ejbqlQuery.isBlank()) {
            ErrorMsg.error(I18NHelper.getMessage(msgs,
                "EXC_MissingEjbqlQueryText", ejbName,
                getMethodSignature(method)));
        }
        LOG.log(DEBUG, "EJBQLC compile query\n"
            + "Bean:   {0}\n"
            + "Method: {1}\n"
            + "EJBQL:  {2}",
            ejbName, getMethodSignature(method), ejbqlQuery);

        JDOQLElements result = null;
        TypeSupport typeSupport = new TypeSupport(model, nameMapper);
        ParameterSupport paramSupport = new ParameterSupport(method);

        try {
            LOG.log(DEBUG, "EJBQLC start syntax analysis");
            EJBQLParser parser = createStringParser(ejbqlQuery);
            parser.query();
            ast = (EJBQLAST) parser.getAST();
            LOG.log(DEBUG, "EJBQLC dump tree {0}", ast.getTreeRepr("(AST)"));

            // semantic analysis
            LOG.log(DEBUG, "EJBQLC start semantic analysis");
            Semantic semantic = new Semantic();
            semantic.init(typeSupport, paramSupport, method, resultTypeMapping,
                          finderNotSelector, ejbName);
            semantic.setASTFactory(EJBQLASTFactory.getInstance());
            semantic.query(ast);
            ast = (EJBQLAST)semantic.getAST();
            LOG.log(DEBUG, "EJBQLC dump tree {0}", ast.getTreeRepr("(typed AST)"));

            // JDOQL code generation
            LOG.log(DEBUG, "EJBQLC start code generation");
            JDOQLCodeGeneration codeGen = new JDOQLCodeGeneration();
            codeGen.init(typeSupport, paramSupport);
            codeGen.setASTFactory(EJBQLASTFactory.getInstance());
            codeGen.query(ast);
            result = codeGen.getJDOQLElements();
            LOG.log(DEBUG, "EJBQLC result {0}", result);
        } catch (EJBQLException ex) {
            // add EJB name, finder/selector, EJBQL to error message.
            Object[] msgArgs = {ejbName, getMethodSignature(method), ejbqlQuery, ex.getMessage()};
            ErrorMsg.error(I18NHelper.getMessage(msgs, "EXC_InvalidEJBQLQuery", msgArgs));
        } catch (Throwable t) {
            throw new EJBQLException(I18NHelper.getMessage(
                msgs, "Fatal internal exception while parsing EJBQL:\n"
                    + "Bean:   {0}\n"
                    + "Method: {1}\n"
                    + "EJBQL:  {2}\n"
                    + "Error:  {3}",
                ejbName, getMethodSignature(method), ejbqlQuery, t), t);
        }

        // return the JDOQLElements instance representing the elements
        // of the JDOQL query.
        return result;
    }

    //========= Internal helper methods ==========

    /**
     * Creates an ANTLR EJBQL parser reading a string.
     */
    private EJBQLParser createStringParser(String text)
    {
        Reader in = new StringReader(text);
        EJBQLLexer lexer = new EJBQLLexer(in);
        TokenBuffer buffer = new TokenBuffer(lexer);
        EJBQLParser parser = new EJBQLParser(buffer);
        parser.setASTFactory(EJBQLASTFactory.getInstance());
        return parser;
    }

    /**
     * Returns the signature of a method w/o exceptions and modifiers
     * as a string.
     */
    private String getMethodSignature(Method m)
    {
        if (m == null) {
            return "";
        }

        return m.getReturnType().getName() + ' ' + m.getName() +
            JavaClassWriterHelper.parenleft_ +
            JavaClassWriterHelper.getParameterTypesList(m) +
            JavaClassWriterHelper.parenright_ ;
    }

}

