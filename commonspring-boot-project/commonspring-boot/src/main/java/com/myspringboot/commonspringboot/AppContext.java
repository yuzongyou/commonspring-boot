package com.myspringboot.commonspringboot;


import com.myspringboot.commonspringboot.annotations.CommonSpringBootApplication;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yzy
 * @version 1.0
 * @since 2019/01/03 16:19
 */
public class AppContext {

    private AppContext() {
    }

    private static List<String> initInfo = new ArrayList<>();

    private static ConfigurableApplicationContext acx;

    public static final String ENV_DEV = "dev";
    public static final String ENV_TEST = "test";
    public static final String ENV_PROD = "prod";

    /**
     * 环境
     */
    private static String env;

    /**
     * 项目代号
     */
    private static String projectNo;

    /**
     * 模块代号
     **/
    private static String moduleNo;

    /**
     * 运行当前项目的目录，这个选项在开发环境下和模块目录才有区别
     **/
    private static String projectDir;

    /**
     * 当前运行模块的根路径
     **/
    private static String moduleDir;

    /**
     * 日志目录
     **/
    private static String logDir;

    /**
     * 资源文件搜索目录
     **/
    private static List<String> resourceLookupDirs = new ArrayList<>();

    private static Map<String, Object> projectInfoMap = new HashMap<>();

    /**
     * 应用程序属性配置
     **/
    private static Map<String, Object> applicationProperties = new HashMap<>();

    private static StandardEnvironment environment = new StandardEnvironment();

    /**
     * App 应用所有的key集合
     */
    private static volatile Set<String> appKeySet;

    public static Set<String> getAppKeySet() {
        return appKeySet;
    }

    public static String getEnv() {
        return env;
    }

    /**
     * 给定的环境是否一致
     *
     * @param givenEnv 环境
     * @return 返回是否和给定的环境一致
     */
    public static boolean isEnvMatched(String givenEnv) {

        return givenEnv.equals(env);
    }

    public static boolean isDev() {
        return ENV_DEV.equals(env);
    }

    public static boolean isTest() {
        return ENV_TEST.equals(env);
    }

    public static boolean isProd() {
        return ENV_PROD.equals(env);
    }

    public static String getProjectNo() {
        return projectNo;
    }

    public static String getModuleNo() {
        return moduleNo;
    }

    public static ConfigurableApplicationContext getAcx() {
        return acx;
    }

    public static void setAcx(ConfigurableApplicationContext acx) {
        AppContext.acx = acx;
    }

    public static StandardEnvironment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(StandardEnvironment environment) {
        AppContext.environment = environment;
        initAppAllKeySet();
    }

    public static <T> T bindProperties(String prefix, Class<T> target) {
        return bindProperties(null, prefix, target, null);
    }

    public static <T> T bindProperties(Environment environment, String prefix, Class<T> target) {
        return bindProperties(environment, prefix, target, null);
    }

    public static <T> T bindProperties(String prefix, Class<T> target, T defaultProperties) {
        return bindProperties(null, prefix, target, defaultProperties);
    }

    public static <T> T bindProperties(Environment environment, String prefix, Class<T> target, T defaultProperties) {
        try {
            if (environment == null) {
                environment = AppContext.environment;
            }
            return Binder.get(environment).bind(prefix, target).get();
        } catch (NoSuchElementException e) {
            return defaultProperties;
        }
    }

    public static String getProjectDir() {
        return projectDir;
    }

    public static String getModuleDir() {
        return moduleDir;
    }

    public static String getLogDir() {
        return logDir;
    }

    public static Map<String, Object> getProjectInfoMap() {
        return projectInfoMap;
    }

    public static Map<String, Object> getApplicationProperties() {
        return applicationProperties;
    }

    public static List<String> getInitInfo() {
        return initInfo;
    }


