#ifndef _VCE_TCP_LONG_LINK_THREAD_

#define _VCE_TCP_LONG_LINK_THREAD_ 1

#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <semaphore.h>

#include "vce_listen.h"
#include "vce_common.h"
#include "vce_enum_const.h"

struct vce_socket_thread {
	enum conn_direction direction;
	enum conn_purpose purpose;
	pthread_t thd_id;
	int socket_id;
	char remote_host_ip[32];
	int remote_host_port;
	sem_t switch_sem;
	pthread_mutex_t sync_mutex;
	struct vce_socket_thread *prev;
	struct vce_socket_thread *next;
	struct vce_task *task_chain_head;
	struct vce_task *task_chain_tail;
	// struct simple_link_node *pkgs_chain_head;
	// struct simple_link_node *pkgs_chain_tail;
};

void vst_add_task(struct vce_socket_thread *vst, struct vce_task *task);

struct vce_task {
	long rand_code;
	struct vce_socket_thread *v_skt_th_p;
	void *task_data_pkg;
	void *result_pkg;

	int sub_rs_pkg;
	struct simple_link_node *sub_rs_pkg_chain_head;
	struct simple_link_node *sub_rs_pkg_chain_tail;

	pthread_mutex_t sync_mutex;
	struct vce_task *prev;
	struct vce_task *next;
};

struct L_TCP_thread_info {

	      int  socket_id;
	pthread_t  thread_id;
		 char  desc[32];
		  int  type; // [ RC_MST_SUBH_LLINK | RC_SUBH_MST_LLINK ]

	sem_t semt_swch;

	struct simple_link_node *data_link_head;
	struct simple_link_node *data_link_tail;
	pthread_mutex_t link_mutex;

	struct L_TCP_thread_info *prev;
	struct L_TCP_thread_info *next;
};

void vce_tcpllnk_thmng__init();

void put__tcp_thread_info(struct L_TCP_thread_info *p);
void rmv__tcp_thread_info(struct L_TCP_thread_info *p);

void distr_tasks_subnodes(void *tsk_data_pkg);

#endif
