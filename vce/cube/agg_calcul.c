// agg_calcul.c

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "agg_calcul.h"
#include "cube_mng.h"

static void vector_agg_calcul(void *params, struct cube_info *ci_p, struct aggcal_quyres_stct *aqs, unsigned int *q_vector_p, void *quy_result_p);

static void det_dm_role_range(void *params, struct cube_info *ci_p, struct cube_data_stct *cds, int dm_role_queue_num, unsigned int *q_vector_p, int *p_start_end);

static void _vector_agg_calcul_(void *params, struct cube_info *ci_p, int dimRole_locNum, void *_coordinate_range_,
											   void *_quy_result_p_, unsigned int adopted_value_idx);

void free_mem_cubedatastct(struct cube_data_stct *cds)
{
	struct cube_info *ci_p = find_cube_info_by_mg_id(cds -> cube_mg_id);
	int i;
	// logD("// temp > ci_p -> dm_roles_qtty = [[ %d ]]\n", ci_p -> dm_roles_qtty);
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		// logD("// temp > [ i = %d ]free(%p)\n", i, cds -> dmrole_tables[i]);
		free(cds -> dmrole_tables[i]);
	}

	free(cds -> measures_table);
	free(cds);
} // function free_mem_cubedatastct() end

void *agg_calcul_query_(void *task_data_pkg)
{
	int pkg_h_info_s = sizeof(int) * 2 + sizeof(double);

	struct aggcal_quyres_stct aqs;

	struct cube_info *cube_i = find_cube_info_by_mg_id(((int *) task_data_pkg)[2]);
	int n = ((int *) task_data_pkg)[3];
	int result_mem_size = pkg_h_info_s + (1 + sizeof(double)) * n * cube_i -> mea_mbrs_qtty;

	void *result_pkg = malloc(result_mem_size);

	agg_calcul_query(task_data_pkg + sizeof(int), *((int *) task_data_pkg) - sizeof(int), result_pkg + pkg_h_info_s, &aqs);

	if (aqs.total_num_of_valid_bytes + pkg_h_info_s == result_mem_size) {
		logI("agg_calcul_query_() result_mem_size = %d", aqs.total_num_of_valid_bytes);
	} else {
		logE("agg_calcul_query_() result_mem_size wrong! %d, %d", aqs.total_num_of_valid_bytes, result_mem_size);
	}

	((int *) result_pkg)[0] = result_mem_size;
	((int *) result_pkg)[1] = DPC__RET_AGG_DATA_S;

	*((long *) (result_pkg + 2 * sizeof(int))) = *((long *) (task_data_pkg + 4 * sizeof(int))); // set random ID

	return result_pkg;
} // agg_calcul_query_() end

int agg_calcul_query(void *req_data, int req_data_len, void *quy_result, struct aggcal_quyres_stct *aqs)
{
	logD("// temp > enter fn agg_calcul_query()\n");

	unsigned int cube_mg_id = ((int *) req_data)[1];
	unsigned int vectors_qtty_N = ((int *) req_data)[2];

log_("randomID randomID randomID randomID randomID randomID randomID randomID = %ld", *((long *) (req_data + 12)));

	int vector_data_len = cube_data_vector_len(cube_mg_id);
	int mea_members_qtty = cube_measure_mbrs_quantity(cube_mg_id);

	struct cube_info *ci_p = find_cube_info_by_mg_id(cube_mg_id);
	if (ci_p -> _cube_data_stct_ == NULL) {
		log_("cube[ %d ] data file not de loaded.\n", cube_mg_id);
		return -1;
	}

	int v_measure_part_len = mea_members_qtty * (1 + sizeof(double));

	logD("// temp > request code = %d\n", ((int *) req_data)[0]);
	logD("// temp > cube MG_ID = %d\n", cube_mg_id);
	logD("// temp > N (quantity of vectors) = %d\n", vectors_qtty_N);
	logD("// temp > request data length = %d\n", req_data_len);

	req_data += AGG_CAL_PKGH_RESERVED_LEN + 3 * sizeof(unsigned int);

	if ((AGG_CAL_PKGH_RESERVED_LEN + 3 * sizeof(unsigned int) + (vector_data_len - v_measure_part_len) * vectors_qtty_N) != req_data_len) {
		logD("// temp > ERROR! Query data bytes cannot be aligned !!!");
		return -1;
	}

	int i;
	unsigned int *q_vector_p;
	for (i = 0; i < vectors_qtty_N; i++) {
		// printf("TODO\tTODO\tTODO\tTODO\tTODO\tTODO\tTODO\tTODO\tTODO\tTODO\n");
		// logD("// temp > 得到cube在内存中的数据集结构体，此结构体要作为参数传进去，效率会高些。貌似这结构体还没定义呢。。。\n");

		q_vector_p = req_data + i * (vector_data_len - v_measure_part_len);

		vector_agg_calcul(NULL, ci_p, aqs, q_vector_p, quy_result + i * v_measure_part_len);

	}

	aqs -> total_num_of_valid_bytes = vectors_qtty_N * v_measure_part_len;

	logD("// temp > leave fn agg_calcul_query()\n");
	return 0;

} // function agg_calcul_query() end

