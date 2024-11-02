下面是一个详细的 README.md 文档示例，旨在帮助首次接触该项目的用户轻松上手和配置项目。文档包含了项目功能、结构、安装和使用说明等内容。

markdown
Copy code
# HDFS 文件夹上传工具

## 项目概述

HDFS 文件夹上传工具是一个用于将本地文件夹及其子目录和文件上传到 Hadoop 分布式文件系统 (HDFS) 的 Java 应用程序。用户可以通过图形界面选择要上传的目录，并将其内容上传到 HDFS 的目标目录。

## 功能

- 选择本地文件夹并上传到 HDFS 指定目录
- 支持递归上传子目录及其内容
- 支持显示上传进度及上传结果
- 提示用户操作状态（如成功或错误消息）
- 目录树展示 HDFS 文件系统结构，便于用户选择目标路径

## 项目结构

HDFSUploadTool/ ├── src/ │ ├── HDFSUtils.java // HDFS 操作工具类 │ ├── MainFrame.java // 主界面类 │ ├── Application.java // 应用程序入口 │ └── ... // 其他类 ├── resources/ │ └── hdfs-site.xml // HDFS 配置文件 ├── lib/ // 依赖库 │ ├── hadoop-common.jar │ ├── hadoop-hdfs.jar │ └── ... // 其他依赖库 └── README.md // 项目文档

markdown
Copy code

## 环境要求

- Java Development Kit (JDK) 8 或以上版本
- Hadoop 环境（HDFS）配置正确
- Maven 或 Gradle（可选，用于构建项目）

## 安装步骤

1. **克隆或下载项目**
   ```bash
   git clone https://github.com/yourusername/HDFSUploadTool.git
   cd HDFSUploadTool
配置 HDFS 确保您已经正确配置了 HDFS，并在 resources/hdfs-site.xml 中设置了相关的 HDFS 连接参数。

添加依赖 如果使用 Maven，您可以在 pom.xml 中添加 HDFS 相关依赖：

xml
Copy code
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-common</artifactId>
    <version>3.3.0</version>
</dependency>
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-hdfs</artifactId>
    <version>3.3.0</version>
</dependency>
编译项目 使用命令行或 IDE 编译项目。

使用说明
运行应用程序 在终端中使用以下命令运行程序：

bash
Copy code
java -cp "lib/*:target/classes" Application
选择目录 在程序界面中，您将看到一个目录树。选择您想要上传的目标目录（右键点击可选择）。

上传文件夹

点击“上传文件夹”按钮，选择您要上传的本地目录（如 D:/text）。
点击“确定”开始上传，程序将递归上传所选目录及其所有内容。
查看上传状态 上传完成后，程序会提示上传结果（成功或失败），并更新目录树。

常见问题
1. 上传时出现权限问题
请确保您的 HDFS 配置了正确的权限，用户具有在目标目录下创建子目录和上传文件的权限。

2. 无法连接 HDFS
请检查 HDFS 服务是否正常运行，确保 hdfs-site.xml 中的配置正确。

3. 上传后文件丢失
确保在上传时使用了正确的目标路径，并且在上传代码中保持了当前目录。
