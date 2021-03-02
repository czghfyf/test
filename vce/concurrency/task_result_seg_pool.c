// file: task_result_seg_pool.c

#include "task_result_seg_pool.h"
#include "cube_mng.h"
#include "socket_thread_pool.h"

static struct vce_task *head;
static struct vce_task *tail;
static pthread_mutex_t sync_mtx;

static void merge_task_sub_pkgs(struct vce_task *v_task);

void task_reseg_pool_init()
{
	head = tail = NULL;
	pthread_mutex_init(&sync_mtx, NULL);
} // fn task_reseg_pool_init end

void put_vtask_seg(struct vce_task *vt)
{
	pthread_mutex_lock(&sync_mtx);
	if (head == NULL && tail == NULL) {
		head = tail = vt;
	} else {
		vt -> prev = tail;
		tail -> next = vt;
		tail = vt;
	}
	pthread_mutex_unlock(&sync_mtx);
} // fn put_vtask_seg end

void *combined_result_data(void *result_pkg) // 当前节点必为master，但不一定是root
{

	pthread_mutex_lock(&sync_mtx);

	long rand = *((long *) (result_pkg + sizeof(int) * 2));

	struct vce_task *vt = head, *v_task = NULL;
	while (vt != NULL) {
		if (vt -> rand_code != rand) {
			vt = vt -> next;
			continue;
		}
		pthread_mutex_lock(&(vt -> sync_mutex));

		struct simple_link_node *sln = malloc(sizeof(struct simple_link_node));
		memset(sln, 0, sizeof(struct simple_link_node));

		sln -> p = result_pkg;

		if (vt -> sub_rs_pkg_chain_head == NULL) {
			vt -> sub_rs_pkg_chain_head = vt -> sub_rs_pkg_chain_tail = sln;
		} else {
			sln -> prev = vt -> sub_rs_pkg_chain_tail;
			vt -> sub_rs_pkg_chain_tail = vt -> sub_rs_pkg_chain_tail -> next = sln;
		}

		if (dis_ts_count() != ++(vt -> sub_rs_pkg)) {
			pthread_mutex_unlock(&(vt -> sync_mutex));
			break;
		}

		v_task = vt;
		if (head == tail) {
			head = tail = NULL;
		} else if (vt -> prev != NULL && vt -> next != NULL) {
			vt -> prev -> next = vt -> next;
			vt -> next -> prev = vt -> prev;
		} else if (vt -> prev == NULL && vt -> next != NULL) {
			head = vt -> next;
			head -> prev = NULL;
		} else if (vt -> prev != NULL && vt -> next == NULL) {
			tail = vt -> prev;
			tail -> next = NULL;
		} else {
			logE("combined_result_data() logic error, system exit");
			exit(-1);
		}
		vt -> next = vt -> prev = NULL;
		pthread_mutex_unlock(&(vt -> sync_mutex));
		break;
	}
	pthread_mutex_unlock(&sync_mtx);

	if (v_task == NULL)
		return;

	merge_task_sub_pkgs(v_task);

	struct vce_socket_thread *vst = v_task -> v_skt_th_p;
	if (vst -> direction == INWARD && vst -> purpose == REAL_TIME) { // 实时短连接
		sem_post(&(vst -> switch_sem));
	} else if (vst -> direction == INWARD && vst -> purpose == TASK_DISTRIB) { // 当前任务来源于上级master下发
		vst_add_task(tm_st, v_task);
		sem_post(&(tm_st -> switch_sem));
	} else {
		logE("combined_result_data() serious error");
		exit(-1);
	}
} // combined_result_data() end

static void merge_task_sub_pkgs(struct vce_task *v_task)
{
	unsigned int cube_id = *((unsigned int *) (v_task -> task_data_pkg + 8));
	struct cube_info *ci = find_cube_info_by_mg_id(cube_id);

	int pkg_h_info_len = sizeof(int) * 2 + sizeof(long);

	int span = sizeof(char) + sizeof(double);

	// 将各个子结果数据包合并成整体结果数据包

	int mea_vals_c = ((int *) (v_task -> task_data_pkg))[3] * ci -> mea_mbrs_qtty;

	struct simple_link_node *h = v_task -> sub_rs_pkg_chain_head;
	struct simple_link_node *h_next = h -> next;
	while (h_next != NULL) {
		char *h_null_f, *n_null_f;
		double *h_mv_p, *n_mv_p;
		h_null_f = (char *) ((h -> p) + pkg_h_info_len);
		h_mv_p = (double *) (h_null_f + sizeof(char));
		n_null_f = (char *) ((h_next -> p) + pkg_h_info_len);
		n_mv_p = (double *) (n_null_f + sizeof(char));
		int i;
		for (i = 0; i < mea_vals_c; i++) {
			if (*n_null_f != 0)
				goto continue_f;
			if (*h_null_f != 0) {
				*h_null_f = *n_null_f;
				*h_mv_p = *n_mv_p;
			} else {
				*h_mv_p += *n_mv_p;
			}
			continue_f:
			h_null_f = (char *) (((void *) h_null_f) + span);
			h_mv_p = (double *) (((void *) h_mv_p) + span);
			n_null_f = (char *) (((void *) n_null_f) + span);
			n_mv_p = (double *) (((void *) n_mv_p) + span);
		}
		h_next = h_next -> next;
	}
	// vce_task实例引用整体结果数据包
	v_task -> result_pkg = h -> p;
	// 释放所有子数据包内存以及相关struct simple_link_node实例内存
	while (h != NULL) {
		struct simple_link_node *tmp = h;
		h = h -> next;
		if (tmp != v_task -> sub_rs_pkg_chain_head)
			free(tmp -> p);
		free(tmp);
	}
	v_task -> sub_rs_pkg_chain_head = v_task -> sub_rs_pkg_chain_tail = NULL;

} // merge_task_sub_pkgs() end

