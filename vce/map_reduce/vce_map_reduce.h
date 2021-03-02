#ifndef _VCE_MAP_REDUCE_H_
#define _VCE_MAP_REDUCE_H_ 1

void map_reduce_init();

int agg_map_reduce(void *params, void *m_buf, size_t m_size, void **r_buf, size_t *r_size);

#endif
