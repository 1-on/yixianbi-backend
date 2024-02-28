package com.yixian.yixianbi.resolver;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiResolver {


    public String doChatWithWXYY(String message) {
        // 创建 JSON 对象
        JSONObject jsonObject = new JSONObject();
        // 创建包含在 "messages" 键下的 JSON 数组
        JSONArray messagesArray = new JSONArray();
        // 创建 JSON 对象，表示数组中的第一个元素
        JSONObject messageObject = new JSONObject();
        messageObject.set("role", "user");
        messageObject.set("content", message);
        messagesArray.set(messageObject);
        jsonObject.set("messages", messagesArray);
        String jsonObjectString = jsonObject.toString();

        String access_token = "24.31a61e759ce9cdefbab8b6e08bb5fc8c.2592000.1710760834.282335-51804297";
        String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + access_token;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        String jsonStr = JSONUtil.toJsonStr(jsonObjectString);

        String result = HttpRequest.post(url).addHeaders(headerMap).body(jsonStr).execute().body();
        JSON json = JSONUtil.parse(result);
        JSONObject jsonObject1 = new JSONObject(json);
        Object result1 = jsonObject1.get("result");
        String string = result1.toString();
        System.err.println(string);
        return string;
    }


}
