package com.nibl.bot.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

public abstract class DataAccessObject extends BotExtend {
	private Connection _connection;
	
	public DataAccessObject(Bot myBot) {
		super(myBot);
		_connection = _myBot.getDatabase().getConnection();
		createTables();
	}
	
	public final Connection getConnection()
	{
		return _connection;
	}
	
	public final boolean tableExists(String table) {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("desc " + table);
		} catch (SQLException e) {
			// table doesn't exist eat exception
			return false;
		}
		return true;
	}
	
	public abstract void createTables();
	public abstract void dropTables();
}
