package com.tavern.bartender.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import com.tavern.bartender.models.OrdersDTO;
import com.tavern.bartender.models.DrinkMenu.DrinkType;

@Service
public class OrderingServiceImpl implements OrderingService {

	@Override
	public CompletableFuture<Void> serveOrder(OrdersDTO orderDetails, Integer prepTimeInSeconds) {

		Runnable prepareOrderTask = () -> {
			try {
				Integer prepTimeInMilliseconds = prepTimeInSeconds * 1000;

				Thread.sleep(prepTimeInMilliseconds);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};

		return CompletableFuture.runAsync(prepareOrderTask);

	}

}
