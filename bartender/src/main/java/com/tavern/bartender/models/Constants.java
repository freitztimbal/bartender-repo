package com.tavern.bartender.models;

import java.time.format.DateTimeFormatter;

public final class Constants {
     
	 private Constants() {
		 
	 }
	 public static DateTimeFormatter logTimestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
}
