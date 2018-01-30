package com.dongnao.demo.client;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;

/**
 * Created by 3 on 2018/1/30.
 */
public class GetChildrenDemo1 {
    public static void main(String[] args) {
        try {
            String path = "/zk-client";
            ZkClient client = new ZkClient("172.20.32.80:2181");
            client.subscribeChildChanges(path, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    System.out.println(parentPath + "de ChirdrenNode have change:" + currentChilds);
                }
            });
            client.createPersistent(path);
            Thread.sleep(1000);
            System.out.println(path + "路径下的子节点是" + client.getChildren(path));
            Thread.sleep(1000);
            client.createEphemeral(path + "/c1");
            Thread.sleep(1000);
            client.delete(path + "/c1");
            client.delete(path );
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