static void vector_agg_calcul(void *params, struct cube_info *ci_p, struct aggcal_quyres_stct *aqs, unsigned int *q_vector_p, void *quy_result_p)
{
	struct cube_data_stct *cds = (struct cube_data_stct *) ci_p -> _cube_data_stct_;

	int coordinate_range[ci_p -> dm_roles_qtty][2];

	int i;
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		det_dm_role_range(NULL, ci_p, cds, i, q_vector_p, (int *) coordinate_range[i]);
		if (coordinate_range[i][0] < 0 || coordinate_range[i][1] < 0) {
			memset(quy_result_p, 1, (1 + sizeof(double)) * ci_p -> mea_mbrs_qtty);
			return;
		}
	}
	/*
	// test code !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		logD("// temp > dmRole[%d]. range[%d][%d]\n", i, coordinate_range[i][0], coordinate_range[i][1]);
	}
	// test code ?????????????????????????????????????????????????????????
	*/
	memset(quy_result_p, 1, (1 + sizeof(double)) * ci_p -> mea_mbrs_qtty);
	_vector_agg_calcul_(NULL, ci_p, 0, coordinate_range, quy_result_p, 0);
	
	/* int j;
	void *mea_values_p = cds -> measures_table;
	int jum_dis = (1 + sizeof(double)) * ci_p -> mea_mbrs_qtty; // measure velue jumping distance
	for (i = 0; i < ci_p -> dm_roles_qtty; i++) {
		for (j = coordinate_range[i][0]; j <= coordinate_range[i][1]; j++) {
			
		}
	} */

} // function vector_agg_calcul() end

