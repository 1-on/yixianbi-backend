package com.yixian.yixianbi.model.dto.chart;

import com.baomidou.mybatisplus.annotation.*;
import com.yixian.yixianbi.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询图表请求
 *
 * @TableName chart
 */
@TableName(value = "chart")
@Data
public class ChartQueryDTO extends PageRequest implements Serializable {

    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 创建用户 id
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}