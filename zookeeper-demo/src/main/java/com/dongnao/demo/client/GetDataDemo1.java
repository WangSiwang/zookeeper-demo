package com.dongnao.demo.client;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

public class GetDataDemo1 {
	public static void main(String[] args) {
        try {
            String path  = "/zk-client";
            ZkClient zkClient = new ZkClient("172.20.32.80:2181");
            zkClient.createEphemeral(path,true);
            zkClient.subscribeDataChanges(path, new IZkDataListener() {

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    // TODO Auto-generated method stub
                    System.out.println(dataPath + "拥有了删除的迹象");
                }

                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    // TODO Auto-generated method stub
                    System.out.println(dataPath + "change :" + data);
                }
            });
            //读数据
            System.out.println(zkClient.readData(path).toString());
            //写数据
            zkClient.writeData(path,"23256");
            Thread.sleep(1000);
            zkClient.delete(path);
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
