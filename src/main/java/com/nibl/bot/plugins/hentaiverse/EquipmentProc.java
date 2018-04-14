package com.nibl.bot.plugins.hentaiverse;

import org.pircbotx.Colors;

public class EquipmentProc {
	
	private String _titleText = null;
	private String _chance = null;
	private String _turns = null;
	private String _points = null;
	private String _dot = null;
	
	public EquipmentProc(String title){
		this._titleText = title;
	}
	
	public String getTitle() {
		return _titleText;
	}

	public String getChanceString() {
		if( _chance == null ){
			return "";
		}else{
			return _chance + " chance ";
		}
	}
	
	public String getChance(){
		return this._chance;
	}

	public void setChance(String _chance) {
		this._chance = _chance;
	}

	public String getTurnsString() {
		if( _turns == null ){
			return "";
		}else{
			return _turns + " turn(s) ";
		}
	}
	
	public String getTurns(){
		return this._turns;
	}

	public void setTurns(String _turns) {
		this._turns = _turns;
	}

	public String getPointsString() {
		if( _points == null ){
			return "";
		}else{
			return _points + " points ";
		}
	}
	
	public String getPoints(){
		return this._points;
	}

	public void setPoints(String _points) {
		this._points = _points;
	}
	
	public String getDotString() {
		if( _dot == null ){
			return "";
		}else{
			return _dot + " DOT ";
		}
	}

	public String getDot(){
		return this._dot;
	}
	
	public void setDot(String _dot) {
		this._dot = _dot;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(Colors.CYAN + this.getTitle());
		sb.append(": ");
		sb.append(Colors.NORMAL + this.getChanceString());
		sb.append(this.getTurnsString());
		sb.append(this.getPointsString());
		sb.append(this.getDotString());
		
		return sb.toString();
	}
	
	
}
