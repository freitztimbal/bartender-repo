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

import com.tavern.bartender.models.Constants;
import com.tavern.bartender.models.DrinkMenu;
import com.tavern.bartender.models.DrinkMenu.DrinkType;
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

	@PostMapping(value = "/customers/{customerId}/orders/{drinkType}")
	public ResponseEntity<String> serveCustomerOrder(
			@PathVariable(name = "customerId", required = true) long customerId,
			@PathVariable(name = "drinkType", required = true) String drinkType,
			@RequestParam(name = "prepTimeInSeconds", required = false, defaultValue = "5") Integer prepTimeInSeconds,
			HttpServletRequest request) {
		logger.info("{} : Request method: {} , Request URI: {}{} ", ZonedDateTime.now().format(Constants.logTimestamp),
				request.getMethod(), request.getRequestURI(),
				request.getQueryString() != null ? "?" + request.getQueryString() : "");
        try {
        	if (!DrinkMenu.isValidOrder(drinkType.trim().toUpperCase())) 
        	{
    			return new ResponseEntity<>("Cannot accept orders at the moment", HttpStatus.TOO_MANY_REQUESTS);
    		}
        }catch(IllegalArgumentException e) {
        	logger.error("{} : Status Code: {} , Message: Order is not on the menu.",
					ZonedDateTime.now().format(Constants.logTimestamp), HttpStatus.BAD_REQUEST);
        	return new ResponseEntity<>("Message: Order is not on the menu.", HttpStatus.BAD_REQUEST);
        }
		

		OrdersDTO orderDetails = new OrdersDTO();
		orderDetails.setCustomerId(customerId);
		orderDetails.setDrinkType(drinkType.trim().toUpperCase());

		logger.info("{} : Preparing Order: {}", ZonedDateTime.now().format(Constants.logTimestamp), drinkType);

		CompletableFuture<Void> serving = orderingService.serveOrder(orderDetails, prepTimeInSeconds);

		serving.whenComplete((result, exception) -> {

			DrinkType.valueOf(drinkType.trim().toUpperCase()).decrementCounter();

			if (exception != null || Thread.currentThread().isInterrupted()) {
				logger.error(String.format("%s : Order processing failed.",
						ZonedDateTime.now().format(Constants.logTimestamp)));
			} else {
				orderlist.add(orderDetails);
				logger.info(String.format("%s : %s Order is served.",
						ZonedDateTime.now().format(Constants.logTimestamp), drinkType));
			}
		});

		return new ResponseEntity<>("Order will be served: " + drinkType, HttpStatus.OK);
	}

	@GetMapping(value = "/orders")
	public ResponseEntity<OrdersWrapper> getOrders(HttpServletRequest request) {

		logger.info("{} : Request method: {} , Request URI: {}", ZonedDateTime.now().format(Constants.logTimestamp),
				request.getMethod(), request.getRequestURI());

		OrdersWrapper wrapper = new OrdersWrapper();
		wrapper.setOrdersList(orderlist);

		logger.info("{} : Return Code: {}", ZonedDateTime.now().format(Constants.logTimestamp), HttpStatus.OK);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);

	}

}
