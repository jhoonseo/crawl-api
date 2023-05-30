package com.project.crawl.service;

import com.project.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class FtpService {
    @Value("${ftp.address}")
    private String ftpAddress;
    @Value("${ftp.port.number}")
    private Integer ftpPortNumber;
    @Value("${ftp.login.id}")
    private String ftpLoginId;
    @Value("${ftp.login.pw}")
    private String ftpLoginPw;
    @Value("${local.directory}")
    private String localDirectory;
    @Value("${local.daily.directory}")
    private String localDailyDirectory;
    @Value("${local.images.directory}")
    private String localImagesDirectory;
    private FTPClient ftp;
    private final CommonUtil commonUtil;

    public void connectFtp() throws IOException {
        ftp = new FTPClient();
        ftp.connect(ftpAddress, ftpPortNumber);
        ftp.login(ftpLoginId, ftpLoginPw);
    }

    public void quitFtp() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    public void uploadFtp(File file, String ftpPath, String filename) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ftpPath);
            ftp.storeFile(filename, inputStream);
            log.debug("uploaded successfully : {}", filename);
        }
    }

    public void uploadDirectoryFiles(String localDir, String ftpDir) throws IOException {
        File directory = new File(localDir);
        if (!directory.exists())
            return;

        File[] files = directory.listFiles();

        if (files != null && files.length == 0)
            return;

        connectFtp();

        Set<File> fileSet = commonUtil.getFilteredFileSet(files);

        for (File file : fileSet) {
            uploadFtp(file, ftpDir, file.getName());
        }

        quitFtp();
    }

    public String[][] getPathArray(String formatToday) {
        return new String[][]{
                {String.join("/", localDailyDirectory, formatToday, "images"), "/web/product/big"},
                {String.join("/", localDailyDirectory, formatToday, "images"), "/web/product/extra/excel"},
                {String.join("/", localDailyDirectory, formatToday, "medium"), "/web/product/medium"},
                {String.join("/", localDailyDirectory, formatToday, "small"), "/web/product/small"},
                {String.join("/", localDailyDirectory, formatToday, "tiny"), "/web/product/tiny"},
        };

    }

    public String[][] getTotalPathArray() {
        return new String[][]{
                {String.join("/", localImagesDirectory), "/web/product/big"},
                {String.join("/", localImagesDirectory), "/web/product/extra/excel"},
                {String.join("/", localDirectory, "images_resized", "medium"), "/web/product/medium"},
                {String.join("/", localDirectory, "images_resized", "small"), "/web/product/small"},
                {String.join("/", localDirectory, "images_resized", "tiny"), "/web/product/tiny"},
        };

    }


}