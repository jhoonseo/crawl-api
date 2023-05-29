package com.project.crawl.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class ImageUtil {
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
                image = ImageIO.read(Objects.requireNonNull(ImageUtil.class.getClassLoader().getResourceAsStream(url)));
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

    public void writeImage(BufferedImage image, String directory, String fileName) {
        try {
            ImageIO.write(image, "jpg", new File(directory, fileName));
        } catch (Exception e) {
            log.error("Error writing image to directory '{}': {}", directory, e.getMessage());
        }
    }

    public void writeImage(BufferedImage image, Path path) {
        try {
            ImageIO.write(image, "jpg", new File(path.toUri()));
        } catch (Exception e) {
            log.error("Error writing image to directory '{}': {}", path, e.getMessage());
        }
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

}