package com.tavern.bartender.service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tavern.bartender.controller.OrderController;
import com.tavern.bartender.models.OrdersDTO;

@Service
public class OrderingServiceImpl implements OrderingService{

	//private static Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
	
	@Override
	public CompletableFuture<Void> serveOrder(OrdersDTO orderDetails, Integer prepTimeInSeconds) {
		return CompletableFuture.runAsync(()-> {
			try {
			   Integer prepTimeInMilliseconds = prepTimeInSeconds * 1000;
			    
				if(orderDetails.getDrinkType().trim().toUpperCase().equals("BEER")) {
					prepTimeInMilliseconds = prepTimeInMilliseconds * 2 ;
				} 
				Thread.sleep(prepTimeInMilliseconds);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		});
		
	}

}
