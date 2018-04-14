package com.nibl.bot.command;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.reflections.Reflections;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

public class CommandFactory extends BotExtend {
	
	List<Command> _registeredCommand;
	
	public CommandFactory(Bot myBot) {
		super(myBot);
		_registeredCommand = register();
	}

	public List<Command> getRegisteredCommand() {
		return _registeredCommand;
	}
	
	public Command findRegisteredCommand(String cname) {
		for (Command command : _registeredCommand) {
			if(command.getName().equals(cname) || command.getCommand().contains(cname))
				return command;
		}
		return null;
	}

	private List<Command> register() {
		List<Command> result = new LinkedList<Command>();

	      Reflections reflections = new Reflections("com.nibl.bot.plugins");
	        
	         Set<Class<? extends Command>> allClasses = 
	                 reflections.getSubTypesOf(Command.class);
	         
		try {
		        for(Class<? extends Command> command : allClasses ) {
                    String simpleClassName = StringUtils.substringAfterLast(command.getName(), ".");
                    _myBot.getLogger().info("Class " + simpleClassName + " Loading.");
                    @SuppressWarnings("unchecked")
                    Constructor<Command> constructor = (Constructor<Command>) command.getConstructor(new Class[] { Bot.class, Channel.class, User.class, String.class, String.class });
                    Command cmd = constructor.newInstance(_myBot, null, null, null, null);
                    if( cmd.isEnabled() ) {
                        cmd.enable(); // First time execution for schedules and whatnot
                    }
                    result.add(cmd);
	            }
		} catch (Exception e) {
			_myBot.getLogger().error("You goofed loading the damn classes.", e);
		}

		return result;
	}

	public Command create(Channel channel, User user, String login, String hostname, String message) {
		String args;

		for (Command command : _registeredCommand) {
			if (Arrays.asList(command.getCommand().split(" ")).contains(message.split(" ")[0])) {
				String actualcmd = null;
				for (String cmd : command.getCommand().split(" ")){
					if(message.split(" ")[0].equalsIgnoreCase(cmd)){
						actualcmd=cmd;
						break;
					}
				}
				String property = _myBot.getProperty(command.getName());
				if (property == null) {
					_myBot.getLogger().error("FROM : " + command.getName() + " -- " + actualcmd + " : Is the config correct?");
					return null;
				} else if (!property.equals("enabled") || !command.isEnabled()) {
					return null;
				}
				args = message.substring(actualcmd.length()).trim();
				try {
					@SuppressWarnings("unchecked")
					Constructor<Command> constructor = (Constructor<Command>) command.getClass().getConstructor(new Class[] { Bot.class, Channel.class, User.class, String.class, String.class });
					return constructor.newInstance(_myBot, channel, user, actualcmd, args);
				} catch (Exception e) {
					_myBot.getLogger().error("Error creating command",e);
				}
			}
		}
		return null;
	}
	public void reRegister() {
		_registeredCommand = register();
	
	}
}
