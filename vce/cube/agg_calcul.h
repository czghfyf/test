#ifndef _VCE_AGG_CALCUL_H_
#define _VCE_AGG_CALCUL_H_ 1

#include "vce_listen.h"

#define AGG_CAL_PKGH_RESERVED_LEN 64 // reserve 64 bytes

struct aggcal_quyres_stct { // Aggregate calculation query result structure
	int total_num_of_valid_bytes;
};

struct cube_data_stct {
	unsigned int cube_mg_id;
	void *dmrole_tables[512]; // max dimension roles quantity 512
	unsigned int dmrole_mbrs_qtty_arr[512]; // record the number of dimension members in the dimension role list
	void *measures_table;
	long long dmroles_offset[512]; // The previous dimension role (the smaller the MG_ID), the larger the offset.
};

int agg_calcul_query(void *req_data, int req_data_len, void *quy_result, struct aggcal_quyres_stct *aqs);

void * agg_calcul_query_(void *task_data_pkg);

void free_mem_cubedatastct(struct cube_data_stct *cds);

#endif
