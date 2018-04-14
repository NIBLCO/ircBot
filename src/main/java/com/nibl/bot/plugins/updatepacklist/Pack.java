package com.nibl.bot.plugins.updatepacklist;

import java.sql.Timestamp;

public class Pack {
	
	private int _id;
	private int _bot_id;
	private int _number;
	private String _name;
	private String _size;
	private Timestamp _lastModified;
	private String _botName;
	private int _episodeNumber;
	
	public Pack(int bot_id, int packNumber, String botName, String packName, String packSize, int episodeNumber, Timestamp lastModified){
		_bot_id = bot_id;
		_number = packNumber;
		_name = packName;
		_size = packSize;
		_episodeNumber = episodeNumber;
		_lastModified = lastModified;
		_botName = botName;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getBotName(){
		return _botName;
	}
	
	public int getBotId() {
		return _bot_id;
	}
	
	public int getNumber(){
		return _number;
	}
	
	public String getName(){
		return _name;
	}
	
	public String getSize(){
		return _size;
	}
	
	public int getEpisodeNumber(){
		return _episodeNumber;
	}
	
	public void setEpisodeNumber(int episodeNumber){
		_episodeNumber = episodeNumber;
	}
	
	public Timestamp getLastModified(){
		return _lastModified;
	}
	
	public void setLastModified(Timestamp time){
		_lastModified = time;
	}
	
	@Override
	public String toString()
	{
		return _bot_id + " | " + _number + " | " + _size + " | " + _name + " | " + _lastModified; 
	}
	
	public boolean compareTo(Pack compare){
		if(compare.getBotId()==_bot_id && compare.getBotName().equals(_botName) &&
				compare.getId()==_id && compare.getName().equals(_name) && compare.getNumber()==_number &&
				compare.getSize().equals(_size))
			return true;
		else
			return false;
	}

}
