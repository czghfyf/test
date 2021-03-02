// main_vce.c

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

#include "vce_cfg.h"
#include "vce_listen.h"
#include "vce_map_reduce.h"
#include "tasks_manager.h"
#include "vce_tcp_llnk_th.h"
#include "socket_thread_pool.h"

#include "task_result_seg_pool.h"

extern struct vce_cfg_stct vce_cfg_instance;

static void *_new_thread_run_(void *addr);

static void real_time_short_socket(int client_socket);
static void task_down_long_socket(int client_socket);
static void ret_res_long_socket(int client_socket);

static void *submit_result(void *addr);
static void *dis_tasks(void *addr);

int main(int argc, char *argv[])
{
	vce_cfg_init(argc, argv);

	vce_log_init();

	task_reseg_pool_init();

	load_all_cubes_info();

	if (strcmp(vce_cfg_instance.mode, "W") == 0) {
		load_all_cubes_data();
	}

	stp_init();

	int svr_socket = open_svr_socket(vce_cfg_instance.svr_port);
	while (1) {
		int client_socket = wait_client(svr_socket);
		pthread_t thd_id;
		pthread_create(&thd_id, NULL, _new_thread_run_, &client_socket);
		pthread_detach(thd_id);
	}
	close(svr_socket);

	return 0;
} // function main() end.

static void *_new_thread_run_(void *addr)
{
	int client_socket = *((int *) addr), cr_;

	size_t tmp_buf_size = sizeof(int) * 3; // 12
	char tmp_buf[tmp_buf_size];

	ssize_t ret = read_data_pkg(client_socket, tmp_buf, tmp_buf_size);
	if (ret != tmp_buf_size) {
		logW("fn:_new_thread_run_ > first time, ret = %d", ret);
		goto new_thread_exit;
	}

	int request_code = ((int *) tmp_buf)[1];
	if (request_code != RC_1ST) {
		logW("socket 1st request_code = %d", request_code);
		goto new_thread_exit;
	}

	int socket_type = ((int *) tmp_buf)[2];

	if (socket_type != SKT_TYPE_REAL_TIME
			&& socket_type != SKT_TYPE_TASK_DOWN
			&& socket_type != SKT_TYPE_RET_RES) {
		
		logW("socket 1st socket_type = %d", socket_type);
		int negative_number_1 = -1;
		write(client_socket, &negative_number_1, sizeof(int));
		goto new_thread_exit;
	}

	int success_0 = SUCCESS_0;
	write(client_socket, &success_0, sizeof(int));

	if (socket_type == SKT_TYPE_REAL_TIME) // 实时响应的短连接
		real_time_short_socket(client_socket);
	else if (socket_type == SKT_TYPE_TASK_DOWN) // 上级节点向下分配任务的长连接
		task_down_long_socket(client_socket);
	else if (socket_type == SKT_TYPE_RET_RES) // 下级节点返回执行结果的长连接
		ret_res_long_socket(client_socket);
	else {
		logE("serious error! system exit!");
		exit(1);
	}

	new_thread_exit:
	cr_ = close(client_socket);
	if (cr_ != 0)
		logW("fn:_new_thread_run_ > close [%d]", cr_);
	return NULL;
} // function _new_thread_run_() end.

