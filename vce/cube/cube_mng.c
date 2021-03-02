// cube_mng.c

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>

#include "cube_mng.h"
#include "vce_cfg.h"
#include "vce_common.h"
#include "agg_calcul.h"

extern struct vce_cfg_stct vce_cfg_instance;

struct cube_info *cube_infos_chain_h;

static void load_cube_info(char *cube_file_path);

static void _mem_writeback_cubedf_(struct cube_info *ci_p, struct cube_data_stct *cds, unsigned int dm_role_idx, unsigned int *vector_idx, unsigned int *mbr_idx_arr, FILE *cube_df_tmp);

static void load_cube_data(int cube_mg_id);

static void load_cube_data_light(int cube_mg_id);

static int extract_cube_mg_id(char *cube_file_path);

static void sort_dmRoleMbrs_by_mgId(unsigned int cube_mg_id, void *cube_data_addr, struct cube_data_stct *cds);

static int cube_dimension_roles_quantity(int cube_mg_id);
// static int cube_data_vector_len(int cube_mg_id);
static int cube_data_vectors_quantity(int cube_mg_id);

// 'dm_role_sorted_position' is not dimension role MG_ID
static int member_max_level(unsigned int cube_mg_id, int dm_role_sorted_position);

static void arrange_mbrmgid_path_neatly(unsigned int cube_mg_id, void *sorted_cube_data, struct cube_data_stct *cds);

static void generate_measure_values_table(void *params, unsigned int cube_mg_id, struct cube_data_stct *cds);

static void fill_measure_values_table(void *params, unsigned int cube_mg_id, void *cube_crude_data_p, struct cube_data_stct *cds);

/*
int is_cube_exist(int cube_mg_id)
{
	return 0;
}
*/

int build_cube(void *addr, int effect_len)
{
	logD("// temp > ================================ build_cube(), effect_len = %d\n", effect_len);

	int *p = (int *) addr;
	logD("// temp > build cube function. code = %d\n", *p);
	p++;

	logD("// temp > ############################## CUBE bytes <<<\n");
	int cube_mg_id = *p;
	logD("// temp > cube MG_ID = %d\n", cube_mg_id);
	p++;

	int dimensions_num = *p;
	logD("// temp > dimensions_num = %d\n", dimensions_num);
	p++;

	int i;
	for (i = 0; i < dimensions_num; i++) {
		int dimension_mg_id = *p;
		p++;
		int dm_mbr_max_lv = *p;
		p++;
		logD("// temp > dimension_mg_id = %d, dm_mbr_max_lv = %d\n", dimension_mg_id, dm_mbr_max_lv);
	}

	int dm_roles_num = *p;
	logD("// temp > dimension roles number = %d\n", dm_roles_num);
	p++;
	for (i = 0; i < dm_roles_num; i++) {
		int dm_role_mg_id = *p;
		p++;
		int dimension_mg_id = *p;
		p++;
		logD("// temp > dm_role_mg_id = %d, dimension_mg_id = %d\n", dm_role_mg_id, dimension_mg_id);
	}

	int measures_num = *p;
	logD("// temp > measure members number = %d\n", measures_num);
	p++;
	for (i = 0; i < measures_num; i++) {
		int mea_mbr_mg_id = *p;
		p++;
		logD("// temp > mea_mbr_mg_id = %d\n", mea_mbr_mg_id);
	}
	logD("// temp > ############################## CUBE bytes >>>\n");

	// build cube info file
	FILE *fp;
	char cube_file_path[256];
	splice_cube_info_f(cube_mg_id, cube_file_path);
	fp = fopen(cube_file_path, "wb");
	void *tmp_v_p_ = addr;
	fwrite(tmp_v_p_ + 4, effect_len - sizeof(int), 1, fp);
	fclose(fp);

	load_cube_info(cube_file_path);

	return 0;
} // function build_cube() end

int mem_writeback_cubedf(unsigned int cube_mg_id)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	if (ci_p == NULL)
		return 1;
	struct cube_data_stct *cds = (struct cube_data_stct *) ci_p -> _cube_data_stct_;
	if (cds == NULL)
		return 2;

	char cube_data_file_tmp[256];
	memset(cube_data_file_tmp, 0, sizeof(cube_data_file_tmp));
// strcat(cube_df_path, "/");
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_data_file_tmp);
	strcat(cube_data_file_tmp, "_temp");

	// 181     FILE *fp = fopen(cube_file_path, "ab");
	// 182     fwrite(&((int *)addr)[2], effect_len - sizeof(int) * 2, 1, fp);
	// 183     fclose(fp);
	
	FILE *cube_df_tmp = fopen(cube_data_file_tmp, "wb");

	unsigned int mbr_idx_arr[ci_p -> dm_roles_qtty];

	unsigned int vector_idx = 0;

	_mem_writeback_cubedf_(ci_p, cds, 0, &vector_idx, mbr_idx_arr, cube_df_tmp);
	fclose(cube_df_tmp);

	char cube_df_path[256];
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_df_path);
	rename(cube_data_file_tmp, cube_df_path);

	return 0;
} // function mem_writeback_cubedf() end

