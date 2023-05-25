package com.project.crawl.service;

import com.project.crawl.dao.RestrictedKeywordDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestrictedKeywordService {
    private final RestrictedKeywordDao restrictedKeywordDao;

    public List<String> getResetrictedKeywordList() {
        return restrictedKeywordDao.getResetrictedKeywordList();
    }

}
