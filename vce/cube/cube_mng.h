#ifndef _VCE_CUBE_MNG_H_
#define _VCE_CUBE_MNG_H_ 1

#define CUBE_INFO_FILE_PREFIX "cube_info_"
#define CUBE_DATA_FILE_PREFIX "cube_data_file_"
#define CUBE_UD_FILE_PREFIX   "cube_ud_" // be used to update and delete cube data

struct cube_info {
	int cube_mg_id;
	int dm_roles_qtty; // dimension roles quantity
	int mea_mbrs_qtty; // measure members quantity
	int data_vector_len;
	char cube_file_path[256];
	int cube_file_size; // bytes
	// void *cube_info_addr;
	struct cube_info *next;
	unsigned int dm_roles_info_table[512][3]; // [ [ dimension_role_mg_id, member_max_lv, amount of leaf members ] ... ]
	void *_cube_data_stct_; // struct cube_data_stct *_cube_data_stct_
};

int build_cube(void *addr, int effect_len);

int mem_writeback_cubedf(unsigned int cube_mg_id);

int insert_measure_values(void *addr, int effect_len);

// splice cube info file path by cube MG_ID
void splice_cube_info_f(unsigned int cube_mg_id, char *cube_df_path);

// splice cube data file path by cube MG_ID
void splice_cube_dfpath_by_mgid(unsigned int cube_mg_id, char *cube_df_path);

void splice_cube_UD_file(unsigned int cube_mg_id, char *cube_UD_f);

int cube_data_vector_len(int cube_mg_id);

int cube_measure_mbrs_quantity(int cube_mg_id);

void load_all_cubes_info();

// void load_cube_data(int cube_mg_id);

void load_all_cubes_data();

struct cube_info *find_cube_info_by_mg_id(int cube_mg_id);

void rebuild_cube_mem_data_struct(unsigned int cube_mg_id);

int positioning_vector_index(void *params, struct cube_info *ci_p, void *vector_p, void *_cds);

#endif
