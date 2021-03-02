#ifndef _VCE_NODE_MASTER_H_
#define _VCE_NODE_MASTER_H_ 1

#include <pthread.h>
#include <semaphore.h>
#include <arpa/inet.h>

// 4194304 bytes = 4 M
#define WORK_LINK_BUF_SIZE 4194304

struct work_link_thread_info {

	char wk_ip[32]; // worker ip
	int wk_port; // worker port

	pthread_t thread_id;
	sem_t sem;

	int client_socket;
	struct sockaddr_in addr;
	int listen_socket;

	char *req_buf;
	size_t req_buf_len;

	char resp_buf[WORK_LINK_BUF_SIZE];
	size_t r_size;

	char completed_f;

	struct work_link_thread_info *next;
};

int master_link_worker(void *params, char *worker_ip, int worker_port);

#endif
