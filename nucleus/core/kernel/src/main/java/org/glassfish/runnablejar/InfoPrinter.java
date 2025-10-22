/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.runnablejar;

import com.sun.enterprise.config.serverbeans.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Ondro Mihalyi
 */
public class InfoPrinter {

    static final String BORDER_CHARACTER = "#";
    static final String TITLE = "GLASSFISH STARTED";
    static final int TITLE_LENGTH = TITLE.length();

    public String getInfoAfterStartup(List<Application> applications, List<NetworkListener> networkListeners) throws GlassFishException {

        final List<String> listenerUrls = getListenerUrls(networkListeners);
        final StringBuilder output = new StringBuilder();
        List<Integer[]> linesInfo = new ArrayList<>(); // tuples of index of last line character and line length

        final int maxLength = processApplications(applications, listenerUrls, output, linesInfo);

        justifyLines(linesInfo, maxLength, output);

        addBorder(maxLength, output);

        return output.toString();
    }

    private void addBorder(final int maxLength, final StringBuilder output) {
        output.insert(0, "\n");
        output.insert(0, BORDER_CHARACTER);
        output.insert(0, " ".repeat(maxLength - 2));
        output.insert(0, BORDER_CHARACTER);
        output.insert(0, "\n");
        final int numberOfBorderCharsInFirstLine = maxLength - 2 - TITLE_LENGTH;
        output.insert(0, BORDER_CHARACTER.repeat(numberOfBorderCharsInFirstLine / 2));
        output.insert(0, " ");
        output.insert(0, TITLE);
        output.insert(0, " ");
        output.insert(0, BORDER_CHARACTER.repeat(numberOfBorderCharsInFirstLine / 2 + numberOfBorderCharsInFirstLine % 2));
        output.append(BORDER_CHARACTER);
        output.append(" ".repeat(maxLength - 2));
        output.append(BORDER_CHARACTER);
        output.append("\n");
        output.append(BORDER_CHARACTER.repeat(maxLength));
    }

    private void justifyLines(List<Integer[]> linesInfo, int maxLength, final StringBuilder output) {
        for (Integer[] lineInfo : linesInfo) {
            if (maxLength > lineInfo[1]) {
                output.insert(lineInfo[0], " ".repeat(maxLength - lineInfo[1]));
            }
        }
    }

    private List<String> getListenerUrls(List<NetworkListener> networkListeners) {
        return networkListeners.stream()
                .filter(listener -> Boolean.valueOf(listener.getEnabled()))
                .map(listener -> {
                    final Protocol protocol = listener.findProtocol();
                    String schema = (Boolean.parseBoolean(protocol.getSecurityEnabled())) ? "https" : "http";
                    String port = listener.getPort();
                    return schema + "://localhost:" + port;
                })
                .collect(toList());
    }

    private int processApplications(List<Application> applications, final List<String> listenerUrls,
            /*out*/ final StringBuilder output, /*out*/ final List<Integer[]> linesInfo) {
        int maxLength = 0;
        if (applications.isEmpty()) {
            final int lengthBefore = output.length();
            output.append(BORDER_CHARACTER)
                    .append(" ")
                    .append("No applications deployed")
                    .append(listenerUrls.isEmpty()
                            ? ""
                            : ". Listening on " + String.join(", ", listenerUrls))
                    .append(" ")
                    .append(BORDER_CHARACTER);
            int lengthAfter = output.length();
            output.append("\n");
            return lengthAfter - lengthBefore;
        }
        for (Application app : applications) {
            final String appUrls = listenerUrls.stream()
                    .map(url -> {
                        final String contextRoot = app.getContextRoot();
                        if (Set.of("", "/").contains(contextRoot)) {
                            return url;
                        } else if (contextRoot.startsWith("/")) {
                            return url + contextRoot;
                        } else {
                            return url + "/" + contextRoot;
                        }
                    })
                    .collect(joining(", "));
            final int lengthBefore = output.length();
            output.append(BORDER_CHARACTER)
                    .append(" ")
                    .append(app.getName())
                    .append(" deployed")
                    .append( appUrls.isEmpty()
                            ? ""
                            : " at: " + appUrls)
                    .append(" ")
                    .append(BORDER_CHARACTER);
            int lengthAfter = output.length();
            int lineLength = lengthAfter - lengthBefore;
            if (lineLength < TITLE_LENGTH) {
                output.append(" ".repeat(TITLE_LENGTH - lineLength));
                lengthAfter = output.length();
                lineLength = lengthAfter - lengthBefore;
            }
            linesInfo.add(new Integer[]{lengthAfter - 1, lineLength});
            output.append("\n");
            maxLength = Math.max(maxLength, lineLength);
        }
        return maxLength;
    }
}
