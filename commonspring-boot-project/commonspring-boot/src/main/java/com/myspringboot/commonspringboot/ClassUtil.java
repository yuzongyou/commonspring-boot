package com.myspringboot.commonspringboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.io.Closeable;
import java.io.Externalizable;
import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class 操作工具类
 *
 * @author yzy
 */
public class ClassUtil {

    private ClassUtil() {
        throw new IllegalStateException("Utility class");
    }

    public interface ClassAccept {

        /**
         * 是否接受结果
         *
         * @param clazz 类对象
         * @return 返回是否接受
         */
        boolean accept(Class<?> clazz);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

    private static final String PATH_DELIMITER = "/";

    /**
     * Suffix for array class names: "[]"
     */
    public static final String ARRAY_SUFFIX = "[]";

    /**
     * Prefix for internal array class names: "["
     */
    private static final String INTERNAL_ARRAY_PREFIX = "[";

    /**
     * Prefix for internal non-primitive array class names: "[L"
     */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    /**
     * The package separator character: '.'
     */
    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * The path separator character: '/'
     */
    private static final char PATH_SEPARATOR = '/';

    /**
     * The inner class separator character: '$'
     */
    private static final char INNER_CLASS_SEPARATOR = '$';

    /**
     * The CGLIB class separator: "$$"
     */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * The ".class" file suffix
     */
    public static final String CLASS_FILE_SUFFIX = ".class";


    /**
     * Map with primitive wrapper type as key and corresponding primitive
     * type as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    /**
     * Map with primitive type as key and corresponding wrapper
     * type as value, for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    /**
     * Map with primitive type name as key and corresponding primitive
     * type as value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    /**
     * Map with common Java language class name as key and corresponding Class as value.
     * Primarily for efficient deserialization of remote invocations.
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(64);

    /**
     * Common Java language interfaces which are supposed to be ignored
     * when searching for 'primary' user-level interfaces.
     */
    private static final Set<Class<?>> javaLanguageInterfaces;


    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
            registerCommonClasses(entry.getKey());
        }

