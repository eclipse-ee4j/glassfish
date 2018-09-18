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

function getDisplay(elem) {
    return document.getElementById(elem).style.display;
}

function setDisplay(elem, value) {
    if (typeof(elem) == 'string') {
        elem = document.getElementById(elem);
    }
    return elem.style.display = value;
}

function updateUI() {
    try {
        var jmsBasicConfig = document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optBasic').checked;
        var jmsCustomConfig = document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optCustom').checked;
    } catch (e) {
        jmsBasicConfig = false;
        jmsCustomConfig = true;
    }
    if (!(jmsBasicConfig || jmsCustomConfig)) {
        document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optBasic').checked = true;
        jmsBasicConfig = true;
    }
    updateJmsPropertySheet(!jmsBasicConfig);
}
    
function updateJmsPropertySheet(customConfig) {
    var jmsTypeSheet = 'propertyForm:jmsTypePropertySheet';
    var jmsPropsheet = 'propertyForm:jmsPropertySheet';
    if (!customConfig) {
        setDisplay(jmsTypeSheet, 'none');
        setDisplay(jmsPropsheet, 'none');
        return;
    }
    
    var baseId = jmsPropsheet + ':configureJmsClusterSection';
    var configStoreType = document.getElementById(baseId+':ConfigStoreTypeProp:configStoreType').value;
    var messageStoreType = document.getElementById(baseId+':MessageStoreTypeProp:messageStoreType').value;
    var pwdSel = document.getElementById(baseId+':PswdSelProp:pwdSel').value;
    
    var conventional = document.getElementById(baseId + ':ClusterTypeProp:optConventional').checked;
    var enhanced = document.getElementById(baseId + ':ClusterTypeProp:optEnhanced').checked;
    
    var embedded = document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:optEmbedded').checked;
    var local = document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:optLocal').checked;
    var remote = document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:optRemote').checked;

    setDisplay(jmsTypeSheet, 'block');
    setDisplay(jmsPropsheet, 'block');

    // Update hidden field for type
    document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:jmsType').value =
        (embedded ? "EMBEDDED" : (local ? "LOCAL" : "REMOTE"));
    
    if (remote) {
        setDisplay(jmsPropsheet, 'none');
    } else {     
        setDisplay(jmsPropsheet, 'block');
        
        if (embedded) {
            setDisplay(baseId + ':ClusterTypeProp:optEnhanced_span', 'none');
            document.getElementById(baseId + ':ClusterTypeProp:optConventional').checked = true;
            conventional = true;
            enhanced = false;
        } else {
            setDisplay(baseId + ':ClusterTypeProp:optEnhanced_span', 'block');
        }
        document.getElementById(baseId+':ClusterTypeProp:clusterType').value = (conventional ? "conventional" : "enhanced");
       
        if (enhanced) {
            setDisplay(baseId+':ConfigStoreTypeProp', 'none');
            setDisplay(baseId+':MessageStoreTypeProp', 'none');
            var elems = getByClass("__database");
            for (var i=0; i < elems.length; i++) {
                setDisplay(elems[i], 'table-row');
            }
            if (embedded) {
                document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:optLocal').checked = true;
                document.getElementById(jmsTypeSheet + ':jmsTypeSection:jmsTypeProp:jmsType').value = 'LOCAL';
                local = true;
                embedded = false;
            }
            fixPasswordFields(baseId, pwdSel);
        }
        
        if (conventional) {
            setDisplay(baseId+':ConfigStoreTypeProp', 'table-row');
            setDisplay(baseId+':MessageStoreTypeProp', 'table-row');

            if ((messageStoreType == 'file') && (configStoreType == 'masterbroker')) { //} && (getDisplay(baseId+':MessageStoreTypeProp') != 'none')) {
                var elems = getByClass("__database");
                for (var i=0; i < elems.length; i++) {
                    setDisplay(elems[i], 'none');
                }
            } else {
                var elems = getByClass("__database");
                for (var i=0; i < elems.length; i++) {
                    setDisplay(elems[i], 'table-row');
                }
        
                fixPasswordFields(baseId, pwdSel);
            }
        }
    }
}

function fixPasswordFields(baseId, pwdSel) {
    if (pwdSel == 'password') {
        setDisplay(baseId+':PswdTextProp', 'table-row');
        setDisplay(baseId+':PswdAliasProp', 'none');
    } else {
        setDisplay(baseId+':PswdTextProp', 'none');
        setDisplay(baseId+':PswdAliasProp', 'table-row');
    }
}

function getByClass (className, parent) {
    parent || (parent=document);
    var descendants=parent.getElementsByTagName('*'), i=-1, e, result=[];
    while (e = descendants[++i]) {
        ((' '+(e['class']||e.className)+' ').indexOf(' '+className+' ') > -1) && result.push(e);
    }
    return result;
}
