package com.project.crawl.util;

public enum LanguageCode {
    Korean("한국어", "ko"),
    English("영어", "en"),
    Japanese("일본어", "ja"),
    SimplifiedChinese("중국어 간체", "zh-CN"),
    TraditionalChinese("중국어 번체", "zh-TW"),
    Vietnamese("베트남어", "vi"),
    Thai("태국어", "th"),
    Indonesian("인도네시아어", "id"),
    French("프랑스어", "fr"),
    Spanish("스페인어", "es"),
    Russian("러시아어", "ru"),
    German("독일어", "de"),
    Italian("이탈리아어", "it"),
    Portuguese("포르투갈어", "pt"),
    Hindi("힌디어", "hi");


    private final String language;
    private final String code;

    LanguageCode(String language, String code) {
        this.language = language;
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

}
