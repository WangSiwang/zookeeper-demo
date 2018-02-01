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
2018年1月31日 
缺点：“惊群效应”<br>
<hr>
2018年1月31日更新
关于订单类com.dongnao.demo.lock.OrderServiceImpl
中的分布式锁com.dongnao.demo.lock.ImproveLock的相关解释
<pre><ol>
<li>在OrderServiceImpl的业务场景中申明了CountDownLatch线程计数器，以及模拟了100个线程并发的情况</li>
<li>在分布式锁ImproveLock中
<ol><li>tryLock(),如果currentPath为空则为第一次尝试加锁，第一次加锁赋值currentPath：<br/>如果currentPath等于当前建立的第一个临时顺序节点：0000000400，返回true加锁成功<br/>如果当前节点在所有节点中排名中不是排名第一，则获取前面的节点名称，并赋值给beforePath,返回Lock()中等待waitForLock(),<br/></li>
<li>在waitForLock(),设置beforePath监听器监听subscribeDataChanges===>IZkDataListener可以解决惊群效应</li>
</ol>
</li>
</ol></pre>
<hr>
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
<image src="/zookeeper-demo/src/resource/1123.png"></image>
<hr>
Election的实现FastLeaderElection选举类源码分析<br/>
<pre>
QuorumCnxManager：主要完成服务器间的网络交互<br/>
	senderWorkerMap 主要用于发送
    queueSendMap主要用于发送
    	SendWorker用于发送器,底层连接的<sid,socket>
    	RecvWorker用于接收器
	lastMessageSent最后一条发送的消息
	QuorumPeer 半数对等协议,在初始化的过程中出现,饱含选举IP地址 选举port等状态
Notification:表示服务器节点收到的选票信息
	version:
	leader:Proposed leader
	zxid:zxid of the proposed leader
	electionEpoch:Epoch
	QuorumPeer.ServerState:LOOKING, FOLLOWING, LEADING(广播状态), OBSERVING;
	sid:Address of sender
	peerEpoch:epoch of the proposed leader
lookForLeader():
	recvset：存储本节点来自其他节点的选票(投票用)<br/>
	outofelection：存储本节点从其他节点由following、leading状态发送来的选票（选举确认用）<br/>
	updateProposal(参与者的选票,最后的logger的zxid,返回当前的年号):更新选票,第一次投票给自己
	sendNotifications();放置发送队列中
	循环...(选举没有stop)
	recvqueue.poll():获取到一张消息
		进行检查:
			网断
			发送队列为0
				重连所有的socket连接
				tmpTimeOut退避策略*2
	self.getVotingView().containsKey(n.sid):是否包含当前的sid:
		 LOG.warn(发送自不在本集群的消息)
		 Notification消息的状态:
		 	LOOKING:
		 		n.electionEpoch > logicalclock:
		 			logicalclock = n.electionEpoch;
                    recvset.clear();
                    totalOrderPredicate():投票对比:周期Epoch大||Zxid大||myid:
                    updateProposal()跟新选票成为本地的选票
                    sendNotifications()发送队列
                n.electionEpoch < logicalclock:
                	ignore 
            	n.electionEpoch = logicalclock:
            		对比totalOrderPredicate()
            		pdateProposal();
            		sendNotifications();
				recvset.put():将选票放置进收件箱
				termPredicate():是否要结束选举
					vote.equals(votes):遍历选票
					containsQuorum(set):过半监测
						校验/返回endVote选票
			FOLLOWING:
            LEADING:
            	前头的半数检测协议
            	checkLeader():
        			leader的状态
            		比较周期
				构造新选票并且返回		


</pre>
</hr>
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
<h2>Zookeeper Master选举</h2>
2018年1月31日 
<ol><li>
用Tomcat模拟一个实现Master选举过程(应用tomcat 阀com.dongnao.demo.election.ZkTomcatValve)</li><li>
使用acl处理脑裂的代码</li></ol>
<h1>Zookeeper基本模型</h1>
<ol><li>
持久节点（除非主动删除，zk不会做清理工作）</li><li>
临时节点（随着session会话消亡，而消亡）</li><li>
持久顺序节点</li><li>
临时顺序节点</li></ol>
<h2>数据模型——节点</h2>
树模型（采用文件系统的形式，只不过去掉文件和目录），叫数据节点。
<h2>ACL——权限控制</h2>
org.apache.zookeeper.ZooDefs.Ids
<hr>
对于一个节点可操作的权限有5种：
<pre>
READ（只读）
WRITE（只写）
CREATE（创建）
DELETE（删除）
ADMIN（节点管理权限）(对节点的删除和创建的权限)
</pre>
ACL机制：权限模式（Schema）、授权对象（ID）、权限（Permission）
<ol><li>
1.	world：开放模式。意思所有人都可以访问。</li><li>
2.	IP：   针对某个开放权限</li><li>
3.	digest：用户/密码模式</li><li>
4.	Super：超级用户模式</li></ol>
<pre>

