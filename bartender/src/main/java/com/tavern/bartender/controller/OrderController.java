package com.tavern.bartender.controller;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	private static Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired 
	OrderingService orderingService;
	
	private static List<OrdersDTO> orderlist = new ArrayList<>();
	private static AtomicInteger beerCounter = new AtomicInteger(0);
	private static AtomicInteger drinkCounter = new AtomicInteger(0);
	private DateTimeFormatter logTimestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	
	@PostMapping(value="/customers/{customerId}/orders/{drinkType}")
	public ResponseEntity<String> serveCustomerOrder(
		  @PathVariable(name="customerId", required=true) long customerId
		 ,@PathVariable(name="drinkType", required=true)  String drinkType
		 ,@RequestParam(name="prepTimeInSeconds", required=false, defaultValue="5") Integer prepTimeInSeconds
		 ,HttpServletRequest request)
	{
		logger.info("{} : Request method: {} , Request URI: {}{} "
				, ZonedDateTime.now().format(logTimestamp), request.getMethod()
				, request.getRequestURI(), request.getQueryString() != null ? "?"+request.getQueryString(): "");
		
		if(drinkType.trim().equalsIgnoreCase("BEER")) {
			if(beerCounter.get() >=2) {
				logger.error("{} : Status Code: {} , Message: Cannot accept orders at the moment.", ZonedDateTime.now().format(logTimestamp) , HttpStatus.TOO_MANY_REQUESTS);
				return new ResponseEntity<>("Cannot accept orders at the moment", HttpStatus.TOO_MANY_REQUESTS);
			}
			  
			beerCounter.incrementAndGet();
		}else if(drinkType.trim().equalsIgnoreCase("DRINK")){
			  
			if(drinkCounter.get() >= 1) {
				logger.error("{} : Status Code: {} , Message: Cannot accept orders at the moment.", ZonedDateTime.now().format(logTimestamp) , HttpStatus.TOO_MANY_REQUESTS);
				return new ResponseEntity<>("Cannot accept orders at the moment", HttpStatus.TOO_MANY_REQUESTS);
  		    }
			
			drinkCounter.incrementAndGet();
		}else{
			logger.error("{} : Status Code: {} , Message: Order is not on the menu.", ZonedDateTime.now().format(logTimestamp) , HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>("Order is not on the menu.", HttpStatus.BAD_REQUEST);
		}
		
		OrdersDTO orderDetails = new OrdersDTO();
		orderDetails.setCustomerId(customerId);
		orderDetails.setDrinkType(drinkType);
		
		logger.info("{} : Preparing Order: {}", ZonedDateTime.now().format(logTimestamp) , drinkType);
		
		CompletableFuture<Void> serving = orderingService.serveOrder(orderDetails, prepTimeInSeconds);
		
		serving.whenComplete((result, exception) -> {
			
			if(drinkType.trim().equalsIgnoreCase("BEER")) {
				beerCounter.decrementAndGet();
			}else{
				drinkCounter.decrementAndGet();
			}
			
			if(exception !=null) {
				logger.error(String.format("%s : Order processing failed.", ZonedDateTime.now().format(logTimestamp)));
			}else {
				orderlist.add(orderDetails);
				logger.info(String.format("%s : %s Order is served.", ZonedDateTime.now().format(logTimestamp), drinkType));
			}
		});
		
		return new ResponseEntity<>("Order will be served: " + drinkType, HttpStatus.OK);
	}
	
	@GetMapping(value="/orders")
	public ResponseEntity<OrdersWrapper> getOrders(HttpServletRequest request){
	    
		logger.info("{} : Request method: {} , Request URI: {}"
				, ZonedDateTime.now().format(logTimestamp), request.getMethod()
				, request.getRequestURI());
		
		OrdersWrapper wrapper = new OrdersWrapper();
		wrapper.setOrdersList(orderlist);
		
		logger.info("{} : Return Code: {}", ZonedDateTime.now().format(logTimestamp), HttpStatus.OK);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
		
	}
		
}
