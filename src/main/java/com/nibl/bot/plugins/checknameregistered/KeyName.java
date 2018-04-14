package com.nibl.bot.plugins.checknameregistered;

public class KeyName {

	public String _name;
	public String _key;
	public volatile boolean _set = false;
	
	public KeyName(String key, String name)
	{
		_key = key;
		_name = name;
	}

}
