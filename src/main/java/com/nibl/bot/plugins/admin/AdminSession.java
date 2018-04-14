package com.nibl.bot.plugins.admin;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.nibl.bot.command.Command;
import com.nibl.bot.service.Service;

public class AdminSession extends BotExtend implements Runnable {
	
	SendChat _session;
	User _user;
	BotUser _botUser;
	String _password;
	AdminSessionDAO _adminDAO;
	int _accessLevel;
/*
 * Access Levels:
 * 10: kill_bot, reload
 * 9: adduser
 * 8: manage (all modules)
 * 7-3: manage (some modules), show
 * 2: help, (eventually manage my bot)
 * 1: nothing now, maybe a quick or different way to search?
 * 0 or -1: kicked out
 */
	public AdminSession(Bot myBot, BotUser botUser, User user, String password) {
		super(myBot);
		_user = user;
		_password = password;
		_adminDAO = (AdminSessionDAO) _myBot.getDAOFactory().getDAO("AdminSessionDAO");
	}

	private void help() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("Commands:\r\n");
		
		sb.append("help - displays this help\r\n");
		if(_accessLevel>=3){
			sb.append("msg [user] [message] - private message user\r\n");
			sb.append("show - shows the loaded modules\r\n");
			sb.append("manage [module] - goes to the module's admin\r\n");
		}
		sb.append("password [new_pass] - changes your admin password\r\n");
		if(_accessLevel>=9){
			sb.append("adduser [user] [pw] [level] - adds a new admin user\r\n");
			sb.append("deluser [user] - deletes an admin user\r\n");
		}
		sb.append("loglevel - set loglevel [1,2,3]");
		if(_accessLevel>=10){
			sb.append("reload - reload the configuration file\r\n");
			sb.append("jump [server] - reconnect to another server (dont do it, framework is being updated for this issue)\r\n");
		//sb.append("reload modules - reload all of the modules\r\n");
			sb.append("kill bot - X.X\r\n");
		}
		sb.append("quit - ends the session\r\n");
		_session.sendLine(sb.toString());
	}

	private void show() throws IOException {
		StringBuilder sb = new StringBuilder();
		List<Command> commands = _myBot.getCommandFactory().getRegisteredCommand();
		for (Command command : commands) {
			if(command.getAccessLevel()<=_accessLevel)
				sb.append(command.getName() + " | " + command.getDescription() + "\r\n");
		}
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if(service.getAccessLevel()<=_accessLevel)
				sb.append(service.getName() + " | " + service.getDescription() + "\r\n");
		}

		_session.sendLine(sb.toString());
	}

	private void manage(String args, SendChat session) throws IOException {
		List<Command> commands = _myBot.getCommandFactory().getRegisteredCommand();
		for (Command command : commands) {
			if (args.equals(command.getName())) {
				session.sendLine(Colors.UNDERLINE + Colors.BROWN + "Welcome to " + command.getName() + " admin.");
				command.adminWrapper(session, _user);
				session.sendLine(Colors.UNDERLINE + Colors.BROWN + "Welcome back to main admin.");
				return;
			}
		}
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (args.equals(service.getName())) {
				session.sendLine(Colors.UNDERLINE + Colors.BROWN + "Welcome to " + service.getName() + " admin.");
				service.adminWrapper(session, _user);
				session.sendLine(Colors.UNDERLINE + Colors.BROWN + "Welcome back to main admin.");
				return;
			}
		}
		session.sendLine("Unknown Module. Please Try Again.");
	}
	
	private void jump(String server) throws UnknownHostException, IOException {
		/*try{
			if(InetAddress.getByName(server).isReachable(3000))
				_myBot.jumpServer(server);
			else
				_session.sendLine("Host not online: " + server);
		}catch(Exception e){
			_session.sendLine("Bad host: " + server);
		}*/
	}

	private boolean process(String input, SendChat session) throws IOException {
		if (input == null)
			return false;

		if (input.toLowerCase().equals("quit")) {
			return false;
		} else if (input.toLowerCase().equals("help")) {
			help();
		} else if (input.toLowerCase().startsWith("jump")) {
			jump(input.replaceFirst("jump", "").trim());
		} else if (input.toLowerCase().equals("show") && _accessLevel>=3) {
			show();
		} else if (input.toLowerCase().startsWith("jump") && _accessLevel>=10) {
			//_myBot.jumpServer("");
		} else if (input.toLowerCase().equals("reload") && _accessLevel>=10) {
			//_myBot.reloadProperties();
		} else if (input.toLowerCase().equals("reload modules") && _accessLevel>=10) { 
			//_myBot.reloadModules(); 
		} else if (input.toLowerCase().startsWith("manage ") && _accessLevel>=3) {
			manage(input.substring(7), session);
		} else if (input.toLowerCase().startsWith("password ")) {
			_session.sendLine(_adminDAO.changePass(input.substring(9),_user.getNick()));
		} else if (input.toLowerCase().startsWith("adduser ") && _accessLevel>=9) {
			_session.sendLine(_adminDAO.addAdmin(input.substring(7)));
		} else if (input.toLowerCase().startsWith("deluser ") && _accessLevel>=9) {
			_session.sendLine(_adminDAO.delAdmin(input.substring(7)));
		} else if (input.toLowerCase().startsWith("loglevel ")) {
			_session.sendLine(_adminDAO.setAdminLogLevel(input.substring(9),_user.getNick()));
		} else if (input.toLowerCase().equals("kill bot") && _accessLevel>=10) {
			System.exit(0);
		} else if (input.toLowerCase().startsWith("msg ") && _accessLevel>=3){
			String[] args = input.replace("msg ", "").split(" ", 2);
			_myBot.sendMessageFair(args[0],args[1]);
		}else {
			_session.sendLine("Unknown Command. Please Try Again.");
		}
		return true;
	}
	
	public SendChat getIRCSession(){
		return this._session;
	}
	
	@Override
	public void run() {
		try {
			if( !_adminDAO.authorizeUser(_user, _password) ){
				return;
			}
			
			_accessLevel = _adminDAO.getAccessLevel(_user);
			
			try {
				_myBot.getLogger().info(_user.getNick() + " " + _user.getLogin() + "@" + _user.getHostmask() + " Logged in to admin.");
				_myBot.setAdminSession(this);
				_session = _user.send().dccChat();
				
				_session.sendLine(Colors.UNDERLINE + Colors.BROWN + "Welcome to " + _myBot.getProperty("bot_name"));
				help();
				boolean done = false;
				do {
					_session.sendLine("What can I do for you today?");
					String input = _session.readLine();
					if(input==null)
						input = "quit";
					_myBot.getLogger().info(_user.getNick() + " " + _user.getLogin() + "@" + _user.getHostmask() + ": " + input);
					done = process(input,_session);
				} while (done);
				_session.sendLine("Connection Closed.");
			} catch (SocketException e) {
				_myBot.getLogger().info(_user.getNick() + " closed the connection to admin.");
			} catch (IOException e) {
				_myBot.sendMessageFair(_user.getNick(), "Sorry I hit the red button by accident.");
				_myBot.getLogger().error("Failed AdminSession",e);
			} finally {
				_myBot.delAdminSession(this);
				_session.close();
			}
		} catch (Exception e) {
			_myBot.sendMessageFair(_user.getNick(), "Sorry I hit the red button by accident.");
			_myBot.getLogger().error(_user.getNick() + " " + _user.getLogin() + "@" + _user.getHostmask() + " Failed.", e);

		}

	}
}
