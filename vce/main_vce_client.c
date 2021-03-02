#include <stdio.h>
#include <stdlib.h>
#include <string.h>
// #include <sys/socket.h>
#include <arpa/inet.h>

#define INPUT_BUF_S (1024 * 8)
#define CMD_ARR_LEN 128

char conning_vnode_ip[16];
int conning_vnode_port = -1;
struct sockaddr_in sckt_addr;
int sckt_id = -1;

void conn(char **cmd_arr_p);
void link(char **cmd_arr_p);

int main(int argc, char *argv[])
{
	// sckt_id = -1;

	printf("VCE client\n");

	char *buf = malloc(INPUT_BUF_S);
	char *cmd_arr[CMD_ARR_LEN];

	while (1) {
		memset(buf, 0, INPUT_BUF_S);
		memset(cmd_arr, 0, sizeof(cmd_arr));

		printf("> ");
		scanf("%[^\n]", buf);
		buf[INPUT_BUF_S - 1] = 0;
		scanf("%*c");

		int buf_i, arr_i = 0;
		for (buf_i = 0; buf_i < INPUT_BUF_S - 1; buf_i++) {
			if (buf[buf_i] == ' ' || buf[buf_i] == '\t')
				buf[buf_i] = 0;

			if (buf[buf_i] != 0 && (buf_i == 0 || buf[buf_i - 1] == 0))
				cmd_arr[arr_i++] = &(buf[buf_i]);
		}

		/*
		for (arr_i = 0; arr_i < CMD_ARR_LEN; arr_i++) {
			if (cmd_arr[arr_i] == NULL)
				break;
			printf("%s\n", cmd_arr[arr_i]);
		}
		*/

		if (cmd_arr[0] == NULL)
			continue;

		if (strcmp(cmd_arr[0], "exit") == 0) {
			printf("bye\n");
			break;
		} else if (strcmp(cmd_arr[0], "conn") == 0) {
			conn((char **) cmd_arr);
		} else if (strcmp(cmd_arr[0], "link") == 0) {
			link((char **) cmd_arr);
		} else {
			printf("请输入有效的命令\n");
		}

	}

	free(buf);

	return 0;
} // fn main end

void link(char **cmd_arr_p) // link [down|up] 127.0.0.1 8760
{
	if (cmd_arr_p[3] == NULL) {
		printf("缺失参数\n");
		return;
	}

	char *direction = cmd_arr_p[1], *ip = cmd_arr_p[2], *port_str = cmd_arr_p[3];

	if (strcmp(direction, "down") != 0 && strcmp(direction, "up") != 0) {
		printf("不能被识别的连接方向 %s\n", direction);
		return;
	}

	if (strlen(ip) > 15) {
		printf("错误的IP %s\n", ip);
		return;
	}

	int port = atoi(port_str);
	if (port == 0) {
		printf("错误的端口号 %s\n", port_str);
		return;
	}

	char req_pkg[28];
	memset(req_pkg, 0, 28);
	void *p = req_pkg;
	*((int *) p) = 28;
	((int *) p)[1] = strcmp(direction, "down") == 0 ? 21 : 22;
	strcpy(p + 8, ip);
	*((int *) (p + 24)) = port;

	write(sckt_id, p, 28);

	char tmp_buf[128];
	memset(tmp_buf, 0, sizeof(tmp_buf));

    int ret = read(sckt_id, tmp_buf, sizeof(tmp_buf));
	if (ret < 1)
		printf("连接异常！%d\n", ret);

	printf("连接设置完毕\n");
} // fn link end

void conn(char **cmd_arr_p) // conn 127.0.0.1 8760
{
	if (cmd_arr_p[2] == NULL) {
		printf("缺失参数\n");
		return;
	}

	if (strlen(cmd_arr_p[1]) > 15) {
		printf("错误的IP %s\n", cmd_arr_p[1]);
		return;
	}

	int port = atoi(cmd_arr_p[2]);
	if (port == 0) {
		printf("错误的端口号 %s\n", cmd_arr_p[2]);
		return;
	}

	printf("尝试连接 %s:%d\n", cmd_arr_p[1], port);

	if (sckt_id > 0) {
		printf("断开连接 %s:%d\n", conning_vnode_ip, conning_vnode_port);
		close(sckt_id);
	}
	
	memset(conning_vnode_ip, 0, sizeof(conning_vnode_ip));
	strcpy(conning_vnode_ip, cmd_arr_p[1]);
	conning_vnode_port = port;

	sckt_id = socket(AF_INET, SOCK_STREAM, 0);
    if(sckt_id == -1) {
		printf("创建套接字失败\n");
		return;
    }   
    memset(&sckt_addr, 0, sizeof(sckt_addr));
    
    sckt_addr.sin_family = AF_INET;
    sckt_addr.sin_port = htons(conning_vnode_port);
    sckt_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    inet_aton(conning_vnode_ip, &(sckt_addr.sin_addr));
 
	printf("正在连接服务器...\n");
	if (connect(sckt_id,  (struct sockaddr *) &sckt_addr, sizeof(sckt_addr)) != 0) {
		printf("连接失败\n");
		conning_vnode_port = -1;
		close(sckt_id);
		sckt_id = -1;
		return;
	}

	int sktype_inf[] = {12, 20, 2001};
	write(sckt_id, sktype_inf, sizeof(sktype_inf));
	char tmp_buf[128];
	memset(tmp_buf, 0, sizeof(tmp_buf));
    int ret = read(sckt_id, tmp_buf, sizeof(tmp_buf));
	if (ret < 1)
		printf("连接异常！%d\n", ret);

    printf("成功连接到一个服务器\n", ret);

} // fn conn end