</pre>
使用密文：<pre>
setAcl path digest|ip:username:password:c|d|r|w|a</pre>
密文生成org.apache.zookeeper.server.auth.DigestAuthenticationProvider

使用明文：<pre>
   addauth digest username:password
   setAcl /path auth:username:password:cdrwa</pre>
删除节点的bug
<pre>rmr / delete</pre>

密文命令（执行目录在zk根目录）：<pre>
java -cp ./zookeeper-3.4.6.jar:./lib/log4j-1.2.16.jar:./lib/slf4j-api-1.6.1.jar:./lib/slf4j-log4j12-1.6.1.jar org.apache.zookeeper.server.auth.DigestAuthenticationProvider user1:12345
</pre>
<h2>版本</h2>

cversion 当前节点的权限<br>
dataversion 当前节点数据内容的版本号<br>
aclVersion  就是ACL版本号<br>
zookeeper版本的含义：版本指的是变更的次数。<br>
CAS（compare and swap）比较然后交换。<br>
<h2>Watcher</h2>
org.apache.zookeeper.ZooKeeper
org.apache.zookeeper.Watcher
org.apache.zookeeper.server.DataTree
<image src="/zookeeper-demo/src/resource/Watcher.png"></image>

<h3>客户端：</h3>
<pre>
Zookeeper （getChild）
Cliencnxn
WatchRegistration
SendThread
Packet(creatBB经过处理，简单化)
ZKWatchManager
</pre>
<h3>服务端：</h3>
<pre>
FinalRequestProcessor
WatchManager（triggerWatcher）
SendThread.readResponse
EventThread</pre>
<hr/>
2018年2月1日  内容:zookeeper的事件监听的机制源码分析<br/>
ps:也是似懂非懂蒙蔽中
Zookeeper Class中的构造方法中我们可以看到
<image src="/zookeeper-demo/src/resource/Zookeeper构造方法.png"></image>
会传入一个Watch的一个事件监听体系<br/>
<image src="/zookeeper-demo/src/resource/Zookeeper.getData.png"></image>
接下来,进入到submitRequest()中,
<image src="/zookeeper-demo/src/resource/ClientCnxn.submitRequest.png"></image>
话分两头单表一支</br>
封装的packet会进行线程中发送
<image src="/zookeeper-demo/src/resource/ClientCnxn.sendPacket.png"></image>
在这个SendThread是完全的继承自java的Thread线程<br/>
<image src="/zookeeper-demo/src/resource/SendThread hierarchy.png"></image>
在这个方法中是将前面的Packet封装的具体实现,并且是在同步块中会将<b>outgoingqueue</b>进行输出
<image src="/zookeeper-demo/src/resource/ClientCnxn.queuePacket.png"></image>
其中底层的实现还是NIO通信
<image src="/zookeeper-demo/src/resource/ClientCnxnSocketNIO.png"></image>
但是还是由于并发的信息量过大的缘故,所以还需要进行再次反序列化以及进行封装
<image src="/zookeeper-demo/src/resource/ClientCnxnSocketNIO.snedPacket.png"></image>
createBB的具体的实现
<image src="/zookeeper-demo/src/resource/ClientCnxn.Packet.createBB.png"></image>
<hr/>
到了finnish阶段,会存储到ClientWatchManager中的三个map中
<image src="/zookeeper-demo/src/resource/ClientCnxn.finishPacket.png"></image>
<image src="/zookeeper-demo/src/resource/Zookeeper.WatcherRegister.register.png"></image>
<image src="/zookeeper-demo/src/resource/Zookeeper.WatcherRegister.getwatcher.png"></image>
<image src="/zookeeper-demo/src/resource/ZKWatchManager.getWatches.png"></image>
在线程处理阶段会对操作数进行判断处理
<image src="/zookeeper-demo/src/resource/FinalRequestProcess.process.png"></image>
已getData操作进行验证,
<image src="/zookeeper-demo/src/resource/getData操作数.png"></image>
在完成事件中添加触发的机制
<image src="/zookeeper-demo/src/resource/WatchManager.triggerWatch.png"></image>
在sendThread中的readermeResponse中添加回调判断已经执行的操作数,并且在event中的EventThread中进行性处理
<image src="/zookeeper-demo/src/resource/SendThread.readResponse.png"></image>
添加时间监听机制在判断完操作数
<image src="/zookeeper-demo/src/resource/SendThread.readResponse.getXid.png"></image>
在EventThread中对事件进行处理
<image src="/zookeeper-demo/src/resource/EventThread.png"></image>
<hr/>
终于完了,视频听了三遍还是迷迷糊糊的,欢迎各位dalao指教...嗯有空了把master选举的源码分析也作出来,快两点了..好饿..要去吃饭了...
<hr/>











