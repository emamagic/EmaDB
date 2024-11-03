package com.emamagic.processor;

import com.emamagic.annotation.Config;
import com.emamagic.conf.DB;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes("com.emamagic.annotation.Config")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class ConfigProcessor extends AbstractProcessor {
    private static final String SERVICE_FILE = "META-INF/services/com.emamagic.conf.IConfig";
    private final Map<DB, String> dbToClassMap = new HashMap<>(2);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {

            if (isClass(element)) return true;

            if (isImplementIConfig(element)) return true;

            Config config = element.getAnnotation(Config.class);
            DB dbType = config.db();

            if (!checkForDuplicateDbConfig(dbType, element)) {
                return true;
            }

            TypeElement classElement = (TypeElement) element;
            String className = classElement.getQualifiedName().toString();
            dbToClassMap.put(dbType, className);

        }

        try {
            generateServiceFile(dbToClassMap);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            return true;
        }

        return true;
    }

    private void printError(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private boolean isImplementIConfig(Element element) {
        TypeElement superclass = processingEnv.getElementUtils().getTypeElement("com.emamagic.conf.IConfig");
        boolean isImplement = processingEnv.getTypeUtils().isSubtype(element.asType(), superclass.asType());
        if (!isImplement) {
            printError("Classes annotated with @Config must implement IConfig", element);
            return true;
        }
        return false;
    }

    private boolean isClass(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            printError("Only classes can be annotated with @Config", element);
            return true;
        }
        return false;
    }

    private boolean checkForDuplicateDbConfig(DB dbType, Element element) {
        if (dbToClassMap.containsKey(dbType)) {
            printError("Violation: Only one class can be annotated with @Config for " + dbType +
                            ". Found conflicts between " + dbToClassMap.get(dbType) + " and " + element.getSimpleName(),
                    element);
            return false;
        }
        return true;
    }


    private void generateServiceFile(Map<DB, String> classNames) throws IOException {
        Filer filer = processingEnv.getFiler();
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_FILE);
        try (Writer writer = resource.openWriter()) {
            classNames.forEach((db, className) -> {
                try {
                    writer.write(className + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
