package com.project.crawl.service;

import com.project.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPReply;
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
    @Value("${ftp.address.costco}")
    private String ftpAddressCostco;
    @Value("${ftp.port.number}")
    private Integer ftpPortNumber;
    @Value("${ftp.login.id.costco}")
    private String ftpLoginIdCostco;
    @Value("${ftp.login.pw.costco}")
    private String ftpLoginPwCostco;
    @Value("${local.directory.costco}")
    private String localDirectoryCostco;
    @Value("${local.daily.directory.costco}")
    private String localDailyDirectoryCostco;
    @Value("${local.images.directory.costco}")
    private String localImagesDirectoryCostco;
    private FTPClient ftp;
    private final CommonUtil commonUtil;

    /*
    public void connectFtp() throws IOException {
        ftp = new FTPClient();
        ftp.connect(ftpAddressCostco, ftpPortNumber);
        ftp.login(ftpLoginIdCostco, ftpLoginPwCostco);
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

     */
    public void connectFtp() throws IOException {
        ftp = new FTPClient();
        try {
            ftp.connect(ftpAddressCostco, ftpPortNumber);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            ftp.login(ftpLoginIdCostco, ftpLoginPwCostco);
            log.debug("Connected and logged in to FTP server.");
        } catch (IOException e) {
            log.error("Could not connect or login to FTP server", e);
            throw e;
        }
    }

    public void quitFtp() throws IOException {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
                log.debug("Logged out and disconnected from FTP server.");
            }
        } catch (IOException e) {
            log.error("Error while disconnecting from the FTP server", e);
            throw e;
        }
    }

    public void uploadFtp(File file, String ftpPath, String filename) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ftpPath);
            boolean completed = ftp.storeFile(filename, inputStream);
            if (completed) {
//                log.debug("Uploaded successfully: {}", filename);
                System.out.println("Uploaded successfully: " + filename);
            } else {
                throw new IOException("Failed to upload file: " + filename);
            }
        } catch (IOException e) {
            log.error("Error uploading file: {}", filename, e);
            throw e;
        }
    }

    public void uploadDirectoryFiles(String localDir, String ftpDir) throws IOException {
        File directory = new File(localDir);
        if (!directory.exists()) {
            log.warn("Directory does not exist: {}", localDir);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("No files to upload in directory: {}", localDir);
            return;
        }

        Set<File> fileSet = commonUtil.getFilteredFileSet(files);
        for (File file : fileSet) {
            uploadFtp(file, ftpDir, file.getName());
        }
    }


    public String[][] getPathArray(String formatToday) {
        return new String[][]{
                {String.join("/", localDailyDirectoryCostco, formatToday, "images"), "/web/product/big"},
                {String.join("/", localDailyDirectoryCostco, formatToday, "images"), "/web/product/extra/excel"},
                {String.join("/", localDailyDirectoryCostco, formatToday, "medium"), "/web/product/medium"},
                {String.join("/", localDailyDirectoryCostco, formatToday, "small"), "/web/product/small"},
                {String.join("/", localDailyDirectoryCostco, formatToday, "tiny"), "/web/product/tiny"},
        };

    }

    public String[][] getTotalPathArray() {
        return new String[][]{
                {String.join("/", localImagesDirectoryCostco), "/web/product/big"},
                {String.join("/", localImagesDirectoryCostco), "/web/product/extra/excel"},
                {String.join("/", localDirectoryCostco, "images_resized", "medium"), "/web/product/medium"},
                {String.join("/", localDirectoryCostco, "images_resized", "small"), "/web/product/small"},
                {String.join("/", localDirectoryCostco, "images_resized", "tiny"), "/web/product/tiny"},
        };

    }


}