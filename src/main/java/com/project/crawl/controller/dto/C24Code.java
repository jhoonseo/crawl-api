package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class C24Code {
    private Character a;
    private Character b;
    private Character c;
    private Character d;
    
    public void setCharsByCode(String code) {
        a = code.charAt(7);
        b = code.charAt(6);
        c = code.charAt(5);
        d = code.charAt(4);
    }

    public void setCharsByChars(Character a, Character b, Character c, Character d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public String getC24Code() {
        return String.format("P000%s%s%s%s", d, c, b, a);
    }
}
