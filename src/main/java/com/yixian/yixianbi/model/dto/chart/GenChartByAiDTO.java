package com.yixian.yixianbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAiDTO implements Serializable {


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

    private static final long serialVersionUID = -8257968257667064319L;
}