static void real_time_short_socket(int client_socket)
{
	void *request_pkg;
	size_t req_pkg_size;
	ssize_t ss;

	int request_code;

	struct vce_task *vt = NULL;
	struct vce_socket_thread *ts = NULL;

	while (1) {
		ss = read_tcp_pkg(client_socket, &request_pkg, &req_pkg_size);
		if (ss <= 0) {
			logW("fn:real_time_short_socket > read_tcp_pkg return %zd", ss);
			break;
		}

		request_code = *((int *) (request_pkg + sizeof(int)));

		if (request_code == REQ_AGG_CAL_QUERY) { // aggregate query

			if (vt == NULL) {
				vt = (struct vce_task *) malloc(sizeof(struct vce_task));
				memset(vt, 0, sizeof(struct vce_task));
			}

			if (ts == NULL) {
				ts = (struct vce_socket_thread*) malloc(sizeof(struct vce_socket_thread));
				memset(ts, 0, sizeof(struct vce_socket_thread));
			}

			vt -> v_skt_th_p = ts;
			vt -> task_data_pkg = request_pkg;

			vt -> rand_code = *((long *) (request_pkg + 4 * sizeof(int))); // random code

			pthread_t thd_id;
			pthread_create(&thd_id, NULL, process_agg_task, vt);
			pthread_detach(thd_id);

			sem_wait(&(ts -> switch_sem));
			int phis = 2 * sizeof(int) + sizeof(long);
			write(client_socket, vt -> result_pkg + phis, *((int *) (vt -> result_pkg)) - phis);

			free(vt -> result_pkg);
			free(request_pkg);
			vt -> task_data_pkg = vt -> result_pkg = NULL;

			continue;
		}

		// 除聚合运算外，其他任务均被视为节点设置型操作
		if (request_code == REQ_SYNC_CUBE) { // sync cube
			build_cube(request_pkg + sizeof(int), *((int *) request_pkg) - sizeof(int));
		} else if (request_code == REQ_INSERT_MEASURE) { // insert measures
			insert_measure_values(request_pkg + sizeof(int), *((int *) request_pkg) - sizeof(int));
		} else if (request_code == REQ_REBUI_CUBE_DATA_MEM) { // load cube data into memory
			rebuild_cube_mem_data_struct(((unsigned int *) request_pkg)[2]);
		} else if (request_code == REQ_UPDATE_MEASURE) { // update measures
			update_measure_values(request_pkg + sizeof(int), *((int *) request_pkg) - sizeof(int));
		} else if (request_code == REQ_DELETE_MEASURE) { // delete measures
			del_measure_values(request_pkg + sizeof(int), *((int *) request_pkg) - sizeof(int));
		} else if (request_code == REQ_REBUI_CUBEDATAFILE_FROMMEM) { // write back cube data from memory to disk
			mem_writeback_cubedf(((unsigned int *) request_pkg)[2]);
		} else if (request_code == RC_SUBH_MST_LLINK) { // 创建指向上级master用于提交结果的长连接
			pthread_t mstd_pth_id;
			pthread_create(&mstd_pth_id, NULL, submit_result, request_pkg);
			pthread_detach(mstd_pth_id);

		} else if (request_code == RC_MST_SUBH_LLINK) { // 创建一个指向下级节点用于下发任务的长连接
			pthread_t mstd_pth_id;
			pthread_create(&mstd_pth_id, NULL, dis_tasks, request_pkg);
			pthread_detach(mstd_pth_id);

		} else {
			logW("fn:real_time_short_socket > can not be executed. [socket type %d] [request code %d]",
				SKT_TYPE_REAL_TIME, request_code);
			int e = ERROR_UNK;
			write(client_socket, &e, sizeof(e));
			free(request_pkg);
			continue;
		}
		free(request_pkg);
		int succ = SUCCESS_0;
		write(client_socket, &succ, sizeof(succ));
	}
	if (vt != NULL) {
		if (vt -> task_data_pkg != NULL)
			free(vt -> task_data_pkg);
		if (vt -> result_pkg != NULL)
			free(vt -> result_pkg);
		free(vt);
	}
	if (ts != NULL)
		free(ts);
} // function real_time_short_socket() end

static void *submit_result(void *addr)
{
	if (tm_st != NULL) {
		logW("submit_result() tm_st is not NULL!");
		return;
	}

	// 解析数据包，获得上级master的ip和port
	char *ip = (char *) addr + sizeof(int) * 2;
	int port = ((int *) addr)[6];

	// 建立socket
	int soid = create_tcp_link(ip, port);

	// 如果未能确定一切运行正常则结束此函数
	if (soid < 1) {
		logW("submit_result() soid = %d", soid);
		return;
	}

	// 发送表明此连接目的的数据包
	int inten[] = { sizeof(int) * 3, 20, 2003 };
	write(soid, &inten, inten[0]);
	int rcou = read(soid, inten, inten[0]);

	// 创建vce_socket_thread实例，并设置为tm_st（socket_thread_pool.c中定义）
	tm_st = (struct vce_socket_thread *) malloc(sizeof(struct vce_socket_thread));
	memset(tm_st, 0, sizeof(struct vce_socket_thread));
	tm_st -> socket_id = soid;
	sem_init(&(tm_st -> switch_sem), 0, 0);

	while (1) {
		// Sem_wait(vce_socket_thread开关信号量)
		sem_wait(&(tm_st -> switch_sem));

		// 获得锁：vce_socket_thread同步锁
		pthread_mutex_lock(&(tm_st -> sync_mutex));
		// 从任务链中取出一个vce_task实例
		struct vce_task *task = NULL;
		if (tm_st -> task_chain_head == tm_st -> task_chain_tail && tm_st -> task_chain_tail != NULL) {
			task = tm_st -> task_chain_head;
			tm_st -> task_chain_head = tm_st -> task_chain_tail = NULL;
		} else if (tm_st -> task_chain_head != tm_st -> task_chain_tail) {
			task = tm_st -> task_chain_head;
			tm_st -> task_chain_head = task -> next;
			task -> next = tm_st -> task_chain_head -> prev = NULL;
		}
		// 归还锁：vce_socket_thread同步锁
		pthread_mutex_unlock(&(tm_st -> sync_mutex));

		if (task == NULL) {
			logE("dis_tasks() is task null ?!");
			continue;
		}
		// 将vce_task实例任务数据包通过当前vce_socket_thread实例的socket_id发送出去
		write(tm_st -> socket_id, task -> result_pkg, *((int *) task -> result_pkg));

		free(task -> result_pkg);
		free(task -> task_data_pkg);
		free(task);
	}

} // fn submit_result end

