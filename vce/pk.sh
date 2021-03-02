#!/bin/bash

set -e

rr="/root/vce"

rm -rf ${rr}/_temp/*

cp ${rr}/cfg/* ${rr}/_temp
cp ${rr}/listen/* ${rr}/_temp
cp ${rr}/common/* ${rr}/_temp
cp ${rr}/cube/* ${rr}/_temp
# cp ${rr}/svr/* ${rr}/_temp
# cp ${rr}/slave/* ${rr}/_temp
cp ${rr}/node_master/* ${rr}/_temp
cp ${rr}/map_reduce/* ${rr}/_temp
cp ${rr}/log/* ${rr}/_temp
cp ${rr}/concurrency/* ${rr}/_temp

cp ${rr}/main_vce.c ${rr}/_temp
cp ${rr}/main_vce_client.c ${rr}/_temp
cp ${rr}/vce_enum_const.h ${rr}/_temp

cd ${rr}/_temp

if [ "g" = "$1" ]; then
	echo 'gdb'
	# gcc main_vce.c vce_cfg.c vce_svr.c vce_master.c vce_slave.c -o main_vce.out -g -lpthread
	gcc main_vce.c vce_cfg.c vce_common.c vce_listen.c cube_mng.c agg_calcul.c mea_update.c node_master.c \
	vce_map_reduce.c vce_log.c tasks_manager.c vce_tcp_llnk_th.c task_result_seg_pool.c socket_thread_pool.c \
	-o main_vce.out -lpthread -g
	# gcc client.c -o client.out -g
else
	# echo 'without gdb'
	# gcc main_vce.c vce_cfg.c vce_svr.c vce_master.c vce_slave.c -o main_vce.out -lpthread
	gcc main_vce.c vce_cfg.c vce_common.c vce_listen.c cube_mng.c agg_calcul.c mea_update.c node_master.c \
	vce_map_reduce.c vce_log.c tasks_manager.c vce_tcp_llnk_th.c task_result_seg_pool.c socket_thread_pool.c \
	-o main_vce.out -lpthread
	# gcc client.c -o client.out
fi

gcc main_vce_client.c -o main_vce_client.out -g

rm -f ${rr}/work_1/*.out
rm -f ${rr}/work_2/*.out
rm -f ${rr}/work_3/*.out
rm -f ${rr}/master_1/*.out
rm -f ${rr}/master_A/*.out
rm -f ${rr}/master_B/*.out

cp ${rr}/_temp/*.out ${rr}/work_1
cp ${rr}/_temp/*.out ${rr}/work_2
cp ${rr}/_temp/*.out ${rr}/work_3
# cp ${rr}/vce-cfg ${rr}/out
cp ${rr}/_temp/*.out ${rr}/master_1
cp ${rr}/_temp/*.out ${rr}/master_A
cp ${rr}/_temp/*.out ${rr}/master_B

cp ${rr}/_temp/main_vce_client.out ${rr}/

# rm -rf ${rr}/_temp/*

echo 'success'
