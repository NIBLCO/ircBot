package com.nibl.bot.plugins.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;
import com.nibl.bot.service.Service;

public class Scheduler extends Service {
	
	ArrayList<SchedulerTask> _tasks = new ArrayList<SchedulerTask>();
	String _adminHelp = null;
	private SchedulerDAO _schedulerDAO;
	
	public Scheduler(Bot myBot) {
		super(myBot);
		_schedulerDAO = (SchedulerDAO) myBot.getDAOFactory().getDAO("SchedulerDAO");
	}

	@Override
	public void execute() {
		waitUntilStopped();
	}
	
	public long calcElapsed(Command command, int scheduleNumber){
		Date dbDate = _schedulerDAO.getTime(command,scheduleNumber);
		if(dbDate!=null){
	    Date today = new Date();
	    	return (today.getTime() - dbDate.getTime())/1000;
		}else{
			return -1;
		}
	}
	
	public void updateDelay(Command command, int scheduleNumber){
		_schedulerDAO.setTime(command,scheduleNumber);
	}
	
	/**
	 * Register a command to run mySchedule() every xx seconds
	 * @param uniqueName
	 * @param myCommand
	 * @param seconds
	 */
	public void register (Command myCommand, int scheduleNumber, long delay, long seconds) {
		if(scheduleNumber>10){
			_myBot.getLogger().error(getPrefix() + "#" + scheduleNumber + " for " + myCommand.getName() + " every " + seconds + " seconds - Cannot add a SchedulerTask above 10");
			return;
		}
			for(SchedulerTask tempTask : _tasks){
				if(scheduleNumber == tempTask.getNumber() && myCommand.getCommand().equals(tempTask.command().getCommand())){
					_myBot.getLogger().info(getPrefix() + "SchedulerTask #" + scheduleNumber + " for " + myCommand.getName() + " has already been registered!");
					return;
				}
			}
			long secondsElapsed = calcElapsed(myCommand,scheduleNumber);
			if(secondsElapsed>=seconds || secondsElapsed==-1){
				_myBot.getLogger().info(getPrefix() + "Initialized SchedulerTask: #" + scheduleNumber + " for " + myCommand.getName() + " every " + seconds + " seconds");
				_tasks.add(new SchedulerTask(_myBot,this,scheduleNumber,myCommand,delay,seconds));
			}else{
				_myBot.getLogger().info(getPrefix() + "Initialized SchedulerTask: #" + scheduleNumber + " for " + myCommand.getName() + " every " + seconds + " seconds; Starting in " + (seconds-secondsElapsed) + " seconds");
				_tasks.add(new SchedulerTask(_myBot,this,scheduleNumber,myCommand,seconds-secondsElapsed,seconds));
			}
	}
	
	public void destroy (Command myCommand, int scheduleNumber){
		for(SchedulerTask tempTask : _tasks){
			if(scheduleNumber == tempTask.getNumber() && myCommand.getCommand().equals(tempTask.command().getCommand())){
				_myBot.getLogger().info(getPrefix() + "Destroyed SchedulerTask #" + scheduleNumber + " for " + myCommand.getName());
				_tasks.remove(tempTask);
				tempTask.getTimer().cancel();
				return;
			}
		}
		_myBot.getLogger().error(getPrefix() + "Cannot remove SchedulerTask: #" + scheduleNumber + " " + myCommand.getName() + " - SchedulerTask is not registered!");
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
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
	public void onMessage(MessageEvent message) {}

	@Override
	public void onNotice(NoticeEvent notice) {}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		if (message.equals("show")) {
			show(session);
		} else if (message.startsWith("del ") || message.startsWith("delete ")) {
			String[] args = message.replace("del ", "").replace("delete ", "").split(" ");
			if(args.length!=2)
				session.sendLine("Invalid arguments. Ex. del commandName scheduleNumber");
			destroy(_myBot.getCommandFactory().findRegisteredCommand(args[0]),Integer.parseInt(args[1]));
		} else if (message.startsWith("add ")){
			String[] args = message.replace("add ", "").split(" ");
			if(args.length!=4)
				session.sendLine("Invalid arguments. Ex. add commandName scheduleNumber delayToStart secondsToWait");
			register(_myBot.getCommandFactory().findRegisteredCommand(args[0]),Integer.parseInt(args[1]),Long.parseLong(args[2]),Integer.parseInt(args[3]));
		}
	}

	private void show(SendChat session) throws IOException {
		for(SchedulerTask tempTask : _tasks){
			session.sendLine(getPrefix() + tempTask._command.getName() + " " + "#" + tempTask._number);
		}
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> temp = new LinkedList<String>();
		
		temp.add("show - shows registered tasks\r\n");
		temp.add("del - delete registered task (ex. del commandName scheduleNumber)\r\n");
		temp.add("add - add registered task (ex. add commandName scheduleNumber delayToStart secondsToWait)\r\n");
		return temp;
		/*StringBuilder commands = new StringBuilder();
		commands.append(getPrefix() + "show - shows registered tasks\r\n");
		commands.append(getPrefix() + "del - delete registered task (ex. del commandName scheduleNumber)\r\n");
		commands.append(getPrefix() + "add - add registered task (ex. add commandName scheduleNumber delayToStart secondsToWait)\r\n");
		_adminHelp = sb.toString() + commands.toString();
		return _adminHelp;*/
	}

	@Override
	public String getDescription() {
		return "This schedules daily tasks";
	}

	@Override
	public String getName() {
		return "scheduler";
	}

	@Override
	public boolean needsOnPart() {
		return false;
	}

	@Override
	public void onPart(String nick) {}

	@Override
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public int getAccessLevel() {
		return 9;
	}

	@Override
	public int delay() {
		return 0;		
	}

	@Override
	public boolean needsOnHop() {
		return false;
	}

	@Override
	public void onHalfOp(HalfOpEvent event) {}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}

}