        Set<Class<?>> primitiveTypes = new HashSet<>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class, double[].class, float[].class, int[].class, long[].class, short[].class);
        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class, Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class, Class.class, Class[].class, Object.class, Object[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class, Error.class, StackTraceElement.class, StackTraceElement[].class);
        registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class, Collection.class, List.class, Set.class, Map.class, Map.Entry.class);

        Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class, Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class};
        registerCommonClasses(javaLanguageInterfaceArray);
        javaLanguageInterfaces = new HashSet<>(Arrays.asList(javaLanguageInterfaceArray));
    }


    /**
     * Register the given common classes with the ClassUtils cache.
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>Call this method if you intend to use the thread context ClassLoader
     * in a scenario where you clearly prefer a non-null ClassLoader reference:
     * for example, for class path resource loading (but not necessarily for
     * {@code Class.forName}, which accepts a {@code null} ClassLoader
     * reference as well).
     *
     * @return the default ClassLoader (only {@code null} if even the system
     * ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtil.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * Override the thread context ClassLoader with the environment's bean ClassLoader
     * if necessary, i.e. if the bean ClassLoader is not equivalent to the thread
     * context ClassLoader already.
     *
     * @param classLoaderToUse the actual ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or {@code null} if not overridden
     */

    public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
            currentThread.setContextClassLoader(classLoaderToUse);
            return threadContextClassLoader;
        } else {
            return null;
        }
    }

    /**
     * Replacement for {@code Class.forName()} that also returns Class instances
     * for primitives (e.g. "int") and array class names (e.g. "String[]").
     * Furthermore, it is also capable of resolving inner class names in Java source
     * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
     *
     * @param name        the name of the Class
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
        Assert.isTrue(!StringUtils.isEmpty(name), "Name must not be null");
        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                String innerClassName = name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
                try {
                    return (clToUse != null ? clToUse.loadClass(innerClassName) : Class.forName(innerClassName));
                } catch (ClassNotFoundException ex2) {
                    // Swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * Resolve the given class name into a Class instance. Supports
     * primitives (like "int") and array class names (like "String[]").
     * <p>This is effectively equivalent to the {@code forName}
     * method with the same arguments, with the only difference being
     * the exceptions thrown in case of class loading failure.
     *
     * @param className   the name of the Class
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws IllegalArgumentException if the class name was not resolvable
     *                                  (that is, the class could not be found or the class file could not be loaded)
     * @see #forName(String, ClassLoader)
     */
    public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {

        try {
            return forName(className, classLoader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Could not find class [" + className + "]", ex);
        } catch (LinkageError err) {
            throw new IllegalArgumentException("Unresolvable class definition for class [" + className + "]", err);
        }
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className   the name of the class to check
     * @param classLoader the class loader to use
     *                    (may be {@code null} which indicates the default class loader)
     * @return whether the specified class is present
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            forName(className, classLoader);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Check whether the given class is visible in the given ClassLoader.
     *
     * @param clazz       the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against
     *                    (may be {@code null} in which case this method will always return {@code true})
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        if (classLoader == null) {
            return true;
        }
        try {
            if (clazz.getClassLoader() == classLoader) {
                return true;
            }
        } catch (SecurityException ex) {
            // Fall through to loadable check below
        }

        // Visible if same Class can be loaded from given ClassLoader
        return isLoadable(clazz, classLoader);
    }

    /**
     * Check whether the given class is cache-safe in the given context,
     * i.e. whether it is loaded by the given ClassLoader or a parent of it.
     *
     * @param clazz       the class to analyze
     * @param classLoader the ClassLoader to potentially cache metadata in
     *                    (may be {@code null} which indicates the system class loader)
     * @return true or false
     */
    public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
        Assert.isTrue(clazz != null, "Class must not be null");
        try {
            ClassLoader target = clazz.getClassLoader();
            // Common cases
            if (target == classLoader || target == null) {
                return true;
            }
            if (classLoader == null) {
                return false;
            }
            // Check for match in ancestors -> positive
            ClassLoader current = classLoader;
            while (current != null) {
                current = current.getParent();
                if (current == target) {
                    return true;
                }
            }
            // Check for match in children -> negative
            while (target != null) {
                target = target.getParent();
                if (target == classLoader) {
                    return false;
                }
            }
        } catch (SecurityException ex) {
            // Fall through to loadable check below
        }

        // Fallback for ClassLoaders without parent/child relationship:
        // safe if same Class can be loaded from given ClassLoader
        return (classLoader != null && isLoadable(clazz, classLoader));
    }

    /**
     * Check whether the given class is loadable in the given ClassLoader.
     *
     * @param clazz       the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against
     * @since 5.0.6
     */
    private static boolean isLoadable(Class<?> clazz, ClassLoader classLoader) {
        try {
            return (clazz == classLoader.loadClass(clazz.getName()));
            // Else: different class with same name found
        } catch (ClassNotFoundException ex) {
            // No corresponding class found at all
            return false;
        }
    }

    /**
     * Resolve the given class name as primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>Also supports the JVM's internal class names for primitive arrays.
     * Does <i>not</i> support the "[]" suffix notation for primitive arrays;
     * this is only supported by {@link #forName(String, ClassLoader)}.
     *
     * @param name the name of the potentially primitive class
     * @return the primitive class, or {@code null} if the name does not denote
     * a primitive class or primitive array class
     */

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     * Check if the given class represents a primitive wrapper,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Assert.isTrue(clazz != null, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Check if the given class represents a primitive (i.e. boolean, byte,
     * char, short, int, long, float, or double) or a primitive wrapper
     * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive or primitive wrapper class
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.isTrue(clazz != null, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * Check if the given class represents an array of primitives,
     * i.e. boolean, byte, char, short, int, long, float, or double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray(Class<?> clazz) {
        Assert.isTrue(clazz != null, "Class must not be null");
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Check if the given class represents an array of primitive wrappers,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
        Assert.isTrue(clazz != null, "Class must not be null");
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Resolve the given class if it is a primitive class,
     * returning the corresponding primitive wrapper type instead.
     *
     * @param clazz the class to check
     * @return the original class, or a primitive wrapper for the original primitive type
     */
    public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
        Assert.isTrue(clazz != null, "Class must not be null");
        return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
    }

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type, assuming setting by reflection. Considers primitive wrapper
     * classes as assignable to the corresponding primitive types.
     *
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Assert.isTrue(lhsType != null, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            if (lhsType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the given type is assignable from the given value,
     * assuming setting by reflection. Considers primitive wrapper classes
     * as assignable to the corresponding primitive types.
     *
     * @param type  the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
     */
    public static boolean isAssignableValue(Class<?> type, Object value) {
        Assert.notNull(type, "Type must not be null");
        return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
    }

    /**
     * Convert a "/"-based resource path to a "."-based fully qualified class name.
     *
     * @param resourcePath the resource path pointing to a class
     * @return the corresponding fully qualified class name
     */
    public static String convertResourcePathToClassName(String resourcePath) {
        Assert.notNull(resourcePath, "Resource path must not be null");
        return resourcePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    /**
     * Convert a "."-based fully qualified class name to a "/"-based resource path.
     *
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        Assert.notNull(className, "Class name must not be null");
        return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Return a path suitable for use with {@code ClassLoader.getResource}
     * (also suitable for use with {@code Class.getResource} by prepending a
     * slash ('/') to the return value). Built by taking the package of the specified
     * class file, converting all dots ('.') to slashes ('/'), adding a trailing slash
     * if necessary, and concatenating the specified resource name to this.
     * <p>
     * As such, this function may be used to build a path suitable for
     * loading a resource file that is in the same package as a class file,
     * even more convenient.
     *
     * @param clazz        the Class whose package will be used as the base
     * @param resourceName the resource name to append. A leading slash is optional.
     * @return the built-up resource path
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
        Assert.notNull(resourceName, "Resource name must not be null");
        if (!resourceName.startsWith("/")) {
            return classPackageAsResourcePath(clazz) + '/' + resourceName;
        }
        return classPackageAsResourcePath(clazz) + resourceName;
    }

    /**
     * Given an input class object, return a string which consists of the
     * class's package name as a pathname, i.e., all dots ('.') are replaced by
     * slashes ('/'). Neither a leading nor trailing slash is added. The result
     * could be concatenated with a slash and the name of a resource and fed
     * directly to {@code ClassLoader.getResource()}. For it to be fed to
     * {@code Class.getResource} instead, a leading slash would also have
     * to be prepended to the returned value.
     *
     * @param clazz the input class. A {@code null} value or the default
     *              (empty) package will result in an empty string ("") being returned.
     * @return a path which represents the package name
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String classPackageAsResourcePath(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given array.
     * <p>Basically like {@code AbstractCollection.toString()}, but stripping
     * the "class "/"interface " prefix before every class name.
     *
     * @param classes an array of Class objects
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see AbstractCollection#toString()
     */
    public static String classNamesToString(Class<?>... classes) {
        return classNamesToString(Arrays.asList(classes));
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given collection.
     * <p>Basically like {@code AbstractCollection.toString()}, but stripping
     * the "class "/"interface " prefix before every class name.
     *
     * @param classes a Collection of Class objects (may be {@code null})
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see AbstractCollection#toString()
     */
    public static String classNamesToString(Collection<Class<?>> classes) {
        if (CollectionUtils.isEmpty(classes)) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Iterator<Class<?>> it = classes.iterator(); it.hasNext(); ) {
            Class<?> clazz = it.next();
            sb.append(clazz.getName());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Copy the given {@code Collection} into a {@code Class} array.
     * <p>The {@code Collection} must contain {@code Class} elements only.
     *
     * @param collection the {@code Collection} to copy
     * @return the {@code Class} array
     * @since 3.1
     */
    public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
        return collection.toArray(new Class<?>[0]);
    }

    /**
     * Return all interfaces that the given instance implements as an array,
     * including ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as an array
     */
    public static Class<?>[] getAllInterfaces(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getAllInterfacesForClass(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as an array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
        return getAllInterfacesForClass(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as an array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (may be {@code null} when accepting all declared interfaces)
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
        return toClassArray(getAllInterfacesForClassAsSet(clazz, classLoader));
    }

    /**
     * Return all interfaces that the given instance implements as a Set,
     * including ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getAllInterfacesForClassAsSet(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as a Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
        return getAllInterfacesForClassAsSet(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as a Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (may be {@code null} when accepting all declared interfaces)
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, ClassLoader classLoader) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface() && isVisible(clazz, classLoader)) {
            Set<Class<?>> set = new HashSet<>();
            set.add(clazz);
            return set;
        }
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (current != null) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                if (isVisible(ifc, classLoader)) {
                    interfaces.add(ifc);
                }
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Create a composite interface Class for the given interfaces,
     * implementing the given interfaces in one single Class.
     * <p>This implementation builds a JDK proxy class for the given interfaces.
     *
     * @param interfaces  the interfaces to merge
     * @param classLoader the ClassLoader to create the composite Class in
     * @return the merged interface as Class
     * @see Proxy#getProxyClass
     */
    @SuppressWarnings("deprecation")
    public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
        if (interfaces == null || interfaces.length < 1) {
            throw new RuntimeException("Interfaces must not be empty");
        }
        return Proxy.getProxyClass(classLoader, interfaces);
    }

    /**
     * Determine the common ancestor of the given classes, if any.
     *
     * @param clazz1 the class to introspect
     * @param clazz2 the other class to introspect
     * @return the common ancestor (i.e. common superclass, one interface
     * extending the other), or {@code null} if none found. If any of the
     * given classes is {@code null}, the other class will be returned.
     * @since 3.2.6
     */

    public static Class<?> determineCommonAncestor(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1 == null) {
            return clazz2;
        }
        if (clazz2 == null) {
            return clazz1;
        }
        if (clazz1.isAssignableFrom(clazz2)) {
            return clazz1;
        }
        if (clazz2.isAssignableFrom(clazz1)) {
            return clazz2;
        }
        Class<?> ancestor = clazz1;
        do {
            ancestor = ancestor.getSuperclass();
            if (ancestor == null || Object.class == ancestor) {
                return null;
            }
        } while (!ancestor.isAssignableFrom(clazz2));
        return ancestor;
    }

    /**
     * Determine whether the given interface is a common Java language interface:
     * {@link Serializable}, {@link Externalizable}, {@link Closeable}, {@link AutoCloseable},
     * {@link Cloneable}, {@link Comparable} - all of which can be ignored when looking
     * for 'primary' user-level interfaces. Common characteristics: no service-level
     * operations, no bean property methods, no default methods.
     *
     * @param ifc the interface to check
     * @return true or false
     * @since 5.0.3
     */
    public static boolean isJavaLanguageInterface(Class<?> ifc) {
        return javaLanguageInterfaces.contains(ifc);
    }

    /**
     * Determine if the supplied class is an <em>inner class</em>,
     * i.e. a non-static member of an enclosing class.
     *
     * @param clazz clazz
     * @return {@code true} if the supplied class is an inner class
     * @see Class#isMemberClass()
     * @since 5.0.5
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()));
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     *
     * @param object the object to check
     * @return true or false
     * @see #isCglibProxyClass(Class)
     */
    public static boolean isCglibProxy(Object object) {
        return isCglibProxyClass(object.getClass());
    }

    /**
     * Check whether the specified class is a CGLIB-generated class.
     *
     * @param clazz the class to check
     * @return true or false
     * @see #isCglibProxyClassName(String)
     */
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }

    /**
     * Check whether the specified class name is a CGLIB-generated class.
     *
     * @param className the class name to check
     * @return true or false
     */
    public static boolean isCglibProxyClassName(String className) {
        return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
    }

    /**
     * Return the user-defined class for the given instance: usually simply
     * the class of the given instance, but the original class in case of a
     * CGLIB-generated subclass.
     *
     * @param instance the instance to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getUserClass(instance.getClass());
    }

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in case of a CGLIB-generated subclass.
     *
     * @param clazz the class to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && Object.class != superclass) {
                return superclass;
            }
        }
        return clazz;
    }


    /**
     * Get the class name without the qualified package name.
     *
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    public static String getShortName(String className) {
        Assert.notNull(className, "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }

    /**
     * Get the class name without the qualified package name.
     *
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    public static String getShortName(Class<?> clazz) {
        return getShortName(clazz.getSimpleName());
    }

    /**
     * Return the short string name of a Java class in uncapitalized JavaBeans
     * property format. Strips the outer class name in case of an inner class.
     *
     * @param clazz the class
     * @return the short name rendered in a standard JavaBeans property format
     * @see Introspector#decapitalize(String)
     */
    public static String getShortNameAsProperty(Class<?> clazz) {
        String shortName = getShortName(clazz);
        int dotIndex = shortName.lastIndexOf(PACKAGE_SEPARATOR);
        shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
        return Introspector.decapitalize(shortName);
    }

    /**
     * Determine the name of the class file, relative to the containing
     * package: e.g. "String.class"
     *
     * @param clazz the class
     * @return the file name of the ".class" file
     */
    public static String getClassFileName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * Determine the name of the package of the given class,
     * e.g. "java.lang" for the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        Assert.notNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }

    /**
     * Return the qualified name of the given method, consisting of
     * fully qualified interface/class name + "." + method name.
     *
     * @param method the method
     * @return the qualified name of the method
     */
    public static String getQualifiedMethodName(Method method) {
        return getQualifiedMethodName(method, null);
    }

    /**
     * Return the qualified name of the given method, consisting of
     * fully qualified interface/class name + "." + method name.
     *
     * @param method the method
     * @param clazz  the clazz that the method is being invoked on
     *               (may be {@code null} to indicate the method's declaring class)
     * @return the qualified name of the method
     * @since 4.3.4
     */
    public static String getQualifiedMethodName(Method method, Class<?> clazz) {
        Assert.notNull(method, "Method must not be null");
        return (clazz != null ? clazz : method.getDeclaringClass()).getName() + '.' + method.getName();
    }

    /**
     * Determine whether the given class has a public constructor with the given signature.
     * <p>Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding constructor
     * @see Class#getMethod
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
        return (getConstructorIfAvailable(clazz, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public constructor with the given signature,
     * and return it if available (else return {@code null}).
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @param <T>        constructor belong class
     * @return the constructor, or {@code null} if not found
     * @see Class#getConstructor
     */

    public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Determine whether the given class has a public method with the given signature.
     * <p>Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding method
     * @see Class#getMethod
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public method with the given signature,
     * and return it if available (else throws an {@code IllegalStateException}).
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code IllegalStateException}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the method (never {@code null})
     * @throws IllegalStateException if the method has not been found
     * @see Class#getMethod
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Expected method not found: " + ex);
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
    }

    /**
     * Determine whether the given class has a public method with the given signature,
     * and return it if available (else return {@code null}).
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the method, or {@code null} if not found
     * @see Class#getMethod
     */

    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    /**
     * Return the number of methods with a given name (with any argument types),
     * for the given class and/or its superclasses. Includes non-public methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return the number of methods with the given name
     */
    public static int getMethodCountForName(Class<?> clazz, String methodName) {
        Assert.notNull(methodName, "Method name must not be null");
        int count = 0;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (methodName.equals(method.getName())) {
                count++;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            count += getMethodCountForName(ifc, methodName);
        }
        if (clazz.getSuperclass() != null) {
            count += getMethodCountForName(clazz.getSuperclass(), methodName);
        }
        return count;
    }

    /**
     * Does the given class or one of its superclasses at least have one or more
     * methods with the supplied name (with any argument types)?
     * Includes non-public methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return whether there is at least one method with the given name
     */
    public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
        Assert.notNull(methodName, "Method name must not be null");
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            if (hasAtLeastOneMethodWithName(ifc, methodName)) {
                return true;
            }
        }
        return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
    }

    /**
     * Given a method, which may come from an interface, and a target class used
     * in the current reflective invocation, find the corresponding target method
     * if there is one. E.g. the method may be {@code IFoo.bar()} and the
     * target class may be {@code DefaultFoo}. In this case, the method may be
     * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
     * this method does <i>not</i> resolve Java 5 bridge methods automatically.
     * if bridge method resolution is desirable (e.g. for obtaining metadata from
     * the original method definition).
     * <p><b>NOTE:</b> Since Spring 3.1.1, if Java security settings disallow reflective
     * access (e.g. calls to {@code Class#getDeclaredMethods} etc, this implementation
     * will fall back to returning the originally provided method.
     *
     * @param method      the method to be invoked, which may come from an interface
     * @param targetClass the target class for the current invocation.
     *                    May be {@code null} or may not even implement the method.
     * @return the specific target method, or the original method if the
     * {@code targetClass} doesn't implement it or is {@code null}
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (targetClass != null && targetClass != method.getDeclaringClass() && isOverridable(method, targetClass)) {
            try {
                if (Modifier.isPublic(method.getModifiers())) {
                    try {
                        return targetClass.getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException ex) {
                        return method;
                    }
                } else {
                    Method specificMethod = ReflectUtil.findDeclaredMethod(targetClass, method.getName(), method.getParameterTypes());
                    return (specificMethod != null ? specificMethod : method);
                }
            } catch (SecurityException ex) {
                // Security settings are disallowing reflective access; fall back to 'method' below.
            }
        }
        return method;
    }

    /**
     * Determine whether the given method is declared by the user or at least pointing to
     * a user-declared method.
     * <p>Checks {@link Method#isSynthetic()} (for implementation methods) as well as the
     * {@code GroovyObject} interface (for interface methods; on an implementation class,
     * implementations of the {@code GroovyObject} methods will be marked as synthetic anyway).
     * Note that, despite being synthetic, bridge methods ({@link Method#isBridge()}) are considered
     * as user-level methods since they are eventually pointing to a user-declared generic method.
     *
     * @param method the method to check
     * @return {@code true} if the method can be considered as user-declared; [@code false} otherwise
     */
    public static boolean isUserLevelMethod(Method method) {
        Assert.notNull(method, "Method must not be null");
        return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
    }

    private static boolean isGroovyObjectMethod(Method method) {
        return method.getDeclaringClass().getName().equals("groovy.lang.GroovyObject");
    }

    /**
     * Determine whether the given method is overridable in the given target class.
     *
     * @param method      the method to check
     * @param targetClass the target class to check against
     */
    private static boolean isOverridable(Method method, Class<?> targetClass) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
            return true;
        }
        return (targetClass == null || getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass)));
    }

    /**
     * Return a public static method of a class.
     *
     * @param clazz      the class which defines the method
     * @param methodName the static method name
     * @param args       the parameter types to the method
     * @return the static method, or {@code null} if no static method was found
     * @throws IllegalArgumentException if the method name is blank or the clazz is null
     */
    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Assert.notNull(methodName, "Method name must not be null");
        try {
            Method method = clazz.getMethod(methodName, args);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * 是否包含给定注解中的任意一个
     *
     * @param clazz       类
     * @param annotations 注解列表
     * @return 如果参数为空则返回false
     */
    public static boolean containsAnyAnnotations(Class<?> clazz, final Class<? extends Annotation>... annotations) {

        if (clazz == null || annotations == null || annotations.length < 1) {
            return false;
        }

        for (Class<? extends Annotation> annotation : annotations) {
            if (containsAnnotation(clazz, annotation)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 类是否包含某个注解
     *
     * @param clazz      类
     * @param annotation 注解
     * @return 返回是否包含
     */
    public static boolean containsAnnotation(Class<?> clazz, final Class<? extends Annotation> annotation) {
        if (clazz == null || annotation == null) {
            return false;
        }
        Object ann = clazz.getAnnotation(annotation);
        return ann != null;
    }


    /**
     * 获取包含给定注解的所有类,包或子包下面的
     *
     * @param basePackages 包路径
     * @param annotations  注解
     * @return 始终返回非 null
     */
    public static Set<Class<?>> scan(String basePackages, Class<? extends Annotation>... annotations) {
        Set<String> packages = extractPackages(basePackages);
        if (packages.isEmpty()) {
            return new HashSet<>();
        }
        String[] packageNames = packages.toArray(new String[packages.size()]);
        return scan(packageNames, annotations);
    }

    /**
     * 获取包含给定注解的所有类,包或子包下面的
     *
     * @param basePackages 包路径
     * @param annotations  注解
     * @return 始终返回非 null
     */
    public static Set<Class<?>> scan(String[] basePackages, final Class<? extends Annotation>... annotations) {
        ClassAccept accepter = null;
        if (annotations == null) {
            accepter = new ClassAccept() {
                @Override
                public boolean accept(Class<?> clazz) {
                    return containsAnyAnnotations(clazz, annotations);
                }
            };
        }

        return scan(true, basePackages, accepter);
    }

    /**
     * 搜索指定包下面的类，默认会扫描该包下以及子包的所有类
     *
     * @param superClass   父类
     * @param basePackages 包名
     * @return 返回继承了 superClass 的所有子类， 始终返回非 null
     */
    @SuppressWarnings({"unchecked"})
    public static Set<Class<?>> scan(Class<?> superClass, String... basePackages) {
        return scan(superClass, true, basePackages);
    }

    /**
     * 搜索给定包路径下所有继承了 给定 class的class类
     *
     * @param superClass     父类
     * @param scanSubPackage 是否遍历子包
     * @param basePackages   包列表
     * @return 始终返回非null集合
     */
    @SuppressWarnings({"unchecked"})
    public static Set<Class<?>> scan(final Class<?> superClass, boolean scanSubPackage, String... basePackages) {

        ClassAccept accepter = null;

        if (null != superClass) {
            accepter = new ClassAccept() {
                @Override
                public boolean accept(Class<?> clazz) {
                    return null != clazz && superClass.isAssignableFrom(clazz);
                }
            };
        }

        return scan(scanSubPackage, basePackages, accepter);
    }

    /**
     * 检索指定包下面的满足 accepter 的所有类
     *
     * @param scanSubPackage 是否包含子包
     * @param basePackages   包列表
     * @param accepter       类接受条件
     * @return 始终返回非null
     */
    public static Set<Class<?>> scan(boolean scanSubPackage, String[] basePackages, ClassAccept accepter) {
        // 提取包列表
        Set<String> allPackages = extractPackages(basePackages);

        if (allPackages.isEmpty()) {
            return new HashSet<>();
        }
        Set<Class<?>> classSet = new HashSet<>();

        for (String packageName : basePackages) {
            Set<Class<?>> subClassSet = scanBySinglePackage(accepter, scanSubPackage, packageName);
            if (null != subClassSet && !subClassSet.isEmpty()) {
                classSet.addAll(subClassSet);
            }
        }
        return classSet;
    }

    /**
     * 解析包名，将包名解析成单独的包列表
     *
     * @param basePackages 包名数组，数组的每隔元素可能又是包含多个包名
     * @return 始终返回非null
     */
    public static Set<String> extractPackages(String[] basePackages) {
        Set<String> packageSet = new HashSet<>();
        if (basePackages == null || basePackages.length < 1) {
            return packageSet;
        }

        for (String basePackage : basePackages) {
            Set<String> subPackages = extractPackages(basePackage);
            if (!subPackages.isEmpty()) {
                packageSet.addAll(subPackages);
            }
        }

        return packageSet;
    }


    /**
     * 解析包名，中间可能使用 ｛,，；;|\\s｝ 进行分割
     *
     * @param basePackages 使用分隔符进行分割的一个或多个包名
     * @return 始终返回非 null
     */
    public static Set<String> extractPackages(Collection<String> basePackages) {
        if (null == basePackages || basePackages.isEmpty()) {
            return new HashSet<>();
        }
        return extractPackages(basePackages.toArray(new String[0]));
    }

    /**
     * 解析包名，中间可能使用 ｛,，；;|\\s｝ 进行分割
     *
     * @param basePackage 使用分隔符进行分割的一个或多个包名
     * @return 始终返回非 null
     */
    public static Set<String> extractPackages(String basePackage) {

        Set<String> packageSet = new HashSet<>();

        if (StringUtils.isEmpty(basePackage)) {
            return packageSet;
        }

        String[] packageNames = basePackage.split("[,;|\\s，；]+");
        for (String packageName : packageNames) {
            if (isValidPackageName(packageName)) {
                packageSet.add(packageName.trim());
            }
        }

        return packageSet;
    }

    /**
     * 检查一个包名是否合法:
     * 1. 不能以数字开头
     * 2. 不允许出现 -
     *
     * @param packageName 包名
     * @return 返回是否合法
     */
    public static boolean isValidPackageName(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            return false;
        }
        String[] array = packageName.split("\\.");
        for (String name : array) {
            if (name.matches("^[0-9]+") || name.contains("-")) {
                return false;
            }
        }

        return true;
    }

    /**
     * 从单个包名中搜索类
     *
     * @param accepter       类接收器
     * @param packageName    包名
     * @param scanSubPackage 是否遍历子包
     * @return 始终返回非 null 集合
     */
    private static Set<Class<?>> scanBySinglePackage(ClassAccept accepter, boolean scanSubPackage, String packageName) {

        Set<Class<?>> classSet = new HashSet<>();

        if (StringUtils.isEmpty(packageName)) {
            return classSet;
        }

        try {
            String packagePath = packageName.replace(".", "/");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = classLoader.getResources(packagePath);

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (null == url) {
                    continue;
                }

                String protocol = url.getProtocol();

                if ("file".equalsIgnoreCase(protocol)) {

                    Set<Class<?>> subClassSet = scanFromUrlPath(classLoader, url.getPath(), packageName, accepter, scanSubPackage);
                    if (null != subClassSet && !subClassSet.isEmpty()) {
                        classSet.addAll(subClassSet);
                    }
                    continue;
                }

                if ("jar".equalsIgnoreCase(protocol)) {
                    Set<Class<?>> subClassSet = scanFromJarPath(classLoader, url, packageName, accepter, scanSubPackage);
                    if (null != subClassSet && !subClassSet.isEmpty()) {
                        classSet.addAll(subClassSet);
                    }
                }
            }


        } catch (Exception e) {
            throw new RuntimeException("解析[" + packageName + "]下的类错误！", e);
        }
        return classSet;
    }

    /**
     * 从 Jar 包进行搜索
     *
     * @param classLoader    类加载器
     * @param url            url
     * @param basePackage    包名
     * @param accepter       类接受器
     * @param scanSubPackage 是否扫描子包
     * @return 始终返回非 null
     */
    private static Set<Class<?>> scanFromJarPath(ClassLoader classLoader, URL url, String basePackage, ClassAccept accepter, boolean scanSubPackage) {
        Set<Class<?>> classSet = new HashSet<>();
        try {

            JarFile jarFile = getJarFileFromUrl(url);
            Enumeration<JarEntry> entries = jarFile.entries();

            classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;

            final String regex = "^(.+?)\\.([^\\.]+\\.class)$";

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();

                if (!entryName.endsWith(".class")) {
                    continue;
                }

                String classPath = entryName.replace('/', '.');

                if (!classPath.startsWith(basePackage)) {
                    continue;
                }

                String packageName = classPath.replaceAll(regex, "$1");
                boolean isSubPackage = !packageName.equals(basePackage);

                if (scanSubPackage || !isSubPackage) {
                    collectIfClassPathIsClass(classLoader, accepter, classSet, classPath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("解析[" + url + "]下的类错误！", e);
        }

        return classSet;
    }

    private static void collectIfClassPathIsClass(ClassLoader classLoader, ClassAccept accepter, Set<Class<?>> classSet, String classPath) {
        try {
            classPath = classPath.replaceFirst("(?i)\\.class$", "");
            Class<?> clazz = classLoader.loadClass(classPath);
            boolean isAccept = null == accepter || accepter.accept(clazz);
            if (isAccept) {
                classSet.add(clazz);
            }
        } catch (ClassNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage());
            }
        }
    }

    private static JarFile getJarFileFromUrl(URL url) {

        try {
            String urlJarPath = urlToAbsolutePath(url.getPath());
            return new JarFile(new File(urlJarPath));
        } catch (Exception e) {

            try {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                return connection.getJarFile();
            } catch (Exception e1) {
                throw new RuntimeException("无法将url转换成JarFile: " + e1.getMessage());
            }
        }
    }

    /**
     * 将 URL 对象的 path 转换成绝对路径
     *
     * @param urlPath URL对象getPath
     * @return 返回绝对路径
     */
    private static String urlToAbsolutePath(final String urlPath) {

        String path = urlPath;

        path = path.replaceFirst("(?i)file:[/\\\\]+", "");
        path = path.replaceFirst("(?i)/([^:]+:)", "$1");
        path = path.replaceFirst("(?i)jar:[/\\\\]*", "");
        path = path.replaceFirst("(?i)![^\\\\!]+$", "");

        if (!path.contains(":") && !path.startsWith(PATH_DELIMITER)) {
            path = PATH_DELIMITER + path;
        }

        return path;

    }

    /**
     * 从 文件路径下进行类搜索
     *
     * @param classLoader    类加载器
     * @param urlPath        文件路径
     * @param basePackage    基础包名
     * @param accepter       是否接受
     * @param scanSubPackage 是否搜索子包
     * @return 返回非 null 集合
     */
    private static Set<Class<?>> scanFromUrlPath(ClassLoader classLoader, String urlPath, String basePackage, ClassAccept accepter, boolean scanSubPackage) {

        String filePath = urlToAbsolutePath(urlPath);

        File file = new File(filePath);

        return scanFromFile(classLoader, file, basePackage, accepter, scanSubPackage);

    }


    /**
     * 从文件中搜索class
     *
     * @param classLoader    类加载器
     * @param file           文件
     * @param basePackage    基础包名
     * @param accepter       是否接受指定的类
     * @param scanSubPackage 是否搜索子包
     * @return 始终返回非 null
     */
    private static Set<Class<?>> scanFromFile(ClassLoader classLoader, File file, String basePackage, ClassAccept accepter, boolean scanSubPackage) {

        Set<Class<?>> classSet = new HashSet<>();

        if (null == file) {
            return classSet;
        }

        Class<?> clazz = convertFileToClass(classLoader, file, basePackage);
        boolean isAccept = clazz != null && (null == accepter || accepter.accept(clazz));
        if (isAccept) {
            classSet.add(clazz);
            return classSet;
        }

        // 如果是目录才会继续处理
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (null == childFiles || childFiles.length < 1) {
                return classSet;
            }

            for (File childFile : childFiles) {
                Set<Class<?>> subClassSet = scanFromFile(classLoader, childFile, basePackage, accepter, scanSubPackage);
                if (subClassSet != null && !subClassSet.isEmpty()) {
                    classSet.addAll(subClassSet);
                }
            }
        }
        return classSet;
    }

    private static boolean isClassFile(File file) {
        return null != file && file.isFile() && file.getPath().endsWith(".class");
    }

    /**
     * 根据文件解析Class
     *
     * @param classLoader 类加载器
     * @param file        文件对象
     * @param basePackage 基础包名
     * @return 如果不存在则返回null，不会抛出异常
     */
    private static Class<?> convertFileToClass(ClassLoader classLoader, File file, String basePackage) {

        if (!isClassFile(file)) {
            return null;
        }

        String filePath = file.getPath();

        filePath = PathUtil.normalizePath(filePath);
        String basePackagePath = StringUtils.isEmpty(basePackage) ? "" : basePackage.replace('.', '/') + PATH_DELIMITER;

        int index = filePath.lastIndexOf("/classes/" + basePackagePath);
        if (index < 1) {
            return null;
        }

        // 截取classes后面的
        String classPath = filePath.substring(index);
        classPath = classPath.replaceFirst("/classes/", "");
        classPath = classPath.replace('/', '.');
        classPath = classPath.replaceAll("\\.class$", "");

        classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;

        try {
            return classLoader.loadClass(classPath);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean isClassesImported(String... classes) {
        if (classes == null || classes.length < 1) {
            return true;
        }
        for (String clazz : classes) {
            if (!ClassUtil.isPresent(clazz, Thread.currentThread().getContextClassLoader())) {
                return false;
            }
        }
        return true;
    }


}
