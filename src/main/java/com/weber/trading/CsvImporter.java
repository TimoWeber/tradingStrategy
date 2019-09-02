package com.weber.trading;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.ta4j.core.TimeSeries;

public class CsvImporter {
	
	// This function reads data from a csv file and adds them to the time series.
	public TimeSeries fillTimeSerie(TimeSeries series) {
		String fileSource = "B:/eclipse/workspace/stock_market_checker/src/stock_market_checker/EURUSD_Candlestick_1_h_BID_16.07.2015-04.08.2018.csv";
		String line = "";
		String splitAt = ",";
		
		/**
		 * data[0] -	Local Time	-	16.07.2015 00:00:00.000 GMT+0200
		 * data[1] -	Open		-	1.09461
		 * data[2] -	High		-	1.09595
		 * data[3] -	Low			-	1.09412
		 * data[4] -	Close		-	1.09587
		 * data[5] -	Volume		-	2945.47
		 */
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileSource));
			int x = 0;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");
			while (((line = br.readLine()) != null) && x < 800) {
				String data[] = line.split(splitAt);
				
				data[0] = data[0].replace(" GMT+0200", "");
				data[0] = data[0].replace(" GMT+0100", "");

				LocalDateTime time = LocalDateTime.parse(data[0], formatter);
				series.addBar(ZonedDateTime.of(time, ZoneId.of("Europe/Berlin")),
							Double.parseDouble(data[1]),
							Double.parseDouble(data[2]),
							Double.parseDouble(data[3]),
							Double.parseDouble(data[4]),
							Double.parseDouble(data[5])
							);
				x++;
			}
			br.close();
			return series;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	return null;
	}
}