    /**
     * Get the bean definition registry.
     *
     * @return the BeanDefinitionRegistry if it can be determined
     */
    public static BeanDefinitionRegistry getBeanDefinitionRegistry() {
        if (acx == null) {
            throw new IllegalStateException("AppContext not init yet, Cloud not locate BeanDefinitionRegistry");
        }
        if (acx instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) acx;
        }
        if (acx instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) acx.getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }

    private static volatile boolean hadInit = false;

    private static Class<?> bootstrapClass;

    public static Class<?> getBootstrapClass() {
        return bootstrapClass;
    }

    public static boolean isInitialize() {
        return hadInit;
    }

    /**
     * 初始化应用环境， 项目代号， 环境， 日志等
     *
     * @param sourceClass 应用启动来源
     */
    public static void initialize(Class<?> sourceClass) {

        if (hadInit) {
            return;
        }

        bootstrapClass = sourceClass;

        hadInit = true;

        initInfo.clear();
        initInfo.add("Initialize AppContext By Source : " + sourceClass);

        Assert.isTrue(sourceClass != null, "必须提供应用Source对象");

        CommonSpringBootApplication applicationAnn = sourceClass.getAnnotation(CommonSpringBootApplication.class);

        env = deduceRuntimeEnv(environment, sourceClass, applicationAnn);
        moduleDir = deduceModuleDir(environment, sourceClass, applicationAnn);
        projectDir = deduceProjectDir(environment, sourceClass, applicationAnn);
        projectNo = deduceProjectNo(environment, sourceClass, applicationAnn);
        moduleNo = deduceModuleNo(environment, sourceClass, applicationAnn);
        logDir = deduceLogDir(environment, sourceClass, applicationAnn);

        projectInfoMap.put("projectNo", projectNo);
        projectInfoMap.put("moduleNo", moduleNo);
        projectInfoMap.put("env", env);
        projectInfoMap.put("moduleDir", moduleDir);
        projectInfoMap.put("projectDir", projectDir);
        environment.getPropertySources().addLast(new MapPropertySource("projectInfo", projectInfoMap));

        resourceLookupDirs = deduceResourceLookupDirs(environment, sourceClass, applicationAnn);
        projectInfoMap.put("resourceLookupDirs", resourceLookupDirs);

        // 初始化应用程序属性
        initializeApplicationProperties();

        // 检查Date json 响应默认值是否设置
        adapterDateJsonFormatter(applicationProperties);

        environment.getPropertySources().addLast(new MapPropertySource("commonApplicationProperties", applicationProperties));

        // 校准时间基线
        adapterSpringJacksonTimeZone(environment, applicationProperties);

        initInfo.add(projectInfoMap.toString());

        initAppAllKeySet();
    }

    private static final String SPRING_JACKSON_SERIALIZATION_WRITE_DATES_AS_TIMESTAMPS = "spring.jackson.serialization.write-dates-as-timestamps";

    private static void adapterDateJsonFormatter(Map<String, Object> applicationProperties) {

        // 默认将时间输出为 long 类型
        if (!applicationProperties.containsKey(SPRING_JACKSON_SERIALIZATION_WRITE_DATES_AS_TIMESTAMPS)) {
            applicationProperties.put(SPRING_JACKSON_SERIALIZATION_WRITE_DATES_AS_TIMESTAMPS, "true");
        }

    }

    private static void adapterSpringJacksonTimeZone(StandardEnvironment environment, Map<String, Object> applicationProperties) {
        String timeZoneKey = "spring.jackson.time-zone";
        String timeZone = environment.getProperty(timeZoneKey);
        if (StringUtils.isEmpty(timeZone) || "none".equalsIgnoreCase(timeZone)) {
            System.setProperty(timeZoneKey, TimeZone.getDefault().getID());
        }
    }

    private static String deduceLogDir(StandardEnvironment environment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {

        String logDir = null;
        if (null != applicationAnn) {
            logDir = applicationAnn.logDir();
        }

        if (StringUtils.isEmpty(logDir)) {
            if (isDev()) {
                logDir = System.getProperty("user.dir");
            } else {
                logDir = "/data2/log/resin/";
            }
        }

        if (!StringUtils.isEmpty(logDir)) {
            logDir = environment.resolvePlaceholders(logDir);
        }

        return logDir;

    }

    private static void initAppAllKeySet() {
        MutablePropertySources propertySources = environment.getPropertySources();

        Set<String> allKeySet = new HashSet<>();

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                Collections.addAll(allKeySet, ((EnumerablePropertySource<?>) propertySource).getPropertyNames());
            }
        }

        AppContext.appKeySet = Collections.unmodifiableSet(allKeySet);
    }

    public static Set<String> lookupKeys(Set<String> keySet, KeyFilter keyFilter) {
        if (keySet == null || keySet.isEmpty()) {
            return keySet;
        }

        if (null == keyFilter) {
            return keySet;
        }
        Set<String> resultKeySet = new HashSet<>();
        for (String key : keySet) {
            if (keyFilter.filter(key)) {
                resultKeySet.add(key);
            }
        }
        return resultKeySet;
    }


    private static void initializeApplicationProperties() {

        List<Resource> resourceList = lookupConfigResourceList("application.properties");

        if (null != resourceList && !resourceList.isEmpty()) {
            for (int i = resourceList.size() - 1; i >= 0; --i) {
                Resource resource = resourceList.get(i);
                try {
                    initInfo.add("加载配置文件： " + resource.getURL());
                    Properties properties = new Properties();
                    properties.load(resource.getInputStream());
                    if (!properties.isEmpty()) {
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            applicationProperties.put(String.valueOf(entry.getKey()), entry.getValue());
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException("加载App资源错误：" + e.getMessage(), e);
                }
            }
        }

    }

    /**
     * 推断资源搜索目录，当查找一个资源的时候，在指定的目录下，根据不同环境，会读取不同的配置文件，假设要搜索的资源文件标识为 config.suffix
     * 那么会在资源搜索目录下按如下顺序搜索文件：
     * 1. ${LOOKUP_DIR}/${env}/config.suffix
     * 2. ${LOOKUP_DIR}/config-${env}.suffix
     * <p>
     * 生效的顺序为搜索的顺序，假设 1、2 中都有相同的配置项，那么 1 的会生效
     * <p>
     * 关于资源搜索目录，默认是:
     * 1. classpath:/config/
     * 2. classpath:/
     * 5. /data/app/${projectNo}/config/      这个只有在非开发环境下才生效
     * 6. ${projectDir}/config/               这个只有在开发环境下才生效
     */
    private static List<String> deduceResourceLookupDirs(StandardEnvironment appEnvironment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {
        List<String> lookupDirs = new ArrayList<>();

        try {
            ClassPathResource classPathResource = new ClassPathResource("/config/");
            if (classPathResource.exists()) {
                lookupDirs.add("classpath:/config/");
            }
        } catch (Exception ignored) {
        }

        lookupDirs.add("classpath:/");

        File dir;
        if (!isDev()) {
            dir = tryGetExistsDirFile(appEnvironment, "/data/app/${projectNo}/config/");
            if (null != dir) {
                lookupDirs.add(dir.getAbsolutePath());
            }
        } else {
            dir = tryGetExistsDirFile(appEnvironment, "${projectDir}/config/");
            if (null != dir) {
                lookupDirs.add(dir.getAbsolutePath());
            }
        }

        if (null != applicationAnn) {
            String[] customDirs = applicationAnn.resourceLookupDirs();
            if (customDirs.length > 0) {
                for (String path : customDirs) {

                    try {
                        path = appEnvironment.resolvePlaceholders(path);
                    } catch (Exception ignored) {
                    }

                    if (path.matches("(?i)^classpath:.*")) {
                        lookupDirs.add(path);
                    } else {
                        dir = tryGetExistsDirFile(appEnvironment, path);
                        if (null != dir) {
                            lookupDirs.add(dir.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return lookupDirs;
    }

    private static File tryGetExistsDirFile(StandardEnvironment appEnvironment, String path) {

        try {
            String filePath = appEnvironment.resolveRequiredPlaceholders(path);
            File dir = new File(filePath);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 搜索 多个配置文件
     *
     * @param configFilename 配置文件名称
     * @return 返回配置文件资源
     */
    public static List<Resource> lookupConfigResourceList(String configFilename) {

        List<Resource> resourceList = new ArrayList<>();

        List<String> lookupPaths = extractLookupConfigResourcePaths(configFilename);

        if (null == lookupPaths || lookupPaths.isEmpty()) {
            return resourceList;
        }

        for (String lookupPath : lookupPaths) {

            if (lookupPath.startsWith("classpath")) {
                Resource resource = getClasspathResource(lookupPath);

                if (null != resource && resource.exists()) {
                    resourceList.add(resource);
                }
            } else {
                Resource resource = getResourceFromAbsPath(lookupPath);
                if (null != resource && resource.exists()) {
                    resourceList.add(resource);
                }
            }

        }

        return resourceList;
    }

    /**
     * 提取完整的配置文件路徑
     *
     * @param configFilename 配置文件名称
     * @return 返回非 null 列表
     */
    public static List<String> extractLookupConfigResourcePaths(String configFilename) {

        if (StringUtils.isEmpty(configFilename)) {
            return new ArrayList<>();
        }

        int index = configFilename.lastIndexOf('.');
        String name;
        String suffix;
        if (index > -1) {
            name = configFilename.substring(0, index);
            suffix = configFilename.substring(index);
        } else {
            name = configFilename;
            suffix = "";
        }

        List<String> pathList = new ArrayList<>();

        for (String basePath : resourceLookupDirs) {
            pathList.add(PathUtil.normalizePath(basePath + "/" + env + "/" + configFilename));
            pathList.add(PathUtil.normalizePath(basePath + "/" + name + "-" + env + suffix));
            pathList.add(PathUtil.normalizePath(basePath + "/" + configFilename));
        }

        return pathList;
    }

    public static Resource getClasspathResource(String classpathResource) {

        classpathResource = environment.resolvePlaceholders(classpathResource);
        String path = classpathResource.replaceFirst("(?i)classpath\\*?:", "");
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return null;
        }
        return resource;
    }

    public static Resource getResourceFromAbsPath(String filePath) {
        filePath = environment.resolvePlaceholders(filePath);
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Resource resource = new FileSystemResource(file);
                if (resource.exists()) {
                    return resource;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static Resource tryGetResource(String path) {
        path = environment.resolvePlaceholders(path);
        Resource resource = getClasspathResource(path);
        if (null == resource) {
            resource = getResourceFromAbsPath(path);
        }

        return resource;
    }

    public static List<Resource> tryGetResources(String[] paths) {
        List<Resource> resourceList = new ArrayList<>();
        if (!StringUtils.isEmpty(paths)) {
            for (String path : paths) {
                Resource resource = tryGetResource(path);
                if (null != resource) {
                    resourceList.add(resource);
                }
            }
        }
        return resourceList;
    }

    /**
     * 推断项目的目录，这个在开发环境下才会和 模块DIR不一样
     */
    private static String deduceProjectDir(StandardEnvironment appEnvironment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {

        if (isDev()) {
            // 搜索模块DIR上一层目录，直到搜索不到 pom.xml 文件为止则认为是项目的根目录
            File parent = new File(moduleDir).getParentFile();
            List<File> list = new ArrayList<>();
            while (null != parent && parent.exists()) {
                list.add(parent);
                parent = parent.getParentFile();
            }

            int projectDirIndex = -1;
            for (int i = 0; i < list.size(); ++i) {
                File file = list.get(i);
                String[] filenames = file.list((dir, name) -> "pom.xml".equalsIgnoreCase(name));
                if (null != filenames && filenames.length > 0) {
                    if (projectDirIndex == -1) {
                        projectDirIndex = i;
                    } else {
                        if (projectDirIndex + 1 == i) {
                            projectDirIndex = i;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (projectDirIndex < 0) {
                return moduleDir;
            }
            return list.get(projectDirIndex).getAbsolutePath();
        } else {
            // 非开发环境则和模块目录一样
            return moduleDir;
        }

    }

    /**
     * WAR 包 的LIB目录
     */
    private static final Pattern WAR_LIB_FOLDER_REGEX = Pattern.compile("(?i)(.*)[/\\\\]WEB-INF[/\\\\]lib$");
    private static final String WAR_CLASSES_FOLDER_REGEX_PREFIX = "(?i)(.*)[/\\\\]WEB-INF[/\\\\]classes[/\\\\]";

    /**
     * 推断模块目录
     */
    private static String deduceModuleDir(StandardEnvironment appEnvironment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {

        ApplicationHome home = new ApplicationHome(sourceClass);

        String homeDir = home.getDir().getAbsolutePath();

        if (isDev()) {
            // 开发环境下，计算项目模块路径
            return PathUtil.normalizePath(homeDir.replaceFirst("[/\\\\]target[/\\\\].*$", "/") + "/");
        } else {
            Matcher matcher = WAR_LIB_FOLDER_REGEX.matcher(homeDir);
            if (matcher.find()) {
                return PathUtil.normalizePath(matcher.replaceAll("$1/"));
            } else {
                String sourceClassPackage = sourceClass.getPackage().getName();

                String packageToRegex = sourceClassPackage.replaceAll("\\.", "[/\\\\\\\\]");

                Pattern pattern = Pattern.compile(WAR_CLASSES_FOLDER_REGEX_PREFIX + packageToRegex);

                matcher = pattern.matcher(homeDir);

                if (matcher.find()) {
                    return PathUtil.normalizePath(matcher.replaceAll("$1/"));
                } else {
                    // 非 war， jar模式
                    return PathUtil.normalizePath(homeDir.replaceAll("classes$", "/"));
                }
            }
        }
    }

    private static String deduceRuntimeEnv(StandardEnvironment appEnvironment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {
        EnvReader envReader = null;
        if (null != applicationAnn) {
            Class<? extends EnvReader> readerClass = applicationAnn.envReader();
            try {
                envReader = readerClass.newInstance();
            } catch (Exception ignored) {
            }
        }
        if (envReader == null) {
            envReader = new DefaultEnvReader();
        }
        String env = envReader.readRuntimeEnv(appEnvironment, sourceClass);
        if (StringUtils.isEmpty(env)) {
            return ENV_DEV;
        }
        return env;
    }

    private static String deduceProjectNo(StandardEnvironment appEnvironment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {

        ProjectNoReader reader = null;
        if (null != applicationAnn) {
            Class<? extends ProjectNoReader> readerClass = applicationAnn.projectNoReader();
            try {
                reader = readerClass.newInstance();
            } catch (Exception ignored) {
            }
        }
        if (null == reader) {
            reader = new DefaultProjectNoReader();
        }
        String projectNo = reader.readProjectNo(appEnvironment, sourceClass);
        if (!StringUtils.isEmpty(projectNo)) {
            return projectNo;
        }
        if (StringUtils.isEmpty(projectNo) && isDev()) {
            // 开发环境的话，允许直接通过项目目录计算项目代号，如果项目目录为空则通过模块目录计算
            if (!StringUtils.isEmpty(projectDir)) {
                projectNo = getLastSepFolderName(projectDir);
                if (isDev() && "trunk".equalsIgnoreCase(projectNo)) {
                    projectNo = getLastSepFolderName(projectDir.replaceFirst("[/\\\\]trunk[/\\\\]?$", ""));
                }
            } else if (!StringUtils.isEmpty(moduleDir)) {
                projectNo = getLastSepFolderName(moduleDir);
            } else {
                projectNo = "dev";
            }
            return projectNo;
        }
        // 如果还是为空，则抛出异常，表示无法识别项目代号
        throw new RuntimeException("无法识别项目代号");
    }


    private static String deduceModuleNo(StandardEnvironment environment, Class<?> sourceClass, CommonSpringBootApplication applicationAnn) {

        String mno = environment.getProperty("MODULENO");

        if (StringUtils.isEmpty(mno)) {
            if (null != applicationAnn) {
                mno = applicationAnn.moduleNo();
                if (!StringUtils.isEmpty(mno)) {
                    return mno;
                }
                mno = applicationAnn.value();
                if (!StringUtils.isEmpty(mno)) {
                    return mno;
                }
            }
        }
        if (StringUtils.isEmpty(mno)) {
            if (!StringUtils.isEmpty(moduleDir)) {
                mno = getLastSepFolderName(moduleDir);
            }
        }
        if (!StringUtils.isEmpty(mno)) {
            mno = environment.resolvePlaceholders(mno);
        }
        if (!StringUtils.isEmpty(mno) && mno.startsWith(projectNo) && !mno.equals(projectNo)) {
            mno = mno.replaceFirst(projectNo, "");
            mno = mno.replaceFirst("^-+", "");
        }
        return mno;
    }

    private static String getLastSepFolderName(String dirPath) {
        if (StringUtils.isEmpty(dirPath)) {
            return null;
        }
        return dirPath.replaceAll("[\\\\/]*$", "").replaceFirst(".*[\\\\/](.*)$", "$1");
    }

    public static String lookupFirstNotBlankValue(StandardEnvironment appEnvironment, String[] keys, String defaultValue) {
        for (String projectNoKey : keys) {
            try {
                String value = appEnvironment.resolveRequiredPlaceholders("${" + projectNoKey + "}");
                if (!StringUtils.isEmpty(value)) {
                    return value;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return defaultValue;
    }

    public static String getSystemVar(String key, String defaultValue) {
        String env = System.getenv(key);

        if (StringUtils.isEmpty(env)) {
            return System.getProperty(key, defaultValue);
        }

        return env;
    }

    public static String getAppProperty(String key, String defaultValue) {
        try {
            String realKey = environment.resolveRequiredPlaceholders(key);
            if (StringUtils.isEmpty(realKey)) {
                return defaultValue;
            }
            return environment.getProperty(realKey, defaultValue);
        } catch (Exception ignored) {
        }
        return defaultValue;
    }
}

