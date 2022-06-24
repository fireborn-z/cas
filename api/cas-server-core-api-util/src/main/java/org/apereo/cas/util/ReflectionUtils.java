package org.apereo.cas.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reflection Utilities based on {@link ClassGraph}.
 *
 * @author Lars Grefer
 * @since 6.6.0
 */
@UtilityClass
public class ReflectionUtils {

    /**
     * Finds all {@link Class classes} extending or implementing the given superclass below the given package.
     *
     * @param superclass  The superclass to look for subclasses or implementors.
     * @param packageName The base package to look in.
     * @param <T>         The type of the superclass/interface.
     * @return The - possibly empty - collection of subclasses.
     */
    @NonNull
    public <T> Collection<Class<? extends T>> findSubclassesInPackage(final Class<T> superclass, final String packageName) {

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .enableClassInfo()
                .scan()) {

            if (superclass.isInterface()) {
                return new ArrayList<>(scanResult.getClassesImplementing(superclass).loadClasses(superclass));
            }
            return new ArrayList<>(scanResult.getSubclasses(superclass).loadClasses(superclass));

        }
    }

    /**
     * Finds all classes in the given package, wich are annotated with at least one of the given annotations.
     *
     * @param annotations The annotations to look for.
     * @param packageName The base package to look in.
     * @return The - possibly empty - collection of annotated classes.
     */
    public Collection<Class<?>> findClassesWithAnnotationsInPackage(final Collection<Class<? extends Annotation>> annotations, final String packageName) {

        List<Class<?>> result = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .enableAnnotationInfo()
                .scan()) {

            for (var annotation : annotations) {
                result.addAll(scanResult.getClassesWithAnnotation(annotation).loadClasses());
            }

        }

        return result;
    }

    /**
     * Finds a class with the given {@link Class#getSimpleName()} below the given package.
     *
     * @param simpleName  The simple name of the class.
     * @param packageName The base package to look in.
     * @return The found class.
     */
    public static Optional<Class<?>> findClassBySimpleNameInPackage(final String simpleName, final String packageName) {
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .enableClassInfo()
                .scan()) {

            return scanResult.getAllClasses()
                    .stream()
                    .filter(c -> c.getSimpleName().equalsIgnoreCase(simpleName))
                    .findFirst()
                    .map(ClassInfo::loadClass);
        }
    }
}