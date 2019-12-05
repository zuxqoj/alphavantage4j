package com.zsl.alphavantage.app;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.patriques.AlphaVantageConnector;
import org.patriques.ApiConnector;
import org.patriques.BatchStockQuotes;
import org.patriques.SingleStockQuote;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.data.StockData;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zsl.alphavantage.app.commons.StockBean;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AccontAggregateReport {
	
	public static final int MAX_CALL_PER_MIN = 5;
	private static boolean debugEnabled = false;
	
	private static StringBuffer errorReport = new StringBuffer();
	private static Map<String, StringBuilder> debugReport = new HashMap<String, StringBuilder>();
	
	public static void main(String[] args) throws Exception{

		Config config = ConfigFactory.load();
		final String API_KEY = config.getString("API_KEY");
		debugEnabled = config.getBoolean("enable.debug");
		
		final String STOCKS_FILE_NAME = config.getString("stock.file.name");
		String[] cover_dates_list = config.getString("cover.dates").split(" ");
		LocalDateTime[] cover_dates = new LocalDateTime[cover_dates_list.length];  
		for (int i = 0; i < cover_dates_list.length; i++) {
			cover_dates[i] = LocalDate.parse(cover_dates_list[i], org.patriques.output.JsonParser.SIMPLE_DATE_FORMAT).atStartOfDay();
		}
		
		ApiConnector apiConnector = new AlphaVantageConnector(API_KEY, 30000);
		TimeSeries timeSeries = new TimeSeries(apiConnector);
		
		List<StockBean> stockList = getStockList(STOCKS_FILE_NAME);
		
		System.out.println("processing ["+stockList.size()+"] stocks , will take roughly ["+(stockList.size()/5)+"] mins" );
		
		Map<String, Daily> stockTrendsMap = new HashMap<String, Daily>();
		
		
		int num_calls = 1;
		for (Iterator<StockBean> iterator = stockList.iterator(); iterator.hasNext();) {
			
			if(num_calls % MAX_CALL_PER_MIN == 0) {
				System.out.println("sleeping for a min:: " + new Date() + ", queried records :: "  + (num_calls-1) + "/" + stockList.size());
				Thread.sleep(61000);
			}
			
			StockBean stockListBean = (StockBean) iterator.next();

			if(!stockTrendsMap.containsKey(stockListBean.getStockCode())) {
				Daily trends = timeSeries.daily(URLEncoder.encode(stockListBean.getStockCode(), StandardCharsets.UTF_8.name()), OutputSize.COMPACT);
				stockTrendsMap.put(stockListBean.getStockCode(), trends);
				num_calls++;
			}

			//getTimeSeriesDaily(stockListBean.getStockCode(), API_KEY);
		}
		 
		for (int i = 0; i < cover_dates.length; i++) {
			
			Map<String, Double> accountToAggValueMap = new HashMap<String, Double>();
			
			for (Iterator<StockBean> iterator = stockList.iterator(); iterator.hasNext();) {
				StockBean stockListBean = iterator.next();
				
				double closingPrice = getClosingPrice(cover_dates[i], stockListBean, stockTrendsMap);
				double stockValue = closingPrice * stockListBean.getQuantity();
				
				if(accountToAggValueMap.containsKey(stockListBean.getAccount())) {
					accountToAggValueMap.put(stockListBean.getAccount(), stockValue + accountToAggValueMap.get(stockListBean.getAccount()));
				}else {
					accountToAggValueMap.put(stockListBean.getAccount(), stockValue);
				}
				
				
				if(debugEnabled) {
					StringBuilder debugReportBuilder = new StringBuilder();
					debugReportBuilder.append("\t")
						.append(stockListBean.getStockName()).append(",")
						.append(stockListBean.getQuantity()).append(",")
						.append(stockListBean.getHoldPrice()).append(",")
						.append(closingPrice).append(",")
						.append(stockValue-stockListBean.getQuantity()*stockListBean.getHoldPrice()).append("\n");
					
					if(debugReport.containsKey(stockListBean.getAccount())) {
						debugReport.get(stockListBean.getAccount()).append(debugReportBuilder);
					}else {
						debugReport.put(stockListBean.getAccount(), debugReportBuilder);
					}
				}
				
			}
			System.out.println("************************************************************");
			System.out.println("Report for date:: " + cover_dates[i]);
			for (Iterator<String> iterator = accountToAggValueMap.keySet().iterator(); iterator.hasNext();) {
				String account = iterator.next();
				System.out.println(account + " - " + accountToAggValueMap.get(account));
				
			}
			System.out.println("************************************************************");
		}
		
		System.out.println("errorReport:: " + errorReport);
		System.out.println("************************************************************");
		printDebugReport();
		System.out.println("******************************* done *****************************");
		
	}
	
	private static void printDebugReport() {
		if(debugEnabled) {
			for (Iterator<String> iterator = debugReport.keySet().iterator(); iterator.hasNext();) {
				String accountName = (String) iterator.next();
				System.out.println("printint debug report for account:: " + accountName);
				System.out.println(debugReport.get(accountName));
			}
		}
	}
	
	private static double getClosingPrice(LocalDateTime date, StockBean stockBean, Map<String, Daily> stockTrendsMap) {
		List<StockData> stockDatas = stockTrendsMap.get(stockBean.getStockCode()).getStockData();
		for (Iterator<StockData> iterator = stockDatas.iterator(); iterator.hasNext();) {
			StockData stockData = (StockData) iterator.next();
			if(stockData.getDateTime().equals(date)) {
				return stockData.getClose();
			}
		}
		
		//throw new IllegalArgumentException("Date ["+date+"] not found for stock ["+stockCode+"] in list:: " + stockDatas);
		errorReport.append("Date ["+date+"] not found for stock ["+stockBean.getStockCode()+"], hold quantity ["+stockBean.getQuantity()+"] for account ["+stockBean.getAccount()+"] in list:: " + stockDatas).append("\n");
		System.out.println("Date ["+date+"] not found for stock ["+stockBean.getStockCode()+"], hold quantity ["+stockBean.getQuantity()+"] for account ["+stockBean.getAccount()+"] in list:: " + stockDatas);
		
		return 0.0d;
	}
	
	public static void getCurrentPrice(String stockSymbol, final String API_KEY) {
		
		ApiConnector apiConnector = new AlphaVantageConnector(API_KEY, 30000);
		
		SingleStockQuote singleStockQuote = new SingleStockQuote(apiConnector);
		System.out.println(singleStockQuote.quote(stockSymbol));
	}
	
	public static void getBatchStockPrice(String[] stockSymbols, final String API_KEY) {
		ApiConnector apiConnector = new AlphaVantageConnector(API_KEY, 30000);
		BatchStockQuotes batchStockQuotes = new BatchStockQuotes(apiConnector);
		batchStockQuotes.quote(stockSymbols);
	}
	
	public static void getTimeSeriesDaily(String stockSymbol, final String API_KEY) throws Exception{
		ApiConnector apiConnector = new AlphaVantageConnector(API_KEY, 30000);
		TimeSeries timeSeries = new TimeSeries(apiConnector);
		timeSeries.daily(URLEncoder.encode(stockSymbol, StandardCharsets.UTF_8.name()), OutputSize.COMPACT);
	}
	
	
	private static List<StockBean> getStockList(String fileName) throws Exception {
		List<StockBean> stockListBeans = new ArrayList<StockBean>();
		List<String> lines = Files.readAllLines(Paths.get(AccontAggregateReport.class.getResource("/"+fileName).toURI()), Charset.defaultCharset());
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			String line = (String) iterator.next();
			if(!line.startsWith("#")) {
				stockListBeans.add(new StockBean(line));
			}
		}
		
		return stockListBeans;
	}
	
}
