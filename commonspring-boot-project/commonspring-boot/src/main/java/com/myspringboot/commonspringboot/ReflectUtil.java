package com.myspringboot.commonspringboot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 反射工具类
 *
 * @author yzy
 */
public class ReflectUtil {

    private ReflectUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class);

    public static List<Class<?>> getAllSuperclasses(final Class<?> cls, boolean includeSelf) {
        if (cls == null) {
            return new ArrayList<>();
        }
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        if (includeSelf) {
            classes.add(0, cls);
        }
        return classes;
    }

    /**
     * 查找指定类型的属性列表
     *
     * @param clazz     类名
     * @param fieldType 字段类型， 如果为空就返回所有的字段
     * @return 返回指定类型的属性列表，如果为空就返回所有的字段
     */
    public static List<Field> getNoneStaticDeclaredFields(Class<?> clazz, Class<?> fieldType) {
        if (null == clazz) {
            return new ArrayList<>();
        }
        List<Field> fields = new ArrayList<>();

        List<Class<?>> classes = getAllSuperclasses(clazz, true);

        for (Class<?> cls : classes) {
            Field[] declaredFields = cls.getDeclaredFields();
            if (null != declaredFields && declaredFields.length > 0) {
                for (Field field : declaredFields) {
                    // 过滤静态属性
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (null == fieldType || field.getType().equals(fieldType)) {
                        fields.add(field);
                    }
                }
            }
        }
        return fields;
    }

    /**
     * 获取指定类型所有的非静态属性字段
     *
     * @param clazz 类
     * @return 属性列表
     */
    public static List<Field> getAllNoneStaticDeclaredFields(Class<?> clazz) {
        return getNoneStaticDeclaredFields(clazz, null);
    }

    /**
     * 获取字段的值
     *
     * @param obj   源对象
     * @param field 属性
     * @return 属性值
     */
    public static Object getFieldValue(Object obj, Field field) {
        if (null == obj || null == field) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 获取字段的值
     *
     * @param obj       源对象
     * @param fieldName 属性名称
     * @return 属性值
     */
    @SuppressWarnings({"unchecked"})
    public static Object getFieldValue(Object obj, String fieldName) {
        if (null == obj || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        Class<?> objClass = getObjectClass(obj);
        Field field = findDeclaredField(objClass, fieldName);
        if (null == field) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 搜索字段，包含私有的，从当前类开始搜索，如果当前类没有，继续往父类中查找，直到找到或到Object为止
     *
     * @param clazz     类对象
     * @param fieldName 属性名称
     * @return 属性对象
     */
    public static Field findDeclaredField(Class<?> clazz, String fieldName) {
        if (null == clazz || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    public static void updateFieldAsNotFinal(Field field) {
        try {
            Field modifiersField = ReflectUtil.findDeclaredField(Field.class, "modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateFieldAsFinal(Field field) {
        try {
            Field modifiersField = ReflectUtil.findDeclaredField(Field.class, "modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置属性的值
     *
     * @param obj   对象
     * @param field 属性
     * @param value 值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        boolean oldAccessible = field.isAccessible();
        if (!oldAccessible) {
            field.setAccessible(true);
        }
        boolean hadChangeFinal = false;
        if (Modifier.isFinal(field.getModifiers())) {
            updateFieldAsNotFinal(field);
            hadChangeFinal = true;
        }
        try {
            Class<?> type = field.getType();
            if (type.equals(Integer.class) || type.equals(int.class)) {
                field.set(obj, ((Number) value).intValue());
            } else if (type.equals(Long.class) || type.equals(long.class)) {
                field.set(obj, ((Number) value).longValue());
            } else {
                field.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(oldAccessible);
            if (hadChangeFinal) {
                updateFieldAsFinal(field);
            }
        }
    }

    /**
     * 设置属性的值
     *
     * @param obj       对象
     * @param fieldName 属性名称
     * @param value     值
     */
    @SuppressWarnings({"unchecked"})
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> objClass = getObjectClass(obj);
        Field field = ReflectUtil.findDeclaredField(objClass, fieldName);
        Assert.isTrue(field!=null,"[" + objClass.getName() + "]'s filed[" + fieldName + "] not exists!");
        setFieldValue(obj, field, value);
    }

    /**
     * 获取指定类的指定类型的Annotation
     *
     * @param clazz           类
     * @param annotationClass annotation class
     * @param <A>             返回指定类型的Annotation
     * @return 如果不存在则返回null
     */
    public static <A extends Annotation> A getClassAnnotation(Class<?> clazz, Class<A> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    /**
     * 获取指定类中含有特定注解的方法列表, 同时会搜索父类的方法
     *
     * @param clazz           类
     * @param annotationClass 注解类
     * @param <A>             返回指定类型的Annotation
     * @return 返回方法列表
     */
    public static <A extends Annotation> List<Method> getMethodForSpecificAnnotation(Class<?> clazz, Class<A> annotationClass) {

        List<Method> methodList = new ArrayList<>();
        Method[] methods = clazz.getMethods();
        if (null != methods && methods.length > 0) {

            for (Method method : methods) {
                A annotation = getMethodAnnotation(method, annotationClass);
                if (annotation != null) {
                    methodList.add(method);
                }
            }
        }
        return methodList;
    }

    /**
     * 获取指定属性的指定类型的Annotation
     *
     * @param field           属性
     * @param annotationClass annotation class
     * @param <A>             返回指定类型的Annotation
     * @return 如果不存在则返回null
     */
    public static <A extends Annotation> A getFieldAnnotation(Field field, Class<A> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    /**
     * 获取指定方法的指定类型的Annotation
     *
     * @param method          属性
     * @param annotationClass annotation class
     * @param <A>             返回指定类型的Annotation
     * @return 如果不存在则返回null
     */
    public static <A extends Annotation> A getMethodAnnotation(Method method, Class<A> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public static Class<?> getObjectClass(Object obj) {
        if (obj instanceof Class) {
            return (Class<?>) obj;
        }
        return obj.getClass();
    }

    public static Method findDeclaredMethod(Object target, String methodName, Class<?>... paramTypes) {
        Class<?> targetClass = getObjectClass(target);

        Exception exception = null;

        for (Class<?> superClass = targetClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
                exception = e;
            }
        }
        if (null != exception) {
            throw new RuntimeException(exception);
        }
        throw new RuntimeException("No such method exception[" + methodName + "] for class: " + targetClass.getSimpleName());

    }

    public static Object invokeMethod(Object target, String methodName) {
        return invokeMethod(target, findDeclaredMethod(target, methodName));
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        return invokeMethod(target, findDeclaredMethod(target, methodName, paramTypes), args);
    }

    public static Object invokeMethod(Object target, Method method, Object... args) {
        boolean accessible = method.isAccessible();
        try {
            if (!accessible) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                method.setAccessible(false);
            }
        }
    }

    public static Class<?> findClass(String className) {
        return findClass(className, null);
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            if (classLoader == null) {
                return Class.forName(className);
            } else {
                return classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object newInstanceByConstructor(String className, Class<?>[] paramTypes, Object[] args) {
        return newInstanceByConstructor(findClass(className), paramTypes, args);
    }

    /**
     * 根据构造函数生成实例
     *
     * @param clazz      类对象
     * @param paramTypes 参数类型
     * @param args       构造函数参数
     * @return 返回实例对象
     */
    public static Object newInstanceByConstructor(Class<?> clazz, Class<?>[] paramTypes, Object[] args) {
        if (paramTypes == null || paramTypes.length < 1 || args == null || args.length < 1) {
            return newInstanceByDefaultConstructor(clazz);
        }

        return newInstanceByConstructor(getConstructor(clazz, paramTypes), args);

    }

    public static Object newInstanceByConstructor(Constructor<?> constructor, Object... args) {
        boolean accessible = constructor.isAccessible();
        try {
            if (!accessible) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("NewInstanceByConstructorError:" + e.getMessage(), e);
        } finally {
            if (!accessible) {
                constructor.setAccessible(false);
            }
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("GetConstructor Error:" + e.getMessage(), e);
        }
    }

    public static Object newInstanceByDefaultConstructor(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("NewInstanceError:" + e.getMessage(), e);
        }
    }

    public static Object newInstanceByDefaultConstructor(String className) {
        return newInstanceByDefaultConstructor(findClass(className));
    }

    /**
     * 搜索 packageName 包下面的 实现Or 继承了 superClass 的类， 并且使用默认构造函数进行实例化
     * <p>
     * 注意，指定的包名下的类，必须是 public 的
     *
     * @param superClass   超类
     * @param packageNames 包名列表，使用英文逗号分隔
     * @param <T>          超类类型
     * @return 返回实例列表
     */
    public static <T> List<T> scanAndInstanceByDefaultConstructor(Class<T> superClass, String packageNames) {

        List<T> instanceList = new ArrayList<>();
        Set<String> packages = splitPackageNames(packageNames);
        if (packages.isEmpty()) {
            return instanceList;
        }

        for (String packageName : packages) {
            List<T> subInstanceList = scanAndInstanceByDefaultConstructorWithSinglePackage(superClass, packageName);
            if (!subInstanceList.isEmpty()) {
                instanceList.addAll(subInstanceList);
            }
        }

        return instanceList;

    }

    /**
     * 拆分包名
     *
     * @param packageNames 包名列表，可以是逗号、空白字符，竖线分隔
     * @return 返回包名集合
     */
    public static Set<String> splitPackageNames(String packageNames) {

        if (StringUtils.isEmpty(packageNames)) {
            return new HashSet<>();
        }
        Set<String> packages = new HashSet<>();
        String[] array = packageNames.split("[\\s,，\\|;]+");
        for (String packageName : array) {
            if (!StringUtils.isEmpty(packageName)) {
                packages.add(packageName);
            }
        }

        return packages;
    }

    /**
     * 搜索 packageName 包下面的 实现Or 继承了 superClass 的类， 并且使用默认构造函数进行实例化
     * <p>
     * 注意，指定的包名下的类，必须是 public 的
     *
     * @param superClass  超类
     * @param packageName 单个包名
     * @param <T>         超类类型
     * @return 返回实例列表
     */
    public static <T> List<T> scanAndInstanceByDefaultConstructorWithSinglePackage(Class<T> superClass, String packageName) {
        Set<Class<?>> classes = ClassUtil.scan(superClass, packageName);
        List<T> instances = new ArrayList<>();

        if (null != classes && !classes.isEmpty()) {

            for (Class<?> clazz : classes) {

                T instance = newInstanceByDefaultConstructor(superClass, clazz);

                if (null != instance) {
                    instances.add(instance);
                }

            }

        }

        return instances;
    }


    /**
     * 使用默认构造函数生成多个实例
     *
     * @param superClass 超类，要生成的类实例必须继承这个类
     * @param classNames 要实例化的类,注意，该类必须是 public 的,中间使用 , 分割
     * @param <T>        结果
     * @return 返回实例对象
     */
    public static <T> List<T> newInstancesByDefaultConstructor(Class<T> superClass, String classNames) {
        if (StringUtils.isEmpty(classNames)) {
            return new ArrayList<>();
        }

        List<T> instanceList = new ArrayList<>();
        String[] classArray = classNames.split("[|，,;；\\s]+");
        for (String className : classArray) {
            if (StringUtils.isEmpty(className)) {
                T instance = newInstanceByDefaultConstructor(superClass, className.trim());
                if (null != instance) {
                    instanceList.add(instance);
                }
            }
        }
        return instanceList;
    }

    /**
     * 使用默认构造函数生成实例
     *
     * @param superClass 超类，要生成的类实例必须继承这个类
     * @param className  要实例化的类,注意，该类必须是 public 的
     * @param <T>        结果
     * @return 返回实例对象
     */
    public static <T> T newInstanceByDefaultConstructor(Class<T> superClass, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return newInstanceByDefaultConstructor(superClass, clazz);
        } catch (Exception e) {
            LOGGER.warn("实例化类[{}]失败： {}", className, e.getMessage());
            return null;
        }
    }

    /**
     * 使用默认构造函数生成实例
     *
     * @param superClass 超类，要生成的类实例必须继承这个类
     * @param clazz      要实例化的类,注意，该类必须是 public 的
     * @param <T>        结果
     * @return 返回实例对象
     */
    public static <T> T newInstanceByDefaultConstructor(Class<T> superClass, Class<?> clazz) {
        if (null == clazz) {
            return null;
        }

        try {

            if (!superClass.isAssignableFrom(clazz)) {
                return null;
            }

            int mod = clazz.getModifiers();

            if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
                return null;
            }

            Constructor<?> constructor = clazz.getConstructor();

            if (constructor == null) {
                throw new RuntimeException("指定的类[" + clazz.getName() + "] 没有一个默认的无参构造函数！");
            }

            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }

            return superClass.cast(constructor.newInstance());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        if (null == classLoader) {
            classLoader = ReflectUtil.class.getClassLoader();
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断一个类是否可以通过默认构造函数进行实例化
     *
     * @param clazz 类
     * @return 返回是否允许实例化
     */
    public static boolean canInstanceByDefaultConstructor(Class<?> clazz) {
        int mod = clazz.getModifiers();

        if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
            return false;
        }

        try {
            return clazz.getConstructor() != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static <T> List<T> newInstancesByDefaultConstructor(Class<T> requireType, Set<String> classes) {

        if (null == classes || classes.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<T> instanceList = new ArrayList<>();
        for (String className : classes) {
            if (!StringUtils.isEmpty(className)) {
                T instance = ReflectUtil.newInstanceByDefaultConstructor(requireType, className.trim());
                if (null != instance) {
                    instanceList.add(instance);
                }
            }
        }
        return instanceList;
    }
}
