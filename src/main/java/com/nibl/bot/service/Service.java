package com.nibl.bot.service;

import java.io.IOException;
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
import com.nibl.bot.util.Manageable;

public abstract class Service implements Runnable, Manageable {
	
	public enum Status {
		STOPPED("stopped"), RUNNING("running");

		String _stringRepresentation;

		Status(String stringRepresentation) {
			_stringRepresentation = stringRepresentation;
		}

		@Override
		public String toString() {
			return _stringRepresentation;
		}
	};
	
	protected Bot _myBot;
	protected Status _status;
	protected SendChat _session;
	
	public int _accessLevel = 0;
	public Service(Bot myBot) {
		_myBot = myBot; 
		String state = _myBot.getProperty( getName() );
		if (state != null && state.equals("start")) {
			startService();
		} else {
			stopService();
		}
	}

	@Override
	public String toString() {
		return getName() + " " + getStatus();
	}

	public boolean isRunning() {
		return _status == Status.RUNNING;
	}

	public String getStatus() {
		return _status.toString();
	}
	
	public final String getPrefix(){
		return this.getName() + ">> ";
	}
	
	public final void getHelp(SendChat session) throws IOException{
		StringBuilder sb = new StringBuilder();
		if(isRunning())
			sb.append(getPrefix() + "This service is currently " + Colors.GREEN + _status.name() + ".\r\n");
		else
			sb.append(getPrefix() + "This service is currently " + Colors.RED + _status.name() + ".\r\n");
		sb.append(getPrefix() + "help - displays this help\r\n");
		sb.append(getPrefix() + "start - " + Colors.GREEN + "start" + Colors.NORMAL + " the " + getName() + " service.\r\n");
		sb.append(getPrefix() + "stop - " + Colors.RED + "stop" + Colors.NORMAL + " the " + getName() + " service.\r\n");
		sb.append(getPrefix() + "quit - return to main admin.\r\n");
		sb.append(getPrefix() + "----------------------------\r\n");
		LinkedList<String> help = adminHelp();
		for(String temp : help){
			sb.append(getPrefix() + temp);
		}
		session.sendLine(sb.toString());
	}
	
	public final void adminWrapper(SendChat session, User user) throws IOException {

		_session = session;
		this.getHelp(session);
		String message = null;
		while ((message = session.readLine()) != null && !message.equals("quit")) {
			_myBot.getLogger().info(user.getNick() + " " + user.getLogin() + "@" + user.getHostmask() + " : " + getName() + " >> "+ message);
			if (message.equals("start")) {
				startService();
			} else if (message.equals("stop")) {
				stopService();
			} else if (message.equals("help")){
				this.getHelp(session);
			} else {
				try {
					admin(session, user, message);
				} catch (Exception e) {
					session.sendLine("Syntax Error Please Try Again.");
				}
			}
		}
	}
	
	public abstract int getAccessLevel();
	
	public abstract boolean needsMessages();

	public abstract boolean needsNotices();

	public abstract boolean needsIncomingFileTransfers();
	
	public abstract boolean needsOnPart();
	
	public abstract boolean needsOnJoin();
	
	public abstract boolean needsOnMode();
	
	public abstract boolean needsOnHop();
	
	public abstract boolean needsOnTopic();
	
	public abstract int delay();

	public abstract void onTopic(TopicEvent topic);
	
	public abstract void onMessage(MessageEvent message);

	public abstract void onNotice(NoticeEvent notice);
	
	public abstract void onPart(String nick);
	
	public abstract void onJoin(String nick);
	
	public abstract void onMode(ModeEvent mode);
	
	public abstract void onHalfOp(HalfOpEvent event);
	
	public abstract void onIncomingFileTransfer(IncomingFileTransferEvent transfer);
	
	public abstract void execute();

	@Override
	public void run() {
		try {
			_myBot.getServiceGate().await();
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Service gate interrupted error", e);
		}
		while (_status == Status.RUNNING) {
			execute();
		}
	}

	public final void waitUntilStopped(){
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*60);
			}
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Interrupted in Scheduler sleep", e);
		}
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}
	
	public final void startService() {
		_status = Status.RUNNING;
		_myBot.setProperty(this.getName(), "start");
		_myBot.getServiceExecutor().execute(this);
		_myBot.getLogger().info("Service " + getName() + " started.");
	}

	public final void stopService() {
		_status = Status.STOPPED;
		_myBot.getLogger().info("Please wait for " + getName() + " to stop.");
		_myBot.setProperty(this.getName(), "stop");
	}

}