static void _mem_writeback_cubedf_(struct cube_info *ci_p, struct cube_data_stct *cds, unsigned int dm_role_idx, unsigned int *vector_idx, unsigned int *mbr_idx_arr, FILE *cube_df_tmp)
{
	unsigned int i, j, k, dmrole_mbrs_qtty = (cds -> dmrole_mbrs_qtty_arr)[dm_role_idx];
	if (dm_role_idx < ci_p -> dm_roles_qtty - 1) {
		for (i = 0; i < dmrole_mbrs_qtty; i++) {
			mbr_idx_arr[dm_role_idx] = i;
			_mem_writeback_cubedf_(ci_p, cds, dm_role_idx + 1, vector_idx, mbr_idx_arr, cube_df_tmp);
		}
	} else { // dm_role_idx == ci_p -> dm_roles_qtty - 1
		int _mea_mbrs_seglen_ = (sizeof(double) + 1) * ci_p -> mea_mbrs_qtty;
		int size_of_int = sizeof(int);
		void *data_addr;
		int data_len;
		for (i = 0; i < dmrole_mbrs_qtty; i++, (*vector_idx)++) {
			for (j = 0; j < ci_p -> mea_mbrs_qtty; j++) {
				if (*((char *) (cds -> measures_table + _mea_mbrs_seglen_ * (*vector_idx) + j * (sizeof(double) + 1))) == 0) {
					mbr_idx_arr[dm_role_idx] = i;
					for (k = 0; k < ci_p -> dm_roles_qtty; k++) {
						data_addr = (cds -> dmrole_tables)[k] + mbr_idx_arr[k] * size_of_int * (ci_p -> dm_roles_info_table)[k][1];
						data_len = size_of_int * (ci_p -> dm_roles_info_table)[k][1];
						fwrite(data_addr, data_len, 1, cube_df_tmp);
					}
					data_addr = (cds -> measures_table) + (*vector_idx) * _mea_mbrs_seglen_;
					fwrite(data_addr, _mea_mbrs_seglen_, 1, cube_df_tmp);
					break;
				}
			}
		}
	}
} // function _mem_writeback_cubedf_() end

int insert_measure_values(void *addr, int effect_len)
{
	int cube_mg_id = ((unsigned int *) addr)[1];
	// logD("// temp > [INSERT_CUBE_MEASURE_OP], cube_mg_id = %d\n", cube_mg_id);

	char cube_file_path[256];

	splice_cube_dfpath_by_mgid(cube_mg_id, cube_file_path);

	FILE *fp = fopen(cube_file_path, "ab");
	fwrite(&((int *)addr)[2], effect_len - sizeof(int) * 2, 1, fp);
	fclose(fp);

	// test code !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	/*
	unsigned char *ucp = (unsigned char *) addr;
	int i;
	for (i = 0; i < effect_len; i++) {
		if (i % 16 == 0)
			printf("\n");
		printf("%#X\t", ucp[i]);
	}
	printf("\n");
	*/
	// test code ??????????????????????????????????????????????????????

	// rebuild_cube_mem_data_struct(cube_mg_id);

	return 0;

} // function insert_measure_values() end.

void rebuild_cube_mem_data_struct(unsigned int cube_mg_id)
{
	load_cube_data_light(cube_mg_id);
}

static void load_cube_info(char *cube_file_path)
{
	// logD("// temp > load a cube info into ram: [ %s ]\n", cube_file_path);

	char buf[1024 * 512];
	FILE *fp = fopen(cube_file_path, "rb");
	size_t file_size = fread(buf, 1, sizeof(buf), fp);
	fclose(fp);

	// logD("// temp > [ %s ] size = %d\n", cube_file_path, file_size);

	struct cube_info *ci_p = find_cube_info_by_mg_id(*((unsigned int *) buf));
	if (ci_p != NULL) {
		struct cube_info *ci_p_next_ = ci_p -> next;
		void *_cube_data_stct_temp_ = ci_p -> _cube_data_stct_;
		memset(ci_p, 0, sizeof(struct cube_info));
		ci_p -> next = ci_p_next_;
		ci_p -> _cube_data_stct_ = _cube_data_stct_temp_;
	} else {
		ci_p = malloc(sizeof(struct cube_info));
		memset(ci_p, 0, sizeof(struct cube_info));
		if (cube_infos_chain_h == NULL)
			cube_infos_chain_h = ci_p;
		else {
			struct cube_info *chain_tail = cube_infos_chain_h;
			while (chain_tail -> next != NULL)
				chain_tail = chain_tail -> next;
			chain_tail -> next = ci_p;
		}
	}


	// ci_p -> cube_info_addr = malloc(file_size);
	// memcpy(ci_p -> cube_info_addr, buf, file_size);

	unsigned int *uint_p = (unsigned int *) buf;

	ci_p -> cube_mg_id = *uint_p++;
	// logD("// temp > CUBE MG_ID = [%d]\n", ci_p -> cube_mg_id);
	memcpy(ci_p -> cube_file_path, cube_file_path, strlen(cube_file_path) + 1);
	ci_p -> cube_file_size = file_size;

	unsigned int *part_dms_p = uint_p;
	unsigned int dm_qtty = *part_dms_p; // dimensions quantity

	// skip dimensions info part
	uint_p += 1 + dm_qtty * 2;

	unsigned int *part_dm_roles_p = uint_p;
	ci_p -> dm_roles_qtty = *part_dm_roles_p; // dimension roles quantity
	// logD("// temp > dimension roles quantity = %d\n", dm_roles_qtty);

	// Calculate vector data length (quantity of bytes)
	ci_p -> data_vector_len = 0;
	unsigned int *tm_p = part_dm_roles_p + 1, *tp_2;
	int x, j;
	for (x = 0; x < ci_p -> dm_roles_qtty; x++) {
		int dm_role_mg_id = *tm_p++;
		unsigned int dimension_mg_id = *tm_p++;
		tp_2 = part_dms_p + 1;
		for (j = 0; j < dm_qtty; j++) {
			if (dimension_mg_id != *tp_2)
				tp_2 += 2;
			else {
				// ci_p -> data_vector_len += *(tp_2 * sizeof(unsigned int) + 1);
				int dm_member_max_lv = tp_2[1];
				ci_p -> data_vector_len += dm_member_max_lv * sizeof(unsigned int);
				(ci_p -> dm_roles_info_table)[x][0] = dm_role_mg_id;
				(ci_p -> dm_roles_info_table)[x][1] = dm_member_max_lv;
				break;
			}
		}
	}

	unsigned int *part_measures_p = part_dm_roles_p + ci_p -> dm_roles_qtty * 2 + 1;
	unsigned int measures_qtty = *part_measures_p; // measures quantity
	ci_p -> data_vector_len += measures_qtty * (1 + sizeof(double));
	ci_p -> mea_mbrs_qtty = measures_qtty;

	// move to location that dimension roles and amount of leaf members
	uint_p += 1 + 2 * ci_p -> dm_roles_qtty + 1 + ci_p -> mea_mbrs_qtty;
	for (x = 0; x < ci_p -> dm_roles_qtty; x++) {
		logD("// temp > DimensionRoleMGID [%d] leaf Members [%d]\n", *uint_p, *(uint_p + 1));
		for (j = 0; j < ci_p -> dm_roles_qtty; j++) {
			if ((ci_p -> dm_roles_info_table)[j][0] == *uint_p) {
				(ci_p -> dm_roles_info_table)[j][2] = *(uint_p + 1);
				break;
			}
		}
		uint_p += 2;
	}


	/*
	unsigned int *ui_p = (unsigned int *) buf;
	ui_p++; // skip cube MG_ID
	unsigned int dm_roles_quantity = *ui_p;
	ui_p++;
	int i;
	for (i = 0; i < dm_roles_quantity; i++) {
		(ci_p -> dm_roles_info_table)[i][0] = *ui_p;
		ui_p++;
		(ci_p -> dm_roles_info_table)[i][1] = *ui_p;
		ui_p++;
	}
	*/


	// logD("// temp > Calculate '%s' vector data length (quantity of bytes)\n", cube_file_path);
	// part_dms_p++; // skip cube's MG ID.
	// logD("// temp > dimensions quantity = %d\n", dm_qtty);
	
	/* p++;
	int i;
	for (i = 0; i < dimensions_num; i++) {
		int dimension_mg_id = *p;
		p++;
		int dm_mbr_max_lv = *p;
		p++;
	} */

	/* int measures_num = *p;
	p++;
	for (i = 0; i < measures_num; i++) {
		int mea_mbr_mg_id = *p;
		p++;
	} */
	// logD("// temp > Calculate vector data length = %d\n", ci_p -> data_vector_len);

	// logD("// temp > load a cube info into ram: [ %s ] success.\n", cube_file_path);
} // function load_cube_info() end

