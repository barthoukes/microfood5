from com.grpc import sql_address_pb2_grpc
from com.grpc import sql_address_pb2
from com.grpc import common_types_pb2
import grpc


def run():
    with grpc.insecure_channel("127.0.0.1:50051") as channel:
        svc = sql_address_pb2_grpc.AddressServiceStub(channel)
        msg = common_types_pb2.Empty()
        print("Sending request:", msg, "<")
        response = svc.GetAllLines(msg)
        print("RESPONSE:", response)


if __name__ == "__main__":
    run()
