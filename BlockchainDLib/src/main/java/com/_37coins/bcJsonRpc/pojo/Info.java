package com._37coins.bcJsonRpc.pojo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class Info {
	
	private long version;
	private long protocolversion;
	private long walletversion;
	private BigDecimal balance;
	private long blocks;
	private long timeoffset;
	private long connections;
	private String proxy;
	private BigDecimal difficulty;
	private boolean testnet;
	private long keypoololdest;
	private long keypoolsize;
	private BigDecimal paytxfee;
	private BigDecimal relayfee;
	private String errors;
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public long getProtocolversion() {
		return protocolversion;
	}
	public void setProtocolversion(long protocolversion) {
		this.protocolversion = protocolversion;
	}
	public long getWalletversion() {
		return walletversion;
	}
	public void setWalletversion(long walletversion) {
		this.walletversion = walletversion;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public long getBlocks() {
		return blocks;
	}
	public void setBlocks(long blocks) {
		this.blocks = blocks;
	}
	public long getTimeoffset() {
		return timeoffset;
	}
	public void setTimeoffset(long timeoffset) {
		this.timeoffset = timeoffset;
	}
	public long getConnections() {
		return connections;
	}
	public void setConnections(long connections) {
		this.connections = connections;
	}
	public String getProxy() {
		return proxy;
	}
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}
	public BigDecimal getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(BigDecimal difficulty) {
		this.difficulty = difficulty;
	}
	public boolean isTestnet() {
		return testnet;
	}
	public void setTestnet(boolean testnet) {
		this.testnet = testnet;
	}
	public long getKeypoololdest() {
		return keypoololdest;
	}
	public void setKeypoololdest(long keypoololdest) {
		this.keypoololdest = keypoololdest;
	}
	public long getKeypoolsize() {
		return keypoolsize;
	}
	public void setKeypoolsize(long keypoolsize) {
		this.keypoolsize = keypoolsize;
	}
	public BigDecimal getPaytxfee() {
		return paytxfee;
	}
	public void setPaytxfee(BigDecimal paytxfee) {
		this.paytxfee = paytxfee;
	}
	public String getErrors() {
		return errors;
	}
	public void setErrors(String errors) {
		this.errors = errors;
	}

	public BigDecimal getRelayfee() {
		return relayfee;
	}

	public void setRelayfee(BigDecimal relayfee) {
		this.relayfee = relayfee;
	}
}
