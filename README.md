# One Library

> 一款基于 JavaFX、Spring Boot 的编辑器。

## 特色

- 支持拖拽文档排序
- 支持从网页生成`Markdown`文档
- 支持将文档存储在`GitHub`，可以多台设备互相同步
- 文本翻译

## 支持的文档类型

|    文档类型    |  读  |  写  |
|:----------:|:---:|:---:|
| `Markdown` | `√` | `√` |

## 打包`exe`可执行文件

该工程借助[`exe4j`](https://exe4j.apponic.com/)和[`Inno Setup`](https://jrsoftware.org/isinfo.php)完成`Windows`平台的打包

1. `Maven` 打包生成`application/target/One Library.jar`
    ```shell
    mvn clean install
    ```
2. 打开`exe4j`加载[`One Library.exe4j`](./One%20Library.exe4j)打包成可执行文件`application/target/One Library.exe`
3. 打开`Inno Setup`加载[`One Library.iss`](./One%20Library.iss)打包成可执行的安装包文件`application/target/One Library Setup.exe`


