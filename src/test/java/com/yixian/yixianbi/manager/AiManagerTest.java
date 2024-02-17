package com.yixian.yixianbi.manager;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;
    @Test
    void doChatWithWXYY() {
        String result = "{\"id\":\"as-ffucfe69n8\",\"object\":\"chat.completion\",\"created\":1708171336,\"result\":\"您好，有什么我可以帮到您？\",\"is_truncated\":false,\"need_clear_history\":false,\"finish_reason\":\"normal\",\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":6,\"total_tokens\":7}}";
        JSON parse = JSONUtil.parse(result);
        System.out.println(parse);
        JSONObject jsonObject = new JSONObject(result);
        System.out.println(jsonObject.get("result"));
//        String message = "你好";
//
//        String result = aiManager.doChatWithWXYY(message);
//        System.out.println(result);
    }
}