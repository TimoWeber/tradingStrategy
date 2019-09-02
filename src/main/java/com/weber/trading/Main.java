package com.weber.trading;

import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.*;

import com.weber.trading.TradingStrategies.CCICorrectionStrategy;
import com.weber.trading.TradingStrategies.TwoPeriodRsiStrategy;

import java.util.List;


public class Main {
	public static void main(String[] args) {
		Main main = new Main();
		TimeSeries csvSeries = new BaseTimeSeries.SeriesBuilder().withName("test_series").build();
		
		CsvImporter csvImporter = new CsvImporter();
		csvSeries = csvImporter.fillTimeSerie(csvSeries);
		
		System.out.println();
		System.out.println("CSV analysis:");
		System.out.println("Initial bar count: " + csvSeries.getBarCount());
		
		Strategy csvRsiStrategy = new TwoPeriodRsiStrategy().buildStrategy(csvSeries);
		
		TimeSeriesManager seriesManager = new TimeSeriesManager(csvSeries);
		TradingRecord tradingRecord = seriesManager.run(csvRsiStrategy);
		main.getResult(tradingRecord, csvSeries);
		
		TimeSeries ApiSeries = new BaseTimeSeries.SeriesBuilder().withName("test_series").build();
		
		ApiImporter apiImporter = new ApiImporter();
		ApiSeries = apiImporter.getData("dow", ApiSeries);
		
		System.out.println();
		System.out.println("API analysis:");
		System.out.println("Initial bar count: " + ApiSeries.getBarCount());
		
		//strategy = main.buildStrategy(series);
		Strategy rsiStrategy = new TwoPeriodRsiStrategy().buildStrategy(ApiSeries);
		Strategy cciStrategy = new CCICorrectionStrategy().buildStrategy(ApiSeries);
		
		seriesManager = new TimeSeriesManager(ApiSeries);
		System.out.println("2 Period RSI Strategy:");
		tradingRecord = seriesManager.run(rsiStrategy);
		main.getResult(tradingRecord, ApiSeries);

		System.out.println();
		System.out.println("CCI Strategy:");
		tradingRecord = seriesManager.run(cciStrategy);
		main.getResult(tradingRecord, ApiSeries);
	}
	
	private void getResult(TradingRecord tradingRecord, TimeSeries series) {
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

	    System.out.println();
	    // Total profit
        TotalProfitCriterion totalProfit = new TotalProfitCriterion();
        System.out.println("Total profit: " + totalProfit.calculate(series, tradingRecord));
        // Number of bars
        System.out.println("Number of bars: " + new NumberOfBarsCriterion().calculate(series, tradingRecord));
        // Average profit (per bar)
        System.out.println("Average profit (per bar): " + new AverageProfitCriterion().calculate(series, tradingRecord));
        // Number of trades
        System.out.println("Number of trades: " + new NumberOfTradesCriterion().calculate(series, tradingRecord));
        // Profitable trades ratio
        System.out.println("Profitable trades ratio: " + new AverageProfitableTradesCriterion().calculate(series, tradingRecord));
        // Maximum drawdown
        System.out.println("Maximum drawdown: " + new MaximumDrawdownCriterion().calculate(series, tradingRecord));
        // Reward-risk ratio
        System.out.println("Reward-risk ratio: " + new RewardRiskRatioCriterion().calculate(series, tradingRecord));
        // Total transaction cost
        System.out.println("Total transaction cost (from $1000): " + new LinearTransactionCostCriterion(1000, 0.005).calculate(series, tradingRecord));
        // Buy-and-hold
        System.out.println("Buy-and-hold: " + new BuyAndHoldCriterion().calculate(series, tradingRecord));
        // Total profit vs buy-and-hold
        System.out.println("Custom strategy profit vs buy-and-hold strategy profit: " + new VersusBuyAndHoldCriterion(totalProfit).calculate(series, tradingRecord));
	}
}