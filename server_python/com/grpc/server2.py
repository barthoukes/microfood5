from com.grpc import sql_address_pb2
from com.grpc import sql_address_pb2_grpc
from concurrent import futures
import grpc

class AddressServicer(sql_address_pb2_grpc.AddressServiceServicer):
    def GetAllLines(self, request, context):
        print(f"GetAllLines: {request.payload}")
        all = sql_address_pb2.AddressLineList()
        # --- Method 1: Add AddressLine objects one by one ---
        line1 = all.line.add()  # Adds a new empty AddressLine to the list
        line1.line_id = 1
        line1.value = "123 Main Street"
        line1.features = 5

        line2 = all.line.add()
        line2.line_id = 2
        line2.value = "Apt 4B"
        line2.features = 3
        return all

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
