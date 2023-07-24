package org.sensetimeframework.minio.service;

import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Template {
    /**
     * 获取全部bucket
     *
     * @return 所有桶信息
     */
    List<Bucket> getAllBuckets();

    /**
     * 判断桶是否存在
     *
     * @param bucketName bucket名称
     * @return true存在，false不存在
     */
    Boolean bucketExists(String bucketName);

    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     * @return 单个桶信息
     */
    Optional<Bucket> getBucket(String bucketName);

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    void createBucket(String bucketName);

    /**
     * 设置bucket的策略
     *
     * @param bucketName bucket名称
     * @param policyJson policyJson配置
     */
    void setBucketPolicy(String bucketName, String policyJson);

    /**
     * 根据bucketName删除信息(不保留bucket)
     *
     * @param bucketName bucket名称
     */
    void removeBucket(String bucketName);

    /**
     * 根据bucketName清空bucket内容(保留bucket)
     *
     * @param bucketName bucket名称
     */
    void clearBucket(String bucketName);

    /**
     * 根据bucketName和过期时间清理bucket内容
     *
     * @param bucketName bucket名称
     * @param critical 临界值
     * @param timeUnit 时间的单位
     * @param consumer 进度和被删除对象的回调
     */
    void removeBucketObjectsIfExpired(String bucketName, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer);

    /**
     * 根据bucketName、路径和过期时间清理bucket内容
     *
     * @param bucketName bucket名称
     * @param minioPath 文件夹名称
     * @param critical 临界值
     * @param timeUnit 时间的单位
     * @param consumer 进度和被删除对象的回调
     */
    void removePathObjectsIfExpired(String bucketName, String minioPath, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer);

    /**
     * 根据对象列表、bucketName、过期时间清理bucket内容
     *
     * @param results 需要检查的对象列表
     * @param bucketName bucket名称
     * @param critical 临界值
     * @param timeUnit 时间的单位
     * @param consumer 进度和被删除对象的回调
     */
    void removeObjectsFromListIfExpired(Iterable<Result<Item>> results, String bucketName, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer);

    /**
     * 上传MultipartFile文件到指定文件桶中
     *
     * @param bucketName bucket名称
     * @param file 文件
     * @return 文件对应的URL
     */
    String putObject(String bucketName, MultipartFile file);

    /**
     * 上传MultipartFile文件到指定文件桶下的指定路径(FullName)
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     * @return 文件对应的URL
     */
    String putObject(String bucketName, String objectName, MultipartFile file);

    /**
     * 上传流到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param inputStream 文件流
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: <a href="https://www.runoob.com/http/http-content-type.html">...</a>
     * @return 文件对应的URL
     */
    String putObject(String bucketName, String objectName, InputStream inputStream, String contentType);

    /**
     * 上传流到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param bytes       字节
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: <a href="https://www.runoob.com/http/http-content-type.html">...</a>
     * @return 文件对应的URL
     */
    String putObject(String bucketName, String objectName, byte[] bytes, String contentType);

    /**
     * 上传File文件
     *
     * @param bucketName  文件桶
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: <a href="https://www.runoob.com/http/http-content-type.html">...</a>
     * @return 文件对应的URL
     */
    String putObject(String bucketName, String objectName, File file, String contentType);

    /**
     * 上传File文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param fileName   本地文件名
     */
    void uploadObject(String bucketName, String objectName, String fileName);

    /**
     * 上传Folder文件夹
     *
     * @param bucketName 桶名称
     * @param minioPath minio路径
     * @param folderName  本地文件夹
     * @param consumer 进度的回调
     */
    void uploadFolder(String bucketName, String minioPath, String folderName, Consumer<Integer> consumer);

    /**
     * 判断文件是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return true存在, 反之不存在
     */
    Boolean checkFileIsExist(String bucketName, String objectName);

    /**
     * 判断文件夹是否存在
     *
     * @param bucketName 桶名称
     * @param folderName 文件夹名称
     * @return true存在, 反之
     */
    Boolean checkFolderIsExist(String bucketName, String folderName);

    /**
     * 根据文件桶和文件全路径获取文件流
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return 文件流
     */
    InputStream getObject(String bucketName, String objectName);

    /**
     * 根据文件桶和文件全路径下载文件到本地
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param fileName 本地文件名
     * @param overwrite 是否覆盖
     */
    void downloadObject(String bucketName, String objectName, String fileName, Boolean overwrite);

    /**
     * 根据文件桶和文件夹全路径下载文件夹内容到本地指定文件夹
     *
     * @param bucketName 桶名称
     * @param minioPath minio文件夹名
     * @param folderName 本地文件夹名
     * @param overwrite 是否覆盖
     * @param consumer 进度的回调
     */
    void downloadFolder(String bucketName, String minioPath, String folderName, Boolean overwrite, Consumer<Integer> consumer);

    /**
     * 根据文件桶和文件夹全路径下载文件夹放到本地指定路径
     *
     * @param bucketName 桶名称
     * @param minioPath minio文件夹名
     * @param directory 本地指定路径
     * @param overwrite 是否覆盖
     * @param consumer 进度的回调
     */
    void downloadFolderToDirectory(String bucketName, String minioPath, String directory, Boolean overwrite, Consumer<Integer> consumer);

    /**
     * 根据文件桶和文件夹全路径返回压缩后zip文件流
     *
     * @param bucketName 桶名称
     * @param minioPath minio文件夹名
     * @param progressConsumer 进度的回调
     * @param stepConsumer 阶段的回调
     * @return 文件流
     */
    InputStream downloadFolderByZip(String bucketName, String minioPath, Consumer<Integer> progressConsumer, Consumer<String> stepConsumer);

    /**
     * 指定一个GET请求，返回获取文件对象的URL，设置URL过期时间
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param expiry 过期时间
     * @param timeUnit 时间单位
     * @return 文件链接
     */
    String getPresignedObjectUrl(String bucketName, String objectName, Integer expiry, TimeUnit timeUnit);

    /**
     * 根据url获取文件流
     *
     * @param url 文件对于URL
     * @return 文件流
     */
    InputStream getObjectByUrl(String url);

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     */
    void removeObject(String bucketName, String objectName);

    /**
     * 删除文件夹
     *
     * @param bucketName bucket名称
     * @param folderName 文件夹名称
     */
    void removeObjects(String bucketName, String folderName);

    /**
     * 查看指定桶下文件对象列表
     *
     * @param bucketName bucket名称
     * @return 文件对象信息列表
     */
    Iterable<Result<Item>> listObjects(String bucketName);

    /**
     * 查看指定桶下文件对象列表
     *
     * @param bucketName bucket名称
     * @param recursive  是否递归查询
     * @return 文件对象信息列表
     */
    Iterable<Result<Item>> listObjects(String bucketName, Boolean recursive);

    /**
     * 查看指定桶指定路径下文件对象列表
     *
     * @param bucketName bucket名称
     * @param prefix     前缀匹配，如果是文件夹 结尾必须有 /
     * @param recursive  是否递归查询
     * @return 文件对象信息列表
     */
    Iterable<Result<Item>> listObjects(String bucketName, String prefix, Boolean recursive);
}
