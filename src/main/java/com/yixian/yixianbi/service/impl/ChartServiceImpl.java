package com.yixian.yixianbi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.yixianbi.model.entity.Chart;
import com.yixian.yixianbi.service.ChartService;
import com.yixian.yixianbi.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author jiangfei
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-02-15 16:31:00
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




