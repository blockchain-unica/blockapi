package com._37coins.bcJsonRpc.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class LastBlock {
	private String lastblock;
	private List<Transaction> transactions;
	
	
	public String getLastblock() {
		return lastblock;
	}
	public void setLastblock(String lastblock) {
		this.lastblock = lastblock;
	}
	public List<Transaction> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}
	
	
}
