package com.wxy.web.favorites.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class JpaUtils {

    public static <T> T evictSession(T source, Class<T> clz) {
        if (source == null) return null;
        T t = BeanUtils.instantiateClass(clz);
        BeanUtils.copyProperties(source, t);
        return t;
    }

    public static void copyNotNullProperties(Object source, Object target) {
        BeanWrapper srcBean = new BeanWrapperImpl(target);
        PropertyDescriptor[] pds = srcBean.getPropertyDescriptors();
        Set<String> notEmptyName = new HashSet<>();
        for (PropertyDescriptor p : pds) {
            Object value = srcBean.getPropertyValue(p.getName());
            if (value != null) notEmptyName.add(p.getName());
        }
        BeanUtils.copyProperties(source, target, notEmptyName.toArray(new String[0]));
    }
}