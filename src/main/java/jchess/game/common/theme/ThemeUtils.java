package jchess.game.common.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

public class ThemeUtils {
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);

    private static File resourceRootDir;

    static {
        try {
            resourceRootDir = new File(ThemeUtils.class.getResource("/").toURI());
        } catch (URISyntaxException e) {
            logger.error("", e);
        }
    }

    public static String getIconPath(File directory, String imageName) {
        File imageFile = new File(directory, imageName);
        return imageFile.getAbsolutePath()
                .replace(resourceRootDir.getAbsolutePath(), "")
                .replace("\\", "/")
                .replaceFirst("^/", "");
    }
}
