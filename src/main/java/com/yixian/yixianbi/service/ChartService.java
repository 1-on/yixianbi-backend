package com.yixian.yixianbi.service;

import com.yixian.yixianbi.model.dto.chart.GenChartByAiDTO;
import com.yixian.yixianbi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixian.yixianbi.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

/**
* @author jiangfei
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-02-15 16:31:00
*/
public interface ChartService extends IService<Chart> {

    BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiDTO genChartByAiDTO);

    BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiDTO genChartByAiDTO);
}
