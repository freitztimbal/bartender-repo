package com.tavern.bartender.models;

import java.util.List;

public class OrdersWrapper {
	List<OrdersDTO> ordersList;

	public List<OrdersDTO> getOrdersList() {
		return ordersList;
	}
	
	public void setOrdersList(List<OrdersDTO> ordersList) {
		this.ordersList = ordersList;
	}
  
}