static int extract_cube_mg_id(char *cube_file_path)
{
	char *mg_id = &cube_file_path[strlen(cube_file_path) - 1];
	while (mg_id[-1] != '_')
		mg_id--;
	return atoi(mg_id);
}

// No need to load entire cube data file into memory, it consumes less memory than load_cube_data() function.
static void load_cube_data_light(int cube_mg_id)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	struct cube_data_stct *cds = (struct cube_data_stct *) malloc(sizeof(struct cube_data_stct));
	memset(cds, 0, sizeof(struct cube_data_stct));
	cds -> cube_mg_id = cube_mg_id;
	int i;

	// Organize and sort MG_ID information of dimension members
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		(cds -> dmrole_tables)[i] = malloc((ci_p -> dm_roles_info_table)[i][1] * (ci_p -> dm_roles_info_table)[i][2] * sizeof(int));
	}

	char cube_file_path[256];
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_file_path);
	struct stat c_data_f_stat;
	stat(cube_file_path, &c_data_f_stat);
	FILE *fp = fopen(cube_file_path, "rb");
	
	off_t cube_df_size = c_data_f_stat.st_size;
	off_t load_bytes_size, tmp_buf_size = (1 * 1024 * 1024 * 128) / ci_p -> data_vector_len * ci_p -> data_vector_len; // about 128 M
	off_t load_vectors_size;
	int vector_offset, j, k, res, member_max_lv;
	char *tmp_buf = (char *) malloc(tmp_buf_size);
	do {
		load_bytes_size = cube_df_size > tmp_buf_size ? tmp_buf_size : cube_df_size;
		cube_df_size = cube_df_size > tmp_buf_size ? (cube_df_size - tmp_buf_size) : 0;
		load_vectors_size = load_bytes_size / ci_p -> data_vector_len;

		fread(tmp_buf, 1, load_bytes_size, fp);

		for (i = 0; i < load_vectors_size; i++) {
			vector_offset = 0;
			for (j = 0; j < ci_p -> dm_roles_qtty; j++) {
				member_max_lv = (ci_p -> dm_roles_info_table)[j][1];
				for (k = 0; k < (cds -> dmrole_mbrs_qtty_arr)[j]; k++) {
					res = memcmp(
						(cds -> dmrole_tables)[j] + k * member_max_lv * sizeof(int),
						tmp_buf + i * ci_p -> data_vector_len + vector_offset,
						member_max_lv * sizeof(int)
					);
					if (res == 0)
						goto cti_for_000;
				}
				memcpy(
					(cds -> dmrole_tables)[j] + (cds -> dmrole_mbrs_qtty_arr)[j] * member_max_lv * sizeof(int),
					tmp_buf + i * ci_p -> data_vector_len + vector_offset,
					member_max_lv * sizeof(int)
				);
				(cds -> dmrole_mbrs_qtty_arr)[j]++;
				cti_for_000:
				vector_offset += member_max_lv * sizeof(int);
			}
		}
	} while (cube_df_size > 0);

	// To simulate a multidimensional array, set the offset of the dimension role positioning measures
	for (i = ci_p -> dm_roles_qtty - 1; i >= 0; i--) {
		vector_offset = (i == ci_p -> dm_roles_qtty - 1) ? 1 : (vector_offset * cds -> dmrole_mbrs_qtty_arr[i + 1]);
		cds -> dmroles_offset[i] = vector_offset;
	}

	// sort
	for (k = 0; k < ci_p -> dm_roles_qtty; k++) {
		member_max_lv = (ci_p -> dm_roles_info_table)[k][1];
		for (i = 0; i < cds -> dmrole_mbrs_qtty_arr[k] - 1; i++) {
			for (j = i + 1; j < cds -> dmrole_mbrs_qtty_arr[k]; j++) {
				res = unsigned_int_list_comp(
					(cds -> dmrole_tables)[k] + i * member_max_lv * sizeof(int),
					(cds -> dmrole_tables)[k] + j * member_max_lv * sizeof(int),
					member_max_lv
				);
				if (res == 0) {
					logD("// temp > ERROR! program exit with [ %d ]\n", EXIT_FAILURE);
					exit(EXIT_FAILURE);
				} else if (res > 0) {
					unsigned_int_list_exchange(
						(cds -> dmrole_tables)[k] + i * member_max_lv * sizeof(int),
						(cds -> dmrole_tables)[k] + j * member_max_lv * sizeof(int),
						member_max_lv
					);
				}
			}
		}
	}

	// Assign the memory space of measure values
	size_t measure_addr_size = cds -> dmrole_mbrs_qtty_arr[0];
	for (i = 1; cds -> dmrole_mbrs_qtty_arr[i] > 0; i++)
		measure_addr_size *= cds -> dmrole_mbrs_qtty_arr[i];
	measure_addr_size *= ci_p -> mea_mbrs_qtty * (sizeof(double) + 1);

	logI("load_cube_data_light() ... cube_mg_id[%d] measure_addr_size = %zu", cube_mg_id, measure_addr_size);
	cds -> measures_table = malloc(measure_addr_size);
	if ((cds -> measures_table) == NULL)
		logW("load_cube_data_light() ... Failed to allocate memory. [ cds -> measures_table == NULL ]");

	memset(cds -> measures_table, 1, measure_addr_size);

	// Move to the file header location
	fseek(fp, 0, SEEK_SET);

	// Copy measure values to the appropriate memory area
	cube_df_size = c_data_f_stat.st_size;
	void *_desc_p_, *_src_p_;
	do {
		load_bytes_size = cube_df_size > tmp_buf_size ? tmp_buf_size : cube_df_size;
		cube_df_size = cube_df_size > tmp_buf_size ? (cube_df_size - tmp_buf_size) : 0;
		load_vectors_size = load_bytes_size / ci_p -> data_vector_len;
		fread(tmp_buf, 1, load_bytes_size, fp);
		for (i = 0; i < load_vectors_size; i++) {
			j = positioning_vector_index(NULL, ci_p, tmp_buf + i * ci_p -> data_vector_len, cds);
			_desc_p_ = (cds -> measures_table) + j * ci_p -> mea_mbrs_qtty * (sizeof(double) + 1);
			_src_p_ = tmp_buf + (i + 1) * ci_p -> data_vector_len - ci_p -> mea_mbrs_qtty * (sizeof(double) + 1);
			memcpy(_desc_p_, _src_p_, ci_p -> mea_mbrs_qtty * (sizeof(double) + 1));
		}
	} while (cube_df_size > 0);

	// Release resources
	fclose(fp);
	free(tmp_buf);

	// Create a link to the cube info to the cube data
	if (ci_p -> _cube_data_stct_ != NULL)
		free_mem_cubedatastct((struct cube_data_stct *) ci_p -> _cube_data_stct_);
	ci_p -> _cube_data_stct_ = cds;

	logI("load_cube_data_light() load cube data finished %d", cube_mg_id);
} // function load_cube_data_light() end.

