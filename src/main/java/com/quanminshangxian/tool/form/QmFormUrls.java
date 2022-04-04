package com.quanminshangxian.tool.form;

public final class QmFormUrls {

    private static final String API_BASE_URL = "http://api.form.quanminshangxian.com/";
    /**
     * 获取accessToken
     */
    static String GET_ACCESS_TOKEN = API_BASE_URL + "api/getAccessToken";
    /**
     * 表单添加数据
     */
    static String FORM_ADD_DATA = API_BASE_URL + "form/addData?access_token=%s";
    /**
     * 导入文本数据文件
     */
    static String FORM_IMP_TEXT = API_BASE_URL + "form/impText?access_token=%s";
    /**
     * 导入excel数据文件
     */
    static String FORM_IMP_EXCEL = API_BASE_URL + "form/impExcel?access_token=%s";
    /**
     * 获取文本文件导出路径
     */
    static String FORM_GET_TEXT_EXPORT_URL = API_BASE_URL + "form/getTextExportUrl?access_token=%s";
    /**
     * 获取excel文件导出路径
     */
    static String FORM_GET_EXCEL_EXPORT_URL = API_BASE_URL + "form/getExcelExportUrl?access_token=%s";

}
