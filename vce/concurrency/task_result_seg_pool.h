// file: task_result_seg_pool.h

#ifndef _VCE_TASK_RES_SEG_POOL_H_

#define _VCE_TASK_RES_SEG_POOL_H_ 1

// #include <arpa/inet.h>
// #include <stdlib.h>
// #include <string.h>
// #include <semaphore.h>

// #include "vce_listen.h"
// #include "vce_common.h"

#include "vce_tcp_llnk_th.h"

void put_vtask_seg(struct vce_task *vt);

void task_reseg_pool_init();

void *combined_result_data(void *result);

#endif