// Need to load the data file into the memory all at once, it will consume a lot of memory,
// which may cause the program to crash.
// This function will be replaced by a new function - load_cube_data_light()
static void load_cube_data(int cube_mg_id)
{
	char cube_file_path[256];
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_file_path);
logD("// temp > load cube data file [ %s ]\n", cube_file_path);
	struct stat c_data_f_stat;
	stat(cube_file_path, &c_data_f_stat);
logD("// temp > [ %s ] size = %d\n", cube_file_path, c_data_f_stat.st_size);
	FILE *fp = fopen(cube_file_path, "rb");
logD("// temp > open file %s\n", fp == NULL ? "ERROR !!!!!!!!!!!!!!!!!!!!!!!" : "RIGHT");

	void *buf = malloc(c_data_f_stat.st_size);

	int ret = fread(buf, 1, c_data_f_stat.st_size, fp);
logD("// temp > read %d bytes\n", ret);
	ret = fclose(fp);
logD("// temp > close file return [ %d ]. load cube data SUCCESS.\n", ret);

	// void *buf_2 = malloc(c_data_f_stat.st_size);

	// memcpy(buf_2, buf, c_data_f_stat.st_size);

	struct cube_data_stct *cds = (struct cube_data_stct *) malloc(sizeof(struct cube_data_stct));
	memset(cds, 0, sizeof(struct cube_data_stct));
	cds -> cube_mg_id = cube_mg_id;

	// The MG_ID of members information in `buf` has been sorted.
	sort_dmRoleMbrs_by_mgId(cube_mg_id, buf, cds);

	// arrange_mbrmgid_path_neatly(cube_mg_id, buf_2, cds); // arrange cube members MG_ID path neatly

	generate_measure_values_table(NULL, cube_mg_id, cds);
	

	fp = fopen(cube_file_path, "rb");
	ret = fread(buf, 1, c_data_f_stat.st_size, fp);
	ret = fclose(fp);
logD("// temp > close file return [ %d ]. load cube data SUCCESS again.\n", ret);

	fill_measure_values_table(NULL, cube_mg_id, buf, cds);

	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	if (ci_p -> _cube_data_stct_ != NULL)
		free_mem_cubedatastct((struct cube_data_stct *) ci_p -> _cube_data_stct_);
	ci_p -> _cube_data_stct_ = cds;

	free(buf);
	// free(buf_2);
} // function load_cube_data() end.

