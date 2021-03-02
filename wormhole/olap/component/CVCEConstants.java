package cn.bgotech.wormhole.olap.component;

/**
 * Created by czg on 2019/3/15.
 */
public interface CVCEConstants {

    int RC_1ST = 20;               // (request code) socket创建后第一个数据包的类型
    int SKT_TYPE_REAL_TIME = 2001; // 实时响应的短连接


    int REQ_CLI_EXIT = 0; // 客户端退出
    int REQ_CLI_TEST = 1; // 客户端测试
    int REQ_VCE_SHUTDOWN = 3; // 停止服务
    int REQ_SYNC_CUBE = 4; // 同步CUBE结构
    int REQ_INSERT_MEASURE = 5; // 插入CUBE的度量值
    int REQ_UPDATE_MEASURE = 6; // 更新CUBE的度量值
    int REQ_DELETE_MEASURE = 7; // 删除CUBE的度量值
    int REQ_AGG_CALCUL_QUERY = 9; // 查询（进行聚集运算）
    int REQ_REBUI_CUBE_DATA_MEM = 10; // 重新构建Cube的内存数据结构
    int REQ_MASTER_LINK_WORKER = 11; // 建立master到worker的socket长连接
    int REQ_REBUI_CUBEDATAFILE_FROMMEM = 12; // 根据内存数据重构Cube数据文件


    int QUEPKG_RESERVED_SPACE_BYTES_SIZE = 64; // 查询请求预留空间长度
}
