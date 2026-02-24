#!/usr/bin/env python3

from concurrent import futures
import grpc

import sys
from pathlib import Path

# Add the 'generated' folder to sys.path
generated_dir = Path(__file__).parent / "generated"
sys.path.insert(0, str(generated_dir))   # or append, insert at 0 gives priority

print("sys.path after insertion:", sys.path)   # Debug line

# Now try importing
import common_types_pb2
import sql_address_pb2
import sql_address_pb2_grpc


def run():
    with grpc.insecure_channel("127.0.0.1:50053") as channel:
        svc = sql_address_pb2_grpc.AddressServiceStub(channel)
        msg = common_types_pb2.Empty()
        print("Sending request:", msg, "<")
        response = svc.GetAllLines(msg)
        print("RESPONSE:", response)


if __name__ == "__main__":
    run()
