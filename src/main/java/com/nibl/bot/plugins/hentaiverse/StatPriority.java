package com.nibl.bot.plugins.hentaiverse;

import org.pircbotx.Colors;

public class StatPriority {

	private String _name;
	private boolean _linebreak;
	private String _color;
	
	public StatPriority(String name, boolean linebreak, String color){
		this._name = name;
		this._linebreak = linebreak;
		this._color = parseColor(color);
	}
	
	private String parseColor(String color){
		color = color.toLowerCase();
		
		if( color.equals("white") ){
			color = Colors.WHITE;
		}else if( color.equals("black") ){
			color = Colors.BLACK;
		}else if( color.equals("dark_blue") ){
		  color = Colors.DARK_BLUE;
		}else if( color.equals("dark_green") ){
		  color = Colors.DARK_GREEN;
		}else if( color.equals("red") ){
		  color = Colors.RED;
		}else if( color.equals("brown") ){
		  color = Colors.BROWN;
		}else if( color.equals("purple") ){
		  color = Colors.PURPLE;
		}else if( color.equals("olive") ){
		  color = Colors.OLIVE;
		}else if( color.equals("yellow") ){
		  color = Colors.YELLOW;
		}else if( color.equals("green") ){
		  color = Colors.GREEN;
		}else if( color.equals("teal") ){
		  color = Colors.TEAL;
		}else if( color.equals("cyan") ){
		  color = Colors.CYAN;
		}else if( color.equals("blue") ){
		  color = Colors.BLUE;
		}else if( color.equals("magenta") ){
		  color = Colors.MAGENTA;
		}else if( color.equals("dark_gray") ){
		  color = Colors.DARK_GRAY;
		}else if( color.equals("light_gray") ){
		  color = Colors.LIGHT_GRAY;
		}else {
			color = Colors.BLACK;
		}
		
		return color;
		
	}


	public String getName() {
		return _name;
	}

	public boolean isLinebreak() {
		return _linebreak;
	}
	
	public String getColor() {
		return _color;
	}
	
}
