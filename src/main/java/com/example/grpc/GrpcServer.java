package com.example.grpc;

import com.example.grpc.services.HelloServiceImpl;
import com.example.grpc.services.StockServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServer {

    private final Server server;

    @Autowired
    public GrpcServer(HelloServiceImpl helloService,
                      StockServiceImpl stockService) {
        this.server = ServerBuilder.forPort(8080)
                .addService(helloService)
                .addService(stockService)
                .build();
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        server.start();
        server.awaitTermination();
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
}
