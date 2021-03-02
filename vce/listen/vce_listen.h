#ifndef _VCE_LISTEN_H_

#define _VCE_LISTEN_H_ 1


#define  SUCCESS_0  0        // 表示socket或函数的成功状态
#define  ERROR_UNK  -999999  // 表示未知或未定义的错误状态


#define  RC_1ST                20    // (request code) socket创建后第一个数据包的类型，用于确定此次连接的目的
#define    SKT_TYPE_REAL_TIME  2001  // 实时响应的短连接
#define    SKT_TYPE_TASK_DOWN  2002  // 上级节点向下分配任务的长连接
#define    SKT_TYPE_RET_RES    2003  // 下级节点返回执行结果的长连接
// #define    SKT_TYPE_DIS_TASKS  2004  // 向下级节点分配任务的长连接
// #define    SKT_TYPE_DELIVERY   2005  // 向上级节点返回结果的长连接

#define  RC_MST_SUBH_LLINK  21  // (request code) 建立一个从master指向下级节点的用于分发任务的TCP长连接
#define  RC_SUBH_MST_LLINK  22  // (request code) 建立一个从下级节点指向master的用于返回任务结果的TCP长连接

#define  RC_QUIT  30  // (request code) socket正常断开


#define  REQ_CLI_EXIT        0  // (request code) Master端退出
#define  REQ_CLI_TEST        1  // (request code) Master端测试
#define  REQ_VCE_SHUTDOWN    3  // (request code) 停止服务
#define  REQ_SYNC_CUBE       4  // (request code) 同步CUBE结构
#define  REQ_INSERT_MEASURE  5  // (request code) 插入CUBE的度量值
#define  REQ_UPDATE_MEASURE  6  // (request code) 更新CUBE的度量值
#define  REQ_DELETE_MEASURE  7  // (request code) 删除CUBE的度量值

#define  REQ_AGG_CAL_QUERY   9  // (request code) 查询
#define  DPC__RET_AGG_DATA_S 901  // (request code) 聚合查询成功标识

#define  REQ_REBUI_CUBE_DATA_MEM  10 // (request code) 重新构建 cube_data_file_xxxx 文件的内存数据结构

#define  REQ_MASTER_LINK_WORKER   11 // (request code) 建立master到worker的socket长连接

#define  REQ_REBUI_CUBEDATAFILE_FROMMEM 12 // (request code) 根据内存数据重构Cube数据文件（cube_data_file_xxxx）

void open_listen_for_master();

int wait_client(int svr_socket);

#endif