static void sort_dmRoleMbrs_by_mgId(unsigned int cube_mg_id, void *cube_data_addr, struct cube_data_stct *cds)
{
	int cube_dm_roles_quantity = cube_dimension_roles_quantity(cube_mg_id);
	int cube_data_v_len = cube_data_vector_len(cube_mg_id); // bytes
	int cube_data_v_quantity = cube_data_vectors_quantity(cube_mg_id); // lines

	char *vp = (char *)cube_data_addr;

	// The number of members not repeated in each dimension role.
	unsigned int not_rep_drms_arr[cube_dm_roles_quantity];
	unsigned int k, mbr_max_lv, vector_internal_offset;
	size_t i, j;

	for (i = 0; i < cube_dm_roles_quantity; i++)
		not_rep_drms_arr[i] = 1;

	int res;
	char dexa[1024]; // Data exchange area
	for (i = 1; i < cube_data_v_quantity; i++) {
		vector_internal_offset = 0;
		for (k = 0; k < cube_dm_roles_quantity; k++) {
			mbr_max_lv = member_max_level(cube_mg_id, k);
			for (j = 0; j < not_rep_drms_arr[k]; j++) {
				/* res = unsigned_int_list_comp(
					(unsigned int *) (i_vp + vector_internal_offset),
					(unsigned int *) (j_vp + vector_internal_offset),
					mbr_max_lv
				); */
				res = memcmp(
					&vp[i * cube_data_v_len] + vector_internal_offset,
					&vp[j * cube_data_v_len] + vector_internal_offset,
					mbr_max_lv * sizeof(unsigned int));
				if (!res) // res != 0
					goto gp_001;
			}
			not_rep_drms_arr[k]++; // a new member is included in the management
			if (not_rep_drms_arr[k] - 1 > i) {
				logD("// temp > error in sort_dmRoleMbrs_by_mgId()\n");
				exit(EXIT_FAILURE);
			} else if (not_rep_drms_arr[k] - 1 < i) {
				memcpy(dexa, &vp[i * cube_data_v_len] + vector_internal_offset, mbr_max_lv * sizeof(unsigned int));
				memcpy(&vp[i * cube_data_v_len] + vector_internal_offset, &vp[(not_rep_drms_arr[k] - 1) * cube_data_v_len] + vector_internal_offset, mbr_max_lv * sizeof(unsigned int));
				memcpy(&vp[(not_rep_drms_arr[k] - 1) * cube_data_v_len] + vector_internal_offset, dexa, mbr_max_lv * sizeof(unsigned int));
			}
			gp_001:
			vector_internal_offset = vector_internal_offset + mbr_max_lv * sizeof(unsigned int);
		}
	}

	for (k = 0; k < cube_dm_roles_quantity; k++)
		cds -> dmrole_mbrs_qtty_arr[k] = not_rep_drms_arr[k];

	// To simulate a multidimensional array, set the offset of the dimension role positioning measures
	// logD("// temp > !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
	unsigned int dimrole_offset;
	int _k_;
	for (_k_ = cube_dm_roles_quantity - 1; _k_ >= 0; _k_--) {
		// printf("cube_dm_roles_quantity = %d, k = %d\n", cube_dm_roles_quantity, k);
		dimrole_offset = (_k_ == cube_dm_roles_quantity - 1) ? 1 : (dimrole_offset * cds -> dmrole_mbrs_qtty_arr[_k_ + 1]);
		// printf("dimrole_offset = %d\n", dimrole_offset);
		cds -> dmroles_offset[_k_] = dimrole_offset;
	}
	// logD("// temp > ??????????????????????????????????????\n");

	// sort
	vector_internal_offset = 0;
	for (k = 0; k < cube_dm_roles_quantity; k++) {
		mbr_max_lv = member_max_level(cube_mg_id, k);
		for (i = 0; i < cds -> dmrole_mbrs_qtty_arr[k] - 1; i++) {
			for (j = i + 1; j < cds -> dmrole_mbrs_qtty_arr[k]; j++) {
				res = unsigned_int_list_comp(
							(unsigned int *) (&vp[i * cube_data_v_len] + vector_internal_offset), 
							(unsigned int *) (&vp[j * cube_data_v_len] + vector_internal_offset), 
							mbr_max_lv);
				if (res == 0) {
					logD("// temp > ERROR! program exit with [ %d ]\n", EXIT_FAILURE);
					exit(EXIT_FAILURE);
				}
				if (res > 0) {
					unsigned_int_list_exchange(
							(unsigned int *) (&vp[i * cube_data_v_len] + vector_internal_offset), 
							(unsigned int *) (&vp[j * cube_data_v_len] + vector_internal_offset), 
							mbr_max_lv);
					// memcpy(dexa, &i_vp[vector_internal_offset], mbr_max_lv * sizeof(unsigned int));
					// memcpy(&i_vp[vector_internal_offset], &j_vp[vector_internal_offset], mbr_max_lv * sizeof(unsigned int));
					// memcpy(&j_vp[vector_internal_offset], dexa, mbr_max_lv * sizeof(unsigned int));
				}
			}
		}
		vector_internal_offset = vector_internal_offset + mbr_max_lv * sizeof(unsigned int);
	}

/*	int i_end = cube_data_v_quantity - 1;
	char *i_vp;
	char *j_vp;
	for (i = 0; i < i_end; i++) {
		i_vp = &vp[i * cube_data_v_len];
		for (j = i + 1; j < cube_data_v_quantity; j++) {
			j_vp = &vp[j * cube_data_v_len];
			vector_internal_offset = 0;
			for (k = 0; k < cube_dm_roles_quantity; k++) {
				mbr_max_lv = member_max_level(cube_mg_id, k);
				// int res = unsigned_int_list_comp(&i_vp[vector_internal_offset], &j_vp[vector_internal_offset], mbr_max_lv);
				int res = unsigned_int_list_comp((unsigned int *) (i_vp + vector_internal_offset), 
												 (unsigned int *) (j_vp + vector_internal_offset), mbr_max_lv);
				if (res > 0) {
					memcpy(dexa, &i_vp[vector_internal_offset], mbr_max_lv * sizeof(unsigned int));
					memcpy(&i_vp[vector_internal_offset], &j_vp[vector_internal_offset], mbr_max_lv * sizeof(unsigned int));
					memcpy(&j_vp[vector_internal_offset], dexa, mbr_max_lv * sizeof(unsigned int));
				}
				vector_internal_offset = vector_internal_offset + mbr_max_lv * sizeof(unsigned int);
			}
		}
	}
*/

	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	for (i = 0; i < cube_dm_roles_quantity; i++) {
		// logD("// temp > Dimension_Role_MG_ID = %d\n", dmroles_mbroles_tbl[i][1]);
		// logD("// temp > Members_Quantity = %d\n", dmroles_mbroles_tbl[i][2]);
		// logD("// temp > Max_Member_Level = %d\n", ci_p -> dm_roles_info_table[i][1]);
		// cds -> dmrole_mbrs_qtty_arr[i] = dmroles_mbroles_tbl[i][2];
		int malloc_size = cds -> dmrole_mbrs_qtty_arr[i] * ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
		// logD("// temp > Malloc_Size = %d\n", malloc_size);
		cds -> dmrole_tables[i] = malloc(malloc_size);
	}

	int max_mbr_Lv;
	dimrole_offset = 0;
	void *ui_mark, *ui_expl;
	for (i = 0; i < cube_dm_roles_quantity; i++) {
		ui_mark = cube_data_addr + dimrole_offset;
		ui_expl = cds -> dmrole_tables[i];
		max_mbr_Lv = ci_p -> dm_roles_info_table[i][1];
		for (j = 0; j < cds -> dmrole_mbrs_qtty_arr[i]; j++) {
			memcpy(ui_expl, ui_mark, max_mbr_Lv * sizeof(unsigned int));
			ui_mark += cube_data_v_len;
			ui_expl += max_mbr_Lv * sizeof(unsigned int);
		}
		dimrole_offset += ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
	}
} // function sort_dmRoleMbrs_by_mgId() end.

