rsync4j
=======


提供一个java 写的文件差异化对比同步工具。

1. 优化roll算法
2. 提供差异对比临时文件生成工具
3. 提供差异文件合并工具

代码简单；经过验证；

由[深圳一粒云科技有限公司](http://www.yliyun.com)提供开源技术支持,并应用于一粒云盘

---
首先，客户端开始分块并计算出MD5和Alder32值。

如上图，像taoh是一块，对taoh分别计算出MD5和alder32值。以此类推，最后一个n字母不足4位保留。于是，客户端把计算出的MD5和alder32按顺序发出，最后发出字符n。
 
服务器收到后，先把自己保存的File.2的内容按4字节划分。

划分出itao、huia、msom、an，当然，这些串的Alder32值肯定无法从File.1里划分出的：taoh、uiis、soma、n找出相同的。于是向后移一个字节，从t开始继续按4字节划分。

从taoh上找到了alder32相同的块，接着再比较MD5值，也相同！于是记下来，跳过taoh这4个字符，看uiam，又找不到File.1上相同的块了。继续向后跳1个字节从i开始看。还是没有找到Alder32相同，继续向后移，以此类推。

到了soma，又找到相同的块了。
 
 
详细说明文章：
[https://www.cnblogs.com/Creator/archive/2012/03/30/2426070.html]https://www.cnblogs.com/Creator/archive/2012/03/30/2426070.html
 
