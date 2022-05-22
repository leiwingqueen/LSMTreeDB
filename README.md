# LSMTreeDB

make a simple db with LSMTree

# 核心组件

## BitcastDB

以哈希算法为基础的存储。内存存放key和文件对应的position，文件存放对应的command log.

- 索引更新(内存)
- command log更新(WAL)
- log compact(COW)
- 索引恢复(从WAL日志进行恢复)

Bitcast的实现其实性能非常高，WAL的写入是顺序的，每次读取其实只需要一次的随机文件IO。缺点是由于其存储的无序性决定了这个数据结构无法很好地支持scan的操作，并且由于索引是保存在内存的，所以存储的空间也是有限。

## LSM-Tree DB

在bitcast的基础上做进一步修改，我们要做是要保证每一个log的有序性(ssTable)。这样能最大程度地提高compact的性能和减少内存的索引(只保存稀疏索引，在每个稀疏索引范围内再进行顺序查找)

- memTable(也是索引表，但是为了保证有序性，这里可以用RBTree、SkipList、BTree等数据结果)
- SSTable(由多个Segment构成，每个Segment内部会保证唯一有序)
- immutableMemTable(其实是为了保证memTable->SSTable实现COW的中间存储)
- WAL(为了恢复memTable)

# reference

[Understanding LSM Trees: What Powers Write-Heavy Databases](https://yetanotherdevblog.com/lsm/)

[bloom-filters](https://yetanotherdevblog.com/bloom-filters/)

[从零开始写数据库：500行代码实现 LSM 数据库](https://zhuanlan.zhihu.com/p/374535126)

[[LevelDB] 原理：知其然知其所以然——LevelDB基本原理](https://zhuanlan.zhihu.com/p/206608102)

[从SSTable到LSM-Tree之二](https://zhuanlan.zhihu.com/p/103968892)








