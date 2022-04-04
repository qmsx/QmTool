package com.quanminshangxian.tool.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.form.request.QmFormImpExcelRequestParam;
import com.quanminshangxian.tool.form.request.QmFormImpTextRequestParam;
import com.quanminshangxian.tool.model.SendResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QmFormClientTests {

    private QmFormClient qmFormClient = QmFormClient.build("", "");

    /**
     * 添加数据
     */
    @Test
    public void addData() {
        String formCode = "";
        String dataCombType = "2";
        List<Object> dataList = new ArrayList<>();
        JSONObject dataItem = new JSONObject();
        dataItem.put("name", "临时1");
        dataItem.put("age", "19");
        dataItem.put("sex", "男");
        dataItem.put("occupation", "临时工1");
        dataItem.put("income", "10000");
        dataItem.put("edu", "大学");
        dataList.add(dataItem);
        JSONObject dataItem2 = new JSONObject();
        dataItem2.put("name", "临时2");
        dataItem2.put("age", "21");
        dataItem2.put("sex", "女");
        dataItem2.put("occupation", "临时工2");
        dataItem2.put("income", "8000");
        dataItem2.put("edu", "大学2");
        dataList.add(dataItem2);
        SendResponse commonResponse = qmFormClient.addData(formCode, dataCombType, dataList);
        System.out.println(JSON.toJSONString(commonResponse));
    }

    @Test
    public void impText() {
        String filePath = "C:\\Users\\gaokb\\Desktop\\许嵩.txt";
        QmFormImpTextRequestParam qmFormImpTextRequestParam = new QmFormImpTextRequestParam();
        qmFormImpTextRequestParam.setFormCode("643d7d8f69034eb6bede5fe520754e46");
        qmFormImpTextRequestParam.setDataCombType(1);
        qmFormImpTextRequestParam.setSeparator("#");
        qmFormImpTextRequestParam.setFilePath(filePath);
        SendResponse sendResponse = qmFormClient.impText(qmFormImpTextRequestParam);
        System.out.println(JSON.toJSONString(sendResponse));
    }

    @Test
    public void impExcel() {
        String filePath = "C:\\Users\\gaokb\\Desktop\\1.xlsx";
        QmFormImpExcelRequestParam qmFormImpExcelRequestParam = new QmFormImpExcelRequestParam();
        qmFormImpExcelRequestParam.setFormCode("643d7d8f69034eb6bede5fe520754e46");
        qmFormImpExcelRequestParam.setDataCombType(1);
        qmFormImpExcelRequestParam.setSeparator(",");
        qmFormImpExcelRequestParam.setFilePath(filePath);
        SendResponse sendResponse = qmFormClient.impExcel(qmFormImpExcelRequestParam);
        System.out.println(JSON.toJSONString(sendResponse));
    }

    @Test
    public void getTextExportUrl() {
        String formCode = "643d7d8f69034eb6bede5fe520754e46";
        SendResponse sendResponse = qmFormClient.getTextExportUrl(formCode, 60, 1, ",", null);
        System.out.println(JSON.toJSONString(sendResponse));
    }

    @Test
    public void getExcelExportUrl() {
        String formCode = "643d7d8f69034eb6bede5fe520754e46";
        SendResponse sendResponse = qmFormClient.getExcelExportUrl(formCode, 60);
        System.out.println(JSON.toJSONString(sendResponse));
    }

}
