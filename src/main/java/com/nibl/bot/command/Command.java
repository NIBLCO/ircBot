package com.nibl.bot.command;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.nibl.bot.plugins.scheduler.Scheduler;
import com.nibl.bot.util.Manageable;

/**
 * holds variables
 * 
 * Bot _myBot; Bot to respond on
 * 
 * String _channel; channel posted on
 * 
 * String _user; user that made the request
 * 
 * String _login; I donno what this is
 * 
 * String _hostname; host name
 * 
 * String _args; arguments
 * 
 * @author cwang
 * 
 */
public abstract class Command extends BotExtend implements Runnable, Manageable {
	
	protected Channel _channel;
	protected User _user;
	protected String _args;
	protected String _actualcmd;
	//protected DccChat _session;
	protected Scheduler _scheduler;
	protected boolean _firstRun = false;
	protected boolean _origantePrivateMessage = false;
	
	public Command(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot);
		_channel = channel;
		_user = user;
		_actualcmd = actualcmd;
		_args = args;
		_scheduler = (Scheduler) _myBot.getServiceFactory().getRegisteredService().get("Scheduler");
	}

	public final boolean isEnabled() {
		String[] multiCommands = getCommand().split(" ");
		for(int i=0;i<multiCommands.length;i++){
			String property = _myBot.getProperty(multiCommands[i].replace("!", ""));
			if(property!=null){
				if(property.equals("enabled"))
					return true;
			}
		}
		return false;
	}

	public final void enable() {
		String[] multiCommands = getCommand().split(" ");
		for(int i=0;i<multiCommands.length;i++){
			_myBot.setProperty(multiCommands[i].replace("!", ""), "enabled");
		}
		
		this.executeAfterEnabled();
	}

	public final void disable() {
		String[] multiCommands = getCommand().split(" ");
		for(int i=0;i<multiCommands.length;i++){
			_myBot.setProperty(multiCommands[i].replace("!", ""), "disabled");
		}
		this.executeAfterDisabled();
	}
	
	public final String getPrefix(){
		//return this.getName() + ">> ";
		return "";
	}
	
	public final String getHelp() throws IOException{
		StringBuilder sb = new StringBuilder();
		if(isEnabled()){
			sb.append(getPrefix() + "This module is currently" + Colors.GREEN + " Enabled\r\n");
		}else{
			sb.append(getPrefix() + "This module is currently" + Colors.RED + " Disabled\r\n");
		}
		sb.append(getPrefix() + "Commands:\r\n");
		sb.append(getPrefix() + "help - displays this help\r\n");
		sb.append(getPrefix() + "enable - " + Colors.GREEN + "enable" + Colors.NORMAL + " module.\r\n");
		sb.append(getPrefix() + "disable - " + Colors.RED + "disable" + Colors.NORMAL + " module.\r\n");
		sb.append(getPrefix() + "quit - return to main admin.\r\n");
		sb.append(getPrefix() + "----------------------------\r\n");
		
		LinkedList<String> help = adminHelp();
		for(String temp : help){
			sb.append(getPrefix() + temp);
		}
		return sb.toString();
	}
	
	public final void adminWrapper(SendChat session, User user) throws IOException {
		session.sendLine( this.getHelp() );
		String message = null;
		
		while ((message = session.readLine()) != null && !message.equals("quit")) {
			_myBot.getLogger().info(user.getNick() + " " + user.getLogin() + "@" + user.getHostmask() + " : " + " >> "+ message);
			if (message.equals("enable")) {
				enable();
				session.sendLine(getCommand() + " is enabled");
			} else if (message.equals("disable")) {
				disable();
				session.sendLine(getCommand() + " is disabled");
			} else if (message.equals("help")){
				this.getHelp();
			} else {
				try {
					admin(session, user, message);
				} catch (Exception e) {
					session.sendLine("Syntax Error Please Try Again.");
				}
			}
		}
	}

	/**
	 * Create a schedule, Maximum of 10 accepted per command to be ran every x hours started on the hour.
	 * To use, create a function:
	 * @Override
	 * public void mySchedule[1-10](){
	 *  //my data here
	 * }
	 * @param scheduleNumber
	 * @param hours
	 */
	public final void addSchedule(int scheduleNumber, long delay, long seconds){
		_scheduler.register(this,scheduleNumber,delay,seconds);
	}
	
	public final void delSchedule(int scheduleNumber){
		_scheduler.destroy(this,scheduleNumber);
	}
	
	@Override
	public String toString() {
		return getCommand();
	}

	@Override
	public final void run() {
		execute();
	}
	
	public void mySchedule1(){};
	public void mySchedule2(){};
	public void mySchedule3(){};
	public void mySchedule4(){};
	public void mySchedule5(){};
	public void mySchedule6(){};
	public void mySchedule7(){};
	public void mySchedule8(){};
	public void mySchedule9(){};
	public void mySchedule10(){};
	
	public abstract void execute();

	public abstract int getAccessLevel();
	/**
	 * To be run when module is enabled in admin dcc chat
	 */
	public abstract void executeAfterEnabled();
	
	/**
	 * To be run when module is disabled in admin dcc chat
	 */
	public abstract void executeAfterDisabled();
	
	/**
	 * @return the command like !request or !tt or anything you want
	 */
	public abstract String getCommand();

	/**
	 * @return how to used the command the syntax suggest getCommand() + " args";
	 */
	public abstract String getSyntax();

	/**
	 * @return short description of what the command does.
	 */
	public abstract String getDescription();

	public abstract Boolean acceptPrivateMessage();
	
	public void setOriginatePrivateMessage(){
		this._origantePrivateMessage = true;
	}
	
	public Boolean isOriginatePrivateMessage(){
		return this._origantePrivateMessage;
	}
	
	/**
	 * @return TreeMap of the commands and access levels
	 */
	public abstract TreeMap<String,Integer> getCommandAccessLevels();
	
	public Integer getCommandAccessLevel(String command){
		if( this.getCommandAccessLevels().containsKey(command) ){
			return this.getCommandAccessLevels().get(command);
		} else {
			return 10;
		}
	}
	
	/**
	 * When a user does /msg ooinuza stop, notices will not be sent
	 * @return enable sending notices to nick
	 */
	public boolean stillNoticeSend(){
		//return _myBot.disableNoticeSend(getUser().getNick());
		return false;
	}

	public final Channel getChannel() {
		return _channel;
	}

	public final User getUser() {
		return _user;
	}

	public final String getArgs() {
		return _args;
	}

}