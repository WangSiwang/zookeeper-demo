package com.dongnao.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by 3 on 2018/1/30.
 */
public class CreateNodeDemo1 {
    public static void main(String[] args) {

        try {
            String path = "/zk-client/c1";
            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString("172.20.32.80:2181").sessionTimeoutMs(1000)
                    .retryPolicy(
                            new ExponentialBackoffRetry(1000,3))
                    .build();
            client.start();
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path,"test".getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
