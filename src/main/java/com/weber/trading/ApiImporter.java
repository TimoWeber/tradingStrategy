package com.weber.trading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.ta4j.core.TimeSeries;

import com.google.gson.Gson;

public class ApiImporter {
	
	// Function to get api data from tiingo and convert it into trading bars that can be added to a trading series.
	public TimeSeries getData(String stockSymbol, TimeSeries series) {
		String url = "https://api.tiingo.com/iex/" + stockSymbol + "/prices?startDate=2019-08-07&resampleFreq=1min";

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// request header
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Token 8291fd673fa24bc64b9a760384211c29cf3fdb5f");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			System.out.println("");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = "";
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			String jsonResponse = response.toString();
						
			Gson gson = new Gson();
			TradingBar[] data = gson.fromJson(jsonResponse, TradingBar[].class);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			
			for (int i = 0; i < data.length; i++) {
				LocalDateTime time = LocalDateTime.parse(data[i].getDate(), formatter);
				
				series.addBar(
						ZonedDateTime.of(time, ZoneId.of("Europe/Berlin")),
						Double.parseDouble(data[i].getOpen()),
						Double.parseDouble(data[i].getHigh()),
						Double.parseDouble(data[i].getLow()),
						Double.parseDouble(data[i].getClose())
						);
			}		
			return series;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// converts json string to json array
	public  String[] convertToObject(String jsonResponse) {
		Gson gson = new Gson();
		String[] data = gson.fromJson(jsonResponse, String[].class);
		
		return data;
	}
}