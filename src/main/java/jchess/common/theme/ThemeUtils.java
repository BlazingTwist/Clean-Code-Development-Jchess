package jchess.common.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

public class ThemeUtils {
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);

    private static File resourceRootDir;

    static {
        try {
            resourceRootDir = new File(Objects.requireNonNull(ThemeUtils.class.getResource("/")).toURI());
        } catch (URISyntaxException e) {
            logger.error("", e);
        }
    }

    public static String getIconPath(File directory, String imageName) {
        File imageFile = new File(directory, imageName);
        return sanitizeIconPath(imageFile.getAbsolutePath().replace(resourceRootDir.getAbsolutePath(), ""));
    }

    public static String sanitizeIconPath(String path) {
        return path.replace("\\", "/")
                .replaceFirst("^/", "");
    }
}
