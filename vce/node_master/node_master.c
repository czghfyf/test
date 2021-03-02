#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "node_master.h"

struct work_link_thread_info *WLL_HEAD = NULL;

static int _link_worker_(void *params, struct work_link_thread_info *thread_info);

static void *wk_thread_core(void *addr);

int master_link_worker(void *params, char *worker_ip, int worker_port)
{
	logD("// temp > link to worker [ %s ] [ %d ]\n", worker_ip, worker_port);

	struct work_link_thread_info *w_lnk, *lnk_last;
	for (w_lnk = WLL_HEAD; w_lnk != NULL; w_lnk = w_lnk -> next) {
		lnk_last = w_lnk;
		if ((strcmp(worker_ip, w_lnk -> wk_ip) == 0) && (worker_port == w_lnk -> wk_port)) {
			printf("worker [ %s ] [ %d ] already connected\n", worker_ip, worker_port);
			return 0;
		}
	}

	w_lnk = (struct work_link_thread_info *) malloc(sizeof(struct work_link_thread_info));
	memset(w_lnk -> wk_ip, 0, sizeof(w_lnk -> wk_ip));

	strcpy(w_lnk -> wk_ip, worker_ip);
	w_lnk -> wk_port = worker_port;

	int ret = _link_worker_(NULL, w_lnk);
	if (ret == 1) {

		if (WLL_HEAD == NULL)
			WLL_HEAD = w_lnk;
		else
			lnk_last -> next = w_lnk;

		return 1;
	} else {
		free(w_lnk);
		return 0;
	}
}

static int _link_worker_(void *params, struct work_link_thread_info *thread_info)
{
	thread_info -> client_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (thread_info -> client_socket == -1) {
        logD("// temp > create socket error.\n");
        return -1;
    }

	// struct sockaddr_in addr;
    memset(&(thread_info -> addr), 0, sizeof(thread_info -> addr));

    thread_info -> addr.sin_family = AF_INET;
    thread_info -> addr.sin_port = htons(thread_info -> wk_port);
    thread_info -> addr.sin_addr.s_addr = htonl(INADDR_ANY);
    inet_aton(thread_info -> wk_ip, &(thread_info -> addr.sin_addr));

    // int addrlen = sizeof(addr);
    thread_info -> listen_socket = connect(thread_info -> client_socket,
		(struct sockaddr *) &(thread_info -> addr), sizeof(thread_info -> addr));

    if(thread_info -> listen_socket == -1) {
        // perror("connect");
        logD("// temp > connect to worker error.\n");
        return -1;
    }

    logD("// temp > connect worker SUCCESS.\n");

	int ret = sem_init(&(thread_info -> sem), 0, 0);
	if (ret) {
		logD("// temp > init sem_t error.\n");
		return -1;
	}

	pthread_create(&(thread_info -> thread_id), NULL, wk_thread_core, thread_info);
	pthread_detach(thread_info -> thread_id);

	return 1;
}

static void *wk_thread_core(void *addr)
{
	struct work_link_thread_info *ti = (struct work_link_thread_info *) addr;

	while (1) {
		sem_wait(&ti -> sem);
		write(ti -> client_socket, ti -> req_buf, ti -> req_buf_len);
		ti -> r_size = read(ti -> client_socket, ti -> resp_buf, WORK_LINK_BUF_SIZE);
		ti -> completed_f = 1;
	}
	close(ti -> listen_socket);
	return NULL;
}
