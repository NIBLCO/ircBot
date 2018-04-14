package com.nibl.bot.plugins.hentaiverse;

public class EquipmentAttribute {
	
	private String _titleText;
	private String _valueText;
	private String _percentText;
	
	public EquipmentAttribute(String title, String value, String percent){
		this._titleText = title;
		this._valueText = value;
		this._percentText = percent;
	}
	
	public String getTitle() {
		return _titleText;
	}

	public Float getValue() {
		try{
			return Float.parseFloat(_valueText);
		}catch(NumberFormatException e){
			return (float) -1;
		}
	}

	public String getPercent() {
		return _percentText;
	}
	
}
