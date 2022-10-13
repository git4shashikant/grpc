package com.example.grpc.services;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import com.google.rpc.ErrorInfo;
import com.grpc.ErrorResponse;
import com.grpc.HelloRequest;
import com.grpc.HelloResponse;
import com.grpc.HelloServiceGrpc;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("Request received from client:\n" + request);
        HelloResponse response = null;

        /* error handling */
        if (request.getFirstName().equals("123")) {
            // sendErrorStatusInResponseObserverOnError(responseObserver);
            com.google.rpc.Status status = createRpcStatus(request);
            /* you can either send this status on error or set it in response */
            // responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            response = HelloResponse.newBuilder().setStatus(status).build();
        } else {
            String greeting = new StringBuilder().append("Hello, ")
                    .append(request.getFirstName())
                    .append(" ")
                    .append(request.getLastName())
                    .toString();

            response = HelloResponse.newBuilder().setGreeting(greeting).build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sendGRpcStatusInResponseObserverOnError(StreamObserver<HelloResponse> responseObserver) {
        Metadata.Key<ErrorResponse> errorResponseKey = ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance());
        ErrorResponse errorResponse = ErrorResponse.newBuilder()
                .setErrorCode(1001)
                .setErrorMessage("first name can't be null")
                .setTimestamp(Timestamp.newBuilder().setNanos(Instant.now().getNano()).build())
                .build();
        Metadata metadata = new Metadata();
        metadata.put(errorResponseKey, errorResponse);
        responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Argument provided is incorrect").asRuntimeException(metadata));
    }

    private com.google.rpc.Status createRpcStatus(HelloRequest request) {
        return com.google.rpc.Status.newBuilder()
                .setCode(Status.INVALID_ARGUMENT.getCode().value())
                .setMessage("Argument provided is incorrect")
                .addDetails(Any.pack(ErrorInfo.newBuilder()
                        .setDomain("com.example.grpc.services.HelloServiceImpl")
                        .setReason("Argument provided is incorrect")
                        .putMetadata("firstname", request.getFirstName())
                        .build()))
                .build();
    }
}