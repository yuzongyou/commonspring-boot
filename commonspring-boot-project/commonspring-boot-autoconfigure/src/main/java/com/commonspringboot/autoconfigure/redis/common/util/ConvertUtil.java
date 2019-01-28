package com.commonspringboot.autoconfigure.redis.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 转换工具类
 *
 * @author yzy
 */
public class ConvertUtil {

    private ConvertUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertUtil.class);

    /**
     * 转换成字符串
     *
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return 返回字符串
     */
    public static String toStringRejectBlank(Object value, String defaultValue) {
        if (null != value) {
            String ret = String.valueOf(value);
            if (StringUtils.isEmpty(ret)) {
                return defaultValue;
            }
            return ret;
        }
        return defaultValue;
    }

    /**
     * 转换成字符串
     *
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return 转成字符串
     */
    public static String toString(Object value, String defaultValue) {
        if (null != value) {
            return String.valueOf(value);
        }
        return defaultValue;
    }

    /**
     * 转换成字符串
     *
     * @param value 字符串值
     * @return 转成字符串
     */
    public static String toString(Object value) {
        return toString(value, null);
    }

    /**
     * 将 DataMap 指定属性转换成 Integer 类型
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转成Integer
     */
    public static Integer toInteger(Object value, Integer defaultValue) {
        if (null != value) {
            if (value.getClass() == int.class || value.getClass() == Integer.class) {
                return (Integer) value;
            }
            return Integer.parseInt(value.toString());
        }
        return defaultValue;
    }

    /**
     * 将 DataMap 指定属性转换成 int 类型
     *
     * @param value 要转换的值
     * @return 转成Integer
     */
    public static Integer toInteger(Object value) {
        return toInteger(value, null);
    }

    /**
     * 将 DataMap 指定属性转换成 long 类型
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 返回Long
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (null != value) {
            if (value.getClass() == long.class || value.getClass() == Long.class) {
                return (Long) value;
            }
            return Long.parseLong(value.toString());
        }
        return defaultValue;
    }

    /**
     * 将 DataMap 指定属性转换成 long 类型
     *
     * @param value 要转换的值
     * @return 返回Long
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * 将 DataMap 指定属性转换成 float 类型
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 返回Float
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (null != value) {
            if (value.getClass() == float.class || value.getClass() == Float.class) {
                return (Float) value;
            }
            return Float.parseFloat(value.toString());
        }
        return defaultValue;
    }

    /**
     * 将 DataMap 指定属性转换成 float 类型
     *
     * @param value 要转换的值
     * @return 返回Float
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * 将 DataMap 指定属性转换成 Double 类型
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 返回Double
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (null != value) {
            if (value.getClass() == double.class || value.getClass() == Double.class) {
                return (Double) value;
            }
            return Double.parseDouble(value.toString());
        }
        return defaultValue;
    }

    /**
     * 将 DataMap 指定属性转换成 Double 类型
     *
     * @param value 要转换的值
     * @return 返回Double
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * <pre>
     * 将 DataMap 指定属性转换成 Boolean 类型
     * 以下情况会转换成true: true, yes, ok, 1, yeah, on, open
     * 以下情况会转换成false: false, no, not, 0, close
     * 非true or false 则返回默认值
     *
     * 注： 以上匹配均忽略大小写
     *
     * </pre>
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 返回Boolean
     */
    public static Boolean toBoolean(Object value, Boolean defaultValue) {
        if (null != value) {
            if (value.getClass() == boolean.class || value.getClass() == Boolean.class) {
                return (Boolean) value;
            }

            String trueRegex = "(?i)true|yes|ok|1|yeah|on|open|enabled|enable";
            String falseRegex = "(?i)false|no|not|0|close|disabled|disable";

            String valueString = value.toString().trim();
            if (valueString.matches(trueRegex)) {
                return true;
            }
            if (valueString.matches(falseRegex)) {
                return false;
            }
            return defaultValue;
        }
        return defaultValue;
    }

    /**
     * 将 DataMap 指定属性转换成 Boolean 类型
     *
     * @param value 要转换的值
     * @return 返回Boolean
     */
    public static Boolean toBoolean(Object value) {
        return toBoolean(value, null);
    }

    /**
     * 转换成时间类型
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 返回Date
     */
    public static Date toDate(Object value, Date defaultValue) {
        if (null != value) {
            if (value instanceof Date) {
                Date date = (Date) value;
                return new Date(date.getTime());
            }
            String timeString = value.toString().trim();
            String pureNumberRegex = "^[1-9][0-9]+$";
            if (timeString.matches(pureNumberRegex)) {
                Long timeLong = toLong(pureNumberRegex, 0L);
                return new Date(timeLong);
            }

            for (String acceptedDateFormat : acceptedDateFormats) {
                try {
                    return new SimpleDateFormat(acceptedDateFormat).parse(timeString);
                } catch (ParseException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                }
            }
        }
        return defaultValue;
    }

    private static String[] acceptedDateFormats = new String[]{
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyyMMdd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyyMMdd"
    };

    /**
     * 转换成时间类型
     *
     * @param value 要转换的值
     * @return 返回Date
     */
    public static Date toDate(Object value) {
        return toDate(value, null);
    }
}
