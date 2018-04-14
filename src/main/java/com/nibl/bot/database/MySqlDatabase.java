package com.nibl.bot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;


public class MySqlDatabase extends BotExtend implements Database{
	
	String _userName; 
	String _userPass;
	String _server;
	String _port;
	String _dbName;
	Connection _connection;

	public MySqlDatabase(Bot myBot, String userName, String userPass, String server, String port, String dbName)
	{
		super(myBot);
		_userName = userName;
		_userPass = userPass;
		_server = server;
		_port = port;
		_dbName = dbName;
		connect();
	}
	
	private void connect()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//String url = "jdbc:mysql://"+_server +":"+_port+"/"+_dbName+"?useUnicode=yes&characterEncoding=UTF-8";
			String url = "jdbc:mysql://"+_server +":"+_port+"/"+_dbName+"?useUnicode=yes&characterEncoding=utf-8&useServerPrepStmts=true&useServerPrepStmts=true";
			_connection = DriverManager.getConnection(url, _userName, _userPass);
			_myBot.getLogger().info("Database connection established");
		} catch (Exception e) {
			_myBot.getLogger().error("Cannot connect to database server",e);
			disconnect();
			System.exit(1);
		}
	}
	
	public Connection getConnection()
	{
		try {
			if(_connection == null || !_connection.isValid(5))
				connect();
		} catch (SQLException e) {
			_myBot.getLogger().error("SQL Connection died.  Trying to reconnect. "+ e);
			connect();
		}
		return _connection;
	}
	
	public void disconnect()
	{
		if (_connection != null) {
			try {
				_connection.close();
				_myBot.getLogger().warn("Database connection terminated");
			} catch (Exception e) { /* ignore close errors */
			}
		}
	}
}
