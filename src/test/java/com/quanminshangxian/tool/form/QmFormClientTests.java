package com.quanminshangxian.tool.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.model.CommonResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QmFormClientTests {

    /**
     * 添加数据
     */
    @Test
    public void addData() {
        QmFormClient qmFormClient = QmFormClient.build("", "");
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
        CommonResponse commonResponse = qmFormClient.addData(formCode, dataCombType, dataList);
        System.out.println(JSON.toJSONString(commonResponse));
    }

}
