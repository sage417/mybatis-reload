package moe.yamato.mybatis.reload.xml.mapper.handler;

import moe.yamato.mybatis.reload.xml.mapper.reloader.MapperReloader;

import java.nio.file.Path;
import java.util.Set;

public class XmlMapperEventHandler implements MapperEventHandler {
    private boolean needReload;
    private MapperReloader mapperReloader;
    private Set<String> resources;

    public XmlMapperEventHandler(MapperReloader mapperReloader, Set<String> resources) {
        this.mapperReloader = mapperReloader;
        this.resources = resources;
    }

    @Override
    public void handleMapperCreate(Path mapperPath) {
        needReload = true;
    }

    @Override
    public void handleMapperModify(Path mapperPath) {
        needReload = true;
    }

    @Override
    public void handleMapperDelete(Path mapperPath) {
        needReload = true;
    }

    @Override
    public void reloadIfNeeded() {
        if (needReload) {
            mapperReloader.reload(resources);
        }
    }
}
