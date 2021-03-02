#ifndef _VCE_CCY_TASKS_MNG___H_
#define _VCE_CCY_TASKS_MNG___H_ 1

#include <semaphore.h>

#include "task_result_seg_pool.h"

struct socket_info_node {
	int socket_id;
};

/*
struct vce_task {

	char *data_pkg;
	size_t data_pkg_s; // data_pkg 最大长度
	size_t data_pkg_l; // data_pkg 有效数据长度

	char *result_pkg;
	size_t result_pkg_s; // result_pkg 最大长度
	size_t result_pkg_l; // result_pkg 有效数据长度

	struct vce_task *prev;
	struct vce_task *next;
};
*/

/*
struct ccy_task_node {
	int socket_type;
	void *package_stct;
	struct ccy_task_node *next;
};
*/

struct real_time_socket__respkg {
	sem_t sem;
	size_t buf_in_size;
	size_t buf_out_size;
	char *buf_in;
	char *buf_out;
	size_t data_in_len;
	size_t data_out_len;
};

struct comm_task_info {
	
};

struct trs_agg_w__node {
	struct comm_task_info cti;

	struct trs_agg_w__node *prev;
	struct trs_agg_w__node *next;
};

void tasks_mng__init();

void tasks_mng__syncpost();

void add_task(void *params, int socket_type, void *package);

void receiving_result_from_sub(void *params, void *sub_node_res_data);

void *process_agg_task(void *p);

#endif
