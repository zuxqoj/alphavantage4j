package com.zsl.alphavantage.app.commons;

public class StockBean {

	private String account;
	private String stockCode;
	private String stockName;
	private String industry;
	private int quantity;
	private double holdPrice;
	
	public StockBean(String line) {
		//System.out.println("adding line :: " + line);
		String[] arr = line.split(",");
		this.account=arr[0];
		this.stockCode=arr[1];
		this.stockName=arr[2];
		this.industry=arr[3];
		this.quantity=Integer.parseInt(arr[4]);
		this.holdPrice=Double.parseDouble(arr[5]);
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getHoldPrice() {
		return holdPrice;
	}
	public void setHoldPrice(double holdPrice) {
		this.holdPrice = holdPrice;
	}

	@Override
	public String toString() {
		return "StockListBean [account=" + account + ", stockCode=" + stockCode + ", stockName=" + stockName
				+ ", industry=" + industry + ", quantity=" + quantity + ", holdPrice=" + holdPrice + "]";
	}
	
	
	
}
