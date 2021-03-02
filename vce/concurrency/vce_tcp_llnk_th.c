// #include <arpa/inet.h>
// #include <string.h>
// #include <stdio.h>
// #include <stdarg.h>
// #include <time.h>

// #include "vce_common.h"
// #include "vce_log.h"
// #include "vce_cfg.h"
#include "vce_tcp_llnk_th.h"

static struct L_TCP_thread_info *DOWN__h, *DOWN__t, *UP__h, *UP__t;

static pthread_mutex_t down__mutex, up__mutex;

static void _match_link_(int ltcp_thread_type, pthread_mutex_t **tmp_mtx, struct L_TCP_thread_info ***h, struct L_TCP_thread_info ***t);

void vce_tcpllnk_thmng__init()
{
	DOWN__h = NULL;
	DOWN__t = NULL;
	UP__h = NULL;
	UP__t = NULL;
	pthread_mutex_init(&down__mutex, NULL);
	pthread_mutex_init(&up__mutex, NULL);
} // fn vce_tcpllnk_thmng__init end

void rmv__tcp_thread_info(struct L_TCP_thread_info *p)
{
	pthread_mutex_t *tmp_mtx;
	struct L_TCP_thread_info **hh, **tt;
	_match_link_(p -> type, &tmp_mtx, &hh, &tt);

	pthread_mutex_lock(tmp_mtx);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", tmp_mtx);

	struct L_TCP_thread_info *node = *hh;
	while (node != NULL) {
		if (p != node) {
			node = node -> next;
			continue;
		}
		if (node == *hh && node == *tt) {
			*hh = *tt = NULL;
		} else if (node == *hh && node != *tt) {
			*hh = node -> next;
			(*hh) -> prev = NULL;
		} else if (node != *hh && node == *tt) {
			*tt = node -> prev;
			(*tt) -> next = NULL;
		} else { // node != *hh && node != *tt
			node -> prev -> next = node -> next;
			node -> next -> prev = node -> prev;
		}
	}

	pthread_mutex_unlock(tmp_mtx);
logD(")))(((***--- pthread_mutex_unlock(%p)", tmp_mtx);
} // fn rmv__tcp_thread_info end

void put__tcp_thread_info(struct L_TCP_thread_info *p)
{
	pthread_mutex_t *tmp_mtx;
	struct L_TCP_thread_info **hh, **tt;
	_match_link_(p -> type, &tmp_mtx, &hh, &tt);

	pthread_mutex_lock(tmp_mtx);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", tmp_mtx);

	if (*hh == NULL) {
		*hh = *tt = p;
	} else {
		p -> prev = *tt;
		*tt = (*tt) -> next = p;
	}

	pthread_mutex_unlock(tmp_mtx);
logD(")))(((***--- pthread_mutex_unlock(%p)", tmp_mtx);
} // fn put__tcp_thread_info end

// static void _match_link_(int ltcp_thread_type, pthread_mutex_t **tmp_mtx, struct L_TCP_thread_info **h, struct L_TCP_thread_info **t)
static void _match_link_(int ltcp_thread_type, pthread_mutex_t **tmp_mtx, struct L_TCP_thread_info ***h, struct L_TCP_thread_info ***t)
{
	if (ltcp_thread_type == RC_MST_SUBH_LLINK) {
		*tmp_mtx = &down__mutex;
		*h = &DOWN__h;
		*t = &DOWN__t;
	} else {
		*tmp_mtx = &up__mutex;
		*h = &UP__h;
		*t = &UP__t;
	}
} // fn _match_link_ end

void distr_tasks_subnodes(void *tsk_data_pkg)
{
// static struct L_TCP_thread_info *DOWN__h, *DOWN__t, *UP__h, *UP__t;
// static pthread_mutex_t down__mutex, up__mutex;

	pthread_mutex_lock(down__mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", down__mutex);

	struct L_TCP_thread_info *cursor;

	for (cursor = DOWN__h; cursor != NULL; cursor = cursor -> next) {

		struct simple_link_node *sln = malloc(sizeof(struct simple_link_node));
		memset(sln, 0, sizeof(struct simple_link_node));

		sln -> p = tsk_data_pkg;

		pthread_mutex_lock(cursor -> link_mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", cursor -> link_mutex);

		if (cursor -> data_link_head == NULL) {
			cursor -> data_link_head = cursor -> data_link_tail = sln;
		} else {
			sln -> prev = cursor -> data_link_tail;
			cursor -> data_link_tail = cursor -> data_link_tail -> next = sln;
		}

		sem_post(&(cursor -> semt_swch));

		pthread_mutex_unlock(cursor -> link_mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", cursor -> link_mutex);
	}

	pthread_mutex_unlock(down__mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", down__mutex);

} // fn distr_tasks_subnodes end

void vst_add_task(struct vce_socket_thread *vst, struct vce_task *task)
{
	pthread_mutex_lock(&(vst -> sync_mutex));
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &(vst -> sync_mutex));
	if (vst -> task_chain_head == NULL) {
		vst -> task_chain_head = vst -> task_chain_tail = task;
	} else {
		task -> prev = vst -> task_chain_tail;
		vst -> task_chain_tail = vst -> task_chain_tail -> next = task;
	}
	pthread_mutex_unlock(&(vst -> sync_mutex));
logD(")))(((***--- pthread_mutex_unlock(%p)", &(vst -> sync_mutex));
} // vst_add_task() end
