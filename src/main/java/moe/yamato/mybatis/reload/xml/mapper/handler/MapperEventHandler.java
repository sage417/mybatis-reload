package moe.yamato.mybatis.reload.xml.mapper.handler;

import java.nio.file.Path;
import java.util.Set;

public interface MapperEventHandler {

    void handleMapperCreate(Path mapperPath);

    void handleMapperModify(Path mapperPath);

    void handleMapperDelete(Path mapperPath);

    void reloadIfNeeded();
}
