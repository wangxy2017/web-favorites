##### 软件介绍：
使用SpringBoot开发的小型web应用程序，打包后可直接运行。话不多说，先看效果图：
1. 首页：一键收藏让你快速地收藏你想要的网址，添加分类让你轻松地对收藏网址进行分类管理
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_632e2ad8_5420333.png)
2. 导入/导出：导入收藏和导出收藏，方便你在任何地方上传和备份
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_63a24100_5420333.png)
3. 登录：简洁的登录界面并带有自动登录，不用每次访问都输入密码
![在这里插入图片描述](https://images.gitee.com/uploads/images/2020/0526/165956_e54f7fa8_5420333.png)
##### 安装教程：
1. 进入项目根目录，使用maven命令进行打包：mvn clean package
2. 将打包好的web-favorites.jar文件上传至服务器
2. 服务器终端执行命令：nohup java -jar web-favorites.jar > /dev/null 2>&1 &
##### docker启动：
1. docker pull wangxiaoyuan2021/web-favorites:latest
2. docker run -d -p 8888:8888 -p 8889:8889 wangxiaoyuan2021/web-favorites
##### 访问地址：
1. `http://ip:8888/ `或`http://ip:8888/index.html `
##### QQ群：
- 972265056