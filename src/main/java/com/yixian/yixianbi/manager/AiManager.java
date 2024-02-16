package com.yixian.yixianbi.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiManager {
    String accessKey = "7rllkl16vdne5yiejz2y7g69h39yw4io";
    String secretKey = "q1u2kkrizw4dh7ck9jla4gxyhr1qjhck";
    YuCongMingClient client = new YuCongMingClient(accessKey, secretKey);

    public String doChat(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        System.out.println(response.getData());
        return response.getData().getContent();
    }

}
