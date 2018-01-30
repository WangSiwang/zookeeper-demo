<B>Zk中的专业术语</B><br/>
<pre>
epoch周期值
acceptedEpoch（比喻：年号）：follower已经接受leader更改年好的（newepoch）提议。
currentEpoch（比喻：当前的年号）：当前的年号
lastZxid：history中最近接收到的提议zxid(最大的值)
history：当前节点接受到事务提议的log
</pre>
<h1>ZkClient	</h1>
<ol>
<li>优点1：递归创建</li>
<li>优点2：递归删除</li>
<li>优点3：避免不存在异常</li>
</ol>
<h2>ZkClient注册事件：</h2>
<pre>
subscribeChildChanges/unsubscribeChildChanges(节点变化)
subscribeDataChanges/unsubscribeDataChanges（数据变化）
</pre>


<h1>Curator</h1>
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



