package com.nibl.bot.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;

import org.pircbotx.Colors;
import org.pircbotx.dcc.SendChat;
import org.slf4j.LoggerFactory;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.nibl.bot.plugins.admin.AdminSession;
import com.nibl.bot.plugins.admin.BotUser;

public class Logbook extends BotExtend {
	
	SimpleDateFormat _simpledateformat = new SimpleDateFormat("MM/dd/yyyy [HH:mm:ss] ");
	
	Date _date = new Date();
	int _level = 0; //0 = trace 1=normal 2=error
	
	public Logbook(Bot myBot){
		super(myBot);
	}

	@SuppressWarnings("rawtypes")
	public void dccChatSend(String input, int level){
		ListIterator adminSessions =  _myBot.getAdminSession().listIterator();
		while( adminSessions.hasNext() ){
			AdminSession adminSession = (AdminSession) adminSessions.next();
			SendChat session = adminSession.getIRCSession();
			if( session == null || session.isFinished() ) {
				continue;
			}
			BotUser user = _myBot.getBotUsers().get( session.getUser().getNick().toLowerCase() );
			if( user.getLogLevel() > level ) { // Ignore anything above log level
				continue;
			}

			try {
				switch(level){
				case 3:
					session.sendLine(Colors.RED + input);
					break;
				case 2:
					session.sendLine(Colors.YELLOW + input);
					break;
				case 1:
					session.sendLine(input);
					break;
				case 0:
				default:
					session.sendLine(Colors.LIGHT_GRAY + input);
					break;
				}
			} catch (IOException e) {
				_myBot.sendMessageFair("Jenga", "Failed logging " + input);
			}
		}
	}

	public void error(String string, Exception e)
	{
		LoggerFactory.getLogger( getCallerClass() ).error(string, e);
		String out = timestamp() + "ERROR: " + string;
		dccChatSend(out,3);
	}
	
	public void error(String string)
	{
		LoggerFactory.getLogger( getCallerClass() ).error(string);
		String out = timestamp() + "ERROR: " + string;
		dccChatSend(out,3);
	}

	public void warn(String string)
	{

		LoggerFactory.getLogger( getCallerClass() ).warn(string);
		String out = timestamp() + "WARNING: " + string;
		dccChatSend(out,2);
	}

	public void info(String string)
	{
		LoggerFactory.getLogger( getCallerClass() ).info(string);
		String out = timestamp() + string;
		dccChatSend(out,1);
	}

	public void trace(String string)
	{
		LoggerFactory.getLogger( getCallerClass() ).trace(string);
		String out = timestamp() + "TRACE: " + string;
		dccChatSend(out,0);
	}
	
	private Class<?> getCallerClass(){
		// Reflection.getCallerClass(2)
		// Class.forName( Thread.currentThread().getStackTrace()[3].getClassName() )
		Class<?> cls = Logbook.class;
		try{
			// 3rd stack trace element
			// 1st is Logbook (this method), 2nd is Logbook (log method), 3rd is caller
			cls = Class.forName( Thread.currentThread().getStackTrace()[3].getClassName() );
		} catch (Exception e){}
		return cls;
	}
	
	public void setLoglevel(Integer level) {
		_level = level;
	}
	
	private String timestamp() {
		_date.setTime(System.currentTimeMillis());
		return _simpledateformat.format(_date);
	}
	
}
