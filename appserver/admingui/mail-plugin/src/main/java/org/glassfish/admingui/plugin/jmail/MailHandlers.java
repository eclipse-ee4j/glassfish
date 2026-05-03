/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.admingui.plugin.jmail;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.glassfish.admingui.common.util.GuiUtil;

/**
 * @author Vladimir Bychkov
 * */
public class MailHandlers {

    @Handler(id = "sendTestEmail",
    input = {
        @HandlerInput(name = "host", type = String.class, required = true),
        @HandlerInput(name = "defaultUser", type = String.class, required = true),
        @HandlerInput(name = "from", type = String.class, required = true),
        @HandlerInput(name = "storeProtocol", type = String.class, required = false),
        @HandlerInput(name = "storeProtocolClass", type = String.class, required = false),
        @HandlerInput(name = "transportProtocol", type = String.class, required = false),
        @HandlerInput(name = "transportProtocolClass", type = String.class, required = false),
        @HandlerInput(name = "debug", type = String.class, required = false),
        @HandlerInput(name = "properties", type = List.class, required = false)})
    public static void sendTestEmail(HandlerContext handlerCtx) {
        try {
            // read protocol details
            Properties props = readProperties(handlerCtx);

            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    String protocol = getRequestingProtocol();
                    if(protocol != null) {
                        String password = props.getProperty("mail." + protocol + ".password");
                        String defaultUser = props.getProperty("mail.user");
                        String username = props.getProperty("mail." + protocol + ".user", defaultUser);
                        if (password != null && username != null) {
                            return new PasswordAuthentication(username, password);
                        }
                    }
                    return null;
                }
            };
            String from = (String) handlerCtx.getInputValue("from");
            InternetAddress fromAddress = InternetAddress.parse(from, false)[0];

            // create and send message
            Session session = Session.getInstance(props, authenticator);
            Message msg = new MimeMessage(session);
            msg.setSubject("test message subject");
            msg.setSentDate(new Date());
            msg.setFrom(fromAddress);
            msg.setRecipient(Message.RecipientType.TO, fromAddress);
            msg.setText("test message body");
            Transport.send(msg);

            GuiUtil.prepareAlert("success", GuiUtil.getMessage("org.glassfish.jmail.admingui.Strings", "msg.SendSucceed"), null);
        } catch (Exception ex) {
            GuiUtil.prepareAlert("error", GuiUtil.getMessage("msg.Error"), ex.getMessage());
        }
    }

    private static Properties readProperties(HandlerContext handlerCtx) {
        Properties props = new Properties();
        // parameters marked as required on page
        for (Map.Entry<String, String> entry : Map.of("mail.host", "host",
            "mail.user", "defaultUser", "mail.from", "from").entrySet()) {
            props.put(entry.getKey(), handlerCtx.getInputValue(entry.getValue()));
        }
        // parameters from advanced section on page
        for (Map.Entry<String, String> entry : Map.of("mail.debug", "debug",
            "mail.store.protocol", "storeProtocol",
            "mail.transport.protocol", "transportProtocol").entrySet()) {
            Optional.ofNullable(handlerCtx.getInputValue(entry.getValue()))
                .ifPresent(value -> props.put(entry.getKey(), value));
        }
        if (props.containsKey("mail.store.protocol")) {
            Optional.ofNullable(handlerCtx.getInputValue("storeProtocolClass"))
                .ifPresent(value -> props.put("mail." + props.getProperty("mail.store.protocol") + ".class", value));
        }
        if (props.containsKey("mail.transport.protocol")) {
            Optional.ofNullable(handlerCtx.getInputValue("transportProtocolClass"))
                .ifPresent(value -> props.put("mail." + props.getProperty("mail.transport.protocol") + ".class", value));
        }
        // additional properties
        List<Map<String, String>> propertiesParam = (List) handlerCtx.getInputValue("properties");
        if (propertiesParam != null) {
            for (Map<String, String> item : propertiesParam) {
                if (item.get("name") != null && item.get("value") != null) {
                    props.put(item.get("name"), item.get("value"));
                }
            }
        }
        return props;
    }
}
