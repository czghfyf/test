// file: tasks_manager.c

#include <arpa/inet.h>
#include <string.h>
// #include <stdio.h>
#include <stdlib.h>
#include <semaphore.h>
// #include <stdarg.h>
// #include <time.h>

#include "vce_common.h"
// #include "vce_log.h"
#include "vce_cfg.h"
#include "tasks_manager.h"
#include "vce_listen.h"
#include "agg_calcul.h"
#include "vce_tcp_llnk_th.h"

extern struct vce_cfg_stct vce_cfg_instance;

static struct ccy_task_node *ccy_ts_head;
static struct ccy_task_node *ccy_ts_tail;
static pthread_mutex_t ccyts_syncmtx;

// static void planning_task();

// static void *task_assignment_starting(void *addr);
// static pthread_mutex_t taass_syncmtx;
static sem_t taass_switch_sem;


static void *task_results_agg_mng_thread(void *p);
static struct trs_agg_w__node *task_rsagg_link_HEAD, *task_rsagg_link_TAIL;
static sem_t trs_agg_switch_sem;
static pthread_mutex_t trs_agg_mutex;
static void add__trs_agg_w__node(struct trs_agg_w__node *p);


// static void *running_thread_task(void *addr);

// static void *exe_concurrent_task(void *vp);

// static void *master_sub_tsk_distr(void *vp);

static void *sub_master_long_lnk(void *vp);
static void *master_sub_long_lnk(void *vp);

static void long_tcplnk_thread_loop(int long_tcplnk_type, char *remote_svr_ip, int remote_svr_port);

void tasks_mng__init()
{
	ccy_ts_head = NULL;
	ccy_ts_tail = NULL;
	pthread_mutex_init(&ccyts_syncmtx, NULL);
	pthread_mutex_init(&trs_agg_mutex, NULL);

	// pthread_mutex_init(&taass_syncmtx, NULL);
	int tmp_i = sem_init(&taass_switch_sem, 0, 0);
	if (tmp_i != 0) {
		logE("fn:tasks_mng__init > init taass_switch_sem [return %d]. failed! program exit!", tmp_i);
		exit(1);
	}

	tmp_i = sem_init(&trs_agg_switch_sem, 0, 0);
	if (tmp_i != 0) {
		logE("fn:tasks_mng__init > init trs_agg_switch_sem [return %d]. failed! program exit!", tmp_i);
		exit(1);
	}

	// task assignment
	/*
	pthread_t ta_pt_id;
	pthread_create(&ta_pt_id, NULL, task_assignment_starting, NULL);
	pthread_detach(ta_pt_id);
	*/

	// task_results_agg_mng_thread
	pthread_t trs_agg_thid;
	pthread_create(&trs_agg_thid, NULL, task_results_agg_mng_thread, NULL);
	pthread_detach(trs_agg_thid);


	task_rsagg_link_HEAD = NULL;
	task_rsagg_link_TAIL = NULL;
} // fn tasks_mng__init end
/*
static void *task_assignment_starting(void *addr)
{
	struct ccy_task_node *tmp_ctn;
	while (1) {
		sem_wait(&taass_switch_sem);
		pthread_mutex_lock(&ccyts_syncmtx);
		if (ccy_ts_head == NULL) {
			tmp_ctn = NULL;
		} else if (ccy_ts_head == ccy_ts_tail) {
			tmp_ctn = ccy_ts_head;
			ccy_ts_head = NULL;
			ccy_ts_tail = NULL;			
		} else {
			tmp_ctn = ccy_ts_head;
			ccy_ts_head = ccy_ts_head -> next;
		}
		pthread_mutex_unlock(&ccyts_syncmtx);
		if (tmp_ctn == NULL)
			break;
		pthread_t cct_task_pth;
		pthread_create(&cct_task_pth, NULL, exe_concurrent_task, tmp_ctn);
		pthread_detach(cct_task_pth);
	}
	return NULL;
} // fn task_assignment_starting end
*/
static void *task_results_agg_mng_thread(void *p)
{
	while (1) {
		sem_wait(&trs_agg_switch_sem);
		pthread_mutex_lock(&trs_agg_mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &trs_agg_mutex);
		// todo
		pthread_mutex_unlock(&trs_agg_mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &trs_agg_mutex);
		// todo
	}
	return NULL;
} // fn task_results_agg_mng_thread end

