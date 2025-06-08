from com.grpc import sql_address_pb2_grpc
from com.grpc import sql_address_pb2
from com.grpc import common_types_pb2
import grpc

def run():
    with grpc.insecure_channel("127.0.0.1:50053") as channel:
        server = sql_address_pb2_grpc.AddressServiceStub(channel)
        msg = common_types_pb2.Empty()
        #msg = generate_content()
        response = server.GetAllLines(msg)
        print("Received AddressLineList:", response)
        for line in response.line:
            print(f"ID: {line.line_id}, Value: '{line.value}', Features: {line.features}")


def generate_content():
    # Create an empty AddressLineList
    address_line_list = sql_address_pb2.AddressLineList()

    # --- Method 1: Add AddressLine objects one by one ---
    line1 = address_line_list.line.add()  # Adds a new empty AddressLine to the list
    line1.line_id = 1
    line1.value = "123 Main Street"
    line1.features = 5

    line2 = address_line_list.line.add()
    line2.line_id = 2
    line2.value = "Apt 4B"
    line2.features = 3
    return address_line_list

if __name__ == "__main__":
    run()

