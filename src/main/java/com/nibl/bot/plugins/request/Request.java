package com.nibl.bot.plugins.request;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;

public class Request extends Command {
	
	RequestDAO _requestDAO;
	String _adminHelp = null;
	public static final int _requestAmount = 100;
	
	public Request(Bot myBot, Channel channel, User user, String actualcmd, String args) {
		super(myBot, channel, user, actualcmd, args);
		_requestDAO = (RequestDAO) _myBot.getDAOFactory().getDAO("RequestDAO");
	}

	@Override
	public String getCommand() {
		return "!request !rshow !rfill !rfilling !rdelete";
	}

	@Override
	public String getDescription() {
		return "Takes a request from a user and hopefully someone fills it.";
	}

	@Override
	public String getSyntax() {
		return "[name of show]";
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> temp = new LinkedList<String>();
		temp.add("show - shows unfilled requests\r\n");
		temp.add("myfills - show your requests to be filled\r\n");
		temp.add("filling # - mark the request to be filled\r\n");
		temp.add("fill # [botname] - fills request #\r\n");
		temp.add("delete # - delete request #");
		return temp;
		/*StringBuilder commands = new StringBuilder();
		commands.append(getPrefix() + "show - shows unfilled requests\r\n");
		commands.append(getPrefix() + "myfills - show your requests to be filled\r\n");
		commands.append(getPrefix() + "filling # - mark the request to be filled\r\n");
		commands.append(getPrefix() + "fill # - fills request #\r\n");
		commands.append(getPrefix() + "delete # - delete request #");
		//sb.insert(0, commands.toString());
		_adminHelp = sb.toString() + commands.toString();
		return _adminHelp;*/
	}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		if (message.equals("show")) {
			session.sendLine( show() );
		} else if (message.startsWith("fill ")) {
			try{
				String substr = message.substring(5);
				String[] split = substr.split(" ");
				String botname = null;
				if( split.length > 1 ){
					botname = split[1];
				}
				session.sendLine( fill(split[0], botname, user) );
			} catch (Exception e){
				session.sendLine("Invalid arguments. fill # [botname]");
			}
		} else if (message.startsWith("filling ")) {
			session.sendLine(filling(message.substring(8), user));
		} else if (message.startsWith("myfills")) {
			session.sendLine(myfills(user));
		} else if (message.startsWith("delete ")) {
			session.sendLine(delete(message.substring(7), user));
		} else {
			session.sendLine("Unknown Command.");
		}
	}

	@Override
	public void execute() {
		int accessLevel = _requestDAO.getAccessLevel(_user);
		
		if( accessLevel >= 7 && this._actualcmd.equalsIgnoreCase("!rshow") ){ // show requests			
			try{
				
				String[] show = show().split("\r\n");
				
				for( String s : show ){
					_myBot.sendNoticeFair(_user.getNick(), s);
				}
				
			} catch(Exception e){
				_myBot.sendNoticeFair(_user.getNick(), "Failed showing requests");
			}
			
		} else if( accessLevel >= 7 && this._actualcmd.equalsIgnoreCase("!rfill") ){ // set fill
			if (_args.length() == 0) {
				_myBot.sendMessageFair(_channel, "Use !rfill request_id to fill");
				return;
			}
			
			try{
				try{
					String[] split = _args.split(" ");
					String botname = null;
					if( split.length > 1 ){
						botname = split[1];
					}
					
					String result = fill( split[0], botname, _user );
					if( !result.isEmpty() ){
						_myBot.sendMessageFair(_channel, result);
					}
					
				} catch (Exception e){
					_myBot.sendMessageFair(_channel, "Invalid arguments. fill # [botname]");
				}
			} catch(Exception e){
				_myBot.sendMessageFair(_channel, "Failed to fill request " + _args);
			}
			
		} else if( accessLevel >= 7 && this._actualcmd.equalsIgnoreCase("!rfilling") ){ // set filling
			if (_args.length() == 0) {
				_myBot.sendMessageFair(_channel, "Use !rfilling request_id to start filling");
				return;
			}
			
			try{
				String result = filling( _args, _user );
				
				if( !result.isEmpty() ){
					_myBot.sendMessageFair(_channel, result);
				}
				
			} catch(Exception e){
				_myBot.sendMessageFair(_channel, "Failed filling request " + _args);
			}
			
		} else if( accessLevel >= 7 && this._actualcmd.equalsIgnoreCase("!rdelete") ){ // set delete
			if (_args.length() == 0) {
				_myBot.sendMessageFair(_channel, "Use !rdelete request_id to delete");
				return;
			}
			
			try{
				String result = delete( _args, _user );
				if( !result.isEmpty() ){
					_myBot.sendMessageFair(_channel, result);
				}
				
			} catch(Exception e){
				_myBot.sendMessageFair(_channel, "Failed deleting request " + _args);
			}
			
		} else if( this._actualcmd.equalsIgnoreCase("!request") ) { // set request
			if (_args.length() == 0) {
				_myBot.sendMessageFair(_channel, "Use !request request_term to request");
				return;
			}
			
			if (_requestDAO.add(_channel, _user, _args)) {
				_myBot.sendMessageFair(_channel, _user.getNick() + " has spent $" + _requestAmount + " and requested " + Colors.UNDERLINE + _args);
				_myBot.sendMessageFairToAdmin("NEW REQUEST - " + Colors.UNDERLINE + _args + Colors.NORMAL + " from " + _user.getNick());
			}
			
		} else {
			_myBot.sendMessageFair(_channel, "You don't have access to the " + this._actualcmd + " command.");
			return;
		}
		
	}

	private String show() {
		return _requestDAO.getUnfilledRequests();
	}

	private String delete(String requestID, User user) {
		String result = "";
		RequestObj request = _requestDAO.getRequestObj(requestID);
		if( null == request ){
			result = requestID + " is not a valid request_id";
		} else {
			if( _requestDAO.deleteRequest(request, user) ){
				//success
				String success = "Request #" + request.getID() + " - " + request.getRequest() + " has been deleted by " + user.getNick();
				//send to admin channel, in_channel and made_by
				_myBot.sendMessageFairToAdmin(success);
				_myBot.sendMessageFair(request.getInChannel(), success);
				_myBot.sendNoticeFair(request.getMadeBy(), success);
				
			} else {
				//failure
				result = "Unable to delete request " + requestID;
			}
		}
		return result;
	}

	private String fill(String requestID, String botname, User user) {
		String result = "";
		
		RequestObj request = _requestDAO.getRequestObj(requestID);
		if( null == request ){ // unable to get request
			result = requestID + " is not a valid request_id";
		} else {
			if( _requestDAO.fillRequest(request, botname, user) ){
				//success
				String success = "Request #" + request.getID() + " - " + request.getRequest() + " has been filled by " + user.getNick();
				if( null != botname ){
					success += " on " + botname;
				}
				//send to admin channel, in_channel and made_by
				_myBot.sendMessageFairToAdmin(success);
				_myBot.sendMessageFair(request.getInChannel(), success);
				_myBot.sendNoticeFair(request.getMadeBy(), success);
			} else {
				//failure
				result = "Unable to fill request " + requestID;
			}
			
		}
		// Result is either output via session or to the command's originating channel
		// On success, there is no result
		return result;
	}

	private String filling(String requestID, User user) {
		String result = "";
		RequestObj request = _requestDAO.getRequestObj(requestID);
		if( null == request ){
			result = requestID + " is not a valid request_id";
		} else {
			if( _requestDAO.fillingRequest(request, user) ) {
				
				String success = "Request #" + request.getID() + " - " + request.getRequest() + " will be filled by " + user.getNick();
				_myBot.sendMessageFairToAdmin(success);
				_myBot.sendMessageFair(request.getInChannel(), success);
				_myBot.sendNoticeFair(request.getMadeBy(), success);
			} else {
				result = "Unable to fill request " + requestID;
			}
		}
		return result;
	}

	private String myfills(User user) {
		return _requestDAO.getMyFills(user);
	}

	@Override
	public String getName() {
		return "request";
	}

	@Override
	public int getAccessLevel() {
		return 6;
	}

	@Override
	public void executeAfterDisabled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeAfterEnabled() {
		// TODO Auto-generated method stub
		
	}

	public void mySchedule1() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public TreeMap<String, Integer> getCommandAccessLevels() {
		// Default command access levels
		TreeMap<String, Integer> commandAccessLevels = new TreeMap<String, Integer>();
		for(String command : this.getCommand().split(" ")) {
			commandAccessLevels.put(command.toLowerCase(), this.getAccessLevel());
		}
		commandAccessLevels.put("!request", -1);
		return commandAccessLevels;
	}
	
	@Override
	public Boolean acceptPrivateMessage() {
		return false;
	}

}