static void _vector_agg_calcul_(void *params, struct cube_info *ci_p, int dimRole_locNum, void *_coordinate_range_,
											   void *_quy_result_p_, unsigned int adopted_value_idx)
{
	unsigned int *coordinate_range = (unsigned int *) _coordinate_range_;
	int i, j;
	if (dimRole_locNum == ci_p -> dm_roles_qtty - 1) {
		void *exact_p;
		void *quy_result_p;
		for (i = coordinate_range[dimRole_locNum * 2]; i <= coordinate_range[dimRole_locNum * 2 + 1]; i++) {
			quy_result_p = _quy_result_p_;

			exact_p = (((struct cube_data_stct *) (ci_p -> _cube_data_stct_)) -> measures_table) + (adopted_value_idx + i) * ((1 + sizeof(double)) * ci_p -> mea_mbrs_qtty);
			// exact_p = (ci_p -> _cube_data_stct_) -> measures_table;

			for (j = 0; j < ci_p -> mea_mbrs_qtty; j++) {
				char null_flag = *(char *) exact_p;
				double measure_value = *((double *) (exact_p + 1));

				if (null_flag == 0) {
					*(char *) quy_result_p = 0;
					*(double *) (quy_result_p + 1) += measure_value;
				}

				// *(char *) quy_result_p &= null_flag;
				// *(double *) (quy_result_p + 1) += measure_value;

				// exact_p += ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
				// quy_result_p += ci_p -> mea_mbrs_qtty * (1 + sizeof(double));
				exact_p += 1 + sizeof(double);
				quy_result_p += 1 + sizeof(double);
				// logD("// temp > SSSSSSSSSSSSSssssssssssssss\n");
			}
			// logD("// temp > XXXXXXXXXXXXXXXXXXXXXXXXX\n");
		}
		
	} else if (dimRole_locNum >= 0 && (dimRole_locNum < ci_p -> dm_roles_qtty - 1)) {
		for (i = coordinate_range[dimRole_locNum * 2]; i <= coordinate_range[dimRole_locNum * 2 + 1]; i++) {
			_vector_agg_calcul_(NULL, ci_p, dimRole_locNum + 1, _coordinate_range_, _quy_result_p_, adopted_value_idx + i * ((struct cube_data_stct *) (ci_p -> _cube_data_stct_)) -> dmroles_offset[dimRole_locNum]);
		}
	} else {
		logD("// temp > ERROR! Serious and unforgivable mistake!!! Program exit with EXIT_FAILURE [%d]\n", EXIT_FAILURE);
		exit(EXIT_FAILURE);
	}
} // function _vector_agg_calcul_() end

static void det_dm_role_range(void *params, struct cube_info *ci_p, struct cube_data_stct *cds, 
											     int dm_role_queue_num, unsigned int *q_vector_p, int *p_start_end)
{

	int member_max_lv = ci_p -> dm_roles_info_table[dm_role_queue_num][1];
	
	unsigned int *mbr_mg_id_path_p = q_vector_p;
	int i;
	for (i = 0; i < dm_role_queue_num; i++) {
		mbr_mg_id_path_p += ci_p -> dm_roles_info_table[i][1];
	}

	if (*mbr_mg_id_path_p == 0) { // root member
		p_start_end[0] = 0;
		p_start_end[1] = cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1;
		return;
	}
	
	int observed_length = 1; // members length
	for (i = 1; i < member_max_lv; i++) {
		if (*((unsigned int *) mbr_mg_id_path_p + i) > 0)
			observed_length++;
		else
			break;
	}
	// observed_length *= sizeof(unsigned int); // convert to the length of bytes
	
	int start_idx = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num], observed_length);
	if (start_idx < 0)
		goto pos_1;
	if (start_idx > 0)
		start_idx = -1;
	
	// void *temp_p1 = mbr_mg_id_path_p;
	void *temp_p2 
		= cds -> dmrole_tables[dm_role_queue_num] + (cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1) * (sizeof(unsigned int) * member_max_lv);
	int end_idx = ug_int_cmp(mbr_mg_id_path_p, temp_p2, observed_length);
	if (end_idx > 0)
		goto pos_1;

	if (end_idx < 0)
		end_idx = -1;
	else
		end_idx = cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1;

	
	unsigned int tmp_sta, tmp_end;
	int co_result, co_shw;
	// determine the location of start_idx
	if (start_idx == 0)
		goto pos_ei;

	tmp_sta = 0;
	tmp_end = cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1;
	while (1) {
		start_idx = (tmp_sta + tmp_end) / 2;
		co_result = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + start_idx * (sizeof(unsigned int) * member_max_lv), observed_length);

		if (co_result != 0 && tmp_sta == tmp_end) // no matching value can be found
			goto pos_1;

		if (co_result > 0)
			tmp_sta = start_idx < tmp_end ? start_idx + 1 : start_idx;
		else if (co_result < 0)
			tmp_end = start_idx > tmp_sta ? start_idx - 1 : start_idx;
		else { // co_result == 0
			if (start_idx == 0) // end_idx is the first coordinate.
				break;

			co_shw = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + (start_idx - 1) * (sizeof(unsigned int) * member_max_lv), observed_length);
			
			if (co_shw > 0)
				break;
			else if (co_shw == 0)
				tmp_end = start_idx > tmp_sta ? start_idx - 1 : start_idx;
			else { // co_shw < 0
				logD("// temp > ERROR!!! The data structure is confusing and the program is coming to an end!!!\n");
				exit(EXIT_FAILURE);
			}

		}

