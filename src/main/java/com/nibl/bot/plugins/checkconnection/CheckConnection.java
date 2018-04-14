package com.nibl.bot.plugins.checkconnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;

public class CheckConnection extends Service {
	
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("reconnect_check_time"));
	
	public CheckConnection(Bot bot) {
		super(bot);
	}

	@Override
	public String getName() {
		return "connection";
	}

	@Override
	public String getDescription() {
		return "Keep Alive service for " + _myBot.getBot().getNick();
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("help - Shows a list commands\r\n");
		help.add("reconnect - Try to reconnect bot.\r\n");
		help.add("status - Check if bot is connected.");
		return help;
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException, SQLException {
		if (message.equals("reconnect")) {
			reconnect();
		} else if (message.equals("status")) {
			if(checkStatus()){
				session.sendLine("Bot is " + Colors.GREEN + "connected");
			}else{
				session.sendLine("Bot is " + Colors.RED + "offline");
			}
		} else if (message.toLowerCase().startsWith("help")){
			this.getHelp(session);
		}
		
	}

	@Override
	public int getAccessLevel() {
		return 9;
	}

	@Override
	public boolean needsMessages() {
		return false;
	}

	@Override
	public boolean needsNotices() {
		return false;
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
	}
	
	@Override
	public boolean needsOnPart() {
		return false;
	}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public boolean needsOnHop() {
		return false;
	}

	@Override
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public int delay() {
		return 60;
	}

	@Override
	public void onTopic(TopicEvent topic) {
	}

	@Override
	public void onMessage(MessageEvent message) {
	}

	@Override
	public void onNotice(NoticeEvent notice) {
	}

	@Override
	public void onPart(String nick) {
	}

	@Override
	public void onJoin(String nick) {
	}

	@Override
	public void onMode(ModeEvent mode) {
	}
	
	@Override
	public void onHalfOp(HalfOpEvent event) {
	}

	@Override
	public void execute() {
		long start = 0;
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*60);
				long elapsedTimeMillis = System.currentTimeMillis()-start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;
				if(elapsedTimeSec>=_timeInSeconds || start == 0){
					if(!checkStatus()){
						_myBot.getLogger().info("Bot is offline, attempting to reconnect.");
						reconnect();
					}
					start = System.currentTimeMillis();
				}
			}
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Interrupted in Scheduler sleep", e);
		}
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}
	
	public boolean checkStatus(){
		return _myBot.getBot().isConnected();
	}
	
	public void reconnect(){
		_myBot.getLogger().error("Reconnect was called!");
		/*
		if(checkStatus())
			_myBot.getBot()
		else{
			_myBot.connectToServer("");
			if(checkStatus())
				_myBot.joinChannels();
		}
		*/
	}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}

}
