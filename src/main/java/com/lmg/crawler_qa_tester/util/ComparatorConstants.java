package com.lmg.crawler_qa_tester.util;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ComparatorConstants {
    public static final String HEADER_STRIP_API_NAME = "headerStrip";
    public static final String HEADER_NAV_API_NAME = "headerNavigation";
    public static final String FOOTER_STRIP_API_NAME = "footer";
    public static final String LANG_EN_CODE = "en";
    public static final String LANG_AR_CODE = "ar";
    public static final String SHEET_COLUMN1 = "Path";
    public static final String SHEET_COLUMN2 = "";
    public static final String SHEET_COLUMN3 = "";
    public static final String SHEET_COLUMN4 = "Match";

    public static final String HEADER_STRIP_API_URL_SUFFIX = "/api/menu/menus/amplience/fetch";
    public static final String HEADER_NAV_API_URL_SUFFIX = "/api/menu/menus/amplience/fetch";
    public static final String FOOTER_API_URL_SUFFIX = "/api/menu/menus/amplience/fetch";
    public static final List<String> LANG_CODES = List.of(LANG_EN_CODE, LANG_AR_CODE);

}
