package moe.yamato.mybatis.reload.xml.mapper.reloader;

import java.util.Set;

@FunctionalInterface
public interface MapperReloader {

    void reload(Set<String> resources);
}
