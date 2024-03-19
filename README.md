# 智能 BI 平台DEMO（AIGC应用）

（此仓库为后端代码）[前端仓库](https://github.com/1-on/yixianbi-frontend)
[toc]

> 作者：[一弦](https://github.com/1-on)

### 项目介绍

基于React + SpringBoot + MQ + AIGC 的 智能数据分析平台。
相比于传统 BI，用户只需要导入原始数据集，输入分析目标，就能够自动生成可视化图标以及结论。
<br>
由于AI技术的限制，现在还不能做到那么"智能"，图标及结论仅供参考！

### 技术栈

#### 后端

- springboot 3.2.2
- MySQL 数据库
- MyBatis-Plus 以及 MyBatis X 自动生成
- RateLimiter 限流
- RabbitMQ 消息队列
- AiGenerate SDK (自己开发的调用AI的简易SDK,[地址](https://mvnrepository.com/artifact/cn.yixianweb/AiGenerate))
- JDK 线程池以及异步化
- Easy Excel 处理表格数据
- Swagger + Knife4j 接口文档生成

#### 前端

- React 18
- Ant Design Pro 6.x 脚手架
- Ant Design & ProComponents 组件库
- Echarts 可视化库
- OpenAPI 前端代码生成

### 项目截图

![image](https://github.com/1-on/yixianbi-backend/blob/master/doc/imgs/img.png)
![image](https://github.com/1-on/yixianbi-backend/blob/master/doc/imgs/img_1.png)
![image](https://github.com/1-on/yixianbi-backend/blob/master/doc/imgs/img_2.png)
![image](https://github.com/1-on/yixianbi-backend/blob/master/doc/imgs/img_3.png)
![image](https://github.com/1-on/yixianbi-backend/blob/master/doc/imgs/img_4.png)
