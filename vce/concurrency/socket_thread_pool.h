// file: socket_thread_pool.h

#ifndef _VCE_SOCKET_THREAD_POOL_H_

#define _VCE_SOCKET_THREAD_POOL_H_ 1

// #include <arpa/inet.h>
// #include <stdlib.h>
// #include <string.h>
// #include <semaphore.h>

// #include "vce_listen.h"
// #include "vce_common.h"

#include "vce_tcp_llnk_th.h"

extern struct vce_socket_thread *tm_st; // 向上级master提交任务结果

void stp_init();

void put_vst_dits(struct vce_socket_thread *vst);

int dis_ts_count();

// void vst_dits_pool_lock();
// void vst_dits_pool_unlock();
void ready_dis_task(struct vce_task *vt);

void submit_vce_task(struct vce_task *vt);

#endif