void splice_cube_info_f(unsigned int cube_mg_id, char *cube_df_path)
{
	if (cube_df_path == NULL)
		return;
    char cube_id_str[16];
	sprintf(cube_id_str, "%d", cube_mg_id);
	strcpy(cube_df_path, vce_cfg_instance.data_dir);
	strcat(cube_df_path, "/");
	strcat(cube_df_path, CUBE_INFO_FILE_PREFIX);
	strcat(cube_df_path, cube_id_str);
}

void splice_cube_dfpath_by_mgid(unsigned int cube_mg_id, char *cube_df_path)
{
	if (cube_df_path == NULL)
		return;

    char cube_id_str[16];
	sprintf(cube_id_str, "%d", cube_mg_id);

	strcpy(cube_df_path, vce_cfg_instance.data_dir);
	strcat(cube_df_path, "/");
	strcat(cube_df_path, CUBE_DATA_FILE_PREFIX);
	strcat(cube_df_path, cube_id_str);

}

void splice_cube_UD_file(unsigned int cube_mg_id, char *cube_UD_f)
{
	if (cube_UD_f == NULL)
		return;

    char cube_id_str[16];
	sprintf(cube_id_str, "%d", cube_mg_id);

	strcpy(cube_UD_f, vce_cfg_instance.data_UD_dir);
	strcat(cube_UD_f, "/");
	strcat(cube_UD_f, CUBE_UD_FILE_PREFIX);
	strcat(cube_UD_f, cube_id_str);

}

static int cube_dimension_roles_quantity(int cube_mg_id)
{
	struct cube_info *cip = find_cube_info_by_mg_id(cube_mg_id);
	return cip -> dm_roles_qtty;
}

int cube_data_vector_len(int cube_mg_id)
{
	struct cube_info *cip = find_cube_info_by_mg_id(cube_mg_id);
	return cip -> data_vector_len;
}

static int cube_data_vectors_quantity(int cube_mg_id)
{
	struct cube_info *cip = find_cube_info_by_mg_id(cube_mg_id);
	char cube_file_path[512];
	splice_cube_dfpath_by_mgid(cube_mg_id, cube_file_path);
	struct stat c_data_f_stat;
	stat(cube_file_path, &c_data_f_stat);
	// char buf[c_data_f_stat.st_size];
	if (c_data_f_stat.st_size % cip -> data_vector_len != 0) {
		logD("// temp > ERROR! Cube data file length confusion!\n");
		logD("// temp > %d, %d\n", c_data_f_stat.st_size, cip -> data_vector_len);
		return -1;
	}
	return c_data_f_stat.st_size / cip -> data_vector_len;
}

struct cube_info *find_cube_info_by_mg_id(int cube_mg_id)
{
	struct cube_info *cip = cube_infos_chain_h;
	while (cip != NULL) {
		if (cip -> cube_mg_id == cube_mg_id)
			return cip;
		cip = cip -> next;
	}
	return NULL;
}

static int member_max_level(unsigned int cube_mg_id, int dm_role_sorted_position)
{
	struct cube_info *cip = find_cube_info_by_mg_id(cube_mg_id);
	return (cip -> dm_roles_info_table)[dm_role_sorted_position][1];
}

int cube_measure_mbrs_quantity(int cube_mg_id)
{
	struct cube_info *cip = find_cube_info_by_mg_id(cube_mg_id);
	return cip -> mea_mbrs_qtty;
}

