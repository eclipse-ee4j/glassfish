/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.stringsubs.AttributePreprocessor;
import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutor;
import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutableFactory;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutionAlgorithm;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ChangePair;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ChangePairRef;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Component;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Defaults;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Group;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.GroupRef;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ModeType;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Property;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.PropertyType;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * A class to encapsulate string-subs definition. Parse, validate and performs String substitution for the given
 * string-subs.xml.
 */
public class StringSubstitutionEngine implements StringSubstitutor {
    private static final Logger LOG = System.getLogger(StringSubstitutionEngine.class.getName());

    private static final LocalStringsImpl _strings = new LocalStringsImpl(StringSubstitutionEngine.class);

    //Root of JAXB parsed string-subs configuration
    private StringsubsDefinition _root = null;
    private Map<String, Pair> _changePairsMap = null;
    Map<String, Property> _defaultProperties = null;
    private SubstitutableFactory _substitutableFactory = new SubstituableFactoryImpl();
    private AttributePreprocessor _attrPreprocessor = new AttributePreprocessorImpl();

    /**
     * Constructs {@link StringSubstitutionEngine} based on the given string-subs configuration stream. Engine parse and
     * validate the configuration and build the internal representation to perform string substitution.
     *
     * @param inputStream The string-subs configuration stream.
     * @throws StringSubstitutionException If any error occurs in engine initialization.
     */
    public StringSubstitutionEngine(InputStream inputStream) throws StringSubstitutionException {
        if (inputStream == null) {
            throw new StringSubstitutionException("InputStream is null");
        }
        _root = StringSubstitutionParser.parse(inputStream);
    }

    @Override
    public void setAttributePreprocessor(AttributePreprocessor attributePreprocessor) {
        _attrPreprocessor = attributePreprocessor;
    }

    @Override
    public void setEntryFactory(SubstitutableFactory factory) {
        _substitutableFactory = factory;
    }

    @Override
    public void setFileBackupLocation(File backupLocation) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<Property> getDefaultProperties(PropertyType type) {
        Defaults defaults = _root.getDefaults();
        if (defaults == null) {
            return Collections.emptyList();
        }
        if (type == null) {
            return defaults.getProperty();
        }
        List<Property> props = new ArrayList<>();
        for (Property prop : defaults.getProperty()) {
            if (prop.getType().equals(type)) {
                props.add(prop);
            }
        }
        return props;
    }

    @Override
    public void substituteAll() throws StringSubstitutionException {
        for (Component component : _root.getComponent()) {
            doSubstitution(component);
        }
    }

    @Override
    public void substituteComponents(List<String> components) throws StringSubstitutionException {
        if (!isValid(components)) {
            throw new StringSubstitutionException(_strings.get("missingComponentIdentifiers"));
        }
        for (String componentId : components) {
            Component component = findComponentById(componentId);
            if (component == null) {
                LOG.log(Level.INFO, () -> "Component " + componentId + " is not present.");
                continue;
            }
            doSubstitution(component);
        }
    }

    @Override
    public void substituteGroups(List<String> groups) throws StringSubstitutionException {
        if (!isValid(groups)) {
            throw new StringSubstitutionException(_strings.get("missingGroupIdentifiers"));
        }
        for (String groupId : groups) {
            Group group = findGroupById(groupId);
            if (group == null) {
                LOG.log(WARNING, "Group {0} is not present.", groupId);
                continue;
            }
            doSubstitution(group);
        }
    }

    @Override
    public StringsubsDefinition getStringSubsDefinition() {
        return _root;
    }

    /**
     * Perform's string substitution for a given component.
     *
     * @param component {@link Component} for which the string substitution has to be performed.
     * @throws StringSubstitutionException If any error occurs during substitution.
     */
    private void doSubstitution(Component component) throws StringSubstitutionException {
        List<? extends GroupRef> refList = component.getGroupRef();
        for (GroupRef ref : refList) {
            doSubstitution(findGroupById(ref.getName()));
        }
    }

    /**
     * Perform's string substitution for a given group.
     *
     * @param groups Groups for which the string substitution has to be performed.
     * @throws StringSubstitutionException If any error occurs during substitution.
     */
    private void doSubstitution(Group group) throws StringSubstitutionException {
        List<? extends FileEntry> fileList = group.getFileEntry();
        List<? extends Archive> archiveList = group.getArchive();
        if (!isValid(fileList) && !isValid(archiveList)) {
            if (LOG.isLoggable(Level.DEBUG)) {
                LOG.log(Level.DEBUG, _strings.get("noSubstitutableGroupEntry", group.getId()));
            }
            return;
        }
        List<? extends ChangePairRef> refList = group.getChangePairRef();
        if (!isValid(refList)) {
            if (LOG.isLoggable(Level.DEBUG)) {
                LOG.log(Level.DEBUG, _strings.get("noChangePairForGroup", group.getId()));
            }
            return;
        }

        String groupMode = null;
        ModeType modeType = group.getMode();
        if (modeType != null) {
            groupMode = modeType.value();
        }
        buildChangePairsMap();
        Map<String, String> substitutionMap = new HashMap<>();
        for (ChangePairRef ref : refList) {
            String name = ref.getName();
            String localMode = ref.getMode();
            // if mode is not specified for this change-pair-ref
            // then inherit the mode of the group
            if (localMode == null || localMode.length() == 0) {
                localMode = groupMode;
            }

            Pair pair = _changePairsMap.get(name);
            if (pair == null) {
                LOG.log(Level.INFO, "Change-Pair {0} referred by group {1} is not deDEBUGd.", name, group.getId());
                continue;
            }
            String beforeString = pair.getBefore();
            String afterString = pair.getAfter();

            if (localMode == null || localMode.length() == 0) {
                if (LOG.isLoggable(DEBUG)) {
                    LOG.log(DEBUG, _strings.get("noModeValue", group.getId()));
                }
            } else {
                try {
                    afterString = ModeProcessor.processModeType(ModeType.fromValue(localMode), afterString);
                } catch (Exception e) {
                    LOG.log(WARNING, "Invalid Mode Type {0}.", localMode);
                }
            }
            substitutionMap.put(beforeString, afterString);
        }
        SubstitutionAlgorithm algorithm = new SubstitutionAlgorithmFactory().getAlgorithm(substitutionMap);
        for (FileEntry fileEntry : fileList) {
            fileEntry.setName(_attrPreprocessor.substitutePath(fileEntry.getName()));
            List<? extends Substitutable> substituables = _substitutableFactory.getFileEntrySubstituables(fileEntry);
            for (Substitutable substituable : substituables) {
                algorithm.substitute(substituable);
                substituable.finish();
            }
        }

        for (Archive archive : archiveList) {
            if (archive == null || archive.getName().isEmpty()) {
                continue;
            }
            try {
                archive.setName(_attrPreprocessor.substitutePath(archive.getName()));
                List<? extends Substitutable> substituables = _substitutableFactory.getArchiveEntrySubstitutable(archive);
                if (!isValid(substituables)) {
                    continue;
                }
                for (Substitutable substituable : substituables) {
                    algorithm.substitute(substituable);
                    substituable.finish();
                }
            } catch (Exception e) {
                LOG.log(WARNING, () -> "Failed to update jar " + archive.getName() + " with the substitutable files", e);
            }
        }
    }

    /**
     * Build's a HashMap containing an entry for each <change-pair> in the string-subs configuration file. The HashMap is
     * created so that <change-pair> elements do not need to be re-analyzed each time they're referenced.
     *
     */
    private void buildChangePairsMap() {
        if (_changePairsMap == null || _changePairsMap.isEmpty()) {
            Defaults defaults = _root.getDefaults();
            if (defaults != null) {
                List<Property> properties = defaults.getProperty();
                if (!properties.isEmpty()) {
                    _defaultProperties = new HashMap<>(properties.size(), 1);
                    for (Property prop : properties) {
                        _defaultProperties.put(prop.getKey(), prop);
                    }
                }
            }
            List<? extends ChangePair> changePairList = _root.getChangePair();
            _changePairsMap = new HashMap<>(changePairList.size());
            for (ChangePair pair : _root.getChangePair()) {
                String id = pair.getId();
                String beforeValue = pair.getBefore();
                String afterValue = pair.getAfter();
                if (id == null || beforeValue == null || afterValue == null) {
                    LOG.log(Level.INFO, "Found an empty <change-pair/>.");
                    continue;
                }
                beforeValue = _attrPreprocessor.substituteBefore(beforeValue);
                afterValue = _attrPreprocessor.substituteAfter(afterValue);
                _changePairsMap.put(id, new Pair(beforeValue, afterValue));
            }
        }
    }

    /**
     * Find {@link Group} by the given id. Returns <code>null</code> if no group found.
     *
     * @param id Identifier for a group.
     * @return Matched Group.
     */
    private Group findGroupById(String id) {
        if (id == null) {
            return null;
        }

        List<? extends Group> groupList = _root.getGroup();
        if (!isValid(groupList)) {
            return null;
        }

        for (Group group : groupList) {
            if (id.equals(group.getId())) {
                return group;
            }
        }
        return null;
    }

    /**
     * Find {@link Component} by the given id. Returns <code>null</code> if no component found.
     *
     * @param id Identifier for a component.
     * @return Matched component.
     */
    private Component findComponentById(String id) {
        if (id == null) {
            return null;
        }

        List<? extends Component> components = _root.getComponent();
        if (!isValid(components)) {
            return null;
        }

        for (Component component : components) {
            if (id.equals(component.getId())) {
                return component;
            }
        }
        return null;
    }

    /**
     * Check's if the give {@link Collection} is valid. A non null and non empty Collection is termed as valid Collection.
     *
     * @param collection Collection to validate
     * @return <code>true</code> for valid Collection and <code>false</code> for invalid Collection.
     */
    private boolean isValid(Collection<? extends Object> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * A class to store the before, after tuple. Use to store before and after value of change-pair.
     */
    private static class Pair {
        String _before, _after;

        Pair(String before, String after) {
            _before = before;
            _after = after;
        }

        public String getBefore() {
            return _before;
        }

        public String getAfter() {
            return _after;
        }
    }
}