static void add__trs_agg_w__node(struct trs_agg_w__node *p)
{
	if (p == NULL)
		return;
	// static struct trs_agg_w__node *task_rsagg_link_HEAD, *task_rsagg_link_TAIL;
	pthread_mutex_lock(&trs_agg_mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &trs_agg_mutex);
	// todo
	if (task_rsagg_link_HEAD == NULL) {
		task_rsagg_link_HEAD = p;
		task_rsagg_link_TAIL = p;
	} else  {
		task_rsagg_link_TAIL -> next = p;
		p -> prev = task_rsagg_link_TAIL;
		task_rsagg_link_TAIL = p;
	}
	pthread_mutex_unlock(&trs_agg_mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &trs_agg_mutex);
} // fn add__trs_agg_w__node end
/*
void add_task(void *params, int socket_type, void *package)
{
	struct ccy_task_node *ctn = (struct ccy_task_node *) malloc(sizeof(struct ccy_task_node));
	ctn -> socket_type = socket_type;
	ctn -> package_stct = package;

	pthread_mutex_lock(&ccyts_syncmtx);

	if (ccy_ts_tail == NULL) {
		ccy_ts_head = ctn;
		ccy_ts_tail = ctn;
	} else {
		ccy_ts_tail -> next = ctn;
		ccy_ts_tail = ctn;
	}

	pthread_mutex_unlock(&ccyts_syncmtx);

	sem_post(&taass_switch_sem);

} // fn add_task end
*/
/*
static void planning_task()
{
	sem_wait(&ccy_ts__sem);

	if (ccy_ts_head == NULL)
		goto planning_task___exit;

	// todo 判断正在运行的任务线程的类型，当前任务是否可与之并行，如果可以，启动一个新的线程执行任务，否则不执行任何逻辑
	// todo 目前先不实现判断并行线程的逻辑，所有任务均可并行
	// todo 2019年9月3日

	void *tmp_addr = ccy_ts_head;

	if (ccy_ts_tail == ccy_ts_head) {
		ccy_ts_tail = NULL;
		ccy_ts_head = NULL;
	} else {
		ccy_ts_head = ccy_ts_head -> next;
	}

	pthread_t thd_id;
	pthread_create(&thd_id, NULL, running_thread_task, tmp_addr);
	pthread_detach(thd_id);

	planning_task___exit:
	sem_post(&ccy_ts__sem);
} // function planning_task end
*/