void load_all_cubes_info()
{
	logD("// temp > enter fn load_all_cubes_info()\n");

	DIR *dir;
	struct dirent *entry;

	if ((dir = opendir(vce_cfg_instance.data_dir)) != NULL) {
		char cube_info_file_path[512];
		while (entry = readdir(dir)) {
			logD("// temp > \t[%d] %s\n", entry -> d_type, entry -> d_name);
			if (entry -> d_type != DT_REG)
				continue;
			if (strstr(entry -> d_name, CUBE_DATA_FILE_PREFIX) != NULL)
				continue;
// printf("// temp >>>>>>>>>>>>>>>>>>>>>>>>>>>>> WTF! [%s] [%s]\n", entry -> d_name, CUBE_DATA_FILE_PREFIX);
			strcpy(cube_info_file_path, vce_cfg_instance.data_dir);
			strcat(cube_info_file_path, "/");
			strcat(cube_info_file_path, entry -> d_name);
			load_cube_info(cube_info_file_path);
		}   
		closedir(dir);    
	}

	logD("// temp > leave fn load_all_cubes_info()\n");
}


void load_all_cubes_data()
{
	DIR *dir;
	struct dirent *entry;
	if ((dir = opendir(vce_cfg_instance.data_dir)) != NULL) {
		char cube_df_path[512];
		while (entry = readdir(dir)) {
			// logD("// temp > \t[%d] %s\n", entry -> d_type, entry -> d_name);
			if (entry -> d_type != DT_REG)
				continue;
			if (strstr(entry -> d_name, CUBE_DATA_FILE_PREFIX) == NULL)
				continue;
			strcpy(cube_df_path, vce_cfg_instance.data_dir);
			strcat(cube_df_path, "/");
			strcat(cube_df_path, entry -> d_name);
			load_cube_data_light(extract_cube_mg_id(cube_df_path));
		}
		closedir(dir);    
	}
} // function load_all_cubes_data() end.

static void arrange_mbrmgid_path_neatly(unsigned int cube_mg_id, void *sorted_cube_data, struct cube_data_stct *cds)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);

	int cube_dm_roles_quantity = cube_dimension_roles_quantity(cube_mg_id);
	int cube_data_v_len = cube_data_vector_len(cube_mg_id);
	int cube_data_v_quantity = cube_data_vectors_quantity(cube_mg_id);

	// dimension roles and member roles info table
	// [*][0] the position of the dimension role after sorting by MG_ID by small arrival.
	// [*][1] dimension role MG_ID.
	// [*][2] the number of members of the dimension role in this CUBE data file.
	unsigned int dmroles_mbroles_tbl[cube_dm_roles_quantity][3];
	int i, j, k;
	for (i = 0; i < cube_dm_roles_quantity; i++) {
		dmroles_mbroles_tbl[i][0] = i;
		dmroles_mbroles_tbl[i][1] = ci_p -> dm_roles_info_table[i][0];
		dmroles_mbroles_tbl[i][2] = 1;
	}

	// int member_max_level;
	void *ui_mark, *ui_expl;
	// unsigned int *ui_mark_expl[cube_dm_roles_quantity][2]; // [*][0] - marking pointer; [*][1] - exploring pointer.
	int dimrole_offset = 0;
	for (i = 0; i < cube_dm_roles_quantity; i++) {

		ui_mark = sorted_cube_data + dimrole_offset;
		ui_expl = ui_mark;

		for (j = 1; j < cube_data_v_quantity; j++) {
			ui_expl += cube_data_v_len;
			k = unsigned_int_list_comp((unsigned int *)ui_mark, (unsigned int *)ui_expl, ci_p -> dm_roles_info_table[i][1]);
			if (k == 0)
				continue;
			if (k > 0) {
				logD("// temp > ERROR! Cube data sorting error. Program exit with EXIT_FAILURE [%d]\n", EXIT_FAILURE);
				exit(EXIT_FAILURE);
			}
			// k < 0
			dmroles_mbroles_tbl[i][2]++;
			ui_mark += cube_data_v_len;
			if (ui_mark != ui_expl)
				unsigned_int_list_exchange((unsigned int *)ui_mark, (unsigned int *)ui_expl, ci_p -> dm_roles_info_table[i][1]);
		}
		dimrole_offset += ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
	}

	// temp test code !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	/* for (i = 0; i < cube_dm_roles_quantity; i++) {
		logD("// temp > dimension role MG_ID = %d, members quantity = %d\n", 
			dmroles_mbroles_tbl[i][1], dmroles_mbroles_tbl[i][2]);
	} */
	// temp test code ??????????????????????????????????????????????????????????
	// dividing line (°ー°〃) -----------------------------------------------------------------------------

	for (i = 0; i < cube_dm_roles_quantity; i++) {
		logD("// temp > Dimension_Role_MG_ID = %d\n", dmroles_mbroles_tbl[i][1]);
		logD("// temp > Members_Quantity = %d\n", dmroles_mbroles_tbl[i][2]);
		logD("// temp > Max_Member_Level = %d\n", ci_p -> dm_roles_info_table[i][1]);
		cds -> dmrole_mbrs_qtty_arr[i] = dmroles_mbroles_tbl[i][2];
		int malloc_size = dmroles_mbroles_tbl[i][2] * ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
		logD("// temp > Malloc_Size = %d\n", malloc_size);
		cds -> dmrole_tables[i] = malloc(malloc_size);
	}

	// To simulate a multidimensional array, set the offset of the dimension role positioning measures
	for (i = cube_dm_roles_quantity - 1; i >= 0; i--) {
		dimrole_offset = (i == cube_dm_roles_quantity - 1) ? 1 : (dimrole_offset * cds -> dmrole_mbrs_qtty_arr[i + 1]);
		cds -> dmroles_offset[i] = dimrole_offset;
	}

	// dividing line (°ー°〃) -----------------------------------------------------------------------------
	int max_mbr_Lv;
	dimrole_offset = 0;
	for (i = 0; i < cube_dm_roles_quantity; i++) {
		ui_mark = sorted_cube_data + dimrole_offset;
		ui_expl = cds -> dmrole_tables[i];
		max_mbr_Lv = ci_p -> dm_roles_info_table[i][1];
		for (j = 0; j < dmroles_mbroles_tbl[i][2]; j++) {
			memcpy(ui_expl, ui_mark, max_mbr_Lv * sizeof(unsigned int));
			ui_mark += cube_data_v_len;
			ui_expl += max_mbr_Lv * sizeof(unsigned int);
		}
		dimrole_offset += ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
	}

} // function arrange_mbrmgid_path_neatly end

