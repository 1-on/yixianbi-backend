package com.yixian.yixianbi;

import com.yixian.aigenerate.client.AiGenerateClient;
import com.yixian.aigenerate.common.Result;
import com.yixian.aigenerate.model.AiGenerateRequest;
import com.yixian.aigenerate.model.AiGenerateResponse;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiGenerateTest {

    @Test
    public void AiGenerateTest1() {
        String accessToken = "24.31a61e759ce9cdefbab8b6e08bb5fc8c.2592000.1710760834.282335-51804297";
        AiGenerateClient client = new AiGenerateClient(accessToken);
        AiGenerateRequest request = new AiGenerateRequest();
        request.setMessage("你好");
        Result<AiGenerateResponse> aiGenerateResponseResult = client.doChat(request);
        System.out.println(aiGenerateResponseResult);
        AiGenerateResponse data = aiGenerateResponseResult.getData();
        System.out.println(data.getContent());
    }

}
