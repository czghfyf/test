// #include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "node_master.h"
#include "vce_map_reduce.h"
#include "cube_mng.h"

extern struct work_link_thread_info *WLL_HEAD;

static char *reduce_buf;
static size_t reduce_buf_len;
static size_t reduce_size;

void map_reduce_init()
{
	reduce_buf_len = 1024 * 1024 * 4; // 4 M
	reduce_buf = (char *) malloc(reduce_buf_len);
} // function map_reduce_init() end


int agg_map_reduce(void *params, void *m_buf, size_t m_size, void **r_buf, size_t *r_size)
{
	int size_of_int = sizeof(int);

	struct work_link_thread_info *_h_;
	for (_h_ = WLL_HEAD; _h_ != NULL; _h_ = _h_ -> next) {
		_h_ -> req_buf = (char *) m_buf;
		_h_ -> req_buf_len = m_size;
		_h_ -> completed_f = 0;
		sem_post(&_h_ -> sem);
	}

	char com_flag;
	while (1) {
		com_flag = 1;
		for (_h_ = WLL_HEAD; _h_ != NULL; _h_ = _h_ -> next)
			com_flag &= _h_ -> completed_f;
		if (com_flag)
			break;
	}

	/*
	for (_h_ = WLL_HEAD; _h_ != NULL; _h_ = _h_ -> next) {
		if (*((int *) (_h_ -> resp_buf)) != 1) {
			*((int *) reduce_buf) = -1;
			*r_buf = reduce_buf;
			*r_size = size_of_int;
			return -1;
		}
	}
	*/

	unsigned int cube_mg_id = ((unsigned int *) m_buf)[2];
	unsigned int agg_q_qtty = ((unsigned int *) m_buf)[3];
	struct cube_info *cube_i = find_cube_info_by_mg_id(cube_mg_id);

	memcpy(reduce_buf, WLL_HEAD -> resp_buf, WLL_HEAD -> r_size);
	int size__flag_doub = sizeof(double) + 1;
	unsigned int i, mea_val_qtty = agg_q_qtty * cube_i -> mea_mbrs_qtty;
	for (_h_ = WLL_HEAD -> next; _h_ != NULL; _h_ = _h_ -> next) {




		// char *targ = reduce_buf + size_of_int;
		// char *from = _h_ -> resp_buf + size_of_int;
		char *targ = reduce_buf;
		char *from = _h_ -> resp_buf;




		// double *val_dp = (double *) (reduce_buf + size_of_int + 1);
		for (i = 0; i < mea_val_qtty; i++) {
			// from += size__flag_doub * i;
			if (*from) // null measure value
				goto _continue_flag_000; // continue;
			// targ += size__flag_doub * i;
			if (*targ) {
				memcpy(targ, from, size__flag_doub);
			} else {
				*((double *) (targ + 1)) = *((double *) (from + 1)) + *((double *) (targ + 1));
			}

			_continue_flag_000:
			from += size__flag_doub;
			targ += size__flag_doub;
		}
	}
	
	*r_buf = reduce_buf;
	*r_size = WLL_HEAD -> r_size;

	return 1;
}
