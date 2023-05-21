package com.costco.crawl.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ImageUtils {
    public static boolean checkImgRgb(String route) {
        BufferedImage image = readImage(route);
        if (image == null) {
            return false;
        }

        BufferedImage rgbImage = convertToRgbImage(image);
        int x = image.getWidth();
        int y = image.getHeight();
        if (x > 0 && y > 0) {
            boolean tl = checkPixelRgb(1, 1, rgbImage);
            boolean tr = checkPixelRgb(x - 1, 1, rgbImage);
            boolean bl = checkPixelRgb(1, y - 1, rgbImage);
            boolean br = checkPixelRgb(x - 1, y - 1, rgbImage);
            return tl && tr && bl && br;
        } else {
            return false;
        }
    }

    private static BufferedImage readImage(String url) {
        BufferedImage image;
        try {
            if (isValidUrl(url)) {
                image = ImageIO.read(new URL(url));
            } else {
                image = ImageIO.read(Objects.requireNonNull(ImageUtils.class.getClassLoader().getResourceAsStream(url)));
            }
        } catch (IOException e) {
            return null;
        }
        return image;
    }

    private static BufferedImage convertToRgbImage(BufferedImage image) {
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbImage.getGraphics().drawImage(image, 0, 0, null);
        return rgbImage;
    }

    private static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkPixelRgb(int x, int y, BufferedImage image) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return r == 255 && (g == 255 || b == 255);
    }
}