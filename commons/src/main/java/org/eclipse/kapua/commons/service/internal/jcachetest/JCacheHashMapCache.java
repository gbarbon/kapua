/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.service.internal.jcachetest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Dummy implementation of JCache using an hashmap, to be used only for tests!
 */
public class JCacheHashMapCache<K, V> implements Cache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(JCacheCacheManager.class);
    private HashMap<K, V> hashMap;

    JCacheHashMapCache() {
        hashMap = new HashMap<>();
    }

    @Override
    public Object get(Object key) {
        try {
            return clone(hashMap.get(key));
        } catch (Exception e) {
            logger.error("Error while getting value from cache", e);
        }
        return null;
    }


    @Override
    public Map getAll(Set keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadAll(Set keys, boolean replaceExistingValues, CompletionListener completionListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Object key, Object value) {
        try {
            V newValue = (V) clone(value);
            hashMap.put((K) key, newValue);
        } catch (Exception e) {
            logger.error("Error while putting value in cache", e);
        }
    }

    @Override
    public Object getAndPut(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean putIfAbsent(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key) {
        return hashMap.remove(key) != null;
    }

    @Override
    public boolean remove(Object key, Object oldValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndRemove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndReplace(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll(Set keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unwrap(Class clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map invokeAll(Set keys, EntryProcessor entryProcessor, Object... arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke(Object key, EntryProcessor entryProcessor, Object... arguments) throws EntryProcessorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration getConfiguration(Class clazz) {
        throw new UnsupportedOperationException();
    }

    private Object clone(Object object) throws InvocationTargetException, IllegalAccessException,
            InstantiationException {
        if (object != null) {
            try {
                Class<?> objectClass = object.getClass();
                Constructor<?> objectConstructor = objectClass.getDeclaredConstructor();
                objectConstructor.setAccessible(true);  // change the constructor accessibility for protected fields
                Object newObject = objectConstructor.newInstance();

                Method[] methods = object.getClass().getMethods();
                Map<String, Method> getterMethods = new HashMap<>();
                Map<String, Method> setterMethods = new HashMap<>();

                for (Method method : methods) {
                    if (isGetter(method)) {
                        getterMethods.put(method.getName(), method);
                    } else if (isSetter(method)) {
                        setterMethods.put(method.getName(), method);
                    }
                }

                if (setterMethods.size() == 0) {
                    return object;
                }

                getterMethods.forEach((getterName, getter) -> {
                    Object fieldValue;
                    try {
                        fieldValue = getter.invoke(object, null);
                        Method setter = setterMethods.get("s" + getterName.substring(1));
                        if (setter != null) {
                            if (fieldValue != null && !fieldValue.getClass().isPrimitive()) {
                                fieldValue = clone(fieldValue);
                            }
                            setter.invoke(newObject, fieldValue);
                        }
                    } catch (Exception e) {
                        logger.error("Error while invoking methods.", e);
                    }
                });
                return newObject;
            } catch (NoSuchMethodException noe) {
                return object;
            }
        }
        return null;
    }

    public boolean isGetter(Method method) {
        if (!method.getName().startsWith("get")) { //&& !method.getName().startsWith("is")) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        return !void.class.equals(method.getReturnType());
    }

    public boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        return method.getParameterTypes().length == 1;
    }
}
