package com.tavern.bartender.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tavern.bartender.models.OrdersDTO;
import com.tavern.bartender.models.OrdersWrapper;
import com.tavern.bartender.service.OrderingService;

@RestController
@RequestMapping("/ordering-api")
public class OrderController {
	
	private static Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired 
	OrderingService orderingService;
	
	static List<OrdersDTO> orderlist = new ArrayList<OrdersDTO>();
	
	@PostMapping(value="/customers/{customerId}/orders/{drinkType}")
	public ResponseEntity<?> serveCustomerOrder(
		  @PathVariable(name="customerId", required=true) long customerId
		 ,@PathVariable(name="drinkType", required=true)  String drinkType
		 ,@RequestParam(name="prepTimeInSeconds", required=false, defaultValue="5") Integer prepTimeInSeconds
		 ,HttpServletRequest request) throws Exception
	{
		StringBuilder requestInfo = new StringBuilder();
		requestInfo.append(Instant.now() + ": Request method:  "+ request.getMethod());
		requestInfo.append(", Request URI: " + request.getRequestURI());
		requestInfo.append(request.getQueryString() != null ? "?"+request.getQueryString(): "");
		
		LOGGER.info(requestInfo.toString());
	
		if(!(drinkType.trim().toUpperCase().equals("BEER") || drinkType.trim().toUpperCase().equals("DRINK"))){
			LOGGER.error(Instant.now() + ": Status Code: " + HttpStatus.TOO_MANY_REQUESTS + " , Message: " + "Order is not accepted at the moment");
			return new ResponseEntity<String>("Order is not accepted at the moment", HttpStatus.TOO_MANY_REQUESTS);
		}
		
		OrdersDTO orderDetails = new OrdersDTO();
		orderDetails.setCustomerId(customerId);
		orderDetails.setDrinkType(drinkType);
		
		LOGGER.info(Instant.now() + ": Preparing Order: " + drinkType);
		
		CompletableFuture<Void> serving = orderingService.serveOrder(orderDetails, prepTimeInSeconds);
		
		serving.whenComplete((result, exception) -> {
			if(exception !=null) {
				LOGGER.error(Instant.now() + ": Order processing failed.");
			}else {
				orderlist.add(orderDetails);
				LOGGER.info(Instant.now() + ": Order is served.");
			}
		});
		
		return new ResponseEntity<String>("Order will be served: " + drinkType, HttpStatus.OK);
	}
	
	@GetMapping(value="/orders")
	public ResponseEntity<?> getOrders(){
	
		OrdersWrapper wrapper = new OrdersWrapper();
		wrapper.setOrdersList(orderlist);
		return new ResponseEntity<OrdersWrapper>(wrapper, HttpStatus.OK);
		
	}
	
}
