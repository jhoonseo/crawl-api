import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javax.imageio.ImageIO;

public class ImageUtils {
    public static boolean checkImageRGB(String route) {
        BufferedImage image;
        try {
            if (route.startsWith("http://") || route.startsWith("https://")) {
                URL url = new URL(route);
                image = ImageIO.read(url);
            } else {
                image = ImageIO.read(Objects.requireNonNull(ImageUtils.class.getResource(route)));
            }
        } catch (Exception e) {
            return false;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width > 0 && height > 0) {
            boolean tl = checkPixelRGB(1, 1, image);
            boolean tr = checkPixelRGB(width - 1, 1, image);
            boolean bl = checkPixelRGB(1, height - 1, image);
            boolean br = checkPixelRGB(width - 1, height - 1, image);

            return tl && tr && bl && br;
        } else {
            return false;
        }
    }

    public static boolean checkPixelRGB(int x, int y, BufferedImage image) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        return r == 255 && g == 255 && b == 255;
    }

    public static void main(String[] args) {
        String route = "https://www.costco.co.kr/medias/sys_master/images/h8f/h04/83370091839518.jpg";
        boolean isNukkiImage = checkImageRGB(route);
    }
}
