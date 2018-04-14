package com.nibl.bot.plugins.redirect;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;
import com.nibl.bot.plugins.redirect.RedirectDAO;

public class Redirect extends Command {
	
	RedirectDAO _redirectDAO;
	
	public Redirect(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_redirectDAO = new RedirectDAO(myBot);
	}

	@Override
	public String getName() {
		return "redirect";
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("help - Shows a list commands\r\n");
		return help;
	}

	@Override
	public void admin(SendChat session, User user, String message) {}

	@Override
	public void execute() {
		_args = _args.trim();
		if(_args.length()==0)
		{
			if( this.isOriginatePrivateMessage() ){
				_myBot.sendMessageFair(_user.getNick(), getDescription());
			} else {
				_myBot.sendMessageFair(_channel, getDescription());
			}
			
			return;
		}
		
		try { // test if url is valid
		    URL url = new URL(_args);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		    
		} catch (MalformedURLException e) {
			_myBot.sendMessageFair(_channel, "Please use a valid url");
		} catch (IOException e) {
			_myBot.sendMessageFair(_channel, "Please use a valid url");
		}
		
		try {
			String redirectHash = _redirectDAO.getRedirectHash(_args);
			String redirectURL = _redirectDAO.getRedirect(redirectHash);
			if( null == redirectURL ){
				// redirect url doesn't exist, so create a new one
				_redirectDAO.createRedirect(_args, redirectHash);
				redirectURL = _redirectDAO.getRedirect(redirectHash);
			}
			_myBot.sendMessageFair(_channel, _args + " -> " + redirectURL);
		} catch (Exception e) {
			_myBot.sendMessageFair(_channel, "Failed creating url. Contact Jenga");
		}
	}

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Override
	public void executeAfterEnabled() {
		
	}

	@Override
	public void executeAfterDisabled() {
		
	}

	@Override
	public String getCommand() {
		return "!redirect !tinyfy";
	}

	@Override
	public String getSyntax() {
		return "[url]";
	}

	@Override
	public String getDescription() {
		return "tinyfy a long url.  Result will be http://nibl.co.uk/r/##";
	}

	@Override
	public Boolean acceptPrivateMessage() {
		return false;
	}

	@Override
	public TreeMap<String, Integer> getCommandAccessLevels() {
		// Default command access levels
		TreeMap<String, Integer> commandAccessLevels = new TreeMap<String, Integer>();
		for(String command : this.getCommand().split(" ")) {
			commandAccessLevels.put(command.toLowerCase(), -1);
		}
		return commandAccessLevels;
	}

}
