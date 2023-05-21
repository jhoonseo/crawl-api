package com.costco.crawl.util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

@Service
public class CommonUtil {
    public Boolean checkClassExist(String className, WebDriver driver) {
        try {
            driver.findElement(By.className(className));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;

    }

    public Boolean checkClassFrom(String className, WebElement aFrom) {
        try {
            aFrom.findElement(By.className(className));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public Boolean checkTagFrom(String tagName, WebElement aFrom) {
        try {
            aFrom.findElement(By.tagName(tagName));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public String getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTimeMillis);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(timestamp);
    }

    public Timestamp getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return new Timestamp(currentTimeMillis);
    }

    public boolean isNukkiImage(String imageUrl) {
        try {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            int width = image.getWidth();
            int height = image.getHeight();

            // 이미지의 배경 픽셀을 가져옴
            int topLeftPixel = image.getRGB(0, 0);
            int topRightPixel = image.getRGB(width - 1, 0);
            int bottomLeftPixel = image.getRGB(0, height - 1);
            int bottomRightPixel = image.getRGB(width - 1, height - 1);

            // 배경이 흰색인 경우를 누끼 이미지로 간주
            return isPixelWhite(topLeftPixel)
                    && isPixelWhite(topRightPixel)
                    && isPixelWhite(bottomLeftPixel)
                    && isPixelWhite(bottomRightPixel);
        } catch (IOException e) {
            // 이미지를 불러오는 도중 예외가 발생하면 누끼 이미지로 처리하지 않음
            return false;
        }
    }

    private boolean isPixelWhite(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255;
    }

}
