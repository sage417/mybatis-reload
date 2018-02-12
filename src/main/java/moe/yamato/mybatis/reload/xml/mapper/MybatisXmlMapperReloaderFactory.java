package moe.yamato.mybatis.reload.xml.mapper;

import moe.yamato.mybatis.reload.utils.ConfigurationHelper;
import moe.yamato.mybatis.reload.xml.mapper.filewather.DirectoryWatcher;
import moe.yamato.mybatis.reload.xml.mapper.handler.XmlMapperEventHandler;
import moe.yamato.mybatis.reload.xml.mapper.reloader.XmlMapperReloader;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MybatisXmlMapperReloaderFactory {

    private static final Logger log = LoggerFactory.getLogger(MybatisXmlMapperReloaderFactory.class);
    private static final Pattern PATTERN = Pattern.compile("file \\[(.*?\\.xml)]");
    private DirectoryWatcher watchService;
    private SqlSessionFactory sqlSessionFactory;
    private ScheduledExecutorService executorService;

    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }

        try {
            if (this.watchService != null) {
                this.watchService.close();
            }
        } catch (IOException e) {
            log.error("shutdown watch service error", e);
        }

    }

    public void init() throws Exception {

        Configuration configuration = sqlSessionFactory.getConfiguration();

        ConfigurationHelper configurationHelper = new ConfigurationHelper(configuration);
        Set<String> loadedResources = configurationHelper.readField(ConfigurationHelper.LOADED_RESOURCE_MAP_NAME);

        Set<String> resources = Collections.unmodifiableSet(loadedResources.stream().map(PATTERN::matcher).filter(Matcher::find).map(m -> m.group(1)).collect(Collectors.toSet()));

        // 配置扫描器.
        final XmlMapperReloader scanner = new XmlMapperReloader(configurationHelper);

        String root = StringUtils.getCommonPrefix(Collections.unmodifiableSet(loadedResources.stream().map(PATTERN::matcher).filter(Matcher::find).map(m -> m.group(1)).collect(Collectors.toSet())).toArray(new String[]{}));
        Path path = Paths.get(root);

        boolean regularFile = Files.isRegularFile(path);
        if (regularFile) {
            path = path.getParent();
        }

        watchService = new DirectoryWatcher(() -> new XmlMapperEventHandler(scanner, resources), true, path);


        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(watchService::processEvents, 5L, 1L, TimeUnit.SECONDS);

        log.info("启动mybatis自动热加载");
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}