package com.project.crawl.service;

import com.project.crawl.controller.dto.C24ProductChunk;
import com.project.crawl.controller.dto.C24ProductXlsx;
import com.project.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    @Value("${local.daily.directory.costco}")
    private String localDailyDirectoryCostco;

    private final CommonUtil commonUtil;

    public void closeWorkbook(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception ignored) {
        }
    }

    public List<C24ProductChunk> divideList(List<C24ProductXlsx> originalList, int chunkSize) {
        List<C24ProductChunk> dividedLists = new ArrayList<>();
        int originalSize = originalList.size();
        for (int i = 0; i < originalSize; i += chunkSize) {
            C24ProductChunk c24ProductChunk = new C24ProductChunk();
            int endIndex = Math.min(i + chunkSize, originalSize);
            List<C24ProductXlsx> chunk = originalList.subList(i, endIndex);
            c24ProductChunk.setC24CostcoProductList(chunk);
            c24ProductChunk.setStartIndex(i + 1);
            c24ProductChunk.setEndIndex(endIndex);
            dividedLists.add(c24ProductChunk);
        }
        return dividedLists;
    }

    public void generateC24ProductExcels(C24ProductChunk chunk, String formatToday, boolean isAvailable, String type) {
        // 엑셀 워크북 생성
        try (Workbook workbook = new XSSFWorkbook()) {
            // 시트 생성
            Sheet sheet = workbook.createSheet("SearchData");

            // 헤더 설정
            setC24ProductHeader(sheet);

            String availability = isAvailable ? "Y" : "N";
            String defaultBottomNotice = "<div class=\"kc-notice\">이 제품은 구매대행으로 유통되는 제품임.<br>이 제품은 전기용품 및 생활용품 안전관리법에 따른 대상임.<br>상품 특성상 주문진행 상황이 [배송 준비 중] 상태로 변경되면 취소가 어렵거나 불가능한 경우가 있사오니, 이 점 참고하시어 구매 부탁드립니다.</div>";
            String defaultManufacturerCode = "M0000000";
            String defaultSupplierCode = "S0000000";
            String defaultBrandCode = "B0000000";
            String defaultTrendCode = "T0000000";
            String defaultCategoryCode = "C000000A";
            String defaultDescription = "상세 참조";
            // 데이터 행 생성
            int rowNum = 1;
            for (C24ProductXlsx c24CostcoProductXlsx : chunk.getC24CostcoProductList()) {
                Row dataRow = sheet.createRow(rowNum++);
                String manageName = "cos-" + c24CostcoProductXlsx.getProductCode();
                String name = c24CostcoProductXlsx.getName();
                String qtyName = c24CostcoProductXlsx.getQtyNameCostco();
                String nameEn = c24CostcoProductXlsx.getNameEn();
                int price = c24CostcoProductXlsx.getQtyPrice();
                String categoryName = c24CostcoProductXlsx.getCategoryName();

                // -------- 데이터 셀 생성 및 값 설정 --------
                // 상품코드 0
                dataRow.createCell(0).setCellValue(c24CostcoProductXlsx.getC24Code());
                // 자체 상품코드 1
                dataRow.createCell(1).setCellValue(manageName);
                // 진열상태 2
                dataRow.createCell(2).setCellValue(availability);
                // 판매상태 3
                dataRow.createCell(3).setCellValue(availability);
                // 상품분류 번호 4 //
                dataRow.createCell(4).setCellValue(c24CostcoProductXlsx.getC24CateNo());
                // 상품분류 신상품영역 5
                dataRow.createCell(5).setCellValue(availability);
                // 상품분류 추천상품영역 6
                dataRow.createCell(6).setCellValue(availability);
                // 상품명 7
                dataRow.createCell(7).setCellValue(qtyName);
                // 영문 상품명 8
                dataRow.createCell(8).setCellValue(nameEn);
                // 상품명(관리용) 9
                dataRow.createCell(9).setCellValue(manageName);
                // 공급사 상품명 10
                dataRow.createCell(10).setCellValue(name);
                // 모델명 11
                dataRow.createCell(11).setCellValue(name.substring(0, Math.min(name.length(), 20)));
                // 상품 요약설명 12
                String summaryDescription = String.join(" ", name, nameEn);
                String subSummaryDescription = summaryDescription.substring(0, Math.min(summaryDescription.length(), 40));
                dataRow.createCell(12).setCellValue(subSummaryDescription);
                // 상품 간략설명 13
                dataRow.createCell(13).setCellValue(summaryDescription);
                // 상품 상세설명 14
                String detail = String.join("<br>",
                        c24CostcoProductXlsx.getThumbDetail(),
                        c24CostcoProductXlsx.getDescriptionDetail(),
                        c24CostcoProductXlsx.getSpecInfoTable(),
                        c24CostcoProductXlsx.getDeliveryInfo(),
                        c24CostcoProductXlsx.getRefundInfo(),
                        defaultBottomNotice);
                dataRow.createCell(14).setCellValue("<p align=\"center\">" + detail + "</p>");
                // 모바일 상품 상세설명 설정 15
                dataRow.createCell(15).setCellValue("A");
                // 모바일 상품 상세설명 16
                dataRow.createCell(16);
                // 검색어설정 17 // todo 검색어 키워드 추가
                String searchKeywords = String.join(",", categoryName.replace("/", ","), name.replace(" ", ","), nameEn.replace(" ", ","));
                dataRow.createCell(17).setCellValue(searchKeywords);
                // 과세구분 18
                dataRow.createCell(18).setCellValue("A|10");
                // 소비자가 19
                double adjustedConsumerPrice = price + price * ((double) (new Random().nextInt(69 - 42 + 1) + 42) / 100);
                double consumerPrice = Math.round(adjustedConsumerPrice / 100) * 100;
                dataRow.createCell(19).setCellValue(consumerPrice);
                // 공급가 20
                dataRow.createCell(20).setCellValue(price);
                // 상품가 21
                double adjustedSellingPrice = price + (double) (price * 35) / 100;
                int productPrice = (int) Math.round(adjustedSellingPrice / 100) * 100;
                dataRow.createCell(21).setCellValue(productPrice);
                // 판매가 22
                dataRow.createCell(22).setCellValue(productPrice);
                // 판매가 대체문구 사용 23
                dataRow.createCell(23).setCellValue("N");
                // 판매가 대체문구 24
                dataRow.createCell(24);
                // 주문수량 제한 기준 25
                dataRow.createCell(25).setCellValue("O");
                // 최소 주문수량(이상) 26 (최대는 오픈마켓 적용이 되는데, 최소는 일부 마켓에서 적용이 안된다.)
                dataRow.createCell(26).setCellValue("");
                // 최대 주문수량(이하) 27
                dataRow.createCell(27);
                int maxQty = c24CostcoProductXlsx.getMaxQty();
                if (maxQty > 0) {
                    dataRow.getCell(27).setCellValue(maxQty);
                } else {
                    dataRow.getCell(27).setCellValue("");
                }
                // 적립금 28
                dataRow.createCell(28);
                // 적립금 구분 29
                dataRow.createCell(29).setCellValue("P");
                // 공통이벤트 정보 30
                dataRow.createCell(30).setCellValue("N");
                // 성인인증 31
                dataRow.createCell(31).setCellValue("N");
                // 옵션사용 32
                dataRow.createCell(32).setCellValue("N");
                // 품목 구성방식 33
                dataRow.createCell(33);
                // 옵션 표시방식 34
                dataRow.createCell(34);
                // 옵션세트명 35
                dataRow.createCell(35);
                // 옵션입력 36
                dataRow.createCell(36);
                // 옵션 스타일 37
                dataRow.createCell(37);
                // 버튼이미지 설정 38
                dataRow.createCell(38);
                // 색상 설정 39
                dataRow.createCell(39);
                // 필수여부 40
                dataRow.createCell(40);
                // 품절표시 문구 41
                dataRow.createCell(41);
                // 추가입력옵션 42
                dataRow.createCell(42);
                // 추가입력옵션 명칭 43
                dataRow.createCell(43);
                // 추가입력옵션 선택/필수여부 44
                dataRow.createCell(44);
                // 입력글자수(자) 45
                dataRow.createCell(45);
                // 이미지등록(상세) 46
                String thumbMainFilename = c24CostcoProductXlsx.getThumbMainFilename();
                dataRow.createCell(46).setCellValue(thumbMainFilename);
                // 이미지등록(목록) 47
                dataRow.createCell(47).setCellValue(thumbMainFilename);
                // 이미지등록(작은목록) 48
                dataRow.createCell(48).setCellValue(thumbMainFilename);
                // 이미지등록(축소) 49
                dataRow.createCell(49).setCellValue(thumbMainFilename);
                // 이미지등록(추가) 50
                dataRow.createCell(50).setCellValue(c24CostcoProductXlsx.getThumbExtraFilenames());
                // 제조사 51
                dataRow.createCell(51).setCellValue(defaultManufacturerCode);
                // 공급사 52
                dataRow.createCell(52).setCellValue(defaultSupplierCode);
                // 브랜드 53
                dataRow.createCell(53).setCellValue(defaultBrandCode);
                // 트렌드 54
                dataRow.createCell(54).setCellValue(defaultTrendCode);
                // 자체분류 코드 55
                dataRow.createCell(55).setCellValue(defaultCategoryCode);
                // 제조일자 56
                dataRow.createCell(56);
                // 출시일자 57
                dataRow.createCell(57);
                // 유효기간 사용여부 58
                dataRow.createCell(58).setCellValue("N");
                // 유효기간 59
                dataRow.createCell(59);
                // 원산지 60
                dataRow.createCell(60).setCellValue(1800);
                // 상품부피(cm) 61
                dataRow.createCell(61);
                // 상품결제안내 62
                dataRow.createCell(62);
                // 상품배송안내 63
                dataRow.createCell(63);
                // 교환/반품안내 64
                dataRow.createCell(64);
                // 서비스문의/안내 65
                dataRow.createCell(65);
                // 배송정보 66
                dataRow.createCell(66).setCellValue("T");
                // 배송방법 67
                dataRow.createCell(67).setCellValue("A");
                // 국내/해외배송 68
                dataRow.createCell(68).setCellValue("A");
                // 배송지역 69
                dataRow.createCell(69).setCellValue(defaultDescription);
                // 배송비 선결제 설정 70
                dataRow.createCell(70).setCellValue("P");
                // 배송기간 71
                dataRow.createCell(71).setCellValue("1|5");
                // 배송비 구분 72
                dataRow.createCell(72).setCellValue("T");
                // 배송비입력 73
                dataRow.createCell(73);
                // 스토어픽업 설정 74
                dataRow.createCell(74).setCellValue("N");
                // 상품 전체중량(kg) 75
                dataRow.createCell(75).setCellValue(5);
                // HS코드 76
                dataRow.createCell(76);
                // 상품 구분(해외통관) 77
                dataRow.createCell(77);
                // 상품소재 78
                dataRow.createCell(78).setCellValue(defaultDescription);
                // 영문 상품소재(해외통관) 79
                dataRow.createCell(79).setCellValue(defaultDescription);
                // 검색엔진최적화(SEO) 검색엔진 노출 설정 80
                dataRow.createCell(80).setCellValue("Y");
                // 검색엔진최적화(SEO) Title 81
                dataRow.createCell(81).setCellValue(name);
                // 검색엔진최적화(SEO) Author 82
                dataRow.createCell(82).setCellValue(String.join("-", "코코모몰", name));
                // 검색엔진최적화(SEO) Description 83
                dataRow.createCell(83).setCellValue(summaryDescription);
                // 검색엔진최적화(SEO) Keywords 84 // todo 검색어 키워드 추가
                dataRow.createCell(84).setCellValue(searchKeywords);
                // 검색엔진최적화(SEO) 상품 이미지 Alt 텍스트 85
                dataRow.createCell(85).setCellValue(searchKeywords);
                // 개별결제수단설정 86
                dataRow.createCell(86);
                // 상품배송유형 코드 87
                dataRow.createCell(87);
                // 메모 88
                dataRow.createCell(88);
            }


            // 일자별 경로 생성
            String dailyXlsxDirectory = String.join("/", localDailyDirectoryCostco, formatToday, "xlsx");
            commonUtil.createDirectory(dailyXlsxDirectory);
            // 파일로 저장
            String fileName = type + '_' + chunk.getStartIndex() + '-' + chunk.getEndIndex() + '_'+ LocalTime.now() + '-' + availability + ".xlsx";
            String filePath = String.join("/", dailyXlsxDirectory, fileName);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            log.debug("Excel file created : {}", fileName);
            System.out.println("Excel 파일이 생성되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setC24ProductHeader(Sheet sheet) {

        // 헤더 내용 배열
        String[] headers = {
                "상품코드", "자체 상품코드", "진열상태", "판매상태", "상품분류 번호",
                "상품분류 신상품영역", "상품분류 추천상품영역", "상품명", "영문 상품명", "상품명(관리용)",
                "공급사 상품명", "모델명", "상품 요약설명", "상품 간략설명", "상품 상세설명",
                "모바일 상품 상세설명 설정", "모바일 상품 상세설명", "검색어설정", "과세구분", "소비자가",
                "공급가", "상품가", "판매가", "판매가 대체문구 사용", "판매가 대체문구",
                "주문수량 제한 기준", "최소 주문수량(이상)", "최대 주문수량(이하)", "적립금",
                "적립금 구분", "공통이벤트 정보", "성인인증", "옵션사용", "품목 구성방식",
                "옵션 표시방식", "옵션세트명", "옵션입력", "옵션 스타일", "버튼이미지 설정",
                "색상 설정", "필수여부", "품절표시 문구", "추가입력옵션", "추가입력옵션 명칭",
                "추가입력옵션 선택/필수여부", "입력글자수(자)", "이미지등록(상세)", "이미지등록(목록)",
                "이미지등록(작은목록)", "이미지등록(축소)", "이미지등록(추가)", "제조사", "공급사",
                "브랜드", "트렌드", "자체분류 코드", "제조일자", "출시일자", "유효기간 사용여부",
                "유효기간", "원산지", "상품부피(cm)", "상품결제안내", "상품배송안내", "교환/반품안내",
                "서비스문의/안내", "배송정보", "배송방법", "국내/해외배송", "배송지역",
                "배송비 선결제 설정", "배송기간", "배송비 구분", "배송비입력", "스토어픽업 설정",
                "상품 전체중량(kg)", "HS코드", "상품 구분(해외통관)", "상품소재",
                "영문 상품소재(해외통관)", "검색엔진최적화(SEO) 검색엔진 노출 설정",
                "검색엔진최적화(SEO) Title", "검색엔진최적화(SEO) Author",
                "검색엔진최적화(SEO) Description", "검색엔진최적화(SEO) Keywords",
                "검색엔진최적화(SEO) 상품 이미지 Alt 텍스트", "개별결제수단설정",
                "상품배송유형 코드", "메모"
        };


        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }
}
