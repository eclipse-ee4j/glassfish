/*
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

package org.glassfish.api.deployment;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandParameters;

/**
 * Support class for all types of deployment operation parameters.
 *
 * @author Jerome Dochez
 */
public abstract class OpsParams implements CommandParameters {

    /**
     * There can be so far 6 types of events that can trigger deployment activities.
     *
     * load when an already deployed application is being reloaded. deploy when a new application is deployed on DAS
     * deploy_instance when a new application is deployed on instance unload when a loaded application is stopped undeploy
     * when a deployed application is removed from the system. create_application_ref when an application reference is being
     * created mt_provision when provisioning an application to tenant mt_unprovision when unprovisioning an application
     * from tenant
     */
    public enum Origin {
        load, deploy, deploy_instance, unload, undeploy, create_application_ref, mt_provision, mt_unprovision;

        // whether it's part of the deployment, on DAS or on instance
        public boolean isDeploy() {
            if (this == Origin.deploy || this == Origin.deploy_instance) {
                return true;
            } else {
                return false;
            }
        }

        // whether it's loading application only
        public boolean isLoad() {
            if (this == Origin.load) {
                return true;
            } else {
                return false;
            }
        }

        // whether the artifacts are already present and no need to
        // generate
        public boolean isArtifactsPresent() {
            if (this == Origin.load || this == Origin.deploy_instance || this == Origin.create_application_ref) {
                return true;
            } else {
                return false;
            }
        }

        // whether we need to clean the artifacts
        // we need to do this for undeployment and deployment failure
        // clean up
        public boolean needsCleanArtifacts() {
            if (this == Origin.undeploy || this == Origin.deploy || this == Origin.mt_unprovision) {
                return true;
            } else {
                return false;
            }
        }

        // whether it's undeploy
        public boolean isUndeploy() {
            if (this == Origin.undeploy) {
                return true;
            } else {
                return false;
            }
        }

        // whether it's unloading application only
        public boolean isUnload() {
            if (this == Origin.unload) {
                return true;
            } else {
                return false;
            }
        }

        // whether it's creating application reference
        public boolean isCreateAppRef() {
            if (this == Origin.create_application_ref) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * There can be cases where the container code wants to find out the command associated with the operation when the
     * Origin information is not sufficient
     *
     */
    public enum Command {
        deploy, undeploy, enable, disable, _deploy, create_application_ref, delete_application_ref, startup_server, shutdown_server
    }

    /**
     * Type of deployment operation, by default it's deployment
     */
    public Origin origin = Origin.deploy;

    public Origin getOrigin() {
        return origin;
    }

    /**
     * The command associated with this operation, by default it's deploy
     */
    public Command command = Command.deploy;

    public Command getCommand() {
        return command;
    }

    public abstract String name();

    public abstract String libraries();

    // internal hidden param
    // if this param is set to true, a classic style deployment will be
    // executed regardless of the virtualization settings
    @Param(optional = true, defaultValue = "false")
    public Boolean _classicstyle = false;
}
