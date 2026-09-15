/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This closure is used to provide theme properties.
 */
require(function () {

    return {
        // Button properties.
        button: {
            imageClassName: "Btn3_sun4",
            imageDisabledClassName: "Btn3Dis_sun4",
            imageHovClassName: "Btn3Hov_sun4",
            primaryClassName: "Btn1_sun4",
            primaryDisabledClassName: "Btn1Dis_sun4",
            primaryHovClassName: "Btn1Hov_sun4",
            primaryMiniClassName: "Btn1Mni_sun4",
            primaryMiniHovClassName: "Btn1MniHov_sun4",
            primaryMiniDisabledClassName: "Btn1MniDis_sun4",
            secondaryClassName: "Btn2_sun4",
            secondaryDisabledClassName: "Btn2Dis_sun4",
            secondaryHovClassName: "Btn2Hov_sun4",
            secondaryMiniClassName: "Btn2Mni_sun4",
            secondaryMiniDisabledClassName: "Btn2MniDis_sun4",
            secondaryMiniHovClassName: "Btn2MniHov_sun4"
        },

        // Progress bar properties.
        progressBar: {
            barContainerClassName: "barContainer_sun4",
            busy: "BUSY",
            canceled: "canceled",
            completed: "completed",
            determinate: "DETERMINATE",
            determinateClassName: "barDeterminate_sun4",
            failed: "failed",
            indeterminate: "INDETERMINATE",
            indeterminateClassName: "barIndeterminate_sun4",
            indeterminatePausedClassName: "barIndeterminatePaused_sun4",
            notstarted: "not_started",
            paused: "paused",
            resumed: "resumed",
            stopped: "stopped"
        }
    };
});
