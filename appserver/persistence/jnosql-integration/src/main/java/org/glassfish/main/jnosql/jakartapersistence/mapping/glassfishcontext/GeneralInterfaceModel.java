package org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;


/**
 *
 * @author Ondro Mihalyi
 */
record GeneralInterfaceModel(InterfaceModel plainInterface,
    ParameterizedInterfaceModel parameterizedInterface,
    Collection<ParameterizedInterfaceModel> parameterizedTypes) {

    GeneralInterfaceModel(InterfaceModel plainInterface) {
        this(plainInterface, null, null);
    }

    GeneralInterfaceModel(ParameterizedInterfaceModel parameterizedInterfaceModel) {
        this(null, parameterizedInterfaceModel, null);
    }

    GeneralInterfaceModel(ParameterizedInterfaceModel parameterizedInterfaceModel, Collection<ParameterizedInterfaceModel> parameterizedTypes) {
        this(null, parameterizedInterfaceModel, parameterizedTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plainInterfaceName(), parameterizedInterfaceFullName(), this.parameterizedTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneralInterfaceModel other = (GeneralInterfaceModel) obj;
        if (!Objects.equals(this.plainInterfaceName(), other.plainInterfaceName())) {
            return false;
        }
        if (!Objects.equals(this.parameterizedInterfaceFullName(), other.parameterizedInterfaceFullName())) {
            return false;
        }
        return Objects.equals(this.parameterizedTypes, other.parameterizedTypes);
    }

    private Object plainInterfaceName() {
        return this.plainInterface != null ? this.plainInterface.getName() : null;
    }

    private Object parameterizedInterfaceFullName() {
        return this.parameterizedInterface != null ? parameterizedInterface.getName() : null;
    }

    boolean isParameterized() {
        return parameterizedInterface != null;
    }

    /*
    Is an interface with generics but no declared type parameters. E.g. it's List<T> but not List<String>
    - type of parameters is unknown.
     */
    boolean hasTypeParametersWithUnknownType() {
        if (isParameterized()) {
            return parametizedTypes().isEmpty();
        }
        Map<String, ParameterizedInterfaceModel> formalTypeParameters = plainInterface.getFormalTypeParameters();
        return formalTypeParameters != null && !formalTypeParameters.isEmpty();
    }

    String interfaceName() {
        if (isParameterized()) {
            return parameterizedInterface.getRawInterfaceName();
        } else {
            return plainInterface.getName();
        }
    }

    AnnotationModel getAnnotation(Class<?> annotationClass) {
        if (isParameterized()) {
            return parameterizedInterface.getRawInterface().getAnnotation(annotationClass.getName());
        }
        return plainInterface.getAnnotation(annotationClass.getName());
    }

    ExtensibleType toTypeModel() {
        return isParameterized() ? parameterizedInterface.getRawInterface() : plainInterface;
    }

    Stream<GeneralInterfaceModel> interfacesAsStream() {
        final ExtensibleType typeModel = toTypeModel();
        final Collection<ParameterizedInterfaceModel> parameterizedInterfaces = typeModel.getParameterizedInterfaces();
        final Collection<InterfaceModel> plainInterfaces = typeModel.getInterfaces();
        return Stream.concat(parameterizedInterfaces.stream().map(parameterizedInterface -> GeneralInterfaceModel.parameterizedFromSubInterface(parameterizedInterface, this)), plainInterfaces.stream().map(GeneralInterfaceModel::new));
    }

    Collection<ParameterizedInterfaceModel> parametizedTypes() {
        return parameterizedTypes != null ? parameterizedTypes : parameterizedInterface.getParametizedTypes();
    }

    /*
    Creates an instance for a give parameterized interface and captures parameter types from the interface that extends it. Parameter types are not defined on superinterfaces therefore the information about them is lost down the line. E.g. for IntfB extends IntfA<String>, IntfA<T>, the information about String type is present only on IntfB. IntfA only knows the parameter name is "T" but not that its type is String.
     */
    static GeneralInterfaceModel parameterizedFromSubInterface(ParameterizedInterfaceModel parameterizedInterface, GeneralInterfaceModel subInterface) {

        if (!subInterface.isParameterized()) {
            return new GeneralInterfaceModel(parameterizedInterface);
        }

        List<ParameterizedInterfaceModel> parameterizedTypes = new ArrayList<>();
        final Map<String, ParameterizedInterfaceModel> formalTypeParametersOnSubInterface = subInterface.toTypeModel().getFormalTypeParameters();
        final Iterator<ParameterizedInterfaceModel> iteratorThroughParameterizedTypesOnSubInterface = subInterface.parametizedTypes().iterator();

        for (Map.Entry<String, ParameterizedInterfaceModel> formalTypeParameterOnSubInterface : formalTypeParametersOnSubInterface.entrySet()) {

            if (!iteratorThroughParameterizedTypesOnSubInterface.hasNext()) {
                throw new IllegalStateException("The number of parameterized types and formal type parameters is not the same, interface: " + parameterizedInterface);
            }

            final ParameterizedInterfaceModel parameterizedTypeOnSubInterface = iteratorThroughParameterizedTypesOnSubInterface.next();
            final Map formalTypeParametersOnInterface = parameterizedInterface.getRawInterface().getFormalTypeParameters();

            if (formalTypeParametersOnInterface.containsKey(formalTypeParameterOnSubInterface.getKey())) {
                parameterizedTypes.add(parameterizedTypeOnSubInterface);
            }
        }

        return new GeneralInterfaceModel(parameterizedInterface, parameterizedTypes);
    }
}
