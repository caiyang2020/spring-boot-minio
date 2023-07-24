# Framework构成

### 基础模块
* 结果分析模块

### 启动器
* minio-spring-boot-starter(当前版本apple)
> 由于springboot默认集成的okhttp3版本过低，所以我们在pom文件中手动指定版本<br>
> <br>
> ___<okhttp3.version>4.10.0</okhttp3.version>___<br>
> <br>
> 在yaml文件中配置minio<br>
> ___minio:<br>
> &emsp;endpoint: http://域名:端口<br>
> &emsp;accessKey: 账号<br>
> &emsp;secretKey: 密码<br>
> &emsp;connectTimeout：连接超时时间<br>
> &emsp;writeTimeout: 写超时时间<br>
> &emsp;readTimeout: 读超时时间<br>
> &emsp;bucketPublic: 创建bucket时，是否设置为可读写<br>___
> <br>
> 使用时只需注入MinioTemplate实例即可<br>
> ___@Autowired<br>
> private MinioTemplate minioTemplate;___

### 作者
Sensetime Framework开发小组