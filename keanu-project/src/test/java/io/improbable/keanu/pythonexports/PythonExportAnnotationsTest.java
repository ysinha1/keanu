package io.improbable.keanu.pythonexports;

import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import io.improbable.keanu.vertices.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.System.err;

public class PythonExportAnnotationsTest {

    private String verticesPackageName = "io.improbable.keanu.vertices";
    private Class<?> booleanVertex;
    private Class<?> doubleVertex;
    private Class<?> integerVertex;
    private Class<?> booleanTensor;
    private Class<?> doubleTensor;
    private Class<?> integerTensor;
    private String permittedParameterTypes = "BooleanVertex, DoubleVertex, IntegerVertex, " +
        "Vertex<? extends BooleanTensor>, Vertex<? extends DoubleTensor> or Vertex<? extends IntegerTensor>";

    @Before
    public void createTypeClasses() throws ClassNotFoundException {
        booleanVertex = Class.forName("io.improbable.keanu.vertices.bool.BoolVertex");
        doubleVertex = Class.forName("io.improbable.keanu.vertices.dbl.DoubleVertex");
        integerVertex = Class.forName("io.improbable.keanu.vertices.intgr.IntegerVertex");
        booleanTensor = Class.forName("io.improbable.keanu.tensor.bool.BooleanTensor");
        doubleTensor = Class.forName("io.improbable.keanu.tensor.dbl.DoubleTensor");
        integerTensor = Class.forName("io.improbable.keanu.tensor.intgr.IntegerTensor");
    }

    @Test
    public void annotationsOnAllAppropriateVertexConstructors() throws ClassNotFoundException {
        Set<Class<? extends Vertex>> vertexClasses = getAllVertexClassesInPackage(verticesPackageName);
        long numAppropriateUses = vertexClasses.stream()
            .filter(this::checkVertexUsesExportToPythonBindingsAnnotationAppropriately).count();

        long errors = vertexClasses.size() - numAppropriateUses;
        if (errors > 0) {
            err.println(errors + " errors detected");
        }
    }

    private Set<Class<? extends Vertex>> getAllVertexClassesInPackage(String packageName) {
        return new Reflections(verticesPackageName).getSubTypesOf(Vertex.class);
    }

    /*
     * Checks to see whether a class has the appropriate @ExportToPythonBindings annotation.
     *
     * Abstract vertices should not have this annotation. GenericVertices also should not have this annotation as they
     * are currently unsupported in the Keanu Python API.
     *
     * All other vertex classes should have this annotation on a maximum of one construct. The constructor should take
     * exclusively BooleanVertex, DoubleVertex, IntegerVertex, Vertex<? extends BooleanTensor>, Vertex<? extends DoubleTensor>
     * or Vertex<? extends IntegerTensor> parameters. Classes without constructors that meet this constrain should not
     * have this annotation.
     *
     */
    private boolean checkVertexUsesExportToPythonBindingsAnnotationAppropriately(Class<? extends Vertex> vertex) {
        long numExportAnnotationUses = countExportToPythonBindingsAnnotationUses(vertex);

        if (isAbstract(vertex)) {
            if (numExportAnnotationUses == 0) {
                return true;
            }

            outputAbstractClassAnnotatedErrorMessage(vertex);
            return false;
        }

        List<Constructor> exportableConstructors = getExportableConstructors(vertex);
        if (exportableConstructors.size() > 0) {
            if (numExportAnnotationUses == 0) {
                outputNoAnnotationsErrorMessage(vertex, exportableConstructors);
                return false;
            }

            if (numExportAnnotationUses > 1) {
                outputTooManyAnnotationsErrorMessage(vertex, exportableConstructors);
                return false;
            }

            if (countAppropriateExportAnnotationUses(vertex) == 0) {
                outputInappropriateAnnotationUseErrorMessage(vertex, exportableConstructors);
                return false;
            }
        }

        return true;
    }

    private long countExportToPythonBindingsAnnotationUses(Class<?> vertex) {
        return Arrays.stream(vertex.getDeclaredConstructors())
            .filter(this::hasExportToPythonBindingsAnnotation).count();
    }

    private boolean hasExportToPythonBindingsAnnotation(Constructor constructor) {
        return Arrays.stream(constructor.getDeclaredAnnotations())
            .anyMatch(annotation -> annotation instanceof ExportVertexToPythonBindings);
    }

