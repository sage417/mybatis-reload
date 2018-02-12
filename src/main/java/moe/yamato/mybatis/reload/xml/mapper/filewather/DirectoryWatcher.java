package moe.yamato.mybatis.reload.xml.mapper.filewather;

import moe.yamato.mybatis.reload.xml.mapper.handler.MapperEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Package: com.lianjia.sh.mybatis.reload.filesystem
 * Author: mac
 * Date: 2017/1/10
 */
public class DirectoryWatcher implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final WatchService watcher = FileSystems.getDefault().newWatchService();
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final Supplier<MapperEventHandler> mapperEventHandlerSupplier;
    private final boolean recursive;

    /**
     * Creates a WatchService and registers the given directory
     */
    public DirectoryWatcher(Supplier<MapperEventHandler> mapperEventHandlerSupplier, boolean recursive, Path... paths) throws IOException {
        this.mapperEventHandlerSupplier = mapperEventHandlerSupplier;
        this.recursive = recursive;

        // enable trace after initial registration
        //this.trace = true;

        this.register(paths);
    }

    private void register(Path[] paths) throws IOException {
        for (Path path : recursive ? walkDirectoryTree(paths) : Arrays.asList(paths)) {
            log.info("Scanning {} ...", path.toString());
            register(path);
        }
        log.info("Done.");
    }

    private List<Path> walkDirectoryTree(Path[] paths) {
        return Arrays.stream(paths).map(p -> {
            Optional<Stream<Path>> optional = Optional.empty();

            try {
                optional = Optional.of(Files.walk(p, FileVisitOption.FOLLOW_LINKS));
            } catch (IOException e) {
                log.warn("");
            }
            return optional;
        })
                .filter(Optional::isPresent)
                .flatMap(Optional::get)
                .filter(p -> Files.isDirectory(p)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void close() throws IOException {
        if (this.watcher != null) {
            this.watcher.close();
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                log.error("WatchKey not recognized!!");
                continue;
            }

            MapperEventHandler mapperEventHandler = mapperEventHandlerSupplier.get();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                log.info("{}: {}", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }

                    if (Files.isRegularFile(child, NOFOLLOW_LINKS)) {
                        mapperEventHandler.handleMapperCreate(child);
                    }
                }

                if (kind == ENTRY_MODIFY
//                        && Files.isRegularFile(child, NOFOLLOW_LINKS)
//                        && !reload
//                        && this.reLoader.getImmutableMapperLocations().contains(child.toString())
                        ) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        // dir modify
                    }

                    if (Files.isRegularFile(child, NOFOLLOW_LINKS)) {
                        // mapper modify
                        mapperEventHandler.handleMapperModify(child);
                    }
                }

                if (kind == ENTRY_DELETE) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        // dir deleted may be mapper deleted too
                    }

                    if (Files.isRegularFile(child, NOFOLLOW_LINKS)) {
                        // mapper deleted
                        mapperEventHandler.handleMapperDelete(child);
                    }
                }
            }

            mapperEventHandler.reloadIfNeeded();

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path prev = keys.get(key);
        if (prev == null) {
            log.info("register: {}", dir);
        } else {
            if (!dir.equals(prev)) {
                log.info("update: {} -> {}", prev, dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final FileVisitResult fileVisitResult = super.preVisitDirectory(dir, attrs);
                register(dir);
                return fileVisitResult;
            }
        });
    }
}
