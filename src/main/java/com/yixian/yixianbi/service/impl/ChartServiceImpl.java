package com.yixian.yixianbi.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yixian.yixianbi.common.Result;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.context.BaseContext;
import com.yixian.yixianbi.exception.BaseException;
import com.yixian.yixianbi.model.dto.chart.GenChartByAiDTO;
import com.yixian.yixianbi.model.entity.Chart;
import com.yixian.yixianbi.model.enums.ChartStatusEnum;
import com.yixian.yixianbi.model.vo.BiResponse;
import com.yixian.yixianbi.resolver.AiResolver;
import com.yixian.yixianbi.resolver.RateLimiterResolver;
import com.yixian.yixianbi.service.ChartService;
import com.yixian.yixianbi.mapper.ChartMapper;
import com.yixian.yixianbi.utils.ExcelUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jiangfei
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2024-02-15 16:31:00
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private AiResolver aiResolver;

    @Resource
    private RateLimiterResolver rateLimiterResolver;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 生成图表信息 （同步）
     *
     * @param multipartFile
     * @param genChartByAiDTO
     * @return
     */
    @Override
    public BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiDTO genChartByAiDTO) {

        String name = genChartByAiDTO.getName();
        String goal = genChartByAiDTO.getGoal();
        String chartType = genChartByAiDTO.getChartType();

        // 文件校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024;
        if (size > ONE_MB) {
            throw new BaseException(MessageConstant.FILE_TOO_LARGE);
        }
        // 判断后缀是否符合
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        if (!validFileSuffixList.contains(suffix)) {
            throw new BaseException(MessageConstant.INVALID_FILE);
        }
        // 用户id
        Long userId = BaseContext.getCurrentId();
        // 限流
        rateLimiterResolver.doRateLimiter("genChartByAi_" + userId, 1);

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
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 拿到返回结果
        String result = aiResolver.doChatWithWXYY(userInput.toString());

        // 对结果进行分割
        String[] split = result.split("【【【【【");
        if (split.length != 3) {
            throw new BaseException(MessageConstant.AI_GEN_ERROR);
        }
        // 图表数据
        String genChart = split[1].trim();
        // 查找最后一个 "option" 的位置
        int lastIndex = genChart.lastIndexOf("option");
        if (lastIndex != -1) {
            genChart = genChart.substring(lastIndex + 9);
        }
        System.err.println(genChart);

        // 分析结论
        String genResult = split[2].trim();
        System.err.println(genResult);

        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(userId);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        boolean save = this.save(chart);
        if (!save) {
            throw new BaseException(MessageConstant.SAVE_ERROR);
        }
        // 返回数据给前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    /**
     * 生成图表信息 （异步）
     *
     * @param multipartFile
     * @param genChartByAiDTO
     * @return
     */
    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiDTO genChartByAiDTO) {
        String name = genChartByAiDTO.getName();
        String goal = genChartByAiDTO.getGoal();
        String chartType = genChartByAiDTO.getChartType();

        // 文件校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024;
        if (size > ONE_MB) {
            throw new BaseException(MessageConstant.FILE_TOO_LARGE);
        }
        // 判断后缀是否符合
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        if (!validFileSuffixList.contains(suffix)) {
            throw new BaseException(MessageConstant.INVALID_FILE);
        }
        // 用户id
        Long userId = BaseContext.getCurrentId();
        // 限流
        rateLimiterResolver.doRateLimiter("genChartByAi_" + userId, 1);

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
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(userId);
        // 设置任务状态为排队
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        boolean save = this.save(chart);
        if (!save) {
            throw new BaseException(MessageConstant.SAVE_ERROR);
        }

        CompletableFuture.runAsync(() -> {
            // 修改图表状态为 "执行中"
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            boolean b = this.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            }
            // 拿到返回结果
            String result = aiResolver.doChatWithWXYY(userInput.toString());
            // 对结果进行分割
            String[] split = result.split("【【【【【");
            if (split.length != 3) {
                handleChartUpdateError(chart.getId(), "AI生成错误");
                return;
            }
            // 图表数据
            String genChart = split[1].trim();
            // 查找最后一个 "option" 的位置
            int lastIndex = genChart.lastIndexOf("option");
            if (lastIndex != -1) {
                genChart = genChart.substring(lastIndex + 9);
            }
            boolean validJson = isValidJson(genChart);
            if (!validJson) {
                handleChartUpdateError(chart.getId(), "AI生成错误");
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
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);

        // 返回数据给前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    public static boolean isValidJson(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        } catch (Exception e) {
            // 捕获其他异常，如 IOException
            return false;
        }
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean b = this.updateById(updateChartResult);
        if (!b) {
            log.error("更新图表状态为失败操作执行失败" + chartId + "," + execMessage);
        }
    }

}




