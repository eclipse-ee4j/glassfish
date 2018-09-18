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

/**
 *
 * @author Jitendra Kotamraju
 */

var network = function (eventSource) {
    return {
        initialize: function() {
            var url = 'http://' + document.location.host
                + '/videoplayer/notifications';

            eventSource = new EventSource(url);
            eventSource.onmessage = function (event) {
                // We should use JSON.parse to avoid any security holes,
                // but that also needs json2.js
                //var command = JSON.parse(event.data);
                var command = eval('('+event.data+')');
                if (command.type == "pause") {
                    APP.pauseVideo();
                } else if (command.type == "play") {
                    APP.playVideo();
                } else if (command.type == "seeked") {
                    APP.seekVideo(command.currentTime);
                } else {
                    alert("Unknown command " + command);
                }
            };
        },
        send: function(command) {
            eventSource.send(command);
        }
    }
};

var APP = {
    id: Math.floor(Math.random() * 10000),

    network: network(null),

    // Cannot use 'this' here after updating window.onload (see below)
    initialize: function () {
        APP.network.initialize();

        var video = APP.getVideo();
    },

    getVideo: function () {
        return document.getElementsByTagName("video")[0];
    },

    pauseVideo: function () {
        var video = this.getVideo();
        video.pause();
    },

    playVideo: function () {
        var video = this.getVideo();
        video.play();
    },

    seekVideo: function (currentTime) {
        var video = this.getVideo();
        video.currentTime = currentTime;
    }

};

window.onload = APP.initialize;
