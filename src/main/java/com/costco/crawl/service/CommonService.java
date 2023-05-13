package com.costco.crawl.service;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Service
public class CommonService {
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
}
