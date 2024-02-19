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
import com.yixian.yixianbi.resolver.AiResolver;
import com.yixian.yixianbi.resolver.RateLimiterResolver;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiResolver aiResolver;

    @Resource
    private RateLimiterResolver rateLimiterResolver;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

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

//        BiResponse biResponse = chartService.genChartByAi(multipartFile, genChartByAiDTO);
        BiResponse biResponse = chartService.genChartByAiAsync(multipartFile, genChartByAiDTO);

        return Result.success(biResponse);

    }
}
