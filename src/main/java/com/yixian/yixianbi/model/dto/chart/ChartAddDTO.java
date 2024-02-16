package com.yixian.yixianbi.model.dto.chart;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加图表请求
 *
 * @TableName chart
 */
@TableName(value = "chart")
@Data
public class ChartAddDTO implements Serializable {
    /**
     * 名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表信息
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}