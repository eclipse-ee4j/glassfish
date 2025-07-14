/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.connector;

import com.sun.enterprise.config.serverbeans.Config;

import jakarta.data.repository.Repository;
import jakarta.decorator.Decorator;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import jakarta.interceptor.Interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WeldUtils {

    private static Logger logger = Logger.getLogger(WeldUtils.class.getName());

    public static final char SEPARATOR_CHAR = '/';
    public static final String WEB_INF = "WEB-INF";
    public static final String WEB_INF_CLASSES = WEB_INF + SEPARATOR_CHAR + "classes";
    public static final String WEB_INF_LIB = WEB_INF + SEPARATOR_CHAR + "lib";

    public static final String BEANS_XML_FILENAME = "beans.xml";
    public static final String WEB_INF_BEANS_XML = WEB_INF + SEPARATOR_CHAR + BEANS_XML_FILENAME;
    public static final String META_INF_BEANS_XML = "META-INF" + SEPARATOR_CHAR + BEANS_XML_FILENAME;
    public static final String WEB_INF_CLASSES_META_INF_BEANS_XML = WEB_INF_CLASSES + SEPARATOR_CHAR + META_INF_BEANS_XML;

    private static final String SERVICES_DIR = "services";

    // We don't want this connector module to depend on CDI API, as connector can be present in a distribution
    // which does not have CDI implementation. So, we use the class name as a string.
    private static final String SERVICES_PORTABLE_CLASSNAME = "jakarta.enterprise.inject.spi.Extension";
    private static final String SERVICES_BUILD_CLASSNAME = "jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension";


    public static final String META_INF_SERVICES_PORTABLE_EXTENSION =
        "META-INF" + SEPARATOR_CHAR + SERVICES_DIR + SEPARATOR_CHAR + SERVICES_PORTABLE_CLASSNAME;

    public static final String META_INF_SERVICES_BUILD_EXTENSION =
        "META-INF" + SEPARATOR_CHAR + SERVICES_DIR + SEPARATOR_CHAR + SERVICES_BUILD_CLASSNAME;

    public static final String WEB_INF_SERVICES_PORTABLE_EXTENSION =
        WEB_INF_CLASSES + SEPARATOR_CHAR + META_INF_SERVICES_PORTABLE_EXTENSION;

    public static final String WEB_INF_SERVICES_BUILD_EXTENSION =
        WEB_INF_CLASSES + SEPARATOR_CHAR + META_INF_SERVICES_BUILD_EXTENSION;



    public static final String CLASS_SUFFIX = ".class";
    public static final String JAR_SUFFIX = ".jar";
    public static final String RAR_SUFFIX = ".rar";
    public static final String EXPANDED_RAR_SUFFIX = "_rar";
    public static final String EXPANDED_JAR_SUFFIX = "_jar";

    public static enum BDAType {
        WAR, JAR, RAR, UNKNOWN
    };

    // The name of the deployment context property used to disable implicit bean discovery for a
    // particular application deployment.
    public static final String IMPLICIT_CDI_ENABLED_PROP = "implicitCdiEnabled";

    private static final List<String> cdiScopeAnnotations;
    static {
        cdiScopeAnnotations = new ArrayList<String>();
        cdiScopeAnnotations.add(Scope.class.getName());
        cdiScopeAnnotations.add(NormalScope.class.getName());
        cdiScopeAnnotations.add(ApplicationScoped.class.getName());
        cdiScopeAnnotations.add(SessionScoped.class.getName());
        cdiScopeAnnotations.add(RequestScoped.class.getName());
        cdiScopeAnnotations.add(Dependent.class.getName());
        cdiScopeAnnotations.add(Singleton.class.getName());
    }

    private static final List<String> cdiEnablingAnnotations;
    static {
        cdiEnablingAnnotations = new ArrayList<String>();

        // CDI scopes
        cdiEnablingAnnotations.addAll(cdiScopeAnnotations);

        // 1.2 updates
        cdiEnablingAnnotations.add(Decorator.class.getName());
        cdiEnablingAnnotations.add(Interceptor.class.getName());
        cdiEnablingAnnotations.add(Stereotype.class.getName());

        // EJB annotations
        cdiEnablingAnnotations.add(MessageDriven.class.getName());
        cdiEnablingAnnotations.add(Stateful.class.getName());
        cdiEnablingAnnotations.add(Stateless.class.getName());
        cdiEnablingAnnotations.add(jakarta.ejb.Singleton.class.getName());

        // Jakarta Data
        cdiEnablingAnnotations.add(Repository.class.getName());
    }

    /**
     * Determine whether the specified archive is an implicit bean deployment archive.
     *
     * @param context The deployment context
     * @param archive The archive in question
     *
     * @return true, if it is an implicit bean deployment archive; otherwise, false.
     */
    public static boolean isImplicitBeanArchive(DeploymentContext context, ReadableArchive archive) throws IOException {
        if (!isValidBdaBasedOnExtensionAndBeansXml(archive)) {
            // Refer CDI 2.0 spec section 12.1
            // Archives with extensions and no beans.xml file are not candidates for implicit bean discovery
            return false;
        }

        return isImplicitBeanArchive(context, archive.getURI());
    }

    /**
     * Determine whether the specified archive is an implicit bean deployment archive.
     *
     * @param context The deployment context
     * @param archivePath The URI of the archive
     *
     * @return true, if it is an implicit bean deployment archive; otherwise, false.
     */
    public static boolean isImplicitBeanArchive(DeploymentContext context, URI archivePath) {
        return isImplicitBeanDiscoveryEnabled(context) && hasCDIEnablingAnnotations(context, archivePath);
    }

    /**
     * Determine whether there are any beans annotated with annotations that should enable CDI processing even in the
     * absence of a beans.xml descriptor.
     *
     * @param context The DeploymentContext
     * @param path The path to check for annotated beans
     *
     * @return true, if there is at least one bean annotated with a qualified annotation in the specified path
     */
    public static boolean hasCDIEnablingAnnotations(DeploymentContext context, URI path) {
        return hasCDIEnablingAnnotations(context, Set.of(path));
    }

    /**
     * Determine whether there are any beans annotated with annotations that should enable CDI processing even in the
     * absence of a beans.xml descriptor.
     *
     * @param context The DeploymentContext
     * @param paths The paths to check for annotated beans
     *
     * @return true, if there is at least one bean annotated with a qualified annotation in the specified paths
     */
    public static boolean hasCDIEnablingAnnotations(DeploymentContext context, Collection<URI> paths) {
        List<String> result = new ArrayList<String>();

        Types types = getTypes(context);
        if (types != null) {
            for (Type type : types.getAllTypes()) {
                if (!(type instanceof AnnotationType)) {
                    for (AnnotationModel annotationModel : type.getAnnotations()) {
                        AnnotationType annotationType = annotationModel.getType();
                        if (isCDIEnablingAnnotation(annotationType) && type.wasDefinedIn(paths)) {
                            if (!result.contains(annotationType.getName())) {
                                result.add(annotationType.getName());
                            }
                        }
                    }
                }
            }
        }

        return !result.isEmpty();
    }

    /**
     * Get the names of any annotation types that are applied to beans, which should enable CDI processing even in the
     * absence of a beans.xml descriptor.
     *
     * @param context The DeploymentContext
     *
     * @return An array of annotation type names; The array could be empty if none are found.
     */
    public static String[] getCDIEnablingAnnotations(DeploymentContext context) {
        List<String> result = new ArrayList<String>();

        Types types = getTypes(context);
        if (types != null) {
            for (Type type : types.getAllTypes()) {
                if (!(type instanceof AnnotationType)) {
                    for (AnnotationModel annotationModel : type.getAnnotations()) {
                        AnnotationType annotationType = annotationModel.getType();
                        if (isCDIEnablingAnnotation(annotationType)) {
                            if (!result.contains(annotationType.getName())) {
                                result.add(annotationType.getName());
                            }
                        }
                    }
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Get the names of any classes that are annotated with bean-defining annotations, which should enable CDI processing
     * even in the absence of a beans.xml descriptor.
     *
     * @param context The DeploymentContext
     *
     * @return A collection of class names; The collection could be empty if none are found.
     */
    public static Collection<String> getCDIAnnotatedClassNames(DeploymentContext context) {
        Set<String> result = new HashSet<String>();

        Types types = getTypes(context);
        if (types != null) {
            for (Type type : types.getAllTypes()) {
                if (!(type instanceof AnnotationType)) {
                    for (AnnotationModel annotationModel : type.getAnnotations()) {
                        AnnotationType annotationType = annotationModel.getType();
                        if (isCDIEnablingAnnotation(annotationType)) {
                            if (!result.contains(annotationType.getName())) {
                                result.add(type.getName());
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determine whether the specified class is annotated with a CDI scope annotation.
     *
     * @param clazz The class to check.
     *
     * @return true, if the specified class has a CDI scope annotation; Otherwise, false.
     */
    public static boolean hasScopeAnnotation(Class<?> clazz) {
        return hasValidAnnotation(clazz, cdiScopeAnnotations, null);
    }

    /**
     * Determine whether the specified class is annotated with a CDI-enabling annotation.
     *
     * @param clazz The class to check.
     *
     * @return true, if the specified class has a CDI scope annotation; Otherwise, false.
     */
    public static boolean hasCDIEnablingAnnotation(Class<?> clazz) {
        return hasValidAnnotation(clazz, cdiEnablingAnnotations, null);
    }

    /**
     * Determine if the specified annotation type is a CDI-enabling annotation
     *
     * @param annotationType The annotation type to check
     *
     * @return true, if the specified annotation type qualifies as a CDI enabler; Otherwise, false
     */
    private static boolean isCDIEnablingAnnotation(AnnotationType annotationType) {
        return isCDIEnablingAnnotation(annotationType, null);
    }

    /**
     * Determine if the specified annotation type is a CDI-enabling annotation
     *
     * @param annotationType The annotation type to check
     * @param excludedTypeNames The Set of annotation type names that should be excluded from the analysis
     *
     * @return true, if the specified annotation type qualifies as a CDI enabler; Otherwise, false
     */
    private static boolean isCDIEnablingAnnotation(AnnotationType annotationType, Set<String> excludedTypeNames) {
        Set<String> exclusions = new HashSet<String>();
        if (excludedTypeNames != null) {
            exclusions.addAll(excludedTypeNames);
        }

        String annotationTypeName = annotationType.getName();
        if (cdiEnablingAnnotations.contains(annotationTypeName) && !exclusions.contains(annotationTypeName)) {
            return true;
        }

        if (!exclusions.contains(annotationTypeName)) {
            // If the annotation type itself is not an excluded type, then check it's annotation
            // types, less itself (to avoid infinite recursion)
            exclusions.add(annotationTypeName);
            for (AnnotationModel parent : annotationType.getAnnotations()) {
                if (isCDIEnablingAnnotation(parent.getType(), exclusions)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determine whether the specified class is annotated with one of the annotations in the specified validScopes
     * collection, but not with any of the annotations in the specified exclusion set.
     *
     * @param annotatedClass The class to check.
     * @param validScopes A collection of valid CDI scope type names
     * @param excludedScopes A collection of excluded CDI scope type names
     *
     * @return true, if the specified class has at least one of the annotations specified in validScopes, and none of the
     * annotations specified in excludedScopes; Otherwise, false.
     */
    public static boolean hasValidAnnotation(Class<?> annotatedClass, Collection<String> validScopes, Collection<String> excludedScopes) {
        // Check all the annotations on the specified Class to determine if the class is annotated
        // with a supported CDI scope
        for (Annotation annotation : annotatedClass.getAnnotations()) {
            if (isValidAnnotation(annotation.annotationType(), validScopes, excludedScopes)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine whether the specified annotation type is one of the specified valid types and not in the specified
     * exclusion list. Positive results include those annotations which are themselves annotated with types in the valid
     * list.
     *
     * @param annotationType The annotation type to check
     * @param validTypeNames The valid annotation type names
     * @param excludedTypeNames The excluded annotation type names
     *
     * @return true, if the specified type is in the valid list and not in the excluded list; Otherwise, false.
     */
    protected static boolean isValidAnnotation(Class<? extends Annotation> annotationType, Collection<String> validTypeNames, Collection<String> excludedTypeNames) {
        boolean result = false;

        if (validTypeNames != null && !validTypeNames.isEmpty()) {

            HashSet<String> excludedScopes = new HashSet<String>();
            if (excludedTypeNames != null) {
                excludedScopes.addAll(excludedTypeNames);
            }

            String annotationTypeName = annotationType.getName();
            if (validTypeNames.contains(annotationTypeName) && !excludedScopes.contains(annotationTypeName)) {
                result = true;
            } else if (!excludedScopes.contains(annotationTypeName)) {
                // If the annotation type itself is not an excluded type, then check it's annotation
                // types, less itself (to avoid infinite recursion)
                excludedScopes.add(annotationTypeName);
                for (Annotation parent : annotationType.getAnnotations()) {
                    if (isValidAnnotation(parent.annotationType(), validTypeNames, excludedScopes)) {
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    private static Types getTypes(DeploymentContext context) {
        String metadataKey = Types.class.getName();

        Types types = (Types) context.getTransientAppMetadata().get(metadataKey);
        while (types == null) {
            context = ((ExtendedDeploymentContext) context).getParentContext();
            if (context != null) {
                types = (Types) context.getTransientAppMetadata().get(metadataKey);
            } else {
                break;
            }
        }

        return types;
    }

    public static boolean isImplicitBeanDiscoveryEnabled() {
        boolean result = false;

        // Check the "global" configuration
        ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        if (serviceLocator != null) {
            Config config = serviceLocator.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
            if (config != null) {
                result = Boolean.valueOf(config.getExtensionByType(CDIService.class).getEnableImplicitCdi());
            }
        }

        return result;
    }

    public static boolean isImplicitBeanDiscoveryEnabled(DeploymentContext context) {
        if (!isImplicitBeanDiscoveryEnabled()) {
            return false;
        }

        // If implicit discovery is enabled for the server, then check if it's disabled for the
        // deployment of this application.
        Object propValue = context.getAppProps().get(IMPLICIT_CDI_ENABLED_PROP);

        // If the property is not set, or it's value is true, then implicit discovery is enabled
        return propValue == null || Boolean.parseBoolean((String) propValue);
    }

    public static InputStream getBeansXmlInputStream(DeploymentContext context) {
        return getBeansXmlInputStream(context.getSource());
    }

    /**
     * Determine if an archive is a valid bda based on what the spec says about extensions. See section 12.1 which states
     * that if an archive contains an extension but no beans.xml then it is NOT a valid bean deployment archive.
     *
     * @param archive The archive to check.
     * @return false if there is an extension and no beans.xml true otherwise
     */
    public static boolean isValidBdaBasedOnExtensionAndBeansXml(ReadableArchive archive) {
        try {
            if (hasExtension(archive) && !hasBeansXMl(archive)) {
                // Extension and no beans.xml: not a bda
                return false;
            }
        } catch (IOException ignore) {
        }

        return true;
    }

    public static boolean hasExtension(ReadableArchive archive) {
        try {
            if (isWar(archive)) {
                return
                    archive.exists(WEB_INF_SERVICES_PORTABLE_EXTENSION) ||
                    archive.exists(WEB_INF_SERVICES_BUILD_EXTENSION);
            }

            return
                archive.exists(META_INF_SERVICES_PORTABLE_EXTENSION) ||
                archive.exists(META_INF_SERVICES_BUILD_EXTENSION);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
            return false;
        }
    }

    public static boolean hasBeansXMl(ReadableArchive archive) throws IOException {
        if (isWar(archive)) {
            return
                archive.exists(WEB_INF_BEANS_XML) ||
                archive.exists(WEB_INF_CLASSES_META_INF_BEANS_XML);
        }

        return
            archive.exists(META_INF_BEANS_XML);
    }

    public static boolean isWar(ReadableArchive archive) throws IOException {
        return archive.exists(WEB_INF);
    }

    public static InputStream getBeansXmlInputStream(ReadableArchive archive) {
        InputStream inputStream = null;

        try {
            if (archive.exists(WEB_INF)) {
                inputStream = archive.getEntry(WEB_INF_BEANS_XML);
                if (inputStream == null) {
                    inputStream = archive.getEntry(WEB_INF_CLASSES_META_INF_BEANS_XML);
                }
            } else {
                inputStream = archive.getEntry(META_INF_BEANS_XML);
            }
        } catch (IOException e) {
            return null;
        }

        return inputStream;
    }

    /**
     * Get the "bean-discovery-mode" from the "beans" element if it exists in beans.xml
     *
     * From section 12.1 of CDI spec:
     * A bean archive has a bean discovery mode of all, annotated or none. A bean archive which contains a beans.xml file with
     * no version has a default bean discovery mode of annotated. A bean archive which contains a beans.xml file with version 1.1
     * (or later) must specify the bean-discovery-mode attribute. The default value for the attribute is annotated.
     *
     * @param beansXmlInputStream The InputStream for the beans.xml to check.
     * @return "annotated" if there is no beans.xml, "annotated" if the bean-discovery-mode is missing, "annotated" if the
     * bean-discovery-mode is empty. The value of bean-discovery-mode in all other cases.
     */
    public static String getBeanDiscoveryMode(InputStream beansXmlInputStream) {
        if (beansXmlInputStream == null) {
            // there is no beans.xml.
            return "annotated";
        }

        String beanDiscoveryMode = null;
        LocalDefaultHandler handler = new LocalDefaultHandler();
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(beansXmlInputStream, handler);
        } catch (SAXStoppedIntentionallyException exc) {
            beanDiscoveryMode = handler.getBeanDiscoveryMode();
        } catch (Exception ignore) {
        }

        if (beanDiscoveryMode == null) {
            // Empty beans.xml or bean-discovery-mode attribute not specified
            return "annotated";
        }

        if (beanDiscoveryMode.equals("")) {
            return "annotated";
        }

        return beanDiscoveryMode;
    }

    private static class LocalDefaultHandler extends DefaultHandler {
        String beanDiscoveryMode = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("beans")) {
                beanDiscoveryMode = attributes.getValue("bean-discovery-mode");
                throw new SAXStoppedIntentionallyException();
            }
        }

        public String getBeanDiscoveryMode() {
            return beanDiscoveryMode;
        }
    }

    private static class SAXStoppedIntentionallyException extends SAXException {
        private static final long serialVersionUID = 1L;
    }

}
