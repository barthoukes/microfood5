import pingpong_pb2
import pingpong_pb2_grpc
from concurrent import futures
import grpc

class PingPongServicer(pingpong_pb2_grpc.PingPongServicer):
    def ping(self, request, context):
        print(f"Received: {request.payload}")
        ppmsg = pingpong_pb2.PingPongMsg()
        ppmsg.payload = f"Hello, {request.payload}"
        print(f"About to reply with {ppmsg.payload}")
        return ppmsg

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    pingpong_servicer = PingPongServicer()
    pingpong_pb2_grpc.add_PingPongServicer_to_server(pingpong_servicer, server)
    server.add_insecure_port("0.0.0.0:50051")
    server.start()
    print("Server started")
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
