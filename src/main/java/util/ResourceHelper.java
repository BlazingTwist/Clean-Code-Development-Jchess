package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public enum ResourceHelper {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ResourceHelper.class);
    private static Path resourceRoot = null;

    private static Path getRoot() {
        if (resourceRoot == null) {
            try {
                String jarFile = ResourceHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile();
                logger.info("CodeSource path: {}", jarFile);

                if (jarFile.endsWith(".jar")) {
                    @SuppressWarnings("resource") // FileSystem must remain opened until Server is stopped
                    FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + jarFile), new HashMap<>());
                    resourceRoot = fs.getPath("/");
                } else {
                    FileSystem fs = FileSystems.getDefault();
                    if (jarFile.startsWith("/")) {
                        jarFile = jarFile.substring(1);
                    }
                    resourceRoot = fs.getPath(jarFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to create jar-FileSystem", e);
            }
        }
        return resourceRoot;
    }

    public static ResourceFile getResource(String path) {
        String relativePath = Path.of("/").relativize(Path.of(path)).toString();
        return new ResourceFile(getRoot().resolve(relativePath));
    }

    public static class ResourceFile {
        private final Path path;

        public ResourceFile(Path path) {
            this.path = path;
        }

        public ResourceFile resolve(String subPath) {
            return new ResourceFile(path.resolve(subPath));
        }

        public ResourceFile parent() {
            return new ResourceFile(path.getParent());
        }

        public ResourceFile normalize() {
            return new ResourceFile(path.normalize());
        }

        public ResourceFile asRelativePath(ResourceFile root) {
            return new ResourceFile(root.path.relativize(path));
        }

        public InputStream toInputStream() throws IOException {
            return Files.newInputStream(path, StandardOpenOption.READ);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
