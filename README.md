##### 软件介绍：
使用springboot开发的小型web应用，打包后可直接运行。话不多说，先看效果图：
1. 首页：一键收藏功能方便你快速收集你想要的网址，添加分类功能让你轻松地对收藏网址进行分类管理
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_632e2ad8_5420333.png)
2. 导入/导出：导入书签和导出书签，让你十分方便地管理和备份书签
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_63a24100_5420333.png)
3. 登录：简洁的登录界面并配置自动登录功能，不用每次访问都输入密码。
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_e54f7fa8_5420333.png)
##### 安装教程：
1. 进入项目目录，使用maven命令进行打包：mvn clean package
2. 服务器终端执行命令：nohup java -jar web-favorites.jar > console.log &
##### docker启动：
1. docker pull wangxiaoyuan2020/web-favorites:latest
2. docker run -d -p 9020:9020 wangxiaoyuan2020/web-favorites