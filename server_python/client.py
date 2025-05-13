import pingpong_pb2_grpc
import pingpong_pb2
import grpc


def run():
    with grpc.insecure_channel("127.0.0.1:50051") as channel:
        pingpong_svc = pingpong_pb2_grpc.PingPongStub(channel)
        msg = pingpong_pb2.PingPongMsg()
        msg.payload = "Sikorski"
        response = pingpong_svc.ping(msg)
        print(response)


if __name__ == "__main__":
    run()