/*
static void *exe_concurrent_task(void *vp)
{
	struct ccy_task_node *ccy_tn = (struct ccy_task_node *) vp;
	int request_code, response_code;
	if (ccy_tn -> socket_type == SKT_TYPE_REAL_TIME) {
		struct real_time_socket__respkg *rts_pkg = (struct real_time_socket__respkg *) ccy_tn -> package_stct;
		request_code = *((int *) (rts_pkg -> buf_in + sizeof(int)));
		if (request_code == REQ_SYNC_CUBE) { // sync cube
			response_code = build_cube(rts_pkg -> buf_in + sizeof(int), rts_pkg -> data_in_len - sizeof(int));
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_INSERT_MEASURE) { // insert measures
			response_code = insert_measure_values(rts_pkg -> buf_in + sizeof(int), rts_pkg -> data_in_len - sizeof(int));
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_REBUI_CUBE_DATA_MEM) { // load cube data into memory
			rebuild_cube_mem_data_struct(((unsigned int *) rts_pkg -> buf_in)[2]);
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_UPDATE_MEASURE) { // update measures
			response_code = update_measure_values(rts_pkg -> buf_in + sizeof(int), rts_pkg -> data_in_len - sizeof(int));
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_DELETE_MEASURE) { // delete measures
			response_code = del_measure_values(rts_pkg -> buf_in + sizeof(int), rts_pkg -> data_in_len - sizeof(int));
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_REBUI_CUBEDATAFILE_FROMMEM) { // write back cube data from memory to disk
			response_code = mem_writeback_cubedf(((unsigned int *) rts_pkg -> buf_in)[2]);
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == REQ_AGG_CAL_QUERY) { // aggregate query
			if (strcmp(vce_cfg_instance.mode, "W") == 0) {
				struct aggcal_quyres_stct ag_qu_st;
				response_code = agg_calcul_query(rts_pkg -> buf_in + sizeof(int), rts_pkg -> data_in_len - sizeof(int),
												 rts_pkg -> buf_out, &ag_qu_st);
				rts_pkg -> data_out_len = ag_qu_st.total_num_of_valid_bytes;
				sem_post(&(rts_pkg -> sem));
			} else if (strcmp(vce_cfg_instance.mode, "M") == 0) {
				// 这是一个TCP短连接发起的任务
				// 需要调用add__trs_agg_w__node函数将任务信息先增加到返回结果处理集合中
				// 需要传递的信息：当前tcp短连接相关信息：数据包指针、信号量
				struct trs_agg_w__node *tragg_p = malloc(sizeof(struct trs_agg_w__node));
				log_("malloc > %p", tragg_p);
				add__trs_agg_w__node(tragg_p);

				// 遍历下行长连接处理线程
				// 将任务数据添加到每个线程的待处理链中
				distr_tasks_subnodes(rts_pkg -> buf_in);
			} else {
				logE("mode [%s] ?! program exit!", vce_cfg_instance.mode);
				exit(1);
			}
		} else if (request_code == RC_SUBH_MST_LLINK) {
			pthread_t mstd_pth_id;
			pthread_create(&mstd_pth_id, NULL, sub_master_long_lnk, rts_pkg -> buf_in);
			pthread_detach(mstd_pth_id);
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else if (request_code == RC_MST_SUBH_LLINK) {
			// char *subhost_ip = rts_pkg -> buf_in + sizeof(int) * 2;
			// int subhost_port = *((int *) (subhost_ip + 16));
			// logI("long tcp link [%s:%d] type [%d]", subhost_ip, subhost_port, request_code);
			pthread_t mstd_pth_id;
			// pthread_create(&mstd_pth_id, NULL, master_sub_tsk_distr, rts_pkg -> buf_in);
			pthread_create(&mstd_pth_id, NULL, master_sub_long_lnk, rts_pkg -> buf_in);
			pthread_detach(mstd_pth_id);
			*((int *) rts_pkg -> buf_out) = 0;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		} else {
			logW("fn:exe_concurrent_task > can not be executed. [socket type %d] [request code %d]",
				SKT_TYPE_REAL_TIME, request_code);
			// todo ERROR_UNK
			*((int *) (rts_pkg -> buf_out)) = ERROR_UNK;
			rts_pkg -> data_out_len = sizeof(int);
			sem_post(&(rts_pkg -> sem));
		}
	} else if (ccy_tn -> socket_type == SKT_TYPE_TASK_DOWN) {

	} else if (ccy_tn -> socket_type == SKT_TYPE_RET_RES) {

	} else {
		logE("Serious error(socket_type = %d)! The program is over!");
		exit(1);
	}
	return NULL;
} // fn exe_concurrent_task end
*/
















/* static void *running_thread_task(void *addr)
{
	struct ccy_task_node *task_node = (struct ccy_task_node *) addr;
	int socket_type = task_node -> socket_type;
	void *pkg = task_node -> package_stct;
	free(addr);
	if (socket_type == SKT_TYPE_REAL_TIME) {

	} else if (socket_type == SKT_TYPE_TASK_DOWN) {

	} else if (socket_type == SKT_TYPE_RET_RES) {

	} else {
		logE("running_thread_task() : wrong socket type [%d]", socket_type);
		free(pkg);
	}
	return NULL;
} */

