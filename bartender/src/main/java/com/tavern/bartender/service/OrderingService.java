package com.tavern.bartender.service;

import java.util.concurrent.CompletableFuture;

import com.tavern.bartender.models.OrdersDTO;

public interface OrderingService {
    CompletableFuture<Void> serveOrder(OrdersDTO orderDetails, Integer prepTimeInSeconds);
}