static void generate_measure_values_table(void *params, unsigned int cube_mg_id, struct cube_data_stct *cds)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	int i, len = cds -> dmrole_mbrs_qtty_arr[0];
	for (i = 1; cds -> dmrole_mbrs_qtty_arr[i] > 0; i++) {
		len *= cds -> dmrole_mbrs_qtty_arr[i];
	}
	logD("// temp > MG_ID [%d], measures table length = %d, measure quantity = %d, malloc size = %d\n", 
		cube_mg_id, len, ci_p -> mea_mbrs_qtty, len * ci_p -> mea_mbrs_qtty);
	cds -> measures_table = malloc(len * ci_p -> mea_mbrs_qtty * (sizeof(double) + 1));
	memset(cds -> measures_table, 1, len * ci_p -> mea_mbrs_qtty * (sizeof(double) + 1));
	logD("// temp > X-----#####>   malloc() MALLOC() p[%p], size[%d]  \n", cds -> measures_table, len * ci_p -> mea_mbrs_qtty * (sizeof(double) + 1));
	printf("// WWWWWWWTF... > cds -> measures_table add[ %p ] len[ %d ]\n", cds -> measures_table, len * ci_p -> mea_mbrs_qtty * sizeof(double));
}

static void fill_measure_values_table(void *params, unsigned int cube_mg_id, void *cube_crude_data_p, struct cube_data_stct *cds)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	int cube_dm_roles_quantity = cube_dimension_roles_quantity(cube_mg_id);
	int cube_data_v_len = cube_data_vector_len(cube_mg_id);
	int cube_data_v_quantity = cube_data_vectors_quantity(cube_mg_id);
	int meambrs_qtty = ci_p -> mea_mbrs_qtty;

	int vector_idx;
	size_t i;

	void *vector_p = cube_crude_data_p;
	for (i = 0; i < cube_data_v_quantity; i++, vector_p += cube_data_v_len) {
		// vector_p += cube_data_v_len;
		vector_idx = positioning_vector_index(NULL, ci_p, vector_p, cds);

		void *_desc_p_ = (cds -> measures_table) + vector_idx * meambrs_qtty * (sizeof(double) + 1);
		void *_src_p_ 
			= cube_crude_data_p + /* vector_idx */ i * cube_data_v_len + (cube_data_v_len - meambrs_qtty * (sizeof(double) + 1));
		size_t _size_t_ = meambrs_qtty * (sizeof(double) + 1);
		// logD("// temp > [ %d , %d , %d ] >>>>>> memcpy(%p, %p, %ld)\n", cube_data_v_quantity, i, vector_idx, _desc_p_, _src_p_, _size_t_);

		memcpy(_desc_p_, _src_p_, _size_t_);

		// logD("// temp > X-----#####>      p[%p], sizt_t[%d]\n", _desc_p_, _size_t_);
//		memcpy((cds -> measures_table) + i * meambrs_qtty * (sizeof(double) + 1), 
//			cube_crude_data_p + vector_idx * cube_data_v_len + (cube_data_v_len - meambrs_qtty * (sizeof(double) + 1)), 
//			meambrs_qtty * (sizeof(double) + 1));

		// printf("// WWWWWWWTF... > add[ %p ] len[ %d ]\n", (cds -> measures_table) + i * meambrs_qtty * (sizeof(double) + 1), meambrs_qtty * (sizeof(double) + 1));
	}
}

int positioning_vector_index(void *params, struct cube_info *ci_p, void *vector_p, void *_cds)
{
	struct cube_data_stct *cds = (struct cube_data_stct *) _cds;
	int i, start_idx, end_idx, middle_idx, cmp_rs, dimrole_offset = 0, mbr_mgid_path_bytes_size, index_ = 0;
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		mbr_mgid_path_bytes_size = ci_p -> dm_roles_info_table[i][1] * sizeof(unsigned int);
		start_idx = 0;
		end_idx = cds -> dmrole_mbrs_qtty_arr[i] - 1;
		while (1) {
			void *vp_1 = vector_p + dimrole_offset;
			if (ug_int_cmp(vp_1, cds -> dmrole_tables[i], ci_p -> dm_roles_info_table[i][1]) < 0
					|| ug_int_cmp(vp_1, (cds -> dmrole_tables[i]) + (cds -> dmrole_mbrs_qtty_arr[i] - 1) * mbr_mgid_path_bytes_size, ci_p -> dm_roles_info_table[i][1]) > 0)
				return -1;
			middle_idx = (start_idx + end_idx) / 2;
			void *vp_2 = (cds -> dmrole_tables[i]) + middle_idx * mbr_mgid_path_bytes_size;
			// cmp_rs = memcmp(vp_1, vp_2, mbr_mgid_path_bytes_size);
			cmp_rs = ug_int_cmp(vp_1, vp_2, ci_p -> dm_roles_info_table[i][1]); // ci_p -> dm_roles_info_table[i][1] is max member level
			// logD("// temp > CMP_RS -----------------------------------> = %d \n", cmp_rs);
			if (cmp_rs == 0) {
				index_ += middle_idx * cds -> dmroles_offset[i];
				break;
			} else if (cmp_rs < 0) {
				end_idx = middle_idx - 1;
			} else { // cmp_rs > 0
				start_idx = middle_idx + 1;
			}
		}
		dimrole_offset += mbr_mgid_path_bytes_size;
	}
	return index_;
} // function positioning_vector_index() end

// file 'cube_mng.c' end
