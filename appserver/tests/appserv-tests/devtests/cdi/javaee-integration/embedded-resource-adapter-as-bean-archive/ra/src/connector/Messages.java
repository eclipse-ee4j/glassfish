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

package connector;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * collection of messages.
 *
 * @author Qingqing Ouyang
 */
public class Messages {

    private static Hashtable messages = new Hashtable();

    public static void sendMessage(String destName, String message) {
        if (messages.get(destName) != null) {
            ((ArrayList) messages.get(destName)).add(message);
        } else {
            ArrayList list = new ArrayList();
            list.add(message);
            messages.put(destName, list);
        }
        System.out.println("sendMessage. message at foo is "
                + Messages.hasMessages("Foo"));
    }

    public static boolean hasMessages(String destName) {
        return messages.get(destName) != null;
    }

    public static ArrayList getMessages(String destName) {
        return (ArrayList) messages.get(destName);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.exit(1);
        }

        String command = args[0];
        String destName = args[1];
        String message = args[2];

        if (!"add".equals(command)) {
            System.exit(1);
        }

        sendMessage(destName, message);
        System.out.println("Message : " + message + " sent to " + destName);
    }
}
