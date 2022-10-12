package com.example.grpc.services;

import com.google.protobuf.Timestamp;
import com.grpc.StockRequest;
import com.grpc.StockResponse;
import com.grpc.StockServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class StockServiceImpl extends StockServiceGrpc.StockServiceImplBase {

    @Override
    public void getTickerPriceStreamResponse(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        for (int i = 1; i < 10; i++) {
            StockResponse stockResponse = StockResponse.newBuilder()
                    .setTickerSymbol(request.getTickerSymbol())
                    .setStockPrice(i*2.3)
                    .setTimestamp(Timestamp.newBuilder().setNanos(Instant.now().getNano()).build())
                    .build();
            responseObserver.onNext(stockResponse);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StockRequest> getAverageTickerPrice(StreamObserver<StockResponse> responseObserver) {
        return new StreamObserver<StockRequest>() {
            double price = 0.0;
            int count = 0;
            @Override
            public void onNext(StockRequest stockRequest) {
                count++;
                price += tickerPrice(stockRequest.getTickerSymbol());
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(StockResponse.newBuilder()
                        .setStockPrice(price/count)
                        .setTimestamp(Timestamp.newBuilder().setNanos(Instant.now().getNano()).build())
                        .build());
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("StockServiceImpl --> getAverageTickerPrice() " + Status.fromThrowable(throwable));
            }
        };
    }

    @Override
    public StreamObserver<StockRequest> getTickerPriceStreamRequestResponse(StreamObserver<StockResponse> responseObserver) {
        return new StreamObserver<StockRequest>() {
            @Override
            public void onNext(StockRequest request) {

                for (int i = 1; i <= 5; i++) {
                    StockResponse stockResponse = StockResponse.newBuilder()
                            .setTickerSymbol(request.getTickerSymbol())
                            .setStockPrice(i*1.2)
                            .setTimestamp(Timestamp.newBuilder().setNanos(Instant.now().getNano()).build())
                            .build();
                    responseObserver.onNext(stockResponse);
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error:{}" +  t.getMessage());
            }
        };
    }

    private double tickerPrice(String ticker) {
        Random random = new Random();
        return random.nextDouble();
    }
}
