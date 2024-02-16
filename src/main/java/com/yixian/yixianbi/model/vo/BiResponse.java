package com.yixian.yixianbi.model.vo;

import lombok.Data;

/**
 * BI返回的结果
 */
@Data
public class BiResponse {

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 图表ID
     */
    private Long chartId;
}
