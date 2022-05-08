package com.tavern.bartender.models;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class DrinkMenu {
	private static Logger logger = LoggerFactory.getLogger(DrinkMenu.class);

	static AtomicInteger beerCounter = new AtomicInteger(0);
	static AtomicInteger drinkCounter = new AtomicInteger(0);

	public enum DrinkType {

		BEER("BEER") {
			@Override
			public boolean isServable() {
				if (beerCounter.get() >= 2) {
					logger.error("{} : Status Code: {} , Message: Cannot accept orders at the moment.",
							ZonedDateTime.now().format(Constants.logTimestamp), HttpStatus.TOO_MANY_REQUESTS);
					return false;
				}

				
				return true;
			}

			@Override
			public AtomicInteger getCounter() {
				//logger.info("beer counter: {} ", beerCounter);
				return beerCounter;
			}

			@Override
			public void decrementCounter() {
				beerCounter.decrementAndGet();
				//logger.info("beer counter: {} ", beerCounter);
			}
			
			@Override
			public void incrementCounter() {
				beerCounter.incrementAndGet();
				//logger.info("beer counter: {} ", beerCounter);
			}
		},

		DRINK("DRINK") {
			@Override
			public boolean isServable() {
				if (drinkCounter.get() >= 1) {
					logger.error("{} : Status Code: {} , Message: Cannot accept orders at the moment.",
							ZonedDateTime.now().format(Constants.logTimestamp), HttpStatus.TOO_MANY_REQUESTS);
					return false;
				}

				
				return true;
			}
            
			@Override
			public AtomicInteger getCounter() {
				//logger.info("beer counter: {} ", beerCounter);
				return drinkCounter;
			}
			
			@Override
			public void incrementCounter() {
				drinkCounter.incrementAndGet();
			}

			@Override
			public void decrementCounter() {
				drinkCounter.decrementAndGet();
			//	logger.info("drink counter: {} ", drinkCounter);
			}

		};

		private String drinktype;

		DrinkType(String drinktype) {
			this.drinktype = drinktype;
		}

		public boolean isServable() {
			logger.error("{} : Status Code: {} , Message: Order is not on the menu.",
					ZonedDateTime.now().format(Constants.logTimestamp), HttpStatus.BAD_REQUEST);
			return false;

		}

		public void decrementCounter() {
			// holder
		}
		
		public void incrementCounter() {
			// holder
		}

		public AtomicInteger getCounter() {
			return new AtomicInteger(0);
		}
	}

	public static boolean isValidOrder(String drink) throws IllegalArgumentException{
		boolean isValid = true;
		
		for (DrinkType drinks : DrinkType.values()) {
			if (drinks.name().equalsIgnoreCase(drink.trim())) {
				if (!drinks.isServable()) {
					isValid = false;
				}
			} else {
				if (drinks.getCounter().get() > 0) {
					isValid = false;
				}
			}
		}
     
        if(isValid) {
    			DrinkType.valueOf(drink).incrementCounter();
    	}
       
		
		return isValid;
	}
}
