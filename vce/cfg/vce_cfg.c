#include <stdio.h>
#include <string.h>

#include "vce_cfg.h"

struct vce_cfg_stct vce_cfg_instance = {
	"vce000", "W", 8760, "~/vce_data", "~/vce_ud_", "~/tmp_vce_log_f"
};

// struct slave_host_ip_arr vce_slaves;

// struct slave_socket_stct *SLAVE_SOCKETS_HP;

static void set_vce_property(char *key, char *value);

// Program Begin
void vce_cfg_init(int c, char *v[])
{
	// vce_slaves.idx = 0;
	FILE *fp = fopen("vce-cfg", "r");

	char str[1024];
	int str_len = sizeof(str);

	char *p_value;
	while (!feof(fp)) {
		memset(str, 0, str_len);
		fgets(str, str_len, fp);

		if (str[0] == 0)
			continue;

		if (!((str[0] >= 'a' && str[0] <= 'z') || (str[0] >= 'A' && str[0] <= 'Z')))
			continue;

		p_value = strchr(str, '\n');
		if (p_value != NULL)
			*p_value = 0;

		p_value = strchr(str, '=');
		if (p_value == NULL) {
			printf("// temp > Invalid configuration [%s]\n", str);
			continue;
		}

		*p_value = 0;
		p_value++;

		set_vce_property(str, p_value);
	}
	fclose(fp);
}

static void set_vce_property(char *key, char *value)
{
	printf("// temp > set [%s] = [%s]\n", key, value);

	/*
	if (strcmp(key, "run-mode") == 0) {
		if (*value != 'A' && *value != 'M' && *value != 'S')
			goto bad_cfg;
		vce_cfg_instance.run_mode = *value;
		return;
	}
	*/

	if (strcmp(key, "svr-port") == 0) {
		int port = atoi(value);
		if (port <= 0)
			goto bad_cfg;
		vce_cfg_instance.svr_port = port;
		return;
	}

	/*
	if (strcmp(key, "max-clients-num") == 0) {
		int num = atoi(value);
		if (num <= 0)
			goto bad_cfg;
		vce_cfg_instance.max_clients_num = num;
		return;
	}
	*/

	/*
	if (strcmp(key, "regis-port") == 0) {
		int port = atoi(value);
		if (port <= 0)
			goto bad_cfg;
		vce_cfg_instance.regis_port = port;
		return;
	}
	*/

	if (strcmp(key, "name") == 0) {
		memset(vce_cfg_instance.name, 0, sizeof(vce_cfg_instance.name));
		strcpy(vce_cfg_instance.name, value);
		return;
	}

	if (strcmp(key, "mode") == 0) {
		memset(vce_cfg_instance.mode, 0, sizeof(vce_cfg_instance.mode));
		strcpy(vce_cfg_instance.mode, value);
		return;
	}

	if (strcmp(key, "data-dir") == 0) {
		memset(vce_cfg_instance.data_dir, 0, sizeof(vce_cfg_instance.data_dir));
		strcpy(vce_cfg_instance.data_dir, value);
		return;
	}

	if (strcmp(key, "cube-data-ud-dir") == 0) {
		memset(vce_cfg_instance.data_UD_dir, 0, sizeof(vce_cfg_instance.data_UD_dir));
		strcpy(vce_cfg_instance.data_UD_dir, value);
		return;
	}

	if (strcmp(key, "log-file") == 0) {
		memset(vce_cfg_instance.log_file, 0, sizeof(vce_cfg_instance.log_file));
		strcpy(vce_cfg_instance.log_file, value);
		return;
	}

	/*
	if (strcmp(key, "master-host") == 0) {
		memset(vce_cfg_instance.master_host, 0, sizeof(vce_cfg_instance.master_host));
		strcpy(vce_cfg_instance.master_host, value);
		return;
	}
	*/

	/*
	if (strcmp(key, "slave-hosts") == 0) {
		char *slave_ip = strsep(&value, ";");
		while (slave_ip != NULL) {
			memset(vce_slaves.ip_list[vce_slaves.idx], 0, sizeof(vce_slaves.ip_list[vce_slaves.idx]));
			strcpy(vce_slaves.ip_list[vce_slaves.idx], slave_ip);
			vce_slaves.idx++;
			slave_ip = strsep(&value, ";");
		}
		return;
	}
	*/

	bad_cfg:
	printf("Incomprehensible configuration [%s=%s]\n", key, value);

}