static void long_tcplnk_thread_loop(int long_tcplnk_type, char *remote_svr_ip, int remote_svr_port)
{
	struct L_TCP_thread_info tcp_thre_info;
	tcp_thre_info.next = tcp_thre_info.prev = NULL;
	int tmp_i = sem_init(&(tcp_thre_info.semt_swch), 0, 0);
	if (tmp_i != 0) {
		logE("fn:long_tcplnk_thread_loop > init &tcp_thre_info.semt_swch [return %d]. failed!", tmp_i);
		return;
	}
	pthread_mutex_init(&(tcp_thre_info.link_mutex), NULL);

	tcp_thre_info.socket_id = create_tcp_link(remote_svr_ip, remote_svr_port);
	tcp_thre_info.type = long_tcplnk_type;

	int first_pkg[] = {12, RC_1ST, long_tcplnk_type == RC_MST_SUBH_LLINK ? SKT_TYPE_TASK_DOWN : SKT_TYPE_RET_RES };
	write(tcp_thre_info.socket_id, first_pkg, sizeof(first_pkg));
	char tmp_buf[128];
	int ret = read(tcp_thre_info.socket_id, tmp_buf, sizeof(tmp_buf));

	put__tcp_thread_info(&tcp_thre_info);
// log_(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  %d  %s  %d", long_tcplnk_type, remote_svr_ip, remote_svr_port);
	while (1) {
		struct simple_link_node *sln;
		sem_wait(&(tcp_thre_info.semt_swch));
		pthread_mutex_lock(&(tcp_thre_info.link_mutex));
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &(tcp_thre_info.link_mutex));

		if (tcp_thre_info.data_link_head == NULL)
			goto c;

		if (tcp_thre_info.data_link_head -> next == NULL) {
			sln = tcp_thre_info.data_link_head;
			tcp_thre_info.data_link_head = tcp_thre_info.data_link_tail = NULL;
		} else {
			sln = tcp_thre_info.data_link_head;
			tcp_thre_info.data_link_head = tcp_thre_info.data_link_head -> next;
			sln -> next = tcp_thre_info.data_link_head -> prev = NULL;
		}

		c:
		pthread_mutex_unlock(&(tcp_thre_info.link_mutex));
logD(")))(((***--- pthread_mutex_unlock(%p)", &(tcp_thre_info.link_mutex));

		// 通过socket将任务数据包下发至下级节点
        write(tcp_thre_info.socket_id, sln -> p, sizeof(sln -> p));
		char tmp_buf[128];
		int ret = read(tcp_thre_info.socket_id, tmp_buf, sizeof(tmp_buf));
		if (ret < 1 || ret > sizeof(tmp_buf)) {
			logE("size numbers of readed return be mistake %d", ret);
		} else {
			logD("task assignment finished");
		}
	}

	rmv__tcp_thread_info(&tcp_thre_info);

	close(tcp_thre_info.socket_id);
} // fn long_tcplnk_thread_loop end

static void *sub_master_long_lnk(void *vp)
{
	char *master_ip = vp + sizeof(int) * 2;
	int master_port = *((int *) (master_ip + 16));
	long_tcplnk_thread_loop(RC_SUBH_MST_LLINK, master_ip, master_port);
	return NULL;
} // fn sub_master_long_lnk end

static void *master_sub_long_lnk(void *vp)
{
	char *sub_ip = vp + sizeof(int) * 2;
	int sub_port = *((int *) (sub_ip + 16));
	long_tcplnk_thread_loop(RC_MST_SUBH_LLINK, sub_ip, sub_port);
	return NULL;
} // fn master_sub_long_lnk end

void receiving_result_from_sub(void *params, void *sub_node_res_data)
{

} // fn receiving_result_from_sub end

void *process_agg_task(void *p)
{
	struct vce_task *vt = (struct vce_task *) p;

	// master节点逻辑
	if (strcmp(vce_cfg_instance.mode, "M") == 0) {
		// 将当前vce_task实例添加到等待结果任务池
		put_vtask_seg(vt);

		// 向下级节点分配任务
		ready_dis_task(vt);

		return;
	}

	// worker节点逻辑
	// 调用相关方法执行本地数据聚合操作，获得结果数据包
	struct aggcal_quyres_stct ag_qu_st;
	void *agg_vvs = agg_calcul_query_(vt -> task_data_pkg);

	// vce_task实例指向结果数据包
	vt -> result_pkg = agg_vvs;

	struct vce_socket_thread *vt_vst = vt -> v_skt_th_p;
	if (vt_vst -> direction == INWARD && vt_vst -> purpose == REAL_TIME) { // 任务来源于实时短连接
		sem_post(&(vt_vst -> switch_sem));
	} else if (vt_vst -> direction == INWARD && vt_vst -> purpose == TASK_DISTRIB) { // 任务来源于上级任务分配
		// 向上级master发送任务结果
		submit_vce_task(vt);
	} else {
		logE("process_agg_task() serious error! system exit!");
		exit(1);
	}

} // fn process_agg_task end

