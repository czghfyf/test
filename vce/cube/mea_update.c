// mea_update.c

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>

#include "mea_update.h"
#include "vce_cfg.h"
#include "vce_common.h"
#include "cube_mng.h"
#include "agg_calcul.h"

static int update_measure(void *bs, size_t len, struct cube_info *ci_p);

static int delete_measure(void *bs, size_t len, struct cube_info *ci_p);

static int _update_(struct cube_info *ci_p, size_t v_index, void *new_vector);

static int _delete_(struct cube_info *ci_p, size_t v_index, void *new_vector);

int update_measure_values(void *bs, size_t len)
{
	int up_op_code = *((int *) bs);
	unsigned int cube_mg_id = ((unsigned int *) bs)[1];

	logD("// temp > update measure [ %d ] CUBE_MG_ID[ %d ]\n", up_op_code, cube_mg_id);

	// does the cube data file exist ?
	char cube_data_file[256];
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_data_file);

	if (access(cube_data_file, F_OK) != 0) {
		printf("%s does not exist.\n", cube_data_file);
		return 0;
	}

	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);

	if (ci_p -> _cube_data_stct_ == NULL) {
		printf("%s offline (update)\n", cube_data_file);
		return 0;
	}
	
	int up_res = update_measure(bs + sizeof(int) * 2, len - sizeof(int) * 2, ci_p);

	return up_res;
} // function update_measure_values() end

int del_measure_values(void *bs, size_t len)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(((unsigned int *) bs)[1]);
	if (ci_p -> _cube_data_stct_ == NULL) {
		printf("cube[%d] offline (delete)\n", ((unsigned int *) bs)[1]);
		return 0;
	}
	return delete_measure(bs + sizeof(int) * 2, len - sizeof(int) * 2, ci_p);
} // function del_measure_values() end

static int delete_measure(void *bs, size_t len, struct cube_info *ci_p)
{
	/* if (len % ci_p -> data_vector_len != 0) {
		logD("// temp > ERROR! cube[ %d ] data file broken.\n", ci_p -> cube_mg_id);
		return -1;
	} */

	unsigned int _del_line_size_ = ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * sizeof(double);
	unsigned int del_vector_qtty = len / _del_line_size_;
	unsigned int i, j;
	void *tmp = bs + _del_line_size_ - ci_p -> mea_mbrs_qtty;
	for (i = 0; i < del_vector_qtty; i++) {
		for (j = 0; j < ci_p -> mea_mbrs_qtty; j++) {
//			if (*(((char *)tmp) + j * (1 + sizeof(double))) != 1)
//				*(((char *)tmp) + j * (1 + sizeof(double))) = UD_DEL_FLAG;
			if (*((char *) tmp + j) == 0)
				*((char *) tmp + j) = UD_DEL_FLAG;
		}
		tmp += _del_line_size_;
	}


	unsigned int del_vectors = 0;
	int v_index;
	for (i = 0; i < del_vector_qtty; i++) {
		v_index = positioning_vector_index(NULL, ci_p, bs + i * _del_line_size_, ci_p -> _cube_data_stct_);
		if (v_index < 0)
			continue;
		del_vectors += _delete_(ci_p, v_index, bs + i * _del_line_size_);
	}


	char cube_UD_f[256];
	splice_cube_UD_file(ci_p -> cube_mg_id, cube_UD_f);
    FILE *fp = fopen(cube_UD_f, "ab");
	char _tdca_[ci_p -> mea_mbrs_qtty * (1 + sizeof(double))]; // temp data change area
	for (i = 0; i < del_vector_qtty; i++) {
		fwrite(bs + i * _del_line_size_, ci_p -> data_vector_len - (ci_p -> mea_mbrs_qtty * (1 + sizeof(double))), 1, fp);
		for (j = 0; j < ci_p -> mea_mbrs_qtty; j++)
			memcpy(((char *) _tdca_) + j * (1 + sizeof(double)), bs + (i + 1) * _del_line_size_ - ci_p -> mea_mbrs_qtty + j, 1);
		fwrite(_tdca_, sizeof(_tdca_), 1, fp);
	}
    fclose(fp);


	return del_vectors;
} // function delete_measure() end

static int update_measure(void *bs, size_t len, struct cube_info *ci_p)
{
	char cube_UD_f[256];
	splice_cube_UD_file(ci_p -> cube_mg_id, cube_UD_f);
    FILE *fp = fopen(cube_UD_f, "ab");
    fwrite(bs, len, 1, fp);
    fclose(fp);

	struct cube_data_stct *cds = ci_p -> _cube_data_stct_;

	if (len % ci_p -> data_vector_len != 0) {
		logD("// temp > ERROR! cube[ %d ] data file broken.\n", ci_p -> cube_mg_id);
		return -1;
	}

	unsigned int i, update_vectors = 0, update_vector_qtty = len / ci_p -> data_vector_len;
	int v_index;
	for (i = 0; i < update_vector_qtty; i++) {
		v_index = positioning_vector_index(NULL, ci_p, bs + i * ci_p -> data_vector_len, cds);
		if (v_index < 0)
			continue;
		update_vectors += _update_(ci_p, v_index, bs + i * ci_p -> data_vector_len);
	}

	return update_vectors;

} // function update_measure() end

static int _update_(struct cube_info *ci_p, size_t v_index, void *new_vector)
{
	struct cube_data_stct *cds = (struct cube_data_stct *) ci_p -> _cube_data_stct_;
	size_t off_s = v_index * ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
	void *arr_index_v = cds -> measures_table + off_s;

	void *new_vector_M = new_vector + ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
	// void *arr_index_v_M = arr_index_v + ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * (1 + sizeof(double));

	int i;
	for (i = 0; i < ci_p -> mea_mbrs_qtty; i++) {
		if (*((char *) (arr_index_v + i * (1 + sizeof(double)))) == 0)
			break;
		if (i == ci_p -> mea_mbrs_qtty - 1)
			return 0; // update 0 vector
	}
	for (i = 0; i < ci_p -> mea_mbrs_qtty; i++) {
		if (*((char *) (new_vector_M + i * (1 + sizeof(double)))) == 0)
			memcpy(arr_index_v + i * (1 + sizeof(double)), new_vector_M + i * (1 + sizeof(double)), sizeof(double) + 1);
	}
	return 1;
} // function _update_() end

static int _delete_(struct cube_info *ci_p, size_t v_index, void *new_vector)
{
	struct cube_data_stct *cds = (struct cube_data_stct *) ci_p -> _cube_data_stct_;
	size_t off_s = v_index * ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
	void *arr_index_v = cds -> measures_table + off_s;


	void *new_vector_M = new_vector + ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
	// void *arr_index_v_M = arr_index_v + ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * (1 + sizeof(double));

	int i;
	for (i = 0; i < ci_p -> mea_mbrs_qtty; i++) {
		if (*((char *) (arr_index_v + i * (1 + sizeof(double)))) == 0)
			break;
		if (i == ci_p -> mea_mbrs_qtty - 1)
			return 0; // delete 0 vector
	}
	for (i = 0; i < ci_p -> mea_mbrs_qtty; i++) {
		if (*((char *) new_vector_M + i) == UD_DEL_FLAG)
			*((char *) (arr_index_v + i * (1 + sizeof(double)))) = 1; // skip flag = 1 (the measure value is null)
	}
	return 1;
} // function _delete_() end

// file 'mea_update.c' end
