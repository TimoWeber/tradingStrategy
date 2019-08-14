package com.weber.trading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import org.ta4j.core.TimeSeries;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApiImport {
	
	// Function to get api data from tiingo and convert it into trading bars that can be added to a trading series.
	public String getData(String stockSymbol, TimeSeries series) {
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
			
			for (int i = 0; i < data.length; i++) {
				System.out.format("%30s %15s %15s %15s %15s",
						"Date: " + data[i].getDate(),
						"Open: " + data[i].getOpen(),
						"High: " + data[i].getHigh(),
						"Low: " + data[i].getLow(),
						"Close: " + data[i].getClose()
						);
		        System.out.println();
			}
						
			// {"date":"2019-08-08T19:59:00.000Z","open":46.78,"high":46.8,"low":46.73,"close":46.775}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			
			return response.toString();
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