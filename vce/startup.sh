#!/bin/bash

set -e

rr="/root/vce"

cd ${rr}/work_1
./main_vce.out 1 &

cd ${rr}/work_2
./main_vce.out 2 &

cd ${rr}/work_3
./main_vce.out 3 &
