package com.weber.trading;

import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
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
		series = apiImport.getData("dow", series);
		
		System.out.println();
		System.out.println("Initial bar count: " + series.getBarCount());
		
		strategy = main.buildStrategy(series);
		
		seriesManager = new TimeSeriesManager(series);
		tradingRecord = seriesManager.run(strategy);
		main.getResult(tradingRecord, series);
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

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, 2);
        
        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, 5)) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2
        
        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, 95)) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2
                
        return new BaseStrategy(entryRule, exitRule);
	}
	
	private void getResult(TradingRecord tradingRecord, TimeSeries series) {
		AnalysisCriterion criterion = new TotalProfitCriterion();
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
	    
	    System.out.println("Criterion Calculate: " + criterion.calculate(series, tradingRecord));
		System.out.println("Trade Count: " + tradingRecord.getTradeCount());
		// Reward-risk ratio
        System.out.println("Reward-risk ratio: " + new RewardRiskRatioCriterion().calculate(series, tradingRecord));
        // Profitable trades ratio
        System.out.println("Profitable trades ratio: " + new AverageProfitableTradesCriterion().calculate(series, tradingRecord));
	}
}