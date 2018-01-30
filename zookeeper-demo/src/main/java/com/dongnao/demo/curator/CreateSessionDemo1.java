package com.dongnao.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by 3 on 2018/1/30.
 */
public class CreateSessionDemo1 {
    public static void main(String[] args) {
        try {
            String path = "/zkclient";
            CuratorFramework client = CuratorFrameworkFactory
                    .builder()
                    .sessionTimeoutMs(1000)
                    .connectString("172.20.32.80:2181")
                    .retryPolicy(new ExponentialBackoffRetry(1000,3))
                    .build();
            client.start();
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
