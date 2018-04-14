package com.nibl.bot.plugins.scheduler;

import java.util.Timer;
import java.util.TimerTask;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class SchedulerTask {

	Command _command;
	long _seconds;
	int _number;
	long _delay;
	Bot _myBot;
	Scheduler _scheduler;
	Timer _timer = new Timer();
	
	
	SchedulerTask(Bot myBot,Scheduler scheduler, int scheduleNumber, Command command, long delay, long seconds){
		_myBot = myBot;
		_command = command;
		_seconds = seconds*1000;
		_delay = delay*1000;
		_number = scheduleNumber;
		_scheduler = scheduler;
		setTimer();
	}
	
	private void setTimer(){
		switch(_number){
		case 1:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule1();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 2:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule2();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 3:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule3();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 4:			
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule4();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 5:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule5();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 6:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule6();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 7:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule7();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 8:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule8();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 9:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule9();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		case 10:
			_timer.scheduleAtFixedRate(new TimerTask(){
				public void run() {
					_command.mySchedule10();_scheduler.updateDelay(_command, _number);
				}
			}, _delay, _seconds);
			break;
		}
	}
	
	/*private Date _delay{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, cal.get(Calendar.HOUR)+1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}*/
	
	public int getNumber(){
		return _number;
	}
	
	public Command command(){
		return _command;
	}
	
	public Timer getTimer(){
		return _timer;
	}
}
