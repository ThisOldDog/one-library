# One Library

一款基于 JavaFX、Spring Boot 的编辑器。

## 打包`exe`可执行文件

该工程借助[`exe4j`](https://exe4j.apponic.com/)和[`Inno Setup`](https://jrsoftware.org/isinfo.php)完成`Windows`平台的打包

1. `Maven` 打包生成`application/target/One Library.jar`
    ```shell
    mvn clean install
    ```
2. 打开`exe4j`加载[`One Library.exe4j`](./One%2FLibrary.exe4j)打包成可执行文件`application/target/One Library.exe`
3. 打开`Inno Setup`加载[`One Library.iss`](./One%2FLibrary.iss)打包成可执行的安装包文件`application/target/One Library Setup.exe`


