package com.quanminshangxian.tool.form.request;

public class QmFormImpTextRequestParam {

    private String formCode;
    private Integer dataCombType;
    private String separator;
    private String filePath;

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public Integer getDataCombType() {
        return dataCombType;
    }

    public void setDataCombType(Integer dataCombType) {
        this.dataCombType = dataCombType;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
