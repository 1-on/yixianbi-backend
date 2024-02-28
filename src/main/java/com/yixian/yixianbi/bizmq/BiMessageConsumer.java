package com.yixian.yixianbi.bizmq;

import com.rabbitmq.client.Channel;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.exception.BaseException;
import com.yixian.yixianbi.model.entity.Chart;
import com.yixian.yixianbi.model.enums.ChartStatusEnum;
import com.yixian.yixianbi.resolver.AiResolver;
import com.yixian.yixianbi.service.ChartService;
import com.yixian.yixianbi.utils.ExcelUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private AiResolver aiResolver;

    @Resource
    private ChartService chartService;


    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BaseException(MessageConstant.SYSTEM_ERROR);
        }
        Long id = Long.parseLong(message);
        Chart chart = chartService.getById(id);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 修改图表状态为 "执行中"
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
        }
        // 拿到返回结果
        String result = aiResolver.doChatWithWXYY(buildUserInput(chart));
        // 对结果进行分割
        String[] split = result.split("【【【【【");
        if (split.length != 3) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        // 图表数据
        String genChart = split[1].trim();
        // 查找最后一个 "option" 的位置
        int lastIndex = genChart.lastIndexOf("option");
        if (lastIndex != -1) {
            genChart = genChart.substring(lastIndex + 9);
        }
        boolean validJson = chartService.isValidJson(genChart);
        if (!validJson) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "AI生成错误");
        }
        System.err.println(genChart);
        // 分析结论
        String genResult = split[2].trim();
        System.err.println(genResult);
        // 更新图表
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        // 更新图表状态为成功
        updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        channel.basicAck(deliveryTag, false);
    }

    public String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        // 预设
        final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{此处生成前端 Echarts V5 的 option 配置对象json代码,注意是json格式，属性名称用引号引起来，表格标题以对象形式给出,例如  title: { text: '标题' },除此之外不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
        /*
         * 用户的输入(参考)
            分析需求：
            分析网站用户的增长情况
            原始数据：
            日期,用户数
            1号,10
            2号,20
            3号,30
        * */
        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append(prompt).append("\n");
        userInput.append("分析需求：").append("\n");
        // 拼接目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        // 压缩后的数据
        userInput.append(csvData).append("\n");
        return String.valueOf(userInput);
    }

}
