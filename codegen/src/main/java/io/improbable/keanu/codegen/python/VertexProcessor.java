package io.improbable.keanu.codegen.python;

import freemarker.template.Template;
import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VertexProcessor {

    final private static String TEMPLATE_FILE = "vertex.py.ftl";
    final private static String GENERATED_FILE = "vertex.py";

    static void process(String generatedDir) {
        Map<String, Object> dataModel = buildDataModel();
        Template fileTemplate = TemplateProcessor.getFileTemplate(TEMPLATE_FILE);
        Writer fileWriter = TemplateProcessor.createFileWriter(generatedDir + GENERATED_FILE);

        TemplateProcessor.processDataModel(dataModel, fileTemplate, fileWriter);
    }

    private static Map<String, Object> buildDataModel() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage("io.improbable.keanu.vertices"))
            .setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new MethodParameterScanner(), new MethodParameterNamesScanner()));

        List<Constructor> constructors = getSortedListOfAnnotatedVertexConstructors(reflections);
        List<List<String>> constructorParamNames = getSortedListOfConstructorParams(reflections, constructors);

        Map<String, Object> root = new HashMap<>();
        List<Import> imports = new ArrayList<>();
        List<PythonConstructor> pythonConstructors = new ArrayList<>();
        root.put("imports", imports);
        root.put("constructors", pythonConstructors);

        for (int i = 0; i < constructors.size(); i++) {
            Constructor constructor = constructors.get(i);
            List<String> paramNames = constructorParamNames.get(i);
            String javaClass = constructor.getDeclaringClass().getSimpleName();

            imports.add(new Import(constructor.getDeclaringClass().getCanonicalName()));
            pythonConstructors.add(new PythonConstructor(javaClass, toPythonClass(javaClass), paramNames));
        }

        return root;
    }

    private static List<List<String>> getSortedListOfConstructorParams(Reflections reflections, List<Constructor> constructors) {
        List<List<String>> allConstructorsParamsList = new ArrayList<>();
        for (Constructor constructor : constructors) {
            List<String> paramNames = reflections.getConstructorParamNames(constructor);
            List<String> singleConstructorParamsList = new ArrayList<>(paramNames);
            allConstructorsParamsList.add(singleConstructorParamsList);
        }
        return allConstructorsParamsList;
    }

    private static List<Constructor> getSortedListOfAnnotatedVertexConstructors(Reflections reflections) {
        List<Constructor> constructors = new ArrayList<>(reflections.getConstructorsAnnotatedWith(ExportVertexToPythonBindings.class));
        constructors.sort(Comparator.comparing(Constructor::getName));
        return constructors;
    }

    private static String toPythonClass(String javaClass) {
        return javaClass.replaceAll("Vertex$", "");
    }

    public static class Import {
        @Getter
        private String packageName;

        Import(String packageName) {
            this.packageName = packageName;
        }
    }

    public static class PythonConstructor {
        @Getter
        private String javaClass;
        @Getter
        private String pythonClass;
        @Getter
        private String argsString;

        PythonConstructor(String javaClass, String pythonClass, List<String> paramNames) {
            this.javaClass = javaClass;
            this.pythonClass = pythonClass;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < paramNames.size(); i++) {
                stringBuilder.append(paramNames.get(i));
                if (i != paramNames.size() -1 ) {
                    stringBuilder.append(", ");
                }
            }
            this.argsString = stringBuilder.toString();
        }
    }
}
