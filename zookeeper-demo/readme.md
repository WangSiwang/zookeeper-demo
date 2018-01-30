前面应该还是有有关zk的master选举相关的内容，等有空再补一下
<hr>
<B>Zk中的专业术语</B><br/>
<pre>
epoch周期值
acceptedEpoch（比喻：年号）：follower已经接受leader更改年好的（newepoch）提议。
currentEpoch（比喻：当前的年号）：当前的年号
lastZxid：history中最近接收到的提议zxid(最大的值)
history：当前节点接受到事务提议的log
</pre>
<h1>4.ZkClient	</h1>
<ol>
<li>优点1：递归创建</li>
<li>优点2：递归删除</li>
<li>优点3：避免不存在异常</li>
</ol>
<h2>ZkClient注册事件：</h2>
原文demo在com.dongnao.demo.client下
<pre>
subscribeChildChanges/unsubscribeChildChanges(节点变化)
subscribeDataChanges/unsubscribeDataChanges（数据变化）
</pre>


<h1>Curator</h1>
原文demo在com.dongnao.demo.curator下
<h2>curator连接ZK应用最广泛的工具。</h2>
<ol>
<li>zk应用场景（分布式锁，Master选举等等），curator包含了这些场景。</li>
<li>2.	应用场景出现极端的情况下，curator考虑。</li>
</ol>
<h2>Backoff退避算法：</h2>
<ol>
<li>重试一次，如果网络出现阻塞。
<pre>
    22：25  request1（block）
    22：26  request2（毫无意义）
    22：27  request3（毫无意义）
    22：28  request4（通顺）request2、3、4
</re>
</li>
<li>数间隔重试，比如第一次1分钟，第二次2分钟......随着时间的推移，重试间隔越长。</li></ol>

<h2>重点：Curator时间监听：</h2>
<pre>
NodeCache：节点处理监听（会使用缓存）。回调接口NodeCacheListener
PathChildrenCache：子节点缓存，处理子节点变化。回调接口PathChildrenCacheListener
TreeCache：NodeCache和PathChildrenCache的结合体。回调接口TreeCacheCacheListener
</pre>

<h1>5.Zookeeper实现分布式锁</h1>
缺点：“惊群效应”<br>
<pre>有些分布式系统master-slave，master是一个单节点（备份master-back）。
     实际的案例：Hadoop（NameNode、ResourceManager），普通的部署NameNode、ResourceManager仅仅是单节点。Hadoop HA（NameNode和ResourceManager有多个备份）
     说明：统一的一个临时节点：ActiveOrStandByLock（/distibuted_system/ActiveOrStandByLock仅仅这样一个节点）
     第一步：zk有这样一个持久节点/distibuted_system
     第二步：master1和master2同时启动，同时向向/distributed_system这个节点申请创建临时子节点ActiveOrStandByLock（同一时间只有一个请求能够创建成功）。
         如果master1创建成功，这个节点（ActiveOrStandByLock）就不允许master2创建（锁的机制）
         master1：active===》真正的master。路径：/distributed_system/ActiveOrStandByLock
         master2：改为standby（master-back）。
         同时对/distributed_system/ActiveOrStandByLock注册事件监听。
     第三步：master1挂掉或者超过一定时间。节点会被删除（事件机制就会起作用），就会通知master2，master2就会在/distributed_system/ActiveOrStandByLock，同时修改状态为active。
     备注：假如master1并没有挂掉，只有由于网络延时导致，当网络顺畅的时候就会出现“脑裂”状态。都认为自己是active。
     解决脑裂的办法：对/distributed_system/ActiveOrStandByLock加一个权限ACL控制。master1对于这个节点/distributed_system/ActiveOrStandByLock没有权限。自己把状态改成standby。
</pre>
<image src="/resource/1123.png"></image>
上面是一个image的标签  不知道为什么没有显示出来  望指教<br/>
<hr>
Election选举类<br/>
<pre>
QuorumCnxManager：主要完成服务器间的网络交互<br/>
Vote 选票<br/>****
QuorumPeer 是zk服务器实例类<br/>
QuorumPeer.QuorumServer 存储zk server连接信息<br/></pre>
<hr>
<b>主要实现函数：</b><br/>
lookForLeader：选举过程（主要函数）<br/>
totalOrderPredicate：比较选票<br/>
termPredicate：是否结束选举<br/>

senderWorkerMap：主要用于发送<br/>
queueSendMap：发送队列<br/>
SendWorker：发送器<br/>
RecvWorker：接收器<br/>
lastMessageSent：最后一条发送消息<br/>
Notification：表示该服务器节点收到选票信息<br/>
recvset：存储本节点来自其他节点的选票(投票用)<br/>
outofelection：存储本节点从其他节点由following、leading状态发送来的选票（选举确认用）<br/>
<hr>





