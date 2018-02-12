package moe.yamato.mybatis.reload.utils;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.*;

public class ConfigurationHelper {

    public static final List<String> INCOMPLETE_COLLECTION_NAMES = Arrays.asList("incompleteStatements", "incompleteCacheRefs", "incompleteResultMaps");
    public static final List<String> MAPPER_MAP_NAMES = Arrays.asList("mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments", "cacheRefMap");
    public static final String LOADED_RESOURCE_MAP_NAME = "loadedResources";
    public static final String MAPPER_REGISTRY_NAME = "mapperRegistry";
    private static final Map<String, Class<?>> PROPERTY_NAME_TYPES;

    static {
        Map<String, Class<?>> map = new HashMap<>();
        map.put(LOADED_RESOURCE_MAP_NAME, Set.class);
        map.put(MAPPER_REGISTRY_NAME, MapperRegistry.class);
        INCOMPLETE_COLLECTION_NAMES.forEach(k -> map.put(k, Collection.class));
        MAPPER_MAP_NAMES.forEach(k -> map.put(k, Map.class));

        PROPERTY_NAME_TYPES = Collections.unmodifiableMap(map);
    }

    private final Configuration configuration;

    public ConfigurationHelper(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public <T> T readField(String fieldName) {
        Field field = ReflectionUtil.findField(Configuration.class, fieldName, PROPERTY_NAME_TYPES.get(fieldName));
        if (field == null) {
            throw new NullPointerException("Configuration field not contains field: " + fieldName);
        }
        ReflectionUtil.makeAccessible(field);
        return (T) ReflectionUtil.getField(field, configuration);
    }
}