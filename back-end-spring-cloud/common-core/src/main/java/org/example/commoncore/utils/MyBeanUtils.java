package org.example.commoncore.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.lang.NonNull;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * BeanUtils 工具类
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 提供Bean属性复制功能，支持复制非空属性
 */
public class MyBeanUtils {

    /**
     * 复制非空属性（不包含指定忽略的字段）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNonNullProperties(Object source, Object target) {
        copyNonNullProperties(source, target, (String[]) null);
    }

    /**
     * 复制非空属性（忽略指定字段）
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 要忽略的属性名
     */
    public static void copyNonNullProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }

        // 获取所有值为null的属性名
        String[] nullPropertyNames = getNullPropertyNames(source);

        // 合并忽略字段
        String[] allIgnoreProperties = mergeArrays(nullPropertyNames, ignoreProperties);

        // 执行属性复制
        BeanUtils.copyProperties(source, target, allIgnoreProperties);
    }

    /**
     * 获取对象中所有值为null的属性名
     *
     * @param source 源对象
     * @return 值为null的属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

        Set<String> nullPropertyNames = new HashSet<>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();

            // 排除class属性
            if ("class".equals(propertyName)) {
                continue;
            }

            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            if (propertyValue == null) {
                nullPropertyNames.add(propertyName);
            }
        }

        return nullPropertyNames.toArray(new String[0]);
    }

    /**
     * 合并两个数组
     *
     * @param array1 数组1
     * @param array2 数组2
     * @return 合并后的数组
     */
    private static String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 == null || array1.length == 0) {
            return array2 == null ? new String[0] : array2;
        }

        if (array2 == null || array2.length == 0) {
            return array1;
        }

        Set<String> mergedSet = new HashSet<>();
        for (String item : array1) {
            mergedSet.add(item);
        }
        for (String item : array2) {
            mergedSet.add(item);
        }

        return mergedSet.toArray(new String[0]);
    }
}