package com.example.grpc;

import com.grpc.HelloRequest;
import com.grpc.HelloResponse;
import com.grpc.HelloServiceGrpc;
import com.grpc.StockRequest;
import com.grpc.StockResponse;
import com.grpc.StockServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private final HelloServiceGrpc.HelloServiceBlockingStub helloServiceBlockingStub;
    private final StockServiceGrpc.StockServiceBlockingStub stockServiceBlockingStub;
    private final StockServiceGrpc.StockServiceStub stockServiceStub;
    private final List<StockRequest> stockRequests = new ArrayList<>();

    public GrpcClient(ManagedChannel channel) {
        helloServiceBlockingStub = HelloServiceGrpc.newBlockingStub(channel);
        stockServiceBlockingStub = StockServiceGrpc.newBlockingStub(channel);
        stockServiceStub = StockServiceGrpc.newStub(channel);

        prepareStockRequests();
    }


    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
        GrpcClient grpcClient = new GrpcClient(channel);
        System.out.println("**********************************hello*****************************************");
        grpcClient.hello();
        System.out.println("**************************tickerPriceStreamResponse******************************");
        grpcClient.tickerPriceStreamResponse();
        System.out.println("*****************************getAverageTickerPrice*******************************");
        grpcClient.getAverageTickerPrice();
        System.out.println("***********************getTickerPriceStreamRequestResponse***********************");
        grpcClient.getTickerPriceStreamRequestResponse();
        channel.shutdownNow();
    }

    public void hello() {
        HelloResponse helloResponse = helloServiceBlockingStub.hello(HelloRequest.newBuilder()
                .setFirstName("Shashi Kant")
                .setLastName("Sharma")
                .build());

        System.out.printf("HelloResponse: %s%n", helloResponse.getGreeting());
    }

    public void tickerPriceStreamResponse() {
        Iterator<StockResponse> stockResponseIterator = stockServiceBlockingStub.getTickerPriceStreamResponse(StockRequest.newBuilder()
                .setTickerSymbol("APP")
                .setCompanyName("apple")
                .build());

        do {
            StockResponse stockResponse = stockResponseIterator.next();
            System.out.println(stockResponse);
        } while (stockResponseIterator.hasNext());
    }

    public void getAverageTickerPrice() throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<StockResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StockResponse stockResponse) {
                System.out.println("* Grpc Client: " + stockResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("* Grpc Client: " + Status.fromThrowable(throwable));
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("* Grpc Client: on complete");
                finishLatch.countDown();
            }
        };

        StreamObserver<StockRequest> requestObserver = stockServiceStub.getAverageTickerPrice(responseObserver);
        sendRequests(requestObserver, finishLatch);

        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("getAverageTickerPrice can not finish within 1 minutes");
        }
    }

    public void getTickerPriceStreamRequestResponse() throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<StockResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StockResponse stockResponse) {
                System.out.println("* Grpc Client: " + stockResponse);
            }

            @Override
            public void onCompleted() {
                System.out.println("* Grpc Client: on complete");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("* Grpc Client: " + Status.fromThrowable(throwable));
                finishLatch.countDown();
            }
        };

        StreamObserver<StockRequest> requestObserver = stockServiceStub.getTickerPriceStreamRequestResponse(responseObserver);
        sendRequests(requestObserver, finishLatch);

        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("getAverageTickerPrice can not finish within 1 minutes");
        }
    }

    private void prepareStockRequests() {
        stockRequests.add(StockRequest.newBuilder().setTickerSymbol("APPL").setCompanyName("apple").build());
        stockRequests.add(StockRequest.newBuilder().setTickerSymbol("MA").setCompanyName("microsoft").build());
        stockRequests.add(StockRequest.newBuilder().setTickerSymbol("AZN").setCompanyName("amazon").build());
        stockRequests.add(StockRequest.newBuilder().setTickerSymbol("GGL").setCompanyName("google").build());
    }

    private void sendRequests(StreamObserver<StockRequest> requestObserver, CountDownLatch finishLatch) {
        for (StockRequest stockRequest : stockRequests) {
            requestObserver.onNext(stockRequest);
            if (finishLatch.getCount() == 0) {
                return;
            }
        }
    }
}
