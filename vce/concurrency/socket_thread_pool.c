// file: socket_thread_pool.c

// #include <arpa/inet.h>
// #include <stdlib.h>
// #include <string.h>
// #include <semaphore.h>

// #include "vce_listen.h"
// #include "vce_common.h"

#include "socket_thread_pool.h"

// static struct vce_socket_thread *fm_st; // 接收来自上级master的任务分发

struct vce_socket_thread *tm_st; // 向上级master提交任务结果
static pthread_mutex_t tm_st__mutex;

// 向下级节点分发任务（实例池）
static struct vce_socket_thread *dis_ts_head;
static struct vce_socket_thread *dis_ts_tail;
static pthread_mutex_t dis_ts__Mutex;

// 接收下级节点返回的任务结果（实例池）
// static struct vce_socket_thread *rec_ts_head;
// static struct vce_socket_thread *rec_ts_tail;

void put_vst_dits(struct vce_socket_thread *vst)
{
	if (vst == NULL)
		return;

	pthread_mutex_lock(&dis_ts__Mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &dis_ts__Mutex);

	if (dis_ts_head == NULL) {
		dis_ts_head = dis_ts_tail = vst;
	} else {
		vst -> prev = dis_ts_tail;
		dis_ts_tail = dis_ts_tail -> next = vst;
	}

	pthread_mutex_unlock(&dis_ts__Mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &dis_ts__Mutex);
} // fn put_vst_dits end

void ready_dis_task(struct vce_task *vt)
{
	struct vce_socket_thread *vst = dis_ts_head;

	pthread_mutex_lock(&dis_ts__Mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &dis_ts__Mutex);

	for (; vst != NULL; vst = vst -> next) {
		pthread_mutex_lock(&(vst -> sync_mutex));
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &(vst -> sync_mutex));
		if (vst -> task_chain_head == NULL) {
			vst -> task_chain_head = vst -> task_chain_tail = vt;
		} else {
			vt -> prev = vst -> task_chain_tail;
			vst -> task_chain_head = vst -> task_chain_tail -> next = vt;
		}
		pthread_mutex_unlock(&(vst -> sync_mutex));
logD(")))(((***--- pthread_mutex_unlock(%p)", &(vst -> sync_mutex));
		sem_post(&(vst -> switch_sem));
	}

	pthread_mutex_unlock(&dis_ts__Mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &dis_ts__Mutex);

} // ready_dis_task() end

void submit_vce_task(struct vce_task *vt)
{
	pthread_mutex_lock(&tm_st__mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &tm_st__mutex);
	if (tm_st -> task_chain_head == NULL) {
		tm_st -> task_chain_head = tm_st -> task_chain_tail = vt;
	} else {
		vt -> prev = tm_st -> task_chain_tail;
		tm_st -> task_chain_head = tm_st -> task_chain_tail = vt;
	}
	pthread_mutex_unlock(&tm_st__mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &tm_st__mutex);
	sem_post(&(tm_st -> switch_sem));
} // submit_vce_task() end

/*
void vst_dits_pool_lock()
{
	pthread_mutex_lock(&dis_ts__Mutex);
} // fn vst_dits_pool_lock end

void vst_dits_pool_unlock()
{
	pthread_mutex_unlock(&dis_ts__Mutex);
} // fn vst_dits_pool_unlock end
*/

void stp_init()
{
	// fm_st = tm_st = dis_ts_head = dis_ts_tail = rec_ts_head = rec_ts_tail = NULL;
	tm_st = NULL;
	dis_ts_head = NULL;
	dis_ts_tail = NULL;

	pthread_mutex_init(&tm_st__mutex, NULL);
	pthread_mutex_init(&dis_ts__Mutex, NULL);
} // fn stp_init end

int dis_ts_count()
{
	pthread_mutex_lock(&dis_ts__Mutex);
logD("@@@###$$$>>> pthread_mutex_lock(%p)", &dis_ts__Mutex);
	struct vce_socket_thread *tmp = dis_ts_head;
	int c = 0;
	while (tmp != NULL) {
		c++;
		tmp = tmp -> next;
	}	
	pthread_mutex_unlock(&dis_ts__Mutex);
logD(")))(((***--- pthread_mutex_unlock(%p)", &dis_ts__Mutex);
	return c;
} // dis_ts_count() end