/*
		co_shw = start_idx == 0 ? 0
			: ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + (start_idx - 1) * (sizeof(unsigned int) * member_max_lv), observed_length);
		if ((co_result == 0 && start_idx == 0) || (co_result == 0 && start_idx > 0 && co_shw < 0))
			break;
		else if (co_result > 0)
			tmp_sta = start_idx < tmp_end ? start_idx + 1 : start_idx;
		else // co_result < 0
			tmp_end = start_idx > tmp_sta ? start_idx - 1 : start_idx;
*/

	}

	pos_ei:
	// logD("// temp > !!!!!!!!!!!!!!!!!!!!!!! pos_ei...POS_EI...pos_ei...POS_EI...pos_ei...POS_EI...pos_ei...POS_EI...\n");
	// determine the location of end_idx
	if (end_idx == cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1)
		goto complete;
	// logD("// temp > do not goto complete...\n");
	tmp_sta = 0;
	tmp_end = cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1;
	while (1) {
		// logD("// temp > enter the { ... } of while\n");
		// logD("// temp > tmp_sta = [%d], tmp_end = [%d]\n", tmp_sta, tmp_end);
		end_idx = (tmp_sta + tmp_end) / 2;
		co_result = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + end_idx * (sizeof(unsigned int) * member_max_lv), observed_length);
		// logD("// temp > end_idx = [%d], co_result = [%d]\n", end_idx, co_result);


		if (co_result != 0 && tmp_sta == tmp_end) // no matching value can be found
			goto pos_1;


		if (co_result < 0) {
			tmp_end = end_idx > tmp_sta ? end_idx - 1 : end_idx;
		} else if (co_result > 0) {
			tmp_sta = end_idx < tmp_end ? end_idx + 1 : end_idx;
		} else { // co_result == 0
			if (end_idx == cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1) // end_idx is the last coordinate.
				break;

			co_shw = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + (end_idx + 1) * (sizeof(unsigned int) * member_max_lv), observed_length);
			
			if (co_shw < 0) // end_idx is the end position
				break;
			else if (co_shw == 0)
				tmp_sta = end_idx < tmp_end ? end_idx + 1 : end_idx;
			else { // co_shw > 0
				logD("// temp > ERROR!!! The data structure is confusing and the program is coming to an end!!!\n");
				exit(EXIT_FAILURE);
			}
		}

/*
		if (start_idx < cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1)
			co_shw = ug_int_cmp(mbr_mg_id_path_p, cds -> dmrole_tables[dm_role_queue_num] + (start_idx + 1) * (sizeof(unsigned int) * member_max_lv), observed_length);

		if ((co_result == 0 && start_idx == cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1) || (co_result == 0 && start_idx < cds -> dmrole_mbrs_qtty_arr[dm_role_queue_num] - 1 && co_shw > 0))
			break;
		else if (co_result < 0)
			tmp_sta = start_idx < tmp_end ? start_idx + 1 : start_idx;
		else // co_result > 0
			tmp_end = start_idx > tmp_sta ? start_idx - 1 : start_idx;
*/
		// logD("// temp > leave the { ... } of while\n");
	}
	// logD("// temp > ????????????????????????????? pos_ei...POS_EI...pos_ei...POS_EI...pos_ei...POS_EI...pos_ei...POS_EI...\n");
	
	complete:
	p_start_end[0] = start_idx;
	p_start_end[1] = end_idx;
	return;
	
	pos_1:
	p_start_end[0] = -1;
	p_start_end[1] = -1;
} // function det_dm_role_range() end