static void *dis_tasks(void *addr)
{
	// 解析数据包，获得下级节点的ip和port
	char *ip = (char *) addr + sizeof(int) * 2;
	int port = ((int *) addr)[6];
	// 建立socket
	int soid = create_tcp_link(ip, port);
	// 如果未能确定一切运行正常则结束此函数
	if (soid < 1) {
		logW("dis_tasks() soid = %d", soid);
		return;
	}

	// 发送表明此连接目的的数据包
	int inten[] = { sizeof(int) * 3, 20, 2002 };
	write(soid, &inten, inten[0]);
	int rcou = read(soid, inten, inten[0]);

	// 创建vce_socket_thread实例，并放置到相应池中
	struct vce_socket_thread *vst = (struct vce_socket_thread *) malloc(sizeof(struct vce_socket_thread));
	memset(vst, 0, sizeof(struct vce_socket_thread));
	vst -> socket_id = soid;
	put_vst_dits(vst);

	while (1) {
		// Sem_wait(vce_socket_thread开关信号量)
		sem_wait(&(vst -> switch_sem));
		// 获得锁：vce_socket_thread同步锁
		pthread_mutex_lock(&(vst -> sync_mutex));
		// 从任务链中取出一个vce_task实例
		struct vce_task *task = NULL;
		if (vst -> task_chain_head == vst -> task_chain_tail && vst -> task_chain_tail != NULL) {
			task = vst -> task_chain_head;
			vst -> task_chain_head = vst -> task_chain_tail = NULL;
		} else if (vst -> task_chain_head != vst -> task_chain_tail) {
			task = vst -> task_chain_head;
			vst -> task_chain_head = task -> next;
			task -> next = vst -> task_chain_head -> prev = NULL;
		}
		// 归还锁：vce_socket_thread同步锁
		pthread_mutex_unlock(&(vst -> sync_mutex));

		if (task == NULL) {
			logE("dis_tasks() is task null ?!");
			continue;
		}
		// 将vce_task实例任务数据包通过当前vce_socket_thread实例的socket_id发送出去
		write(vst -> socket_id, task -> task_data_pkg, *((int *) task -> task_data_pkg));
	}
} // fn dis_tasks end

static void task_down_long_socket(int client_socket)
{
	// 此部分程序逻辑与_new_thread_run_在同一线程中执行。

	char *result;
	size_t res_size;

	struct vce_socket_thread *vst = (struct vce_socket_thread *) malloc(sizeof(struct vce_socket_thread));
	memset(vst, 0, sizeof(struct vce_socket_thread));
	vst -> direction = INWARD;
	vst -> purpose = TASK_DISTRIB;
	pthread_mutex_init(&(vst -> sync_mutex), NULL);

	while (1) {
		// 调用实时分配内存的函数接收结果数据包
		ssize_t ress = read_tcp_pkg(client_socket, (void *) &result, &res_size);
		log_("task_down_long_socket() ress = %ld res_size = %ld", ress, res_size);

		char tmp_arr[32];
		memset(tmp_arr, 0, sizeof(tmp_arr));
		write(client_socket, tmp_arr, sizeof(tmp_arr));

		struct vce_task *t = (struct vce_task *) malloc(sizeof(struct vce_task));
		memset(t, 0, sizeof(struct vce_task));
		t -> task_data_pkg = result;
		t -> v_skt_th_p = vst;
		t -> rand_code = *((long *) (result + 4 * sizeof(int))); // random code

		// 启动一个新线程，运行process_agg_task函数，并将vce_task实例地址传入
		pthread_t thd_id;
		pthread_create(&thd_id, NULL, process_agg_task, t);
		pthread_detach(thd_id);
	}

} // function task_down_long_socket() end

static void ret_res_long_socket(int client_socket)
{
	// 此部分程序逻辑与_new_thread_run_在同一线程中执行。

	char *result;
	size_t res_size;
	while (1) {
		// 调用实时分配内存的函数接收结果数据包
		ssize_t ress = read_tcp_pkg(client_socket, (void *) &result, &res_size);
		log_("fn:ret_res_long_socket > ress = %ld res_size = %ld", ress, res_size);

		char tmp_arr[32];
		memset(tmp_arr, 0, sizeof(tmp_arr));
		write(client_socket, tmp_arr, sizeof(tmp_arr));

		// 启动一个新线程运行combined_result_data（结果数据包）函数
		pthread_t thd_id;
		pthread_create(&thd_id, NULL, combined_result_data, result);
		pthread_detach(thd_id);
	}
} // function ret_res_long_socket() end

