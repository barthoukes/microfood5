#!/usr/bin/env bash

source ./venv/bin/activate

python3 -m grpc_tools.protoc -I proto --python_out=. --grpc_python_out=. proto/pingpong.proto
