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
 * ProgressBar widget. Please see setProps for a list of supported properties.
 *
 * Note: This is considered a private API, do not use.
 */
require([
    "webui/suntheme/common",
    "webui/suntheme/field",
    "webui/suntheme/widget/props",
    "webui/suntheme/widget/common",
    "dojo/topic",
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_OnDijitClickMixin",
    "dijit/_TemplatedMixin",
    "dijit/_WidgetsInTemplateMixin",
    "dijit/form/Button",
    "dojo/text!webui/suntheme/../templates/progressBar.html"
], function (common, field, widgetProps, widgetCommon, topic, declare,
        _WidgetBase, _OnDijitClickMixin, _TemplatedMixin, template) {

    setProgressBarVisible = function (show) {
        if (show === null) {
            return false;
        }
        common.setVisibleElement(this, show);
        return true;
    };

    isProgressBarVisible = function () {
        return common.isVisibleElement(this);
    };

    setProgressBarContainerVisible = function (show) {
        if (show === null) {
            return false;
        }

        // FIX: Remove "display:block" from barContainer class and
        // use common.setVisibleElement.
        var widget = dojo.widget.byId(this.id);
        if (show === false) {
            widget.barContainer.style.display = "none";
        } else {
            widget.barContainer.style.display = '';
        }
        return true;
    };

    isProgressBarContainerVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.barContainer);
    };

    setRightControlVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.rightControlsContainer, show);
        return true;
    };

    isRightControlVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.rightControlsContainer);
    };

    setBottomControlVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.bottomControlsContainer, show);
        return true;
    };

    isBottomControlVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.bottomControlsContainer);
    };

    setStatusTextVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.bottomTextContainer, show);
        return true;
    };

    isStatusTextVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.bottomTextContainer);
    };

    setOperationTextVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.topTextContainer, show);
        return true;
    };

    isOperationTextVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.topTextContainer);
    };

    setLogMsgVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.logContainer, show);
        return true;
    };

    isLogMsgVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.logContainer);
    };

    setFailedStateMessageVisible = function (show) {
        if (show === null) {
            return false;
        }
        var widget = dojo.widget.byId(this.id);
        common.setVisibleElement(widget.failedStateContainer, show);
        return true;
    };

    isFailedStateMessageVisible = function () {
        var widget = dojo.widget.byId(this.id);
        return common.isVisibleElement(widget.failedStateContainer);
    };

    cancel = function () {
        clearTimeout(this._timeoutId);

        var widget = dojo.widget.byId(this.id);
        widget.hiddenFieldNode.value = widgetProps.progressBar.canceled;
        if (this._props.type === widgetProps.progressBar.determinate) {
            widget.innerBarContainer.style.width = "0%";
        }
        this._refresh();
    };

    pause = function () {
        clearTimeout(this._timeoutId);

        var widget = dojo.widget.byId(this.id);
        widget.hiddenFieldNode.value = widgetProps.progressBar.paused;
        if (this._props.type === widgetProps.progressBar.indeterminate) {
            widget.innerBarContainer.className =
                    widgetProps.progressBar.indeterminatePausedClassName;
        }
        this._refresh();
    };

    resume = function () {
        clearTimeout(this._timeoutId);

        var widget = dojo.widget.byId(this.id);
        widget.hiddenFieldNode.value = widgetProps.progressBar.resumed;
        if (this._props.type === widgetProps.progressBar.indeterminate) {
            widget.innerBarContainer.className =
                    widgetProps.progressBar.indeterminateClassName;
        }
        this._refresh();
    };

    stop = function () {
        clearTimeout(this._timeoutId);

        var widget = dojo.widget.byId(this.id);
        widget.hiddenFieldNode.value = widgetProps.progressBar.stopped;
        if (this._props.type === widgetProps.progressBar.indeterminate) {
            widget.innerBarIdContainer.className =
                    widgetProps.progressBar.indeterminatePaused;
        }
        this._refresh();
    };

    setOnComplete = function (func) {
        if (func) {
            this._funcComplete = func;
        }
    };

    setOnFail = function (func) {
        if (func) {
            this._funcFailed = func;
        }
    };

    setOnCancel = function (func) {
        if (func) {
            this._funcCanceled = func;
        }
    };

    sleep = function (delay) {
        var start = new Date();
        var exitTime = start.getTime() + delay;

        while (true) {
            start = new Date();
            if (start.getTime() > exitTime) {
                return;
            }
        }
    };

    setProps = function (props) {
        if (props === null) {
            return false;
        }

        // Save properties for later updates.
        if (this._props) {
            Object.extend(this._props, props); // Override existing values, if any.
        } else {
            this._props = props;
        }

        // Set DOM node properties.
        widgetCommon.setCoreProperties(this, props);
        widgetCommon.setJavaScriptProperties(this, props);

        // Set widget properties.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Set tool tip.
        if (props.toolTip) {
            widget.barContainer.title = props.toolTip;
        }

        // Add top text.
        if (props.topText) {
            widgetCommon.addFragment(widget.topTextContainer, props.topText);
            common.setVisibleElement(widget.topTextContainer, true);
        }

        // Add bottom text.
        if (props.bottomText) {
            widgetCommon.addFragment(widget.bottomTextContainer, props.bottomText);
            common.setVisibleElement(widget.bottomTextContainer, true);
        }

        if (this._props.type === widgetProps.progressBar.determinate
                || this._props.type === widgetProps.progressBar.indeterminate) {
            // Set style class.
            widget.barContainer.className =
                    widgetProps.progressBar.barContainerClassName;

            // Set height.
            if (props.barHeight !== null && props.barHeight > 0) {
                widget.barContainer.style.height = props.barHeight + "px;";
                widget.innerBarContainer.style.height = props.barHeight + "px;";
            }

            // Set width.
            if (props.barWidth !== null && props.barWidth > 0) {
                widget.barContainer.style.width = props.barWidth + "px;";
            }

            // Add right controls.
            if (props.progressControlRight !== null) {
                widgetCommon.addFragment(widget.rightControlsContainer,
                        props.progressControlRight);
                common.setVisibleElement(widget.rightControlsContainer, true);
            }

            // Add bottom controls.
            if (props.progressControlBottom !== null) {
                widgetCommon.addFragment(widget.bottomControlsContainer,
                        props.progressControlBottom);
                common.setVisibleElement(widget.bottomControlsContainer, true);
            }
        }

        if (this._props.type === widgetProps.progressBar.determinate) {
            // Set style class.
            widget.innerBarContainer.className =
                    widgetProps.progressBar.determinateClassName;

            // Set width.
            widget.innerBarContainer.style.width = this._props.progress + '%';

            // Add overlay.
            if (props.overlayAnimation === true) {
                widgetCommon.addFragment(widget.innerBarOverlayContainer,
                        this._props.progress + "%");
                common.setVisibleElement(widget.innerBarOverlayContainer, true);
            }

            // Add log.
            if (props.log !== null && props.overlayAnimation === false) {
                widgetCommon.addFragment(widget.logContainer, props.log);
                common.setVisibleElement(widget.logContainer, true);
            }
        } else if (this._props.type === widgetProps.progressBar.indeterminate) {
            // Set style class.
            widget.barContainer.className =
                    widgetProps.progressBar.barContainerClassName;
            widget.innerBarContainer.className =
                    widgetProps.progressBar.indeterminateClassName;
        } else if (this._props.type === widgetProps.progressBar.busy) {
            // Add busy image.
            if (props.busyImage) {
                widgetCommon.addFragment(widget.busyImageContainer, props.busyImage);
                common.setVisibleElement(widget.busyImageContainer, true);
            }
        }

        // Set developer specified image.
        if (props.progressImageUrl !== null) {
            widget.innerBarContainer.style.backgroundImage = 'url(' + props.progressImageUrl + ')';
        }

        // Set A11Y properties.
        if (widget.bottomTextContainer.setAttributeNS) {
            widget.bottomTextContainer.setAttributeNS("http://www.w3.org/2005/07/aaa",
                    "valuenow", this._props.progress);
        }
        return true;
    };

    refresh = {
        /**
         * Event topics for custom AJAX implementations to listen for.
         */
        beginEventTopic: "webui_widget_progressBar_refresh_begin",
        endEventTopic: "webui_widget_progressBar_refresh_end",

        /**
         * Process refresh event.
         * 
         * @param evt Event generated by scroll bar.
         */
        processEvent: function (evt) {
            // Note: evt not currently implemented. For an example, see:
            // webui.suntheme.widget.table2RowGroup.scroll.processEvent

            // publish event
            if (this._props.refreshRate > 0) {
                refresh.publishBeginEvent(this.id);
            }

            // Create a call back function to periodically refresh the progress bar.
            this._timeoutId = setTimeout(
                    refresh.createCallback(this.id),
                    this._props.refreshRate);
        },

        /**
         * Helper function to create callback to refresh server.
         *
         * @param id The HTML element id used to invoke the callback.
         */
        createCallback: function (id) {
            if (id !== null) {
                // New literals are created every time this function
                // is called, and it's saved by closure magic.
                return function () {
                    var domNode = document.getElementById(id);
                    if (domNode === null) {
                        return null;
                    } else {
                        domNode._refresh();
                    }
                };
            }
        },

        /**
         * Publish an event for custom AJAX implementations to listen for.
         */
        publishBeginEvent: function (id) {
            topic.publish(refresh.beginEventTopic, id);
            return true;
        },

        /**
         * Publish an event for custom AJAX implementations to listen for.
         *
         * @param props Key-Value pairs of properties of the widget.
         */
        publishEndEvent: function (props) {
            topic.publish(refresh.endEventTopic, props);
            return true;
        }
    };

    setProgress = function (props) {
        if (props === null) {
            return false;
        }

        // Get widget.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Adjust max value.
        if (props.progress > 99
                || props.taskState === widgetProps.progressBar.completed) {
            props.progress = 100;
        }

        // Save properties for later updates.
        if (this._props) {
            Object.extend(this._props, props); // Override existing values, if any.
        } else {
            this._props = props;
        }

        // Set status.
        if (props.status) {
            widgetCommon.addFragment(widget.bottomTextContainer, props.status);
        }

        // If top text doesnt get change, dont update.
        if (props.topText) {
            if (props.topText !== this._props.topText) {
                widgetCommon.addFragment(widget.topTextContainer, props.topText);
            }
        }

        // Update log messages.
        if (this._props.type === widgetProps.progressBar.determinate) {
            widget.innerBarContainer.style.width = props.progress + '%';

            if (props.logMessage) {
                var f = field.getInputElement(this._props.logId)
                if (f !== null) {
                    f.value = (f.value)
                            ? f.value + props.logMessage + "\n"
                            : props.logMessage + "\n";
                }
            }

            // Add overlay text.
            if (this._props.overlayAnimation === true) {
                widgetCommon.addFragment(widget.innerBarOverlayContainer,
                        props.progress + "%");
            }
        }

        // Failed state.
        if (props.taskState === widgetProps.progressBar.failed) {
            clearTimeout(this._timeoutId);
            this._sleep(1000);
            this.setProgressBarContainerVisible(false);
            this.setBottomControlVisible(false);
            this.setRightControlVisible(false);
            this.setLogMsgVisible(false);

            if (props.failedStateText !== null) {
                var text = props.failedStateText + " " + props.progress
                        + this._props.percentChar;
                widgetCommon.addFragment(widget.failedLabelContainer, text);
                common.setVisibleElement(widget.failedLabelContainer, true);
                common.setVisibleElement(widget.failedStateContainer, true);
            }
            if (this._funcFailed !== null) {
                (this._funcFailed)();
            }
            return;
        }

        // Cancel state.
        if (props.taskState === widgetProps.progressBar.canceled) {
            clearTimeout(this._timeoutId);
            this._sleep(1000);
            this.setOperationTextVisible(false);
            this.setStatusTextVisible(false);
            this.setProgressBarContainerVisible(false);
            this.setBottomControlVisible(false);
            this.setRightControlVisible(false);
            this.setLogMsgVisible(false);

            if (this._props.type === widgetProps.progressBar.determinate) {
                widget.innerBarContainer.style.width = "0%";
            }
            if (this._funcCanceled !== null) {
                (this._funcCanceled)();
            }
            return;
        }

        // paused state
        if (props.taskState === widgetProps.progressBar.paused) {
            clearTimeout(this._timeoutId);
            return;
        }

        // stopped state
        if (props.taskState === widgetProps.progressBar.stopped) {
            clearTimeout(this._timeoutId);
            return;
        }

        if (props.progress > 99
                || props.taskState === widgetProps.progressBar.completed) {
            clearTimeout(this._timeoutId);
            if (this._props.type === widgetProps.progressBar.indeterminate) {
                widget.innerBarContainer.className =
                        widgetProps.progressBar.indeterminatePausedClassName;
            }
            if (this._props.type === widgetProps.progressBar.busy) {
                this.setProgressBarContainerVisible(false);
            }
            if (this._funcComplete !== null) {
                (this._funcComplete)();
            }
        }

        // Set progress for A11Y.
        if (props.progress) {
            if (widget.bottomTextContainer.setAttributeNS) {
                widget.bottomTextContainer.setAttributeNS("http://www.w3.org/2005/07/aaa",
                        "valuenow", props.progress);
            }
        }
    };

    return declare([_WidgetBase, _OnDijitClickMixin, _TemplatedMixin], {
        templateString: template,
        // Set public functions
        cancel : cancel,
        isBottomControlVisible : isBottomControlVisible,
        isFailedStateMessageVisible : isFailedStateMessageVisible,
        isLogMsgVisible : isLogMsgVisible,
        isOperationTextVisible : isOperationTextVisible,
        isProgressBarContainerVisible : isProgressBarContainerVisible,
        isProgressBarVisible : isProgressBarVisible,
        isRightControlVisible : isRightControlVisible,
        isStatusTextVisible : isStatusTextVisible,
        pause : pause,
        resume : resume,
        stop : stop,
        setOnCancel : setOnCancel,
        setOnComplete : setOnComplete,
        setOnFail : setOnFail,
        setBottomControlVisible : setBottomControlVisible,
        setFailedStateMessageVisible : setFailedStateMessageVisible,
        setLogMsgVisible : setLogMsgVisible,
        setOperationTextVisible : setOperationTextVisible,
        setProgressBarContainerVisible : setProgressBarContainerVisible,
        setProgressBarVisible : setProgressBarVisible,
        setProps : setProps,
        setRightControlVisible : setRightControlVisible,
        setStatusTextVisible : setStatusTextVisible,
        // Set private functions (private functions/props prefixed with "_").
        _refresh : refresh.processEvent,
        _setProgress : setProgress,
        _sleep : sleep,
        postCreate: function () {
            if (this.id) {
                this.barContainer.id = this.id + "_barContainer";
                this.bottomControlsContainer.id = this.id + "_bottomControlsContainer";
                this.bottomTextContainer.id = this.id + "_bottomTextContainer";
                this.failedStateContainer.id = this.id + "_failedStateContainer";
                this.failedLabelContainer.id = this.id + "_failedLabelContainer";
                this.hiddenFieldNode.id = this.id + "_" + "controlType";
                this.hiddenFieldNode.name = this.hiddenFieldNode.id;
                this.innerBarContainer.id = this.id + "_innerBarContainer";
                this.innerBarOverlayContainer.id = this.id + "_innerBarOverlayContainer";
                this.logContainer.id = this.id + "_logContainer";
                this.rightControlsContainer.id = this.id + "_rightControlsContainer";
                this.topTextContainer.id = this.id + "_topTextContainer";
            }
            setProps({
                barHeight: this.barHeight,
                barWidth: this.barWidth,
                bottomText: this.bottomText,
                busyImage: this.busyImage,
                failedStateText: this.failedStateText,
                id: this.id,
                log: this.log,
                logId: this.logId,
                logMessage: this.logMessage,
                overlayAnimation: this.overlayAnimation,
                percentChar: (this.percentChar) ? this.percentChar : "%",
                progress: this.progress,
                progressImageUrl: this.progressImageUrl,
                progressControlBottom: this.progressControlBottom,
                progressControlRight: this.progressControlRight,
                refreshRate: this.refreshRate,
                taskState: this.taskState,
                templatePath: this.templatePath,
                toolTip: this.toolTip,
                topText: this.topText,
                type: this.type,
                visible: this.visible
            });
            // Initiate the first refresh.
            refresh();
        }
    });
});
