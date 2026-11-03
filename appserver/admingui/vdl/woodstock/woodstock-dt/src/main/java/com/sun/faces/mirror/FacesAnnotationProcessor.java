/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Event;
import com.sun.faces.annotation.Property;
import com.sun.faces.annotation.PropertyCategory;
import com.sun.faces.annotation.Renderer;
import com.sun.faces.annotation.Resolver;
import com.sun.faces.annotation.Tag;
import com.sun.faces.mirror.DeclaredRendererInfo.RendersInfo;
import com.sun.faces.mirror.generator.BeanInfoSourceGenerator;
import com.sun.faces.mirror.generator.DebugGenerator;
import com.sun.faces.mirror.generator.FacesConfigFileGenerator;
import com.sun.faces.mirror.generator.GeneratorException;
import com.sun.faces.mirror.generator.GeneratorFactory;
import com.sun.faces.mirror.generator.TagLibFileGenerator;
import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;

import jakarta.el.ELResolver;
import jakarta.el.MethodExpression;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static com.sun.rave.designtime.Constants.EventSetDescriptor.BINDING_PROPERTY;

/**
 * An annotation processor that generates JSF source and configuration files for
 * the current compilation unit, as reported by the annotation processor
 * environment.
 */
// TODO - Handle localization of property metadata inherited from external
// libraries
// TODO - Make all XxxInfo classes immutable when seen by generators
// TODO - Handle different versions of JSF (1.1 and 1.2)
// TODO - Handle attribute annotations within a tag class
@SupportedAnnotationTypes(value = {
    "com.sun.faces.annotation.Attribute",
    "com.sun.faces.annotation.Component",
    "com.sun.faces.annotation.Event",
    "com.sun.faces.annotation.Localizable",
    "com.sun.faces.annotation.Property",
    "com.sun.faces.annotation.PropertyCategory",
    "com.sun.faces.annotation.Renderer",
    "com.sun.faces.annotation.Resolver",
    "com.sun.faces.annotation.Tag"
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SuppressWarnings("checkstyle:filelength")
public final class FacesAnnotationProcessor extends AbstractProcessor {

    /**
     * Constant for the localize option.
     */
    private static final String LOCALIZE_OPTION = "localize";

    /**
     * Constant for the JavaEE version option.
     */
    private static final String JAVAEE_VERSION_OPTION = "javaee.version";

    /**
     * Constant for the generate run-time option.
     */
    private static final String GENERATE_RUNTIME_OPTION = "generate.runtime";

    /**
     * Constant for the generate design time option.
     */
    private static final String GENERATE_DESIGNTIME_OPTION =
            "generate.designtime";

    /**
     * Constant for the namespace URI option.
     */
    private static final String NAMESPACE_URI_OPTION = "namespace.uri";

    /**
     * Constant for the namespace prefix option.
     */
    private static final String NAMESPACE_PREFIX_OPTION = "namespace.prefix";

    /**
     * Constant for the tag lib option.
     */
    private static final String TAGLIB_DOC_OPTION = "taglibdoc";

    /**
     * Constant for the tag lib out option.
     */
    private static final String TAGLIB_DOC_OUT_OPTION = "taglibdoc.out";

    /**
     * Constant for the run-time out option.
     */
    private static final String RUNTIME_OUT_OPTION = "runtime.out";

    /**
     * Constant for the debug option.
     */
    private static final String DEBUG_OPTION = "debug";

    /**
     * Set of all packages that define the current compilation unit.
     */
    private final Set<String> packageNames = new HashSet<String>();

    /**
     * Set of all JSF components declared in the current compilation unit.
     */
    private final  Set<DeclaredComponentInfo> declaredComps =
            new HashSet<DeclaredComponentInfo>();

    /**
     * Set of all JSF renderer declared in the current compilation unit.
     */
    private final  Set<DeclaredRendererInfo> declaredRenderers =
            new HashSet<DeclaredRendererInfo>();

    /**
     * A map of fully qualified class names to their class info, for all classes
     * in the current compilation unit.
     */
    private final  Map<String, DeclaredClassInfo> declaredClasses =
            new HashMap<String, DeclaredClassInfo>();

    /**
     * A map of fully qualified interface names to their interface info, for all
     * interfaces in the current compilation unit.
     */
    private final  Map<String, DeclaredInterfaceInfo> declaredIfaces =
            new HashMap<String, DeclaredInterfaceInfo>();

    /**
     * A map of component names to hand-authored tag classes, for all components
     * for which hand-authored tag classes were declared.
     */
    private final  Map<String, DeclaredClassInfo> declaredTagClasses =
            new HashMap<String, DeclaredClassInfo>();

    /**
     * A map of property category names to their category descriptor info.
     */
    private final  Map<String, CategoryInfo> categories =
            new HashMap<String, CategoryInfo>();

    /**
     * A set of all Java EE EL resolvers declared in the current compilation
     * unit.
     */
    private final  Set<String> javaeeResolverNames = new HashSet<String>();

    /**
     * Config option for the namespace URI of the JSP tag lib.
     */
    private String namespaceUri = "http://www.sun.com/web/ui";

    /**
     * Config option for the namespace prefix of the JSP tag lib.
     */
    private String namespacePrefix = "ui";

    /**
     * Config option for the input JSP tag lib file.
     */
    private String taglibDoc = null;

    /**
     * Config option for the output JSP tag lib file name.
     */
    private String taglibDocOut = "taglib.xml";

    /**
     * Config option for the output {@code faces-config.xml} file name.
     */
    private String runtimeOut = "META-INF/faces-config.xml";

    /**
     * Config option to process localization properties.
     */
    private boolean localize = true;

    /**
     * Config option to generate bean info classes.
     */
    private boolean processDesignTime = true;

    /**
     * Config option to process {@code faces-config.xml}.
     */
    private boolean processRunTime = false;

    /**
     * Config option to turn on debug.
     */
    private boolean debug = false;

    /**
     * Flag used to force a single pass.
     */
    private volatile boolean done = false;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {

        if (done) {
            return true;
        }

        if (!processOptions()) {
            return true;
        }

        processAllElements(roundEnv);
        setSuperClassesInfo();
        udpateInheritedInfos();
        verifyTagClasses();

        // Finally, generate the source and config files
        try {
            generateFiles();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (GeneratorException e) {
            throw new IllegalStateException(e);
        } finally {
            done = true;
        }
        return true;
    }

    /**
     * Process options passed to the annotation processor tool. The tool should
     * be doing this processing, but the apt released with JDK 5 does not.
     *
     * @return {@code false}
     */
    private boolean processOptions() {
        Map<String, String> optionMap = processingEnv.getOptions();
        for (Entry<String, String> entry : optionMap.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name.equals(LOCALIZE_OPTION)) {
                if (value == null || value.isEmpty()) {
                    localize = true;
                } else {
                    localize = Boolean.valueOf(value);
                }
            //} else if (name.equals(JAVAEE_VERSION_OPTION)) {
                // TODO - Add support for different versions of JavaEE
            } else if (name.equals(GENERATE_RUNTIME_OPTION)) {
                if (value == null || value.isEmpty()) {
                    processRunTime = true;
                } else {
                    processRunTime = Boolean.valueOf(value);
                }
            } else if (name.equals(GENERATE_DESIGNTIME_OPTION)) {
                if (value == null || value.isEmpty()) {
                    processDesignTime = true;
                } else {
                    processDesignTime = Boolean.valueOf(value);
                }
            } else if (name.equals(NAMESPACE_URI_OPTION)) {
                if (value == null || value.length() == 0) {
                    printError("Option " + NAMESPACE_URI_OPTION
                            + " missing value");
                    return false;
                } else {
                    namespaceUri = value;
                }
            } else if (name.equals(NAMESPACE_PREFIX_OPTION)) {
                if (value == null || value.length() == 0) {
                    printError("Option " + NAMESPACE_PREFIX_OPTION
                            + " missing value");
                    return false;
                } else {
                    namespacePrefix = value;
                }
            } else if (name.equals(TAGLIB_DOC_OPTION)) {
                if (value == null || value.length() == 0) {
                    printError("Option " + TAGLIB_DOC_OPTION
                            + " missing value");
                    return false;
                } else {
                    taglibDoc = value;
                }
            } else if (name.equals(TAGLIB_DOC_OUT_OPTION)) {
                if (value == null || value.length() == 0) {
                    printError("Option " + TAGLIB_DOC_OUT_OPTION
                            + " missing value");
                    return false;
                } else {
                    taglibDocOut = value;
                }
            } else if (name.equals(RUNTIME_OUT_OPTION)) {
                if (value == null || value.length() == 0) {
                    printError("Option " + RUNTIME_OUT_OPTION
                            + " missing value");
                    return false;
                } else {
                    runtimeOut = value;
                }
            } else if (name.equals(DEBUG_OPTION)) {
                if (value == null || value.isEmpty()) {
                    debug = true;
                } else {
                    debug = Boolean.valueOf(value);
                }
            }
        }
        return true;
    }

    /**
     * Process a component class.
     * @param elt element to process
     * @return DeclaredTypeInfo or {@code null} if the type was already
     * processed
     */
    private DeclaredTypeInfo processComponent(final TypeElement elt) {
        Map<String, Object> valueMap = getAnnotationValues(elt,
                Component.class.getName());
        String qualifiedName = elt.getQualifiedName().toString();
        DeclaredClassInfo classInfo = declaredClasses.get(qualifiedName);
        DeclaredComponentInfo compInfo;
        if (classInfo == null) {
            compInfo = new DeclaredComponentInfo(processingEnv, valueMap, elt);
            declaredClasses.put(qualifiedName, compInfo);
            declaredComps.add(compInfo);
            return compInfo;
        }
        return null;
    }

    /**
     * Process a rendered class.
     * @param elt element to process
     */
    private void processRenderer(final TypeElement elt) {
        Map<String, Object> valueMap = getAnnotationValues(elt,
                Renderer.class.getName());
        DeclaredRendererInfo rendererInfo = new DeclaredRendererInfo(valueMap,
                processingEnv, elt);
        if (rendererInfo.getRenderings().isEmpty()) {
            printError(elt,
                    "No renderings declared in renderer annotation");
        }
        declaredRenderers.add(rendererInfo);
    }

    /**
     * Process a hand authored tag.
     * @param elt element to process
     */
    private void processTag(final TypeElement elt) {
        Map<String, Object> annotationValues = getAnnotationValues(elt,
                Tag.class.getName());
        String componentType = (String) annotationValues.get("componentType");
        DeclaredClassInfo tagClassInfo = new DeclaredClassInfo(processingEnv,
                elt);
        declaredTagClasses.put(componentType, tagClassInfo);
    }

    /**
     * This is a JakartaEE EL resolver.
     * @param elt element to process
     */
    private void processResolver(final TypeElement elt) {
        TypeMirror superClass = elt.getSuperclass();
        while (!superClass.getKind().equals(TypeKind.NONE)) {
            TypeElement superClassType = (TypeElement) processingEnv
                    .getTypeUtils().asElement(superClass);
            String superCName = superClassType.getQualifiedName()
                    .toString();
            if (superCName.equals(ELResolver.class.getName())) {
                javaeeResolverNames.add(elt.getQualifiedName().toString());
            }
            superClass = superClassType.getSuperclass();
        }
    }

    /**
     * Process a base class that provides one or more properties (possibly via
     * its super class).
     *
     * @param elt element to process
     * @return DeclaredTypeInfo or {@code null} if the type was already
     * processed or has a BeanInfo class available
     */
    private DeclaredTypeInfo processBaseClass(final TypeElement elt) {
        String qualifiedName = elt.getQualifiedName().toString();
        DeclaredClassInfo classInfo = declaredClasses.get(qualifiedName);
        if (classInfo == null) {
            // test if there is an existing BeanInfo class available
            // for the given element
            String beanInfoClassFile = qualifiedName
                    .replaceAll("\\.", "/") + "BeanInfo.class";
            if (this.getClass().getClassLoader()
                    .getResource(beanInfoClassFile) != null) {
                // skip this type, it needs to be introspected
                return null;
            } else {
                classInfo = new DeclaredClassInfo(processingEnv, elt);
            }
            declaredClasses.put(elt.getQualifiedName().toString(), classInfo);
            return classInfo;
        }
        // type was already processed
        return null;
    }

    /**
     * Process an interface that may provide one or more properties.
     *
     * @param elt element to process
     * @return DeclaredTypeInfo or {@code null} if the type was already
     * processed
     */
    private DeclaredTypeInfo processInterface(final TypeElement elt) {
        String qualifiedName = elt.getQualifiedName().toString();
        DeclaredInterfaceInfo ifaceInfo = declaredIfaces.get(qualifiedName);
        if (ifaceInfo == null) {
            ifaceInfo = new DeclaredInterfaceInfo(processingEnv, elt);
            declaredIfaces.put(elt.getQualifiedName().toString(), ifaceInfo);
            return ifaceInfo;
        }
        return null;
    }

    /**
     * Introspect a class using existing bean info from the class-path.
     * @param className name of the class to introspect
     * @return IntrospectedClassInfo
     */
    private IntrospectedClassInfo introspectClassInfo(final String className) {

        try {
            Class superClass = Class.forName(className);
            BeanInfo beanInfo = Introspector.getBeanInfo(superClass);
            IntrospectedClassInfo classInfo =
                    new IntrospectedClassInfo(beanInfo);
            Map<String, PropertyInfo> propInfos
                    = new HashMap<String, PropertyInfo>();
            Set<CategoryDescriptor> categoryDescriptors
                    = new HashSet<CategoryDescriptor>();
            for (PropertyDescriptor propDesc
                    : beanInfo.getPropertyDescriptors()) {
                String name = propDesc.getName();
                IntrospectedPropertyInfo propInfo
                        = new IntrospectedPropertyInfo(propDesc);
                CategoryDescriptor categoryDescriptor = (CategoryDescriptor)
                        propDesc.getValue(Constants
                                .PropertyDescriptor.CATEGORY);
                if (categoryDescriptor != null) {
                    String catName = categoryDescriptor.getName();
                    if (categories.containsKey(catName)) {
                        propInfo.setCategoryInfo(categories.get(catName));
                    } else {
                        printWarning("No category descriptor found in"
                                + " current compilation unit for '"
                                + catName + "', referenced in "
                                + beanInfo.getClass().getName());
                    }
                }
                propInfo.setDeclaringClassInfo(classInfo);
                propInfos.put(name, propInfo);
            }
            classInfo.setPropertyInfos(propInfos);
            Map<String, EventInfo> eventInfos
                    = new HashMap<String, EventInfo>();
            for (EventSetDescriptor eventDesc : beanInfo
                    .getEventSetDescriptors()) {
                if (eventDesc.getListenerMethods().length == 1) {
                    String name = eventDesc.getName();
                    IntrospectedEventInfo eventInfo
                            = new IntrospectedEventInfo(eventDesc);
                    Object propDesc = eventDesc.getValue(BINDING_PROPERTY);
                    if (propDesc != null
                            && (propDesc instanceof PropertyDescriptor)) {
                        String propName =
                                ((PropertyDescriptor) propDesc).getName();
                        PropertyInfo propInfo = propInfos.get(propName);
                        if (propInfo == null) {
                            throw new IllegalStateException(
                                    "Unable to find binding property: "
                                            + propName);
                        }
                        eventInfo.setPropertyInfo(propInfo);
                    }
                    eventInfo.setDeclaringClassInfo(classInfo);
                    eventInfos.put(name, eventInfo);
                }
            }
            classInfo.setEventInfos(eventInfos);
            return classInfo;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Process a class or interface represented by the given type element.
     * @param elt element
     * @param visitor visitor
     */
    @SuppressWarnings("unchecked")
    private void processType(final TypeElement elt,
            final MemberDeclarationVisitor visitor) {

        DeclaredTypeInfo typeInfo = null;
        visitor.reset();
        elt.accept(visitor, null);
        if (elt.getKind().equals(ElementKind.CLASS)) {
            if (elt.getAnnotation(Component.class) != null) {
                typeInfo = processComponent(elt);
            } else if (elt.getAnnotation(Renderer.class) != null) {
                processRenderer(elt);
            } else if (elt.getAnnotation(Tag.class) != null) {
                processTag(elt);
            } else if (elt.getAnnotation(Resolver.class) != null) {
                processResolver(elt);
            } else {
                typeInfo = processBaseClass(elt);
            }
        } else {
            typeInfo = processInterface(elt);
        }
        if (typeInfo != null) {
            typeInfo.setPropertyInfos(visitor.getPropertyInfos());
            typeInfo.setEventInfos(visitor.getEventInfos());
        }
    }

    /**
     * Find the enclosing class or interface for the given element.
     * @param elt element to find the enclosing class or interface of
     * @return TypeElement
     */
    private static TypeElement findEnclosingClassOrIface(final Element elt) {
        Element e = elt;
        while (e != null
                && !((e.getKind().equals(ElementKind.CLASS)
                || e.getKind().equals(ElementKind.INTERFACE))
                && e instanceof TypeElement)) {
            e = e.getEnclosingElement();
        }
        if (e == null || !(e instanceof TypeElement)) {
            throw new IllegalStateException("Unable to find enclosing class or"
                    + " interface for: " + elt.toString());
        }
        return (TypeElement) e;
    }

    /**
     * Process a scanned element.
     * @param elt element to process
     * @param visitor visitor
     */
    private void processElement(final Element elt,
            final MemberDeclarationVisitor visitor) {

        // get enclosing class or interface
        TypeElement type = findEnclosingClassOrIface(elt);
        // process the class
        processType(type, visitor);

        // recurse on the super types
        for (TypeMirror superTypeMirror : processingEnv.getTypeUtils()
                .directSupertypes(type.asType())) {

             TypeElement superType = (TypeElement) processingEnv.getTypeUtils()
                    .asElement(superTypeMirror);
             // skip the java. namespace
             if (superType.getQualifiedName().toString()
                     .startsWith(("java."))) {
                 continue;
             }
             processElement(superType, visitor);
        }
    }

    /**
     * Process the elements scanned for the given annotation class.
     * @param roundEnv processing round environment
     * @param annotClass annotation class
     * @param visitor visitor
     */
    private void processElements(final RoundEnvironment roundEnv,
            final Class<? extends Annotation> annotClass,
            final MemberDeclarationVisitor visitor) {

        for (Element elt : roundEnv.getElementsAnnotatedWith(annotClass)) {
            processElement(elt, visitor);
        }
    }

    /**
     * Find all component classes in the compilation unit, creating a skeleton
     * ComponentInfo for each, then invoke a declaration
     * MemberDeclarationVisitor that will process all properties declared within
     * each class or interface.
     *
     * @param roundEnv roundEnvironment
     */
    private void processAllElements(final RoundEnvironment roundEnv) {
        MemberDeclarationVisitor memberVisitor = new MemberDeclarationVisitor();
        memberVisitor.setCategoryMap(categories);

        processElements(roundEnv, Component.class, memberVisitor);
        processElements(roundEnv, Renderer.class, memberVisitor);
        processElements(roundEnv, Tag.class, memberVisitor);
        processElements(roundEnv, Resolver.class, memberVisitor);
        processElements(roundEnv, Property.class, memberVisitor);
        processElements(roundEnv, PropertyCategory.class, memberVisitor);
        processElements(roundEnv, Event.class, memberVisitor);
    }

    /**
     * Set super class info for all declared classes. If super class is in the
     * current compilation unit, then we have already seen it; if not, then it
     * must be introspected.
     */
    private void setSuperClassesInfo() {
        Map<String, ClassInfo> cInfos = new HashMap<String, ClassInfo>();
        for (DeclaredClassInfo cInfo : declaredClasses.values()) {
            TypeMirror mirror = cInfo.getDeclaration().getSuperclass();
            TypeElement superClass = (TypeElement) processingEnv
                    .getTypeUtils().asElement(mirror);
            String superCName = superClass.getQualifiedName().toString();
            if (declaredClasses.containsKey(superCName)) {
                cInfo.setSuperClassInfo(declaredClasses.get(superCName));
            } else if (cInfos.containsKey(superCName)) {
                cInfo.setSuperClassInfo(cInfos.get(superCName));
            } else if (!superCName.equals("java.lang.Object")) {
                IntrospectedClassInfo sCInfo = introspectClassInfo(superCName);
                cInfos.put(superCName, sCInfo);
                cInfo.setSuperClassInfo(sCInfo);
            }
        }
    }

    /**
     * Now that all annotations have been read and the inheritance hierarchy has
     * been established among all classes, update inherited and overridden
     * property metadata. This is also the time to check annotations that
     * contain cross references: properties may refer to category descriptors,
     * and they may also refer to events; components may refer to renderer
     * types.
     */
    private void udpateInheritedInfos() {
        for (DeclaredClassInfo cInfo : declaredClasses.values()) {

            // Update metadata of overriding properties and events
            updateInheritedInfo(cInfo);

            // Validate each declared property
            for (PropertyInfo propInfo : cInfo.getPropertyInfos().values()) {
                validateProperty(propInfo, cInfo);
            }

            // Validate events, and supply event listener method names if
            // defaulted
            for (EventInfo eventInfo : cInfo.getEventInfos().values()) {
                validateEvent(eventInfo, cInfo);
            }

            // If this is a component and it will generate a tag, determine
            // its preferred renderer
            if (cInfo instanceof DeclaredComponentInfo
                    && ((DeclaredComponentInfo) cInfo).isTag()) {
                determineRenderer(cInfo);
            }
        }
    }

    /**
     * A recursive method that updates the metadata for inherited and overridden
     * properties. A number of validity checks are performed on overridden
     * properties to ensure that the properties are indeed the same: the
     * property attrName and type must be equal, and neither method attrName can
     * be modified by the overriding property. Metadata from the overridden
     * property is inherited, unless it is overridden by metadata in the
     * overriding property. Finally, any properties inherited from a class not
     * in the current compilation unit are copied into this class's property
     * map.
     * @param cInfo class info
     */
    private void updateInheritedInfo(final DeclaredClassInfo cInfo) {
        // A map of all inherited properties, whether inherited from a super
        // class or from a super interface
        Map<String, PropertyInfo> sPInfos = new HashMap<String, PropertyInfo>();
        ClassInfo sCInfo = cInfo.getSuperClassInfo();
        if (sCInfo != null) {
            if (DeclaredClassInfo.class.isAssignableFrom(sCInfo.getClass())) {
                updateInheritedInfo((DeclaredClassInfo) sCInfo);
            }
            // Add properties from super class to map of inherited properties
            sPInfos.putAll(sCInfo.getPropertyInfos());
            if (DeclaredClassInfo.class.isAssignableFrom(sCInfo.getClass())) {
                sPInfos.putAll(((DeclaredClassInfo) sCInfo)
                        .getInheritedPropertyInfos());
            }
            cInfo.getMethodNames().addAll(sCInfo.getMethodNames());
        }
        Stack<TypeMirror> sIfaces = new Stack<TypeMirror>();
        sIfaces.addAll(cInfo.getDeclaration().getInterfaces());
        while (!sIfaces.isEmpty()) {
            TypeMirror ifaceTypeMirror = sIfaces.pop();
            // Add properties from super interface to map of inherited
            // properties
            if (declaredIfaces.containsKey(ifaceTypeMirror.toString())) {
                DeclaredInterfaceInfo iInfo = declaredIfaces
                        .get(ifaceTypeMirror.toString());
                for (PropertyInfo propInfo
                        : iInfo.getPropertyInfos().values()) {
                    if (sPInfos.containsKey(propInfo.getName())) {
                        printError(cInfo.getDeclaration(),
                                cInfo.getQualifiedName()
                                + " inherits property "
                                + propInfo.getName()
                                + " more than once");
                    } else {
                        sPInfos.put(propInfo.getName(), propInfo);
                    }
                }
            }
            TypeElement ifaceType = (TypeElement) processingEnv.getTypeUtils()
                    .asElement(ifaceTypeMirror);
            sIfaces.addAll(ifaceType.getInterfaces());
        }
        for (PropertyInfo pInfo : cInfo.getPropertyInfos().values()) {
            // Validate overriden properties, and merge their property info
            if (sPInfos.containsKey(pInfo.getName())
                    && pInfo instanceof DeclaredPropertyInfo) {
                DeclaredPropertyInfo thisPropInfo = (DeclaredPropertyInfo)
                        pInfo;
                PropertyInfo sPInfo = sPInfos.get(pInfo.getName());
                boolean updateInheritedValues = true;
                if (!pInfo.getType().equals(sPInfo.getType())) {
                    printError(thisPropInfo.getDeclaration(),
                            "Property in sub class must be of same type as the"
                            + " property that it overrides");
                    updateInheritedValues = false;
                }
                if (pInfo.getReadMethodName() != null
                        && sPInfo.getReadMethodName() != null
                        && !pInfo.getReadMethodName().equals(
                                sPInfo.getReadMethodName())) {
                    printError(thisPropInfo.getDeclaration(),
                            "Read method of property in sub class must"
                            + " have same name as the method that it"
                            + " overrides");
                    updateInheritedValues = false;
                }
                if (pInfo.getWriteMethodName() != null
                        && sPInfo.getWriteMethodName() != null
                        && !pInfo.getWriteMethodName().equals(
                                sPInfo.getWriteMethodName())) {
                    printError(thisPropInfo.getDeclaration(),
                            "Write method of property in sub class must have"
                            + " same name as the method that it"
                            + " overrides");
                    updateInheritedValues = false;
                }
                if (updateInheritedValues) {
                    thisPropInfo.updateInheritedValues(sPInfo);
                }
            }
        }
        // Any properties or events inherited from an interface that were not
        // overridden must
        // be added explicitly to this component's property and event info maps
        Map<String, PropertyInfo> pInfos = cInfo.getPropertyInfos();
        Map<String, EventInfo> eventInfos = cInfo.getEventInfos();
        sIfaces.clear();
        sIfaces.addAll(cInfo.getDeclaration().getInterfaces());
        while (!sIfaces.isEmpty()) {
            TypeMirror ifaceTypeMirror = sIfaces.pop();
            if (declaredIfaces.containsKey(ifaceTypeMirror.toString())) {
                DeclaredInterfaceInfo iInfo = declaredIfaces
                        .get(ifaceTypeMirror.toString());
                for (PropertyInfo pInfo : iInfo.getPropertyInfos().values()) {
                    if (!pInfos.containsKey(pInfo.getName())) {
                        pInfos.put(pInfo.getName(), pInfo);
                    }
                }
                for (EventInfo eventInfo : iInfo.getEventInfos().values()) {
                    if (!eventInfos.containsKey(eventInfo.getName())) {
                        eventInfos.put(eventInfo.getName(), eventInfo);
                    }
                }
            }
            TypeElement ifaceType = (TypeElement) processingEnv.getTypeUtils()
                    .asElement(ifaceTypeMirror);
            sIfaces.addAll(ifaceType.getInterfaces());
        }
    }

    /**
     * Verify that all declared tag classes correspond to a component class
     * in this compilation unit.
     */
    private void verifyTagClasses() {
        for (String componentType : declaredTagClasses.keySet()) {
            boolean found = false;
            for (DeclaredComponentInfo componentInfo : declaredComps) {
                if (componentType.equals(componentInfo.getType())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                printWarning(declaredTagClasses.get(componentType)
                        .getDeclaration(),
                        "No component found for tag's component type");
            }
        }
    }

    /**
     * Create a description for the given element.
     * @param elt element to describe
     * @return String
     */
    private static String elementDesc(final Element elt) {
        if ((elt.getKind().equals(ElementKind.CLASS)
                || elt.getKind().equals(ElementKind.INTERFACE))
                && elt instanceof TypeElement) {
            return ((TypeElement) elt).getQualifiedName().toString();
        }
        TypeElement parentElt = findEnclosingClassOrIface(elt);
        return parentElt.getQualifiedName() + "#" + elt.toString();
    }

    /**
     * Print an error for the given element and message.
     * @param decl element to print an error for
     * @param message error message
     */
    private void printError(final Element decl, final String message) {
        printError(elementDesc(decl) + " - " + message);
    }

    /**
     * Print an error for the given property and message.
     * @param pInfo property to print an error for
     * @param msg error message
     */
    private void printError(final PropertyInfo pInfo, final String msg) {
        if (pInfo == null) {
            printError(msg);
        } else if (pInfo instanceof DeclaredPropertyInfo) {
            printError(((DeclaredPropertyInfo) pInfo).getDeclaration(), msg);
        } else {
            printError(((IntrospectedPropertyInfo) pInfo).getName() + " - "
                    + msg);
        }
    }

    /**
     * Print an error for the given event and message.
     * @param eInfo event to print an error for
     * @param msg error message
     */
    private void printError(final EventInfo eInfo, final String msg) {
        if (eInfo == null) {
            printError(msg);
        } else if (eInfo instanceof DeclaredEventInfo) {
            printError(((DeclaredEventInfo) eInfo).getDeclaration(), msg);
        } else {
            printError(((IntrospectedEventInfo) eInfo).getName() + " - "
                    + msg);
        }
    }

    /**
     * Print an error message.
     * @param msg message
     */
    private void printError(final String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    /**
     * Print a warning for the given element and message.
     * @param decl element to print the warning for
     * @param message warning message
     */
    private void printWarning(final Element decl, final String message) {
        printWarning(elementDesc(decl) + " - " + message);
    }

    /**
     * Print an warning message.
     * @param msg message
     */
    private void printWarning(final String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    /**
     * Validate the given property.
     * @param propInfo property info
     * @param classInfo class being visited.
     */
    @SuppressWarnings("checkstyle:methodlength")
    private void validateProperty(final PropertyInfo propInfo,
            final DeclaredClassInfo classInfo) {

        Set<String> methodNameSet = classInfo.getMethodNames();

        // Ensure that property name is valid
        if (!isNameValid(propInfo.getName())) {
            printError(propInfo,
                    "The name specified is not a valid property name");
        }
        if (propInfo.getAttributeInfo() != null) {
            String name = propInfo.getAttributeInfo().getName();
            if (!isNameValid(name)) {
                printError(propInfo,
                        "The name specified is not a valid attribute name");
            }
        }

        // Ensure that property does not correspond to the special "binding"
        // tag attribute
        if (propInfo.getAttributeInfo() != null && propInfo
                .getAttributeInfo().getName().equals("binding")) {
            printError(propInfo,
                    "Property corresponds to the reserved 'binding'"
                    + " tag attribute");
        }

        // Ensure that property read method exists
        String readMethodName = propInfo.getReadMethodName();
        if (readMethodName == null) {
            readMethodName = genReadMethodName(propInfo);
            if (methodNameSet.contains(readMethodName)) {
                ((DeclaredPropertyInfo) propInfo)
                        .setReadMethodName(readMethodName);
            } else {
                readMethodName = null;
            }
        } else if (!methodNameSet.contains(readMethodName)) {
            printError(propInfo,
                    "No such property method " + readMethodName);
        } else {
            boolean methodFound = false;
            // Ensure that read method has a valid signature
            for (ExecutableElement method : classInfo.getMethods()) {
                String methodName = method.getSimpleName().toString();
                if (methodName.equals(readMethodName)) {
                    String returnType = method.getReturnType().toString();
                    if (returnType.equals(propInfo.getType())
                            && method.getParameters().isEmpty()) {
                        methodFound = true;
                        break;
                    }
                }
            }
            if (!methodFound) {
                printError(propInfo, "Method " + readMethodName + " not found");
            }
        }

        // Ensure that property write method exists
        String writeMethodName = propInfo.getWriteMethodName();
        if (writeMethodName == null) {
            writeMethodName = genWriteMethodName(propInfo);
            if (methodNameSet.contains(writeMethodName)) {
                ((DeclaredPropertyInfo) propInfo)
                        .setWriteMethodName(writeMethodName);
            } else {
                writeMethodName = null;
            }
        } else if (!methodNameSet.contains(writeMethodName)) {
            printError(propInfo, "No such property method " + writeMethodName);
        } else {
            // Ensure that write method has a valid signature
            boolean methodFound = false;
            for (ExecutableElement method : classInfo.getMethods()) {
                String methodName = method.getSimpleName().toString();
                if (methodName.equals(writeMethodName)) {
                    TypeMirror returnType = method.getReturnType();
                    List<? extends VariableElement> params = method
                            .getParameters();
                    if (returnType.getKind().equals(TypeKind.VOID)
                            && params.size() == 1
                            && params.get(0).asType().toString()
                                    .equals(propInfo.getType())) {
                        methodFound = true;
                        break;
                    }
                }
            }
            if (!methodFound) {
                printError(propInfo, "Method " + writeMethodName
                        + " not found");
            }
        }
        if (readMethodName == null && writeMethodName == null) {
            printError(propInfo, "No get or set method found for property");
        }
        if (writeMethodName == null && propInfo.getAttributeInfo() != null) {
            printError(propInfo,
                    "A read-only method cannot be associated with a"
                    + " JSP tag attribute");
        }

        // If property is categorized, verify that the referenced category
        // descriptor exists
        String categoryReferenceName = ((DeclaredPropertyInfo) propInfo)
                .getCategoryReferenceName();
        if (categoryReferenceName != null) {
            CategoryInfo categoryInfo = categories.get(categoryReferenceName);
            if (categoryInfo == null) {
                printError(((DeclaredPropertyInfo) propInfo)
                        .getDeclaration(),
                        "Reference to non-existant category descriptor: "
                        + categoryReferenceName);
            } else {
                ((DeclaredPropertyInfo) propInfo).setCategoryInfo(categoryInfo);
            }
        }

        // If property is of type jakarta.el.MethodExpression, verfiy that it is
        // annotated with a signature, or, that it refers to an event from which
        // the signature can be derived
        Element decl = ((DeclaredPropertyInfo) propInfo).getDeclaration();
        if (decl.getAnnotation(Property.Method.class) != null) {
            if (propInfo.getType().equals(MethodExpression.class.getName())) {
                Map<String, Object> valueMap = getAnnotationValues(
                        decl, Property.Method.class.getCanonicalName());
                DeclaredAttributeInfo attributeInfo = (DeclaredAttributeInfo)
                        propInfo.getAttributeInfo();
                if (valueMap.containsKey("signature")) {
                    attributeInfo.setMethodSignature((String) valueMap
                            .get("signature"));
                } else if (valueMap.containsKey("event")) {
                    String eventName = (String) valueMap.get("event");
                    EventInfo eventInfo = classInfo.getEventInfos()
                            .get(eventName);
                    if (eventInfo == null) {
                        EventInfo inheritedEventInfo = classInfo
                                .getInheritedEventInfos()
                                .get(eventName);
                        if (inheritedEventInfo != null) {
                            eventInfo = inheritedEventInfo.copy();
                            classInfo.getEventInfos().put(eventName, eventInfo);
                        }
                    }
                    if (eventInfo != null) {
                        attributeInfo.setMethodSignature(eventInfo
                                .getListenerMethodSignature());
                        eventInfo.setPropertyInfo(propInfo);
                    } else {
                        printWarning(decl, "No such component event");
                    }
                } else {
                    printError(decl,
                            "Method annotation is missing an event"
                            + " or signature element");
                }
            } else {
                printError(decl,
                        "Method annotation for property that is not of"
                        + " type " + MethodExpression.class.getName());
            }
        }
    }

    /**
     * Validate the given event.
     * @param eventInfo event info
     * @param classInfo class being visited
     */
    private void validateEvent(final EventInfo eventInfo,
            final DeclaredClassInfo classInfo) {

        Set<String> methodNameSet = classInfo.getMethodNames();
        String addListenerName = eventInfo.getAddListenerMethodName();
        if (addListenerName == null) {
            addListenerName = genAddListenerMethodName(eventInfo);
            if (eventInfo instanceof DeclaredEventInfo) {
                if (methodNameSet.contains(addListenerName)) {
                    ((DeclaredEventInfo) eventInfo)
                        .setAddListenerMethodName(addListenerName);
                } else {
                printError(eventInfo, "No add event listener method declared"
                        + " or found in " + classInfo.getDeclaration()
                                .getQualifiedName());
                }
            }
        } else if (!methodNameSet.contains(addListenerName)) {
            printError(eventInfo,
                    "No such event method " + addListenerName);
        }
        String removeListenerName = eventInfo.getRemoveListenerMethodName();
        if (removeListenerName == null) {
            if (eventInfo instanceof DeclaredEventInfo) {
                removeListenerName = genRemoveListenerMethodName(eventInfo);
                if (methodNameSet.contains(removeListenerName)) {
                    ((DeclaredEventInfo) eventInfo)
                            .setRemoveListenerMethodName(removeListenerName);
                } else {
                    printError(eventInfo, "No remove event listener method"
                            + " declared or found in "
                            + classInfo.getDeclaration().getQualifiedName());
                }
            }
        } else if (!methodNameSet.contains(removeListenerName)) {
            printError(eventInfo,
                    "No such event method " + removeListenerName + " in "
                            + classInfo.getDeclaration().getQualifiedName());
        }
        String getListenersName = eventInfo.getGetListenersMethodName();
        if (getListenersName == null) {
            getListenersName = genGetListenersMethodName(eventInfo);
            if (eventInfo instanceof DeclaredEventInfo
                    && methodNameSet.contains(getListenersName)) {
                ((DeclaredEventInfo) eventInfo)
                        .setGetListenersMethodName(getListenersName);
            }
        }
    }

    /**
     * Determine the renderer for the given class.
     * @param classInfo class to determine the renderer for
     */
    private void determineRenderer(final DeclaredClassInfo classInfo) {
        String rendererType =
                ((DeclaredComponentInfo) classInfo).getTagRendererType();
        boolean rendererFound = false;
        if (rendererType == null) {
            String componentFamily =
                    ((DeclaredComponentInfo) classInfo).getFamily();
            for (DeclaredRendererInfo rendererInfo : declaredRenderers) {
                for (RendersInfo rendersInfo : rendererInfo.getRenderings()) {
                    for (String rendererComponentFamily
                            : rendersInfo.getComponentFamilies()) {
                        if (componentFamily.equals(rendererComponentFamily)) {
                            ((DeclaredComponentInfo) classInfo)
                                    .setTagRendererType(
                                            rendersInfo.getRendererType());
                            rendererFound = true;
                        }
                    }
                }
            }
        } else {
            for (DeclaredRendererInfo rendererInfo : declaredRenderers) {
                for (RendersInfo rendersInfo : rendererInfo.getRenderings()) {
                    if (rendererType.equals(rendersInfo.getRendererType())) {
                        rendererFound = true;
                    }
                }
            }
        }
        if (!rendererFound) {
            printWarning(classInfo.getDeclaration(),
                    "No renderer found of correct renderer type and "
                    + "component family");
        }
    }

    /**
     * Generate the faces config file.
     * @param factory generator factory
     * @throws IOException if an I/O error occurs
     * @throws GeneratorException if a generation error occurs
     */
    private void generateFacesConfig(final GeneratorFactory factory)
            throws IOException, GeneratorException {

        Filer filer = processingEnv.getFiler();
        // Generate faces configuration file
        FacesConfigFileGenerator generator = factory
                .getFacesConfigFileGenerator();
        generator.setDeclaredComponentInfos(declaredComps);
        generator.setDeclaredRendererInfos(declaredRenderers);
        generator.setDeclaredJavaEEResolverNames(javaeeResolverNames);
        FileObject sourceFile = filer.createResource(
                StandardLocation.CLASS_OUTPUT, "", runtimeOut);
        generator.setPrintWriter(new PrintWriter(sourceFile.openWriter()));
        generator.generate();
    }

    /**
     * Generate the JSP tag lib.
     * @param factory generator factory
     * @throws IOException if an I/O error occurs
     * @throws GeneratorException if a generation error occurs
     */
    @SuppressWarnings("unchecked")
    private void generateJspTagLib(final GeneratorFactory factory)
            throws IOException, GeneratorException {

        if (taglibDoc != null) {
            try {
                SAXParser parser = SAXParserFactory.newInstance()
                        .newSAXParser();
                TaglibDocHandler handler = new TaglibDocHandler();
                parser.parse(new File(taglibDoc), handler);
                Map<String, String> tagDesc = handler.getTagDescriptionMap();
                Map<String, Map> tagAttrs = handler.getTagAttributeMap();
                for (DeclaredComponentInfo comp : declaredComps) {
                    String tagName = comp.getTagName();
                    if (tagDesc.containsKey(tagName)) {
                        comp.setTagDescription(tagDesc.get(tagName));
                    }
                    if (tagAttrs.containsKey(tagName)) {
                        Map<String, String> attrDescs = tagAttrs.get(tagName);
                        for (PropertyInfo propInfo
                                : comp.getPropertyInfos().values()) {
                            AttributeInfo attrInfo =
                                    propInfo.getAttributeInfo();
                            if (attrInfo instanceof DeclaredAttributeInfo
                                    && attrDescs.containsKey(
                                            attrInfo.getName())) {
                                ((DeclaredAttributeInfo) attrInfo)
                                        .setDescription(attrDescs.get(
                                                attrInfo.getName()));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                printError("Error occurred while processing input"
                        + " tag description file: "
                        + e.getMessage());
            } catch (ParserConfigurationException e) {
                printError("Error occurred while processing input"
                        + " tag description file: "
                        + e.getMessage());
            } catch (SAXException e) {
                printError("Error occurred while processing input"
                        + " tag description file: "
                        + e.getMessage());
            }
        }
        Filer filer = processingEnv.getFiler();
        TagLibFileGenerator generator = factory.getTagLibFileGenerator();
        generator.setDeclaredComponentInfoSet(declaredComps);
        generator.setNamespace(namespaceUri);
        generator.setNamespacePrefix(namespacePrefix);
        FileObject sourceFile = filer.createResource(
                StandardLocation.CLASS_OUTPUT, "", taglibDocOut);
        generator.setPrintWriter(new PrintWriter(sourceFile.openWriter()));
        generator.generate();
    }

    /**
     * Generate the bean info classes.
     * @param factory generator factory
     * @throws IOException if an I/O error occurs
     * @throws GeneratorException if a generation error occurs
     */
    private void generateBeanInfoClasses(final GeneratorFactory factory)
            throws IOException, GeneratorException {

        Filer filer = processingEnv.getFiler();
        Map<String, PropertyBundleMap> propBundles =
                new HashMap<String, PropertyBundleMap>();
        BeanInfoSourceGenerator generator = factory
                .getBeanInfoSourceGenerator();
        generator.setNamespace(namespaceUri);
        generator.setNamespacePrefix(namespacePrefix);
        for (DeclaredComponentInfo compInfo : declaredComps) {
            generator.setDeclaredComponentInfo(compInfo);
            JavaFileObject sourceFile = filer.createSourceFile(
                    generator.getQualifiedName(), compInfo.getDeclaration());
            generator.setPrintWriter(new PrintWriter(sourceFile.openWriter()));
            if (localize) {
                String pkgName = compInfo.getPackageName();
                PropertyBundleMap propBundleMap = propBundles.get(pkgName);
                if (propBundleMap == null) {
                    String qualifiedName = pkgName + ".BeanInfoBundle";
                    propBundleMap = new PropertyBundleMap(qualifiedName);
                    propBundles.put(pkgName, propBundleMap);
                }
                generator.setPropertyBundle(propBundleMap);
            }
            generator.generate();
        }
        if (localize) {
             for (PropertyBundleMap propBundle : propBundles.values()) {
                 if (propBundle.size() > 0) {
                     String qualifiedName = propBundle.getQualifiedName();
                     int lastDotIndex = qualifiedName.lastIndexOf('.');
                     String pkg = qualifiedName.substring(0, lastDotIndex);
                     String fileName = qualifiedName.substring(lastDotIndex + 1)
                             + ".properties";
                     FileObject sourceFile = filer.createResource(
                             StandardLocation.CLASS_OUTPUT,
                             pkg, fileName);
                     PrintWriter printWriter = new PrintWriter(
                             sourceFile.openWriter());
                     for (Object key : propBundle.keyList()) {
                         Object value = propBundle.get(key);
                         printWriter.println(key.toString() + "="
                                 + value.toString());
                     }
                     printWriter.close();
                 }
             }
         }
    }

    /**
     * Generate all files.
     * @throws IOException if an I/O error occurs
     * @throws GeneratorException if a generation error occurs
     */
    private void generateFiles() throws IOException, GeneratorException {

        GeneratorFactory factory = new GeneratorFactory();
        if (debug) {
            DebugGenerator debugGenerator = factory.getDebugGenerator();
            debugGenerator.setNamespace(namespaceUri);
            debugGenerator.setNamespacePrefix(namespacePrefix);
            debugGenerator.setPackageNames(packageNames);
            debugGenerator.setDeclaredComponentInfo(declaredComps);
            debugGenerator.setDeclaredRendererInfos(declaredRenderers);
            PrintWriter debugWriter = new PrintWriter(System.out);
            debugGenerator.setPrintWriter(debugWriter);
            debugGenerator.generate();
            debugWriter.close();
        }

        if (processDesignTime) {
            generateBeanInfoClasses(factory);
        }

        if (processRunTime) {
            if (declaredComps.size() > 0) {
                generateFacesConfig(factory);
            }

            // Generate JSP tag library configuration file
            if (declaredComps.size() > 0) {
                generateJspTagLib(factory);
            }
        }
    }

    /**
     * A utility method for creating a simple map of attrName-value pairs for
     * all annotation elements and values found in the annotation specified
     * among all annotations found in the declaration specified. Note that this
     * map will contain entries only for those elements and value supplied
     * explicitly in the declaration. Elements that are implicitly assuming
     * their default value will not be present.
     * @param decl enclosing class element
     * @param className class name
     * @return {@code Map<String, Object>}
     */
    private Map<String, Object> getAnnotationValues(final Element decl,
            final String className) {

        for (AnnotationMirror mirror : decl.getAnnotationMirrors()) {
            Element elt = mirror.getAnnotationType().asElement();
            if (elt instanceof TypeElement) {
                if (className.equals(((TypeElement) elt)
                        .getQualifiedName().toString())) {
                    return getAnnotationValues(mirror);
                }
            }
        }
        return null;
    }

    /**
     * A utility method for creating a simple map of attrName-value pairs for
     * all annotation elements and values found in the annotation specified.
     * @param mirror annotation mirror
     * @return {@code Map<String, Object>}
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAnnotationValues(
            final AnnotationMirror mirror) {

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> mirrorValues
                = mirror.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : mirrorValues.entrySet()) {
            String name = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if (AnnotationMirror.class.isAssignableFrom(value.getClass())) {
                // Nested annotations should also be stored as maps
                value = getAnnotationValues(((AnnotationMirror) value));
            } else if (List.class.isAssignableFrom(value.getClass())) {
                Iterator iter = ((List) value).iterator();
                ArrayList valueList = new ArrayList();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (AnnotationValue.class.isAssignableFrom(
                            obj.getClass())) {
                        Object annotationValue = ((AnnotationValue) obj)
                                .getValue();
                        if (AnnotationMirror.class.isAssignableFrom(
                                annotationValue.getClass())) {
                            valueList.add(getAnnotationValues(
                                    (AnnotationMirror) annotationValue));
                        } else {
                            valueList.add(annotationValue);
                        }
                    } else {
                        valueList.add(obj);
                    }
                }
                value = valueList;
            }
            valuesMap.put(name, value);
        }
        return valuesMap;
    }

    /**
     * Visitor implementation used to visit field and method declarations in a
     * class.
     */
    private final class MemberDeclarationVisitor extends SimpleElementVisitor6 {

        /**
         * Properties.
         */
        private Map<String, PropertyInfo> propInfos =
                new HashMap<String, PropertyInfo>();

        /**
         * Events.
         */
        private Map<String, EventInfo> eventInfos =
                new HashMap<String, EventInfo>();

        /**
         * Default property name.
         */
        private String defaultPropName = null;

        /**
         * Default event name.
         */
        private String defaultEventName = null;

        /**
         * Categories.
         */
        private Map<String, CategoryInfo> categories;

        @Override
        public Element visitVariable(final VariableElement elt,
                final Object p) {

            if (elt.getAnnotation(Property.class) != null) {
                visitPropertyField(elt);
            } else if (elt.getAnnotation(PropertyCategory.class) != null) {
                visitPropertyCategoryField(elt);
            }
            return elt;
        }

        @Override
        public Element visitExecutable(final ExecutableElement elt,
                final Object p) {

            if (elt.getAnnotation(Property.class) != null) {
                visitPropertyGetterOrSetter(elt);
            } else if (elt.getAnnotation(Event.class) != null) {
                visitEventMethod(elt);
            }
            return elt;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object visitType(final TypeElement elt, final Object p) {
            for (Element e : elt.getEnclosedElements()) {
                e.accept(this, p);
            }
            return elt;
        }

        /**
         * Get the visited properties.
         * @return {@code Map<String, PropertyInfo>}
         */
        public Map<String, PropertyInfo> getPropertyInfos() {
            return propInfos;
        }

        /**
         * Get the visited events.
         * @return {@code Map<String, EventInfo>}
         */
        public Map<String, EventInfo> getEventInfos() {
            return eventInfos;
        }

        /**
         * Set the categories.
         * @param newCategories new categories
         */
        public void setCategoryMap(
                final Map<String, CategoryInfo> newCategories) {

            this.categories = newCategories;
        }

        /**
         * Reset this visitor.
         */
        public void reset() {
            propInfos = new HashMap<String, PropertyInfo>();
            eventInfos = new HashMap<String, EventInfo>();
            defaultPropName = null;
        }

        /**
         * Visit a property field.
         * @param elt field element to visit
         */
        private void visitPropertyField(final VariableElement elt) {
            Map<String, Object> annotationMap = getAnnotationValues(elt,
                    Property.class.getName());
            DeclaredPropertyInfo propertyInfo = new DeclaredPropertyInfo(
                    annotationMap, elt, processingEnv);
            String name = (String) annotationMap.get(DeclaredPropertyInfo.NAME);
            if (name == null || name.length() == 0) {
                name = elt.getSimpleName().toString();
            }
            if (Boolean.TRUE.equals(annotationMap.get(
                    DeclaredPropertyInfo.IS_DEFAULT))) {
                if (defaultPropName == null) {
                    defaultPropName = name;
                } else {
                    printError(elt, "Duplicate default property");
                }
            }
            propertyInfo.setName(name);
            propertyInfo.setType(elt.asType().toString());
            if (propInfos.containsKey(name)) {
                printError(elt, "Duplicate property annotation");
            }
            propInfos.put(name, propertyInfo);
        }

        /**
         * Visit a property category field.
         * @param elt field element to visit
         */
        private void visitPropertyCategoryField(final VariableElement elt) {
            boolean categoryIsValid = true;
            Set<Modifier> modifiers = elt.getModifiers();
            if (!elt.asType().toString().equals(
                    CategoryDescriptor.class.getName())) {
                printError(elt, "Fields identified as property categories must"
                        + " be of type " + CategoryDescriptor.class.getName());
                categoryIsValid = false;
            }
            if (!modifiers.contains(Modifier.PUBLIC)) {
                printError(elt, "Non-public field cannot be a property"
                        + " category");
                categoryIsValid = false;
            }
            if (!modifiers.contains(Modifier.STATIC)) {
                printError(elt, "Non-static field cannot be a property"
                        + " category");
                categoryIsValid = false;
            }
            if (categoryIsValid) {
                CategoryInfo catInfo = new CategoryInfo(
                        getAnnotationValues(elt,
                                PropertyCategory.class.getName()));
                String className = elt.getEnclosingElement().asType()
                        .toString();
                String fieldName = elt.getSimpleName().toString();
                catInfo.setFieldName(className + "." + fieldName);
                categories.put(catInfo.getName(), catInfo);
            }
        }

        /**
         * Visit property getter or setter.
         * @param elt element to visit
         */
        @SuppressWarnings("checkstyle:magicnumber")
        private void visitPropertyGetterOrSetter(final ExecutableElement elt) {
            Map<String, Object> valueMap = getAnnotationValues(elt,
                    Property.class.getName());
            DeclaredPropertyInfo propertyInfo = new DeclaredPropertyInfo(
                    valueMap, elt, processingEnv);
            String name = (String) valueMap.get(DeclaredPropertyInfo.NAME);
            TypeMirror returnType = elt.getReturnType();
            List<? extends VariableElement> paramDecls = elt
                    .getParameters();
            if (returnType.getKind().equals(TypeKind.VOID)
                    && paramDecls.size() == 1) {
                // This is a "set" method
                String methodName = elt.getSimpleName().toString();
                propertyInfo.setWriteMethodName(methodName);
                propertyInfo.setType(paramDecls.iterator().next().asType()
                        .toString());
                if (name == null || name.length() == 0) {
                    if (methodName.startsWith("set")) {
                        name = methodName.substring(3, 4).toLowerCase()
                                + methodName.substring(4);
                    } else {
                        printError(elt, "Property name implied for a write"
                                + " method name that is not standard");
                    }
                }
            } else if (!returnType.getKind().equals(TypeKind.VOID)
                    && paramDecls.isEmpty()) {
                // This is a "get" method
                String methodName = elt.getSimpleName().toString();
                propertyInfo.setReadMethodName(methodName);
                propertyInfo.setType(returnType.toString());
                if (name == null || name.length() == 0) {
                    if (methodName.startsWith("get")) {
                        name = methodName.substring(3, 4).toLowerCase()
                                + methodName.substring(4);
                    } else if (propertyInfo.getType().equals("boolean")
                            && methodName.startsWith("is")) {
                        name = methodName.substring(2, 3).toLowerCase()
                                + methodName.substring(3);
                    } else {
                        printError(elt, "Property name implied for a read"
                                + " method name that is not standard");
                    }
                }
            } else {
                // This is not a valid property "get" or "set" method
                printError(elt, "Annotated property method does not have"
                        + " correct signature");
            }
            if (name != null) {
                propertyInfo.setName(name);
                if (propInfos.containsKey(name)) {
                    printError(elt, "Duplicate property annotation");
                }
                propInfos.put(name, propertyInfo);
            }
        }

        /**
         * Visit event method.
         * @param elt element to visit
         */
        private void visitEventMethod(final ExecutableElement elt) {
            Map<String, Object> values =
                    getAnnotationValues(elt, Event.class.getName());
            DeclaredEventInfo eventInfo = new DeclaredEventInfo(values, elt);
            String name = (String) values.get(DeclaredEventInfo.NAME);
            TypeMirror returnType = elt.getReturnType();
            List<? extends VariableElement> paramDecls = elt .getParameters();
            if (Boolean.TRUE.equals(values.get(DeclaredEventInfo.IS_DEFAULT))) {
                if (defaultEventName == null) {
                    defaultEventName = name;
                } else {
                    printError(elt, "Duplicate default event");
                }
            }
            if (returnType.getKind().equals(TypeKind.VOID)
                    && paramDecls.size() == 1) {
                String methodName = elt.getSimpleName().toString();
                if (methodName.startsWith("add")) {
                    eventInfo.setAddListenerMethodName(methodName);
                } else if (methodName.startsWith("remove")) {
                    eventInfo.setRemoveListenerMethodName(methodName);
                } else {
                    if (!methodName.equals(values.get(
                            DeclaredEventInfo.ADD_LISTENER_METHOD_NAME))
                            && !methodName.equals(values.get(DeclaredEventInfo
                                            .REMOVE_LISTENER_METHOD_NAME))) {
                        printError(elt, "Indeterminate event listener method"
                                + " (may be 'add' or 'remove' method)");
                    }
                    if (name == null) {
                        printError(elt, "Event name unspecified for event"
                                + " listener method with non-standard name");
                    }
                }
                TypeMirror paramType = paramDecls.iterator().next().asType();
                if (DeclaredType.class.isAssignableFrom(paramType.getClass())) {
                    // Event listener class is defined in this compilation
                    // unit
                    eventInfo.setListenerDeclaration(elt);
                } else {
                    // Event listener class is defined in an external
                    // library
                    try {
                        eventInfo.setListenerClass(
                                Class.forName(paramType.toString()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (name == null) {
                    String className = eventInfo.getListenerClassName();
                    if (className.endsWith("Listener")) {
                        int offset;
                        if (className.contains(".")) {
                            offset = className.lastIndexOf(".") + 1;
                        } else {
                            offset = 0;
                        }
                        name = className.substring(offset, offset + 1)
                                .toLowerCase() + className.substring(
                                        offset + 1,
                                        className.indexOf("Listener"));
                    } else {
                        printError(elt,
                                "Event name unspecified for event listener"
                                + " class with non-standard name");
                    }
                }
                if (name != null) {
                    eventInfo.setName(name);
                    if (eventInfos.containsKey(name)) {
                        printError(elt, "Duplicate event annotation");
                    }
                    eventInfos.put(name, eventInfo);
                }
            } else {
                printError(elt, "Invalid signature for an event listener 'add'"
                        + " or 'remove' method");
            }
        }
    }

    /**
     * Handler implementation used to collect tag and attribute descriptions
     * during the parsing of a tag lib file.
     */
    private static final class TaglibDocHandler extends DefaultHandler {

        /**
         * Tag attribute name.
         */
        private String attrName;

        /**
         * Tag attribute description.
         */
        private String attrDesc;

        /**
         * Tag name.
         */
        private String tagName;

        /**
         * Tag description.
         */
        private String tagDesc;

        /**
         * Attribute descriptions.
         */
        private Map<String, String> attrDescMap;

        /**
         * Buffer.
         */
        private final StringBuffer buffer = new StringBuffer();

        /**
         * Element stack.
         */
        private final Stack<String> elementStack = new Stack<String>();

        /**
         * Tag attribute map.
         */
        private final Map<String, Map> tagAttrMap = new HashMap<String, Map>();

        /**
         * Tag description map.
         */
        private final Map<String, String> tagDescMap =
                new HashMap<String, String>();

        @Override
        public void startDocument() throws SAXException {
            tagDescMap.clear();
            tagAttrMap.clear();
            elementStack.clear();
        }

        @Override
        public void characters(final char[] chars, final int start,
                final int length)
                throws SAXException {

            buffer.append(chars, start, length);
        }

        @Override
        public void startElement(final String uri, final String localName,
                final String qName, final Attributes attributes)
                throws SAXException {

            String lName;
            if (localName == null || localName.length() == 0) {
                lName = qName;
            } else {
                lName = localName;
            }
            if (lName.equals("tag")) {
                attrDescMap = new HashMap<String, String>();
                tagName = null;
                tagDesc = null;
            } else if (lName.equals("attribute")) {
                attrName = null;
                attrDesc = null;
            }
            elementStack.push(localName);
        }

        @Override
        public void endElement(final String uri, final String localName,
                final String qName) throws SAXException {

            elementStack.pop();
            String lName;
            if (localName == null || localName.length() == 0) {
                lName = qName;
            } else {
                lName = localName;
            }
            if (lName.equals("name")) {
                if (elementStack.peek().equals("tag")) {
                    tagName = buffer.toString().trim();
                } else {
                    attrName = buffer.toString().trim();
                }
            } else if (lName.equals("description")) {
                if (elementStack.peek().equals("tag")) {
                    tagDesc = buffer.toString().trim();
                } else {
                    attrDesc = buffer.toString().trim();
                }
            } else if (lName.equals("attribute")) {
                if (attrDesc != null) {
                    attrDescMap.put(attrName, attrDesc);
                }
            } else if (lName.equals("tag")) {
                if (tagDesc != null) {
                    tagDescMap.put(tagName, tagDesc);
                }
                tagAttrMap.put(tagName, attrDescMap);
            }
            buffer.setLength(0);
        }

        /**
         * Get the tag description map.
         * @return {@code Map<String, String>}
         */
        public Map<String, String> getTagDescriptionMap() {
            return tagDescMap;
        }

        /**
         * Get the tag attribute map.
         * @return {@code Map<String, Map>}
         */
        public Map<String, Map> getTagAttributeMap() {
            return tagAttrMap;
        }
    }

    /**
     * Validate the attrName specified, to ensure that it can serve as an
     * instance attrName in Java source or as an XML element or attribute
     * attrName in JSP source.
     * @param name name to validate
     * @return {@code true} if the name is valid, {@code false} otherwise
     */
    private static boolean isNameValid(final String name) {
        if (name == null || name.length() == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a default read method attrName for the property specified.
     * @param propInfo property to process
     * @return String
     */
    private static String genReadMethodName(final PropertyInfo propInfo) {
        String name = propInfo.getName();
        String prefix;
        if (propInfo.getType().equals("boolean")) {
            prefix = "is";
        } else {
            prefix = "get";
        }
        return prefix + name.substring(0, 1).toUpperCase()
                + name.substring(1);
    }

    /**
     * Create a default write method attrName for the property specified.
     * @param propInfo property to process
     * @return String
     */
    private static String genWriteMethodName(final PropertyInfo propInfo) {
        String name = propInfo.getName();
        return "set" + name.substring(0, 1).toUpperCase()
                + name.substring(1);
    }

    /**
     * Create a default add listener method attrName for the event specified.
     * @param eventInfo event to process
     * @return String
     */
    private static String genAddListenerMethodName(final EventInfo eventInfo) {
        String name = eventInfo.getName();
        return "add" + name.substring(0, 1).toUpperCase()
                + name.substring(1) + "Listener";
    }

    /**
     * Create a default remove listener method attrName for the event specified.
     * @param eventInfo event to process
     * @return String
     */
    private static String genRemoveListenerMethodName(
            final EventInfo eventInfo) {

        String name = eventInfo.getName();
        return "remove" + name.substring(0, 1).toUpperCase()
                + name.substring(1) + "Listener";
    }

    /**
     * Create a default get listeners method attrName for the event specified.
     * @param eventInfo event to process
     * @return String
     */
    private static String genGetListenersMethodName(final EventInfo eventInfo) {
        String name = eventInfo.getName();
        return "get" + name.substring(0, 1).toUpperCase()
                + name.substring(1) + "Listeners";
    }
}