    private boolean isAbstract(Class<?> cls) {
        return Modifier.isAbstract(cls.getModifiers());
    }

    private List<Constructor> getExportableConstructors(Class<? extends Vertex> vertex) {
        return Arrays.stream(vertex.getDeclaredConstructors())
            .filter(this::constructorCouldBeExportedToPython).collect(Collectors.toList());
    }

    private long countAppropriateExportAnnotationUses(Class<? extends Vertex> vertex) {
        return Arrays.stream(vertex.getDeclaredConstructors()).filter(constructor -> {
            boolean couldExport = constructorCouldBeExportedToPython(constructor);
            boolean hasExportAnnotation = hasExportToPythonBindingsAnnotation(constructor);
            return couldExport && hasExportAnnotation;
        }).count();
    }

    private boolean constructorCouldBeExportedToPython(Constructor constructor) {
        return Arrays.stream(constructor.getGenericParameterTypes()).allMatch(this::isSupportedVertexType);
    }

    private boolean isSupportedVertexType(Type param) {
        if (param instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) param;
            return isSupportedParameterisedType(parameterizedType);
        }

        return param.equals(booleanVertex) || param.equals(doubleVertex) || param.equals(integerVertex);
    }

    private boolean isSupportedParameterisedType(ParameterizedType parameterisedType) {
        Type[] typeArgs = parameterisedType.getActualTypeArguments();
        if (typeArgs.length != 1) {
            return false;
        }

        Type arg = typeArgs[0];

        if (arg instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) arg;
            return isSupportedWildcardType(wildcardType);
        }

        return arg.equals(booleanTensor) || arg.equals(doubleTensor) || arg.equals(integerTensor);
    }

    private boolean isSupportedWildcardType(WildcardType wildcardType) {
        Type[] upBounds = wildcardType.getUpperBounds();

        if (upBounds.length != 1) {
            return false;
        }

        Type upBound = upBounds[0];
        return upBound.equals(booleanTensor) || upBound.equals(doubleTensor) || upBound.equals(integerTensor);
    }

    private void outputAbstractClassAnnotatedErrorMessage(Class<? extends Vertex> vertex) {
        err.println(
            "Vertex " + vertex.getCanonicalName() + " is abstract and has the @ExportToPythonBindings " +
                "annotation on at least one constructor. Abstract vertex constructors should not be exported." +
                System.lineSeparator()
        );
    }

    private void outputNoAnnotationsErrorMessage(Class<? extends Vertex> vertex,
                                                 List<Constructor> exportableConstructors) {
        err.println(
            "Vertex " + vertex.getCanonicalName() + " has " + exportableConstructors.size() + " exportable " +
                "constructors but does not have the @ExportToPythonBindings annotation. Please add the annotation " +
                "to one constructor that takes exclusively " + permittedParameterTypes + " parameters. " +
                listExportableConstructors(exportableConstructors) +
                System.lineSeparator()
        );
    }

    private void outputTooManyAnnotationsErrorMessage(Class<? extends Vertex> vertex,
                                                      List<Constructor> exportableConstructor) {
        err.println(
            "Vertex " + vertex.getCanonicalName() + " has the @ExportToPythonBindings annotation on more " +
                "than one constructor. Only one constructor that takes exclusively " + permittedParameterTypes +
                " parameters should be exported. " + listExportableConstructors(exportableConstructor) +
                System.lineSeparator()
        );
    }

    private void outputInappropriateAnnotationUseErrorMessage(Class<? extends Vertex> vertex,
                                                              List<Constructor> exportableConstructors) {
        err.println(
            "Vertex " + vertex.getCanonicalName() + " appears to be using the @ExportToPythonBindings " +
                "annotation inappropriately. Please ensure that one constructor that takes exclusively " +
                permittedParameterTypes + " is annotated. " + listExportableConstructors(exportableConstructors) +
                System.lineSeparator()
        );
    }

    private String listExportableConstructors(List<Constructor> exportableConstructors) {
        StringBuilder msg = new StringBuilder("The following constructors could be annotated:");
        msg.append(System.lineSeparator());

        for (Constructor constructor : exportableConstructors) {
            msg.append("    ").append(constructor.toGenericString());
        }

        return msg.toString();
    }
}
