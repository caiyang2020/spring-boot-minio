package org.sensetimeframework.minio.service.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.io.FileUtils;
import org.sensetimeframework.minio.config.MinioConfig;
import org.sensetimeframework.minio.messages.Progress;
import org.sensetimeframework.minio.property.MinioConfigProperties;
import org.sensetimeframework.minio.service.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

@Slf4j
@Import(MinioConfig.class)
@EnableConfigurationProperties(MinioConfigProperties.class)
public class MinioTemplate implements Template {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigProperties minioConfigProperties;

    private final static String SEPARATOR = "/";

    private static final String BUCKET_PARAM = "MyBucketName";

    private final static String READ_WRITE = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";

    private final static int DEFAULT_BUFFER_SIZE = 1024 * 8;

    private final Object lock = new Object();

    @Override
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new RuntimeException("获取全部存储桶失败!", e);
        }
    }

    @Override
    public Boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("检查桶是否存在失败!", e);
        }
    }

    @Override
    public Optional<Bucket> getBucket(String bucketName) {
        try {
            return minioClient.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
        } catch (Exception e) {
            throw new RuntimeException("根据存储桶名称获取信息失败!", e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            try {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                if (minioConfigProperties.isBucketPublic()) {
                    setBucketPolicy(bucketName, READ_WRITE.replace(BUCKET_PARAM,bucketName));
                }
            } catch (Exception e) {
                throw new RuntimeException("创建桶失败!", e);
            }
        }
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyJson) {
        try {
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
        } catch (Exception e) {
            throw new RuntimeException("修改策略失败!", e);
        }
    }

    @Override
    public void removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("根据存储桶名称删除桶失败!", e);
        }
    }

    @Override
    public void clearBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            try {
                Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build());
                for (Result<Item> result : results) {
                    removeObject(bucketName,result.get().objectName());
                }
            } catch (Exception e) {
                throw new RuntimeException("根据存储桶名称清理桶失败!", e);
            }
        }
    }

    //暂时只支持秒、分、时、天
    @Override
    public void removeBucketObjectsIfExpired(String bucketName, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> results = listObjects(bucketName, true);
            removeObjectsFromListIfExpired(results, bucketName, critical, timeUnit, consumer);
        }
    }

    //暂时只支持秒、分、时、天
    @Override
    public void removePathObjectsIfExpired(String bucketName, String minioPath, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> results = listObjects(bucketName, addSeparatorToEndIfNotExist(minioPath), true);
            removeObjectsFromListIfExpired(results, bucketName, critical, timeUnit, consumer);
        }
    }

    //暂时只支持秒、分、时、天
    @Override
    public void removeObjectsFromListIfExpired(Iterable < Result < Item >> results, String bucketName, long critical, TimeUnit timeUnit, BiConsumer<Integer, String> consumer) {
        Progress progress = new Progress();
        Progress finishedCount = new Progress();
        try {
            int objectCount = (int) StreamSupport.stream(results.spliterator(), false).count();
            String removingObject;
            for (Result<Item> result : results) {
                Duration between = Duration.between(result.get().lastModified(), ZonedDateTime.now());
                long interval = getInterval(result.get().lastModified(), ZonedDateTime.now(), timeUnit);
                boolean satisfied = interval > critical;
                if (satisfied) {
                    removingObject = result.get().objectName();
                    removeObject(bucketName, removingObject);
                } else {
                    removingObject = null;
                }
                finishedCount.increase();
                provideProgressAndObjectWhenChanged(progress, finishedCount, objectCount, satisfied, removingObject, consumer);
            }
        } catch (Exception e) {
            throw new RuntimeException("根据存储桶名称、路径名称和过期时间清理桶失败!", e);
        }
    }

    @Override
    public String putObject(String bucketName, MultipartFile file) {
        createBucket(bucketName);
        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        putMultipartFile(bucketName, fileName, file);
        return minioConfigProperties.getEndpoint() +
                SEPARATOR +
                bucketName +
                SEPARATOR +
                fileName;
    }

    @Override
    public String putObject(String bucketName, String objectName, MultipartFile file) {
        createBucket(bucketName);
        putMultipartFile(bucketName, objectName, file);
        return minioConfigProperties.getEndpoint() +
                SEPARATOR +
                bucketName +
                SEPARATOR +
                objectName;
    }

    @Override
    public String putObject(String bucketName, String objectName, InputStream inputStream, String contentType) {
        createBucket(bucketName);
        putInputStream(bucketName, objectName, inputStream, contentType);
        return minioConfigProperties.getEndpoint() +
                SEPARATOR +
                bucketName +
                SEPARATOR +
                objectName;
    }

    @Override
    public String putObject(String bucketName, String objectName, byte[] bytes, String contentType) {
        createBucket(bucketName);
        putBytes(bucketName, objectName, bytes, contentType);
        return minioConfigProperties.getEndpoint() +
                SEPARATOR +
                bucketName +
                SEPARATOR +
                objectName;
    }

    @Override
    public String putObject(String bucketName, String objectName, File file, String contentType) {
        createBucket(bucketName);
        putFile(bucketName, objectName, file, contentType);
        return minioConfigProperties.getEndpoint() +
                SEPARATOR +
                bucketName +
                SEPARATOR +
                objectName;
    }

    @Override
    public void uploadObject(String bucketName, String objectName, String fileName) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("根据文件名获取流失败!", e);
        }
    }

    @Override
    public void uploadFolder(String bucketName, String minioPath, String folderName, Consumer<Integer> consumer) {
        try {
            InheritableThreadLocal<Progress> itlProgress = new InheritableThreadLocal<>();
            InheritableThreadLocal<Progress> itlFinishedCount = new InheritableThreadLocal<>();
            itlProgress.set(new Progress());
            itlFinishedCount.set(new Progress());

            if (!StringUtils.endsWithIgnoreCase(folderName, File.separator)) {
                folderName = folderName + File.separator;
            }
            int folderNameLength = folderName.length();
            File folder = new File(folderName);
            Collection<File> files = FileUtils.listFiles(folder, null, true);
            int objectCount = files.size();

            ExecutorService executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            CountDownLatch countDownLatch = new CountDownLatch(objectCount);

            for (File file : files) {
                String relativePath = file.getAbsolutePath().substring(folderNameLength).replace(File.separator, SEPARATOR);
                String objectName = addSeparatorToEndIfNotExist(minioPath) + relativePath;
                executor.execute(() -> {
                    uploadObject(bucketName, objectName, file.getAbsolutePath());
                    synchronized (lock) {
                        itlFinishedCount.get().increase();
                        provideProgressWhenChanged(itlProgress.get(), itlFinishedCount.get(), objectCount, consumer);
                    }
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException("上传文件夹失败!", e);
        }
    }

    @Override
    public Boolean checkFileIsExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build()
            );
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    @Override
    public Boolean checkFolderIsExist(String bucketName, String folderName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs
                            .builder()
                            .bucket(bucketName)
                            .prefix(folderName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && folderName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    // InputStream使用后必须关闭以释放网络资源
    @Override
    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient
                    .getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException("根据文件名获取流失败!", e);
        }
    }

    @Override
    public void downloadObject(String bucketName, String objectName, String fileName, Boolean overwrite) {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(fileName)
                            .overwrite(overwrite)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败!", e);
        }
    }

    @Override
    public void downloadFolder(String bucketName, String minioPath, String folderName, Boolean overwrite, Consumer<Integer> consumer) {
        Progress progress = new Progress();
        Progress finishedCount = new Progress();
        Iterable<Result<Item>> results = listObjects(bucketName, minioPath, true);
        int objectCount = (int) StreamSupport.stream(results.spliterator(), false).count();
        String prefix = addSeparatorToEndIfNotExist(minioPath);
        try {
            for (Result<Item> item : results) {
                String itemName = item.get().objectName();
                String fileName = folderName + File.separator + itemName.substring(prefix.length()).replace(SEPARATOR, File.separator);
                FileUtils.createParentDirectories(new File(fileName));
                downloadObject(bucketName, itemName, fileName, overwrite);
                finishedCount.increase();
                provideProgressWhenChanged(progress, finishedCount, objectCount, consumer);
            }
        } catch (Exception e) {
            throw new RuntimeException("下载文件夹失败!", e);
        }
    }

    @Override
    public void downloadFolderToDirectory(String bucketName, String minioPath, String directory, Boolean overwrite, Consumer<Integer> consumer) {
        String standardObjectName = removeSeparatorFromEndIfExist(minioPath);
        int lastSeparatorIndex = standardObjectName.lastIndexOf(SEPARATOR);
        String folderName = directory + File.separator + (lastSeparatorIndex == -1? standardObjectName : standardObjectName.substring(lastSeparatorIndex + 1));
        downloadFolder(bucketName, minioPath, folderName ,overwrite, consumer);
    }

    // InputStream使用后必须关闭以释放网络资源
    @Override
    public InputStream downloadFolderByZip(String bucketName, String minioPath, Consumer<Integer> progressConsumer, Consumer<String> stepConsumer) {
        Progress progress = new Progress();
        Progress finishedCount = new Progress();
        String objectNameWithoutSeparator = removeSeparatorFromEndIfExist(minioPath);
        int lastSeparatorIndex = objectNameWithoutSeparator.lastIndexOf(SEPARATOR);
        String baseDir = lastSeparatorIndex == -1? objectNameWithoutSeparator : objectNameWithoutSeparator.substring(lastSeparatorIndex + 1);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Iterable<Result<Item>> results = listObjects(bucketName, addSeparatorToEndIfNotExist(minioPath), true);
            InheritableThreadLocal<Map<String, InputStream>> itl = new InheritableThreadLocal<>();
            itl.set(new ConcurrentHashMap<>());

            ExecutorService executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            int objectCount = (int) StreamSupport.stream(results.spliterator(), false).count();
            CountDownLatch countDownLatch = new CountDownLatch(objectCount);

            stepConsumer.accept("正在读取对象！");
            for (Result<Item> result : results) {
                executor.execute(() -> {
                    try {
                        Item item = result.get();
                        InputStream is = getObject(bucketName, item.objectName());
                        String entry = (baseDir + item.objectName().substring(objectNameWithoutSeparator.length())).replace(SEPARATOR, File.separator);
                        itl.get().put(entry, is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            ParallelScatterZipCreator parallelScatterZipCreator = new ParallelScatterZipCreator(executor);
            stepConsumer.accept("正在压缩文件！");

            for (String key : itl.get().keySet()) {
                final InputStreamSupplier inputStreamSupplier = () -> itl.get().get(key);
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(key);
                zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
                zipArchiveEntry.setSize(itl.get().get(key).available());
                zipArchiveEntry.setUnixMode(UnixStat.FILE_FLAG | 436);
                parallelScatterZipCreator.addArchiveEntry(zipArchiveEntry, inputStreamSupplier);
                finishedCount.increase();
                provideProgressWhenChanged(progress, finishedCount, objectCount, progressConsumer);
            }
            ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(baos);
            parallelScatterZipCreator.writeTo(zipArchiveOutputStream);

            zipArchiveOutputStream.close();
            executor.shutdown();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("压缩文件失败!", e);
        }
    }

    @Override
    public String getPresignedObjectUrl(String bucketName, String objectName, Integer expiry, TimeUnit timeUnit) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry, timeUnit)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("获取文件对象URL失败!", e);
        }
    }

    // InputStream使用后必须关闭以释放网络资源
    @Override
    public InputStream getObjectByUrl(String url) {
        try {
            return new URL(url).openStream();
        } catch (IOException e) {
            throw new RuntimeException("根据URL获取流失败!", e);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败!", e);
        }
    }

    @Override
    public void removeObjects(String bucketName, String folderName) {
        try {
            Iterable<Result<Item>> results = listObjects(bucketName, folderName, false);
            for (Result<Item> result : results){
                Item item = result.get();
                if (item.isDir()) {
                    removeObjects(bucketName, item.objectName());
                } else {
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(item.objectName()).build());
                }
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(folderName).build());
            }
        }catch (Exception e){
            throw new RuntimeException("删除文件夹失败!", e);
        }
    }

    @Override
    public Iterable<Result<Item>> listObjects(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    @Override
    public Iterable<Result<Item>> listObjects(String bucketName, Boolean recursive) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).recursive(recursive).build());
    }

    @Override
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, Boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(recursive)
                        .build());
    }

    /**
     * 上传MultipartFile通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param file       文件
     */
    private void putMultipartFile(String bucketName, String objectName, MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException("文件流获取错误", e);
        } catch (Exception e) {
            throw new RuntimeException("文件流上传错误", e);
        }
    }

    /**
     * 上传InputStream通用方法
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param inputStream 文件流
     * @param contentType 内容类型
     */
    private void putInputStream(String bucketName, String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件流上传错误", e);
        }
    }

    /**
     * 上传 bytes 通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param bytes      字节编码
     * @param contentType 内容类型
     */
    private void putBytes(String bucketName, String objectName, byte[] bytes, String contentType) {
        // 字节转文件流
        InputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件流上传错误", e);
        }
    }

    /**
     * 上传 file 通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param file       文件
     * @param contentType 内容类型
     */
    private void putFile(String bucketName, String objectName, File file, String contentType) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileInputStream, fileInputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件上传错误", e);
        }
    }

    private String addSeparatorToEndIfNotExist(String sourceString) {
        return sourceString.endsWith(SEPARATOR)? sourceString : sourceString + SEPARATOR;
    }

    private String removeSeparatorFromEndIfExist(String sourceString) {
        return sourceString.endsWith(SEPARATOR)? sourceString.substring(0, sourceString.length() - 1) : sourceString;
    }

    private long getInterval(ZonedDateTime startTime, ZonedDateTime finishTime, TimeUnit timeUnit) throws Exception {
        Duration between = Duration.between(startTime, finishTime);
        long interval;
        switch (timeUnit) {
            case SECONDS:
                interval = between.toSeconds();
                break;
            case MINUTES:
                interval = between.toMinutes();
                break;
            case HOURS:
                interval = between.toHours();
                break;
            case DAYS:
                interval = between.toDays();
                break;
            default:
                throw new Exception("暂时不支持秒、分、时、天以外的单位");
        }
        return interval;
    }

    private void provideProgressWhenChanged(Progress progress, Progress finishedCount, Integer total, Consumer<Integer> consumer) {
        int newProgress = finishedCount.getProcessed() * 100 / total;
        if (newProgress > progress.getProcessed()) {
            consumer.accept(newProgress);
            progress.setProcessed(newProgress);
        }
    }

    private void provideProgressAndObjectWhenChanged(Progress progress, Progress finishedCount, Integer total, Boolean satisfied, String currentObjectName, BiConsumer<Integer, String> consumer) {
        int newProgress = finishedCount.getProcessed() * 100 / total;
        boolean increased = newProgress > progress.getProcessed();
        if (increased || satisfied) {
            if (increased) {
                progress.setProcessed(newProgress);
            }
            consumer.accept(newProgress, currentObjectName);
        }
    }
}
