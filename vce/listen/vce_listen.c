#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <pthread.h>

#include "vce_listen.h"
#include "vce_cfg.h"
#include "vce_common.h"
#include "cube_mng.h"
#include "mea_update.h"
#include "agg_calcul.h"
#include "node_master.h"
#include "vce_map_reduce.h"

extern struct vce_cfg_stct vce_cfg_instance;

static void *listening_master(void *addr);
static void *hanld_master(void *addr);

void open_listen_for_master()
{
	pthread_t id;
	pthread_create(&id, NULL, listening_master, NULL);
	pthread_detach(id);
}

static void *listening_master(void *addr)
{
	int svr_socket = open_svr_socket(vce_cfg_instance.svr_port);
	while (1) {
		int client_socket = wait_client(svr_socket);

		int *cli_socket_addr = (int *) malloc(sizeof(int));
		*cli_socket_addr = client_socket;
		// *socket_arr = svr_socket;
		// socket_arr[1] = client_socket;

		pthread_t id;
		pthread_create(&id, NULL, hanld_master, cli_socket_addr);
		pthread_detach(id);
	}
	close(svr_socket);
}

int wait_client(int svr_socket)
{
	struct sockaddr_in cliaddr;
	int addrlen = sizeof(cliaddr);
	return accept(svr_socket, (struct sockaddr *)&cliaddr, &addrlen);
}

static void *hanld_master(void *addr)
{
	int size_of_int = sizeof(int);
	int client_socket = *((int *) addr);
	free(addr);

	char buf[1024 * 1024 * 4]; // 4M
	size_t buf_size = sizeof(buf);
	while(1)
	{
		// int ret = read(client_socket, buf, buf_size);
		ssize_t ret = read_data_pkg(client_socket, buf, buf_size);
		logD("// temp > return value is %d\n", ret);
		if (ret <= 0) {
			log_("read [ %d ] bytes, bad client socket!\n", ret);
			goto master_cli_exit;
		}

		int request_code = ((int *) buf)[1];
		char wellcome[] = "wellcome to vce ^_^ o_o";
		int response_code;

		struct aggcal_quyres_stct ag_qu_st; // for agg query (code 9)
		char quy_result[1024 * 1024]; // for agg query (code 9)

		switch (request_code) {
			case REQ_CLI_EXIT :
				write(client_socket, "bye", strlen("bye"));
				logD("// temp > ----------------------------------> a client exit.\n");
				goto master_cli_exit;
				// break;
			case REQ_CLI_TEST :
				write(client_socket, wellcome, sizeof(wellcome));
				break;
			case REQ_SYNC_CUBE :
				response_code = build_cube(buf + size_of_int, ret - size_of_int);
				write(client_socket, &response_code, sizeof(response_code));
				break;
			case REQ_REBUI_CUBEDATAFILE_FROMMEM :
				response_code = mem_writeback_cubedf(((unsigned int *) buf)[2]);
				write(client_socket, &response_code, sizeof(response_code));
				break;
			case REQ_VCE_SHUTDOWN :
				break;
			case REQ_INSERT_MEASURE :
				logD("// temp > insert cube measures. [ %d bytes ]\n", ret);
				response_code = insert_measure_values(buf + size_of_int, ret - size_of_int);
				write(client_socket, "insert cube measures", strlen("insert cube measures"));
				break;
			case REQ_UPDATE_MEASURE :
				response_code = update_measure_values(buf + size_of_int, ret - size_of_int);
				write(client_socket, "update cube measures", strlen("update cube measures"));
				break;
			case REQ_DELETE_MEASURE :
				response_code = del_measure_values(buf + size_of_int, ret - size_of_int);
				write(client_socket, "delete cube measures", strlen("delete cube measures"));
				break;
			case REQ_AGG_CAL_QUERY :
				if (strcmp(vce_cfg_instance.mode, "M") == 0) { // master
					void *reduce_buf;
					size_t reduce_size;
					response_code = agg_map_reduce(NULL, buf, ret, &reduce_buf, &reduce_size);
					write(client_socket, reduce_buf, reduce_size);
				} else {
					response_code = agg_calcul_query(buf + size_of_int, ret - size_of_int, quy_result, &ag_qu_st);
					logD("// temp > agg calculation response code = [ %d ]\n", ag_qu_st.total_num_of_valid_bytes);
					write(client_socket, quy_result, ag_qu_st.total_num_of_valid_bytes);
				}
				break;
			case REQ_REBUI_CUBE_DATA_MEM :
				rebuild_cube_mem_data_struct(((unsigned int *) buf)[2]);
				response_code = 0;
				write(client_socket, &response_code, sizeof(response_code));
				break;
			case REQ_MASTER_LINK_WORKER :
				if (strcmp(vce_cfg_instance.mode, "M") == 0) { // master
					char *worker_ip = buf + size_of_int * 2;
					int *worker_port_p = (int *) (worker_ip + 32);
					response_code = master_link_worker(NULL, worker_ip, *worker_port_p);
				} else { // not master
					response_code = 0;
				}
				write(client_socket, &response_code, sizeof(response_code));
				break;
			default :
				log_("Illegal request code [%d]\n", request_code);
				char illInfo[] = "Illegal request code";
				write(client_socket, illInfo, sizeof(illInfo));
		}
	}
	master_cli_exit:
	logD("// temp > client quit.\n");
	close(client_socket);
}
