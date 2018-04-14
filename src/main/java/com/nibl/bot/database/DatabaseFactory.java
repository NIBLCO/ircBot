package com.nibl.bot.database;

import com.nibl.bot.Bot;

public class DatabaseFactory {

	private static MySqlDatabase _mysql = null;
	
	public static Database create(Bot myBot, String db,String userName, String userPass, String server, String port, String dbName)
	{
		if (db.equals("mysql"))
		{
			if(_mysql!=null)
				return _mysql;
			_mysql = new MySqlDatabase(myBot, userName,userPass,server,port,dbName); 
			return _mysql;
		}
		return null;
	}
}
