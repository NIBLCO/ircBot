package com.nibl.bot.plugins.request;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequestObj {
	
	private int id;
	private String inChannel;
	private String madeBy;
	private Date madeDate;
	private String request;
	private int status;
	private String filledBy;
	private Date filledDate;
	
	public RequestObj() {
		
	}
	
	public RequestObj(ResultSet rs) throws SQLException {
		setID(rs.getInt("id"));
		setInChannel(rs.getString("in_channel"));
		setMadeBy(rs.getString("made_by"));
		setRequest(rs.getString("request"));
		setStatus(rs.getInt("status_id"));
		setFilledBy(rs.getString("filled_by"));
		setFilledDate(rs.getDate("filled_date"));
	}
	
	public int getID(){
		return this.id;
	}
	
	public void setID(int id){
		this.id = id;
	}
	
	public String getInChannel() {
		return inChannel;
	}

	public void setInChannel(String inChannel) {
		this.inChannel = inChannel;
	}

	public String getMadeBy() {
		return madeBy;
	}

	public void setMadeBy(String madeBy) {
		this.madeBy = madeBy;
	}

	public Date getMadeDate() {
		return madeDate;
	}

	public void setMadeDate(Date madeDate) {
		this.madeDate = madeDate;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getFilledBy() {
		return filledBy;
	}

	public void setFilledBy(String filledBy) {
		this.filledBy = filledBy;
	}

	public Date getFilledDate() {
		return filledDate;
	}

	public void setFilledDate(Date filledDate) {
		this.filledDate = filledDate;
	}
	
	

}
