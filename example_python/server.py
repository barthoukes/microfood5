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
import sql_address_pb2
import sql_address_pb2_grpc

class AddressServicer(sql_address_pb2_grpc.AddressServiceServicer):

    def SetAddressLine(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"SetAddressLine: {request.payload}")

    def MysqlDump(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"MysqlDump: {request.payload}")

    def GetAllLines(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"GetAllLines")
        all = sql_address_pb2.AddressLineList()
        # --- Method 1: Add AddressLine objects one by one ---
        line1 = all.line.add()  # Adds a new empty AddressLine to the list
        print(dir(line1))
        line1.line_id = 1
        line1.value = "123 Main Street"
        line1.features = 5

        line2 = all.line.add()
        line2.line_id = 2
        line2.value = "Apt 4B"
        line2.features = 3
        print(all)
        return all

    def BackupAddress(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"BackupAddress: {request.payload}")
    
    def RestoreAddress(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"RestoreAddress: {request.payload}")

    def RemoveAddressLine(self, request, context):
        print("\n=== SERVER DEBUG ===")
        print(f"Request type: {type(request)}")
        print(f"Request content: {request}")
        print(f"RemoveAddressLine: {request.payload}")

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    address_servicer = AddressServicer()
    sql_address_pb2_grpc.add_AddressServiceServicer_to_server(address_servicer, server)
    server.add_insecure_port("0.0.0.0:50053")
    server.start()
    print("Server started")
    server.wait_for_termination()
    print("Server stopped")

if __name__ == "__main__":
    serve()
