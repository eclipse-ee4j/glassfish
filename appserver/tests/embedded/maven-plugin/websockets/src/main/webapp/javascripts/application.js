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

var count = 0;
var loop = 0;
var websocket = null;
var name  = null;
var url = 'ws://' + document.location.host + document.location.pathname + 'chat';

var app = {

    url: url,

    initialize: function() {
        if ("WebSocket" in window) {
            $('login-name').focus();
            app.listen();
        } else {
            $('missing-sockets').style.display = 'inherit';
            $('login-name').style.display = 'none';
            $('login-button').style.display = 'none';
            $('display').style.display = 'none';
        }
    },
    listen: function() {
        $('websockets-frame').src = app.url + '?' + count;
        count ++;
    },
    login: function() {
        name = $F('login-name');
        if (! name.length > 0) {
            $('system-message').style.color = 'red';
            $('login-name').focus();
            return;
        }
        $('system-message').style.color = '#2d2b3d';
        $('system-message').innerHTML = name + ':';

        $('login-button').disabled = true;
        $('login-form').style.display = 'none';
        $('message-form').style.display = '';

        websocket = new WebSocket(url);
        websocket.name = name;
        websocket.onopen = function() {
            // Web Socket is connected. You can send data by send() method
            websocket.send('login:' + name);
        };
        websocket.onmessage = function (evt) {
            eval(evt.data);
            $('message').disabled = false;
            $('post-button').disabled = false;
            $('message').focus();
            $('message').value = '';
        };
        websocket.onclose = function() {
            var p = document.createElement('p');
            p.innerHTML = name + ': has left the chat';

            $('display').appendChild(p);

            new Fx.Scroll('display').down();
        };
    },
    post: function() {
        var message = $F('message');
        if (!message > 0) {
            return;
        }
        $('message').disabled = true;
        $('post-button').disabled = true;

        websocket.send(message);
    },
    update: function(data) {
        if (data) {
            var p = document.createElement('p');
            p.innerHTML = data.message;

            $('display').appendChild(p);

            new Fx.Scroll('display').down();
        }
    }
};

var rules = {
    '#login-name': function(elem) {
        Event.observe(elem, 'keydown', function(e) {
            if (e.keyCode == 13) {
                $('login-button').focus();
            }
        });
    },
    '#login-button': function(elem) {
        elem.onclick = app.login;
    },
    '#message': function(elem) {
        Event.observe(elem, 'keydown', function(e) {
            if (e.shiftKey && e.keyCode == 13) {
                $('post-button').focus();
            }
        });
    },
    '#post-button': function(elem) {
        elem.onclick = app.post;
    }
};
