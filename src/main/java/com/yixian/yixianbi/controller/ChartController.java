package com.yixian.yixianbi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yixian.yixianbi.common.DeleteRequest;
import com.yixian.yixianbi.common.Result;
import com.yixian.yixianbi.constant.CommonConstant;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.context.BaseContext;
import com.yixian.yixianbi.exception.BaseException;
import com.yixian.yixianbi.manager.AiManager;
import com.yixian.yixianbi.manager.RateLimiterManager;
import com.yixian.yixianbi.model.dto.chart.*;
import com.yixian.yixianbi.model.entity.Chart;
import com.yixian.yixianbi.model.entity.User;
import com.yixian.yixianbi.model.vo.BiResponse;
import com.yixian.yixianbi.service.ChartService;
import com.yixian.yixianbi.service.UserService;
import com.yixian.yixianbi.utils.ExcelUtils;
import com.yixian.yixianbi.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RateLimiterManager rateLimiterManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Result<Long> addChart(@RequestBody ChartAddDTO chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser();
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        if (!result) {
            throw new BaseException(MessageConstant.OPERATION_ERROR);
        }
        long newChartId = chart.getId();
        return Result.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        User user = userService.getLoginUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        if (oldChart == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin()) {
            throw new BaseException(MessageConstant.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return Result.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> updateChart(@RequestBody ChartUpdateDTO chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        if (oldChart == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return Result.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        return Result.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryDTO
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public Result<Page<Chart>> listChartByPage(@RequestBody ChartQueryDTO chartQueryDTO,
                                               HttpServletRequest request) {
        long current = chartQueryDTO.getCurrent();
        long size = chartQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryDTO));
        return Result.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryDTO
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public Result<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryDTO chartQueryDTO,
                                                 HttpServletRequest request) {
        if (chartQueryDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        chartQueryDTO.setUserId(loginUser.getId());
        long current = chartQueryDTO.getCurrent();
        long size = chartQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryDTO));
        return Result.success(chartPage);
    }


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public Result<Boolean> editChart(@RequestBody ChartEditDTO chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser();
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        if (oldChart == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BaseException(MessageConstant.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return Result.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryDTO
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryDTO chartQueryDTO) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryDTO == null) {
            return queryWrapper;
        }
        Long id = chartQueryDTO.getId();
        String name = chartQueryDTO.getName();
        String goal = chartQueryDTO.getGoal();
        String chartType = chartQueryDTO.getChartType();
        Long userId = chartQueryDTO.getUserId();
        int current = chartQueryDTO.getCurrent();
        int pageSize = chartQueryDTO.getPageSize();
        String sortField = chartQueryDTO.getSortField();
        String sortOrder = chartQueryDTO.getSortOrder();
        // 拼接查询条件

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param genChartByAiDTO
     * @return
     */
    @PostMapping("/gen")
    public Result<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                           GenChartByAiDTO genChartByAiDTO) {
        String name = genChartByAiDTO.getName();
        String goal = genChartByAiDTO.getGoal();
        String chartType = genChartByAiDTO.getChartType();
        // 校验
        if (StringUtils.isBlank(goal) || (StringUtils.isNotBlank(name) && name.length() > 100)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }

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
        final List<String> validFileSuffixList = Arrays.asList("png", "jpg", "svg", "webp", "jpeg");
        if (!validFileSuffixList.contains(suffix)) {
            throw new BaseException(MessageConstant.INVALID_FILE);
        }
        // 用户id
        Long userId = BaseContext.getCurrentId();
        // 限流
        rateLimiterManager.doRateLimiter("genChartByAi_" + userId, 1);

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
        String result = aiManager.doChatWithWXYY(userInput.toString());

        // 对结果进行分割
        String[] split = result.split("【【【【【");
        if (split.length != 3) {
            throw new BaseException(MessageConstant.AI_GEN_ERROR);
        }
        // 图表数据
        String genChart = split[1].trim();
//        if(genChart.length()>=4){
//            genChart = genChart.substring(2, genChart.length() - 2);
//        }
        // 查找最后一个 "option" 的位置
        int lastIndex = genChart.lastIndexOf("option");
        if (lastIndex != -1) {
            genChart = genChart.substring(lastIndex + 9);
        }
        System.err.println(genChart);

//        // 找到前导字符串的结束位置
//        int startIndex = genJsonChart.indexOf("```js") + "```js".length();
//        // 找到尾部字符串的起始位置
//        int endIndex = genJsonChart.lastIndexOf("```");
//        String genChart = genJsonChart.substring(startIndex, endIndex);

        // 分析结论
        String genResult = split[2].trim();
        System.err.println(genResult);
        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(userId);
        chartService.save(chart);
        // 返回数据给前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return Result.success(biResponse);

    }
}
