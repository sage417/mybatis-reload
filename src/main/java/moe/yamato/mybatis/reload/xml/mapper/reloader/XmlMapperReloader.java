package moe.yamato.mybatis.reload.xml.mapper.reloader;

import moe.yamato.mybatis.reload.utils.ConfigurationHelper;
import moe.yamato.mybatis.reload.utils.ReflectionUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class XmlMapperReloader implements MapperReloader {

    private static final Logger log = LoggerFactory.getLogger(XmlMapperReloader.class);

    private final ConfigurationHelper configurationHelper;

    public XmlMapperReloader(ConfigurationHelper configurationHelper) {
        this.configurationHelper = configurationHelper;
    }

    private <T> void clearCollection(String fieldName) {
        try {
            Collection typeValue = configurationHelper.readField(fieldName);
            typeValue.clear();
        } catch (NullPointerException e) {
            log.warn("could not clear class:{} field:{}", Configuration.class.getSimpleName(), fieldName);
        }
    }

    private <T> void clearMap(String fieldName) {
        try {
            Map typeValue = configurationHelper.readField(fieldName);
            typeValue.clear();
        } catch (NullPointerException e) {
            log.warn("could not clear class:{} field:{}", Configuration.class.getSimpleName(), fieldName);
        }
    }

    private void clearKnownMappers() {
        MapperRegistry mapperRegistry = configurationHelper.readField(ConfigurationHelper.MAPPER_REGISTRY_NAME);
        try {
            Field field = ReflectionUtil.findField(MapperRegistry.class, "knownMappers", Map.class);
            ReflectionUtil.makeAccessible(field);
            Map knownMappers = (Map) ReflectionUtil.getField(field, mapperRegistry);
            knownMappers.clear();
        } catch (NullPointerException e) {
            log.warn("could not clear class:{} field:{}", MapperRegistry.class.getSimpleName(), ConfigurationHelper.MAPPER_REGISTRY_NAME);
        }
    }

    private void load(Configuration configuration, Map<String, XNode> sqlFragments, Set<String> resources) throws RuntimeException {

        for (String source : resources) {
            try {
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(Files.newInputStream(Paths.get(source)), configuration, source, sqlFragments);
                xmlMapperBuilder.parse();
                log.info("reloaded : {}", source);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse mapping resource: '" + source + "'", e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }

    @Override
    public void reload(Set<String> resources) {
        StopWatch sw = StopWatch.createStarted();

        Configuration configuration = configurationHelper.getConfiguration();

        this.resetConfiguration();

        try {
            this.load(configuration, configuration.getSqlFragments(), resources);
        } catch (Exception e) {
            log.error("加载mybatis xml失败", e);
        }

        sw.stop();
        log.info("重新加载mybatis映射文件完成. 耗时{}ms", sw.getTime());
    }

    private void resetConfiguration() {

        ConfigurationHelper.INCOMPLETE_COLLECTION_NAMES.forEach(this::clearCollection);

        ConfigurationHelper.MAPPER_MAP_NAMES.forEach(this::clearMap);

        clearKnownMappers();

        clearCollection(ConfigurationHelper.LOADED_RESOURCE_MAP_NAME);
    }

}
