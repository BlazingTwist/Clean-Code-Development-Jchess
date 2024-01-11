package jchess.common.theme;

import util.ResourceHelper;

public class ThemeUtils {
    private static final ResourceHelper.ResourceFile resourceRoot = ResourceHelper.getResource("/");

    public static String getIconPath(ResourceHelper.ResourceFile directory, String imageName) {
        ResourceHelper.ResourceFile imageFile = directory.resolve(imageName).normalize();
        return sanitizeIconPath(imageFile.asRelativePath(resourceRoot).toString());
    }

    public static String sanitizeIconPath(String path) {
        return path.replace("\\", "/")
                .replaceFirst("^/", "");
    }
}
