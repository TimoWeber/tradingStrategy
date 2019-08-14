package com.weber.trading;

import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class Main {
    private static final int NB_BARS_PER_WEEK = 12 * 24 * 7;

	public static void main(String[] args) {
		Main main = new Main();
		TimeSeries series = new BaseTimeSeries.SeriesBuilder().withName("test_series").build();
		main.fillTimeSerie(series);
		System.out.println();
		System.out.println("Initial bar count: " + series.getBarCount());
		
		Strategy strategy = main.buildStrategy(series);
		
		TimeSeriesManager seriesManager = new TimeSeriesManager(series);
		TradingRecord tradingRecord = seriesManager.run(strategy);
		main.getResult(tradingRecord, series);
		
		
		series = new BaseTimeSeries.SeriesBuilder().withName("test_series").build();
		
		
		ApiImport apiImport = new ApiImport();
		String data = apiImport.getData("dow", series);
		//System.out.println("API response: " + data);
		//apiImport.convertToObject(data);
	}
	
	// This function reads data from a csv file and adds them to the time series.
	private void fillTimeSerie(TimeSeries series) {
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
			while (((line = br.readLine()) != null) && x < 1000) {
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Strategy buildStrategy(TimeSeries series) {
		if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
		
		ClosePriceIndicator closePrices = new ClosePriceIndicator(series);

        // Getting the max price over the past week
        MaxPriceIndicator maxPrices = new MaxPriceIndicator(series);
        HighestValueIndicator weekMaxPrice = new HighestValueIndicator(maxPrices, NB_BARS_PER_WEEK);
        // Getting the min price over the past week
        MinPriceIndicator minPrices = new MinPriceIndicator(series);
        LowestValueIndicator weekMinPrice = new LowestValueIndicator(minPrices, NB_BARS_PER_WEEK);

        // Going long if the close price goes below the min price
        MultiplierIndicator downWeek = new MultiplierIndicator(weekMinPrice, 1.004);
        Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

        // Going short if the close price goes above the max price
        MultiplierIndicator upWeek = new MultiplierIndicator(weekMaxPrice, 0.996);
        Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);

        return new BaseStrategy(buyingRule, sellingRule);
	}
	
	private void getResult(TradingRecord tradingRecord, TimeSeries series) {
		AnalysisCriterion criterion = new TotalProfitCriterion();
		System.out.println("Criterion Calculate: " + criterion.calculate(series, tradingRecord));
		System.out.println("Trade Count: " + tradingRecord.getTradeCount());
		List<Trade> trades = tradingRecord.getTrades();
		
		System.out.println("---------------------------");
	    System.out.printf("%3s %5s %10s %5s", "ID", "Type", "Price", "Amount");
	    System.out.println();
	    System.out.println("---------------------------");
	    for(Trade trade: trades){
	    	System.out.format("%3s %5s %10s %5s",
	                trade.getEntry().getIndex(), trade.getEntry().getType(), trade.getEntry().getPrice(), trade.getEntry().getAmount());
	        System.out.println();
	        System.out.format("%3s %5s %10s %5s",
	                trade.getExit().getIndex(), trade.getExit().getType(), trade.getExit().getPrice(), trade.getExit().getAmount());
	        System.out.println();
		    System.out.println("---------------------------");
	    }
	}
}