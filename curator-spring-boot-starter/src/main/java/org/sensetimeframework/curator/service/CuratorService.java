package org.sensetimeframework.curator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.sensetimeframework.curator.config.CuratorConfig;
import org.sensetimeframework.curator.wrapper.ZNodeWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Import(CuratorConfig.class)
public class CuratorService {
    @Autowired
    private CuratorFramework client;

    public CuratorFramework getOriginFramework() {
        return this.client;
    }

    //创建空子节点
    public String createEmptyZNode(String path) throws Exception {
        return this.client.create().forPath(path);
    }

    //创建空子节点
    public String createEmptyZNode(String path, boolean creatingParentsIfNeeded, CreateMode createMode) throws Exception {
        if (creatingParentsIfNeeded) {
            return this.client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path);
        } else {
            return this.client.create().withMode(createMode).forPath(path);
        }
    }

    //创建数据子节点
    public <T> String createDataZNode(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.create().forPath(path, bytes);
    }

    //创建数据子节点
    public <T> String createDataZNode(String path, boolean creatingParentsIfNeeded, CreateMode createMode, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        if (creatingParentsIfNeeded) {
            return this.client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path, bytes);
        } else {
            return this.client.create().withMode(createMode).forPath(path, bytes);
        }
    }

    //创建永久节点
    public <T> String createPersistent(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.create().forPath(path, bytes);
    }

    //创建临时节点
    public <T> String createEphemeral(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.create().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
    }

    //创建永久有序节点
    public <T> String createPersistentSequential(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, bytes);
    }

    //创建临时有序节点
    public <T> String createEphemeralSequential(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, bytes);
    }

    //删除子节点
    public void deleteZNode(String path) throws Exception {
        this.client.delete().forPath(path);
    }

    //删除节点并指定是否删除其子节点
    public void deleteZNode(String path, boolean deletingChildrenIfNeeded) throws Exception {
        if (deletingChildrenIfNeeded) {
            this.client.delete().deletingChildrenIfNeeded().forPath(path);
        } else {
            this.client.delete().forPath(path);
        }
    }

    //删除指定版本节点
    public void deleteZNodeWithVersion(String path, int version) throws Exception {
        this.client.delete().withVersion(version).forPath(path);
    }

    //确保删除节点
    public void forceDeleteZNode(String path) throws Exception {
        this.client.delete().guaranteed().forPath(path);
    }

    //查看节点数据
    public String getZNodeData(String path) throws Exception {
        return new String(this.client.getData().forPath(path));
    }

    //查看节点数据和状态
    public ZNodeWrapper getZNodeDataWithStat(String path) throws Exception {
        ZNodeWrapper wrapper = new ZNodeWrapper();
        Stat stat = new Stat();
        wrapper.setData(new String(this.client.getData().storingStatIn(stat).forPath(path)));
        wrapper.setStat(stat);
        return wrapper;
    }

    //测试检查某个节点是否存在
    public boolean checkExists(String path) throws Exception {
        return this.client.checkExists().forPath(path) != null;
    }

    //获取某个节点的所有子节点
    public List<String> getChildren(String path) throws Exception {
        return this.client.getChildren().forPath(path);
    }

    //普通更新
    public <T> Stat setZNodeData(String path, T data) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.setData().forPath(path, bytes);
    }

    //指定版本更新
    public <T> Stat setZNodeDataWithVersion(String path, T data, int version) throws Exception {
        byte[] bytes = transToByteArray(data);
        return this.client.setData().withVersion(version).forPath(path, bytes);
    }

    //添加监听器
    public void addListener(String path, CuratorCacheListener listener) {
        CuratorCache cache = CuratorCache.build(this.client, path);
        cache.listenable().addListener(listener);
        cache.start();
    }

    //添加监听器
    public void addListener(String path, CuratorCacheListener listener, ExecutorService pool) {
        CuratorCache cache = CuratorCache.build(this.client, path);
        cache.listenable().addListener(listener, pool);
        cache.start();
    }

    //可重入锁
    public InterProcessMutex getInterProcessMutex(String path) {
        return new InterProcessMutex(this.client, path);
    }

    //不可重入锁
    public InterProcessSemaphoreMutex getInterProcessSemaphoreMutex(String path) {
        return new InterProcessSemaphoreMutex(this.client, path);
    }

    //可重入读写锁
    public InterProcessReadWriteLock getInterProcessReadWriteLock(String path) {
        return new InterProcessReadWriteLock(this.client, path);
    }

    //信号量
    public InterProcessSemaphoreV2 getInterProcessSemaphoreV2(String path, int maxLeases) {
        return new InterProcessSemaphoreV2(this.client, path, maxLeases);
    }

    //栅栏
    public DistributedBarrier getDistributedBarrier(String path) {
        return new DistributedBarrier(this.client, path);
    }

    //双栅栏
    public DistributedDoubleBarrier getDistributedDoubleBarrier(String path, int memberQty) {
        return new DistributedDoubleBarrier(this.client, path, memberQty);
    }

    private <T> byte[] transToByteArray(T data) {
        byte[] bytes;

        switch (data) {
            case null -> {
                log.error("ZNode data can't be null!");
                throw new RuntimeException("ZNode data can't be null!");
            }
            case String s -> bytes = s.getBytes();
            case byte[] bArr -> bytes = bArr;
            default -> bytes = data.toString().getBytes();
        }

        return bytes;
    }
}
