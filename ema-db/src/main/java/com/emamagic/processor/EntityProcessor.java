package com.emamagic.processor;

import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.emamagic.conf.DB;
import com.google.auto.service.AutoService;
import org.bson.types.ObjectId;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.emamagic.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class EntityProcessor extends AbstractProcessor {
    private static final String INTEGER_TYPE = "java.lang.Integer";
    private static final String STRING_TYPE = "java.lang.String";
    private static final String OBJECT_ID_TYPE = "org.bson.types.ObjectId";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean hasErrors = false;

        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            if (!isClass(element)) {
                printError("@Entity can only be applied to classes", element);
                hasErrors = true;
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            if (!validateIdField(classElement) || !validateNoArgsConstructor(classElement)) {
                hasErrors = true;
            }
        }
        return hasErrors;
    }

    private boolean isClass(Element element) {
        return element.getKind() == ElementKind.CLASS;
    }

    private void printError(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private boolean validateIdField(TypeElement classElement) {
        return validateIdFieldCount(classElement) && validateIdFieldType(classElement);
    }

    private boolean validateIdFieldCount(TypeElement classElement) {
        int idFieldCount = 0;

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (isFieldWithIdAnnotation(enclosedElement)) {
                idFieldCount++;
            }
        }

        if (idFieldCount != 1) {
            printError("A class annotated with @Entity must have exactly one field annotated with @Id", classElement);
            return false;
        }
        return true;
    }

    private boolean isFieldWithIdAnnotation(Element element) {
        return element.getKind() == ElementKind.FIELD && element.getAnnotation(Id.class) != null;
    }

    private boolean validateIdFieldType(TypeElement classElement) {
        DB dbType = classElement.getAnnotation(Entity.class).db();
        TypeMirror integerType = processingEnv.getElementUtils().getTypeElement(INTEGER_TYPE).asType();
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement(STRING_TYPE).asType();
        TypeMirror objectIdType = processingEnv.getElementUtils().getTypeElement(OBJECT_ID_TYPE).asType();

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (isFieldWithIdAnnotation(enclosedElement)) {
                TypeMirror fieldType = enclosedElement.asType();

                if (dbType == DB.POSTGRESQL && !isValidPostgresIdType(fieldType, integerType, stringType)) {
                    printError("The @Id field for PostgreSQL must be of type Integer or String (not a primitive)", enclosedElement);
                    return false;
                } else if (dbType == DB.MONGODB && !isValidMongoIdType(fieldType, objectIdType)) {
                    printError("The @Id field for MongoDB must be of type ObjectId", enclosedElement);
                    return false;
                }
            }
        }
        return true;
    }


    private boolean isValidPostgresIdType(TypeMirror fieldType, TypeMirror integerType, TypeMirror stringType) {
        return !fieldType.getKind().isPrimitive() &&
                (processingEnv.getTypeUtils().isSameType(fieldType, integerType) ||
                        processingEnv.getTypeUtils().isSameType(fieldType, stringType));
    }

    private boolean isValidMongoIdType(TypeMirror fieldType, TypeMirror objectIdType) {
        return processingEnv.getTypeUtils().isSameType(fieldType, objectIdType);
    }


    // TODO: you can use the "org.objenesis" library which is allows you to create objects without calling any constructors
    private boolean isNoArgsConstructor(Element element) {
        return element.getKind() == ElementKind.CONSTRUCTOR &&
                ((ExecutableElement) element).getParameters().isEmpty();
    }

    private boolean validateNoArgsConstructor(TypeElement classElement) {
        boolean hasNoArgConstructor = false;
        boolean hasAnyConstructor = false;

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                hasAnyConstructor = true;
                ExecutableElement constructor = (ExecutableElement) enclosedElement;
                if (constructor.getParameters().isEmpty() &&
                        constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    hasNoArgConstructor = true;
                    break;
                }

                else if (constructor.getParameters().isEmpty() &&
                        constructor.getModifiers().contains(Modifier.PRIVATE)) {
                    printError("Class annotated with @Entity must have a public no-argument constructor", classElement);
                    return false;
                }
            }
        }

        if (!hasAnyConstructor) {
            return true;
        }

        if (!hasNoArgConstructor) {
            printError("Class annotated with @Entity must have a public no-argument constructor", classElement);
            return false;
        }

        return true;
    }

}
