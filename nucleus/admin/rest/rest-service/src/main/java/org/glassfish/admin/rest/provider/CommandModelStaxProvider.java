/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.provider;

import com.sun.enterprise.util.StringUtils;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.io.File;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;

/**
 * Marshals {@code CommandModel} into XML and JSON representation.
 *
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON, "application/x-javascript" })
public class CommandModelStaxProvider extends AbstractStaxProvider<CommandModel> {
    public CommandModelStaxProvider() {
        super(CommandModel.class, MediaType.APPLICATION_XML_TYPE, MediaType.TEXT_XML_TYPE, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    protected void writeContentToStream(CommandModel proxy, XMLStreamWriter wr) throws XMLStreamException {
        if (proxy == null) {
            return;
        }
        wr.writeStartDocument();
        wr.writeStartElement("command");
        wr.writeAttribute("name", proxy.getCommandName());
        if (proxy.unknownOptionsAreOperands()) {
            wr.writeAttribute("unknown-options-are-operands", "true");
        }
        if (proxy.isManagedJob()) {
            wr.writeAttribute("managed-job", "true");
        }
        String usage = proxy.getUsageText();
        if (StringUtils.ok(usage)) {
            wr.writeStartElement("usage");
            wr.writeCharacters(usage);
            wr.writeEndElement();
        }
        //Options
        for (CommandModel.ParamModel p : proxy.getParameters()) {
            Param par = p.getParam();
            wr.writeStartElement("option");
            wr.writeAttribute("name", p.getName());
            wr.writeAttribute("type", simplifiedTypeOf(p));
            if (par.primary()) {
                wr.writeAttribute("primary", "true");
            }
            if (par.multiple()) {
                wr.writeAttribute("multiple", "true");
            }
            if (par.optional()) {
                wr.writeAttribute("optional", "true");
            }
            if (par.obsolete()) {
                wr.writeAttribute("obsolete", "true");
            }
            String str = par.shortName();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("short", str);
            }
            str = par.defaultValue();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("default", str);
            }
            str = par.acceptableValues();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("acceptable-values", str);
            }
            str = par.alias();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("alias", str);
            }
            str = p.getLocalizedDescription();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("description", str);
            }
            str = p.getLocalizedPrompt();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("prompt", str);
            }
            str = p.getLocalizedPromptAgain();
            if (StringUtils.ok(str)) {
                wr.writeAttribute("prompt-again", str);
            }
            wr.writeEndElement();
        }
        wr.writeEndElement(); //</command>
        wr.writeEndDocument();
    }

    public static String simplifiedTypeOf(CommandModel.ParamModel p) {
        Class t = p.getType();
        if (t == Boolean.class || t == boolean.class) {
            return "BOOLEAN";
        } else if (t == File.class || t == File[].class) {
            return "FILE";
        } else if (t == Properties.class) { // XXX - allow subclass?
            return "PROPERTIES";
        } else if (p.getParam().password()) {
            return "PASSWORD";
        } else {
            return "STRING";
        }
    }

}
