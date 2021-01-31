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

package org.glassfish.api.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * This holds the late status of the instance, the commands that are Queued up while the instance was starting etc.
 *
 * @author Vijay Ramachandran
 */
public class InstanceState {
    public enum StateType {
        NO_RESPONSE {
            @Override
            public String getDescription() {
                return "NO_RESPONSE";
            }

            @Override
            public String getDisplayString() {
                return " no response";
            }
        },
        NOT_RUNNING {
            @Override
            public String getDescription() {
                return "NOT_RUNNING";
            }

            @Override
            public String getDisplayString() {
                return " not running";
            }
        },
        STARTING {
            @Override
            public String getDescription() {
                return "STARTING";
            }

            @Override
            public String getDisplayString() {
                return " starting";
            }
        },
        RUNNING {
            @Override
            public String getDescription() {
                return "RUNNING";
            }

            @Override
            public String getDisplayString() {
                return " running";
            }
        },
        RESTART_REQUIRED {
            @Override
            public String getDescription() {
                return "REQUIRES_RESTART";
            }

            @Override
            public String getDisplayString() {
                return " requires restart";
            }
        },
        NEVER_STARTED {
            @Override
            public String getDescription() {
                return "NEVER_STARTED";
            }

            @Override
            public String getDisplayString() {
                return " never started";
            }
        };

        public String getDescription() {
            return null;
        }

        public String getDisplayString() {
            return "NONE";
        }

        public static StateType makeStateType(String s) {
            for (StateType st : StateType.values()) {
                if (s.equals(st.getDescription())) {
                    return st;
                }
            }
            return null;
        }
    }

    private StateType currentState;
    private List<String> failedCommands;

    public InstanceState(StateType st) {
        currentState = st;
        failedCommands = new ArrayList<>();
    }

    public StateType getState() {
        return currentState;
    }

    public void setState(StateType state) {
        currentState = state;
    }

    public List<String> getFailedCommands() {
        return failedCommands;
    }

    public void addFailedCommands(String cmd) {
        if (currentState == StateType.NEVER_STARTED) {
            // do not keep track of failed commands for instances that
            // have never been started
            return;
        }
        failedCommands.add(cmd);
    }

    public void removeFailedCommands() {
        failedCommands.clear();
    }
}
