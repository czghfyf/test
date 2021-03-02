#ifndef _VCE_CFG_H_
#define _VCE_CFG_H_ 1

struct vce_cfg_stct {
	char name[64];
	char mode[4];
	int svr_port;
	char data_dir[256];
	char data_UD_dir[256];
	char log_file[256];
};

extern struct vce_cfg_stct vce_cfg_instance;

void vce_cfg_init(int c, char *v[]);

#endif
