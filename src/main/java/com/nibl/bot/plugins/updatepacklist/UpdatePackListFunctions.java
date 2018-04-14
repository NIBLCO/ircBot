package com.nibl.bot.plugins.updatepacklist;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.NoticeEvent;

import com.nibl.bot.Bot;

public class UpdatePackListFunctions {
	
	UpdatePackList _updatepackList;
	UpdatePackListDAO _updatePackListDAO;
	Bot _myBot;
	PircBotX _pircbot;
	private HashSet<String> _offlineBots = new HashSet<String>();
	
	public UpdatePackListFunctions(Bot myBot, UpdatePackList updatePackList) {
		_updatepackList = updatePackList;
		_updatePackListDAO = new UpdatePackListDAO(myBot);
		_myBot = myBot;
		_pircbot = myBot.getBot();
	}


	/**
	 * This method should get the bots that respond via notice with their packlist
	 * from the database and then register them as a notice recipient.
	 */
	public void registerNoticeBots() {
		for (AbstractDistroBot distroBot : _updatepackList.getBotsMap().values()) {
			if (distroBot.getType().equals("NOTICE"))
				_updatepackList.getAcceptedSenders().add(distroBot.getName().toLowerCase());
		}
	}
	/**
	 * Purpose of this function is to:
	 * 	Find all offline bots.
	 * 	Find all online bots.
	 * 	Find all newly online bots.
	 * 	Create all newly online bots.
	 * 
	 * 	Register means adding to HashMaps:
	 * 		_updatepackList.getMyDAO().setOfflineBots
	 * 		_updatepackList.getMyDAO().setOnlineBots
	 */
	void registerNewAndOfflineBots() {
		
		// Get all currently online bots in channel
		HashSet<String> online_bots = new HashSet<String>();
		for(User onlineBot: findHops()){
			online_bots.add(onlineBot.getNick());
			if( getBot(onlineBot.getNick()) != null ){
				try{
					getBot(onlineBot.getNick()).setStatusId(1);
				}catch(Exception e){
					_myBot.sendMessageFair("#" + _myBot.getProperty("admin_channel"), "registerNewAndOfflineBots failed for " + onlineBot.getNick() + ". This bot might be stale.");
				}
			}
		}
		
		// Add possible new bots
		for(String botname : online_bots){
			if( getBot(botname) == null ){
				addNewBot(botname);
				_myBot.getLogger().info("Found new possible bot : " + botname);
			}
		}
		
		// Set all bots offline that are no longer hopped and not external
		for( String botName : _updatepackList.getReverseBotsMap().keySet()){
			if( !online_bots.contains( getBot(botName).getName() ) && !getBot(botName).isExternal() ){
				try{
					if( getBot(botName).getStatusId() == 1){
						_myBot.getLogger().info("Set Bot offline: " + botName);
						getBot(botName).setStatusId(0);
					}
				}catch(Exception e){
					_myBot.sendMessageFair("#" + _myBot.getProperty("admin_channel"), "registerNewAndOfflineBots failed for " + botName + ". This bot might be stale.");
				}
			}
		}
		
	}
	
	void addNewBot(String botName){
		Integer lowestID = 0;
		for( Integer test :  _updatepackList.getBotsMap().keySet() ){
			if( test < lowestID ){
				lowestID = test;
			}
		}

		lowestID--;
		
		_updatepackList.getAcceptedSenders().add(botName.toLowerCase());
		_updatepackList.getBotsMap().put(lowestID, DistroBotFactory.create(_myBot, lowestID, botName, "NONE", "NEW", 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new LinkedList<Pack>(), 0, 0, "unknown", 0));
		_updatepackList.getReverseBotsMap().put(botName.toLowerCase(), lowestID);
	}
	
	/*
	void processPackListFromBots() {
		for (AbstractDistroBot distroBot : _updatepackList.getBotsMap().values()) {
			if(distroBot.isOnlne() && !distroBot.isInformative() && !distroBot.getType().equals("XDCC")){
				try{//XDCC are processed onFileTransferFinished
					distroBot.processPackListFromBot();
					try {
						Thread.sleep(1000); // Sleep after every http request
					} catch (InterruptedException e) {
						_myBot.getLogger().error("ProcessPackListFromBots; Sleep Interruption at bot: " + distroBot.getName() + ". Error: " + e.getMessage());
					}
				} catch (Exception e){
					_myBot.getLogger().error(_updatepackList.getPrefix() + "Failed parsing: " + distroBot.getName() + " ;Type: " + distroBot.getType() + " URL: " + distroBot.getURL());
				}
			}
		}
	}
	*/

	public HashSet<User> findHops() {
		HashSet<User> activeBots = new HashSet<User>();
		for (Channel tempChannel : _myBot.getBot().getUserBot().getChannels()) {
			for (User tempUser : tempChannel.getHalfOps()) {
				activeBots.add(tempUser);
			}
		}
		return activeBots;
	}
	
	public HashSet<User> findUnHopBots(){
		HashSet<User> notHopped = new HashSet<User>();
		for (Channel tempChannel : _myBot.getBot().getUserBot().getChannels()) {
			for (User user : tempChannel.getNormalUsers()) {
				if( getBot(user.getNick()) != null ){
					notHopped.add(user);
				}
			}
		}
		return notHopped;
	}
	
	public HashSet<AbstractDistroBot> findNoPackBots(){
		HashSet<AbstractDistroBot> noPacks = new HashSet<AbstractDistroBot>();
		for (AbstractDistroBot tempBot : _updatepackList.getBotsMap().values()) {
			if(tempBot.getListing().size()==0 && tempBot.isOnlne() && !tempBot.isInformative()){
				noPacks.add(tempBot);
			}
		}
		return noPacks;
	}
	
	public void getPackListFromBots() {
		for (AbstractDistroBot distroBot : _updatepackList.getBotsMap().values()) {
			if( distroBot.isOnlne() && !distroBot.isInformative() ){
				distroBot.getPackListFromBot();
			}
		}
	}
	
	public void addNewBotWithXDCCListingToDatabase(NoticeEvent notice) {
		AbstractDistroBot distroBot = getBot(notice.getUser().getNick());

		String name = notice.getUser().getNick();
		String url = "XDCC SEND -1";
		String type = "XDCC";
		List<Pack> listing = distroBot.getListing();
		AbstractDistroBot newDistroBot = DistroBotFactory.create(_myBot, _updatepackList.getMyDAO(), name, url, type, listing, 0, 0, "unknown", PackParseFunctions.PARSER_NORMAL_ID);
		
		distroBot.addNewBotToDatabase();
		
		_updatepackList.getBotsMap().remove(distroBot.getId());
		_updatepackList.getReverseBotsMap().remove(distroBot.getName().toLowerCase());
		
		_updatepackList.getBotsMap().put(newDistroBot.getId(), newDistroBot);
		_updatepackList.getReverseBotsMap().put(newDistroBot.getName().toLowerCase(), newDistroBot.getId());
		
		distroBot.getPackListFromBot();
	}
	
	public void addNewBotWithNOListingToDatabase(NoticeEvent notice) {
		AbstractDistroBot distroBot = getBot(notice.getUser().getNick());

		String name = notice.getUser().getNick();
		String url = "http://plxchangeme";
		String type = "HTTP";
		List<Pack> listing = distroBot.getListing();
		AbstractDistroBot newDistroBot = DistroBotFactory.create(_myBot, _updatepackList.getMyDAO(), name, url, type, listing, 0, 0, "unknown", PackParseFunctions.PARSER_NULL_ID);
		newDistroBot.addNewBotToDatabase();
		
		_updatepackList.getBotsMap().remove(distroBot.getId());
		_updatepackList.getReverseBotsMap().remove(distroBot.getName().toLowerCase());
		
		_updatepackList.getBotsMap().put(newDistroBot.getId(), newDistroBot);
		_updatepackList.getReverseBotsMap().put(newDistroBot.getName().toLowerCase(), newDistroBot.getId());
	}

	public void addNewBotWithHTTPListingToDatabase(NoticeEvent notice) {
		addNewBotWithHTTPListingToDatabase(notice.getMessage(), notice.getUser().getNick());
	}
	
	public void addNewBotWithHTTPListingToDatabase(String message, String name){
		AbstractDistroBot distroBot = getBot(name.toLowerCase());
		int beginIndex;
		int endIndex;
		if (message.toLowerCase().contains("http")){
			beginIndex = message.indexOf("http://");
			if(beginIndex == -1){
				beginIndex = message.indexOf("https://");
			}
		} else {
			beginIndex = message.indexOf("www");
		}
		endIndex = message.indexOf(' ', beginIndex);
		if (endIndex == -1)
			endIndex = message.lastIndexOf("", message.length());
		
		String url = message.substring(beginIndex, endIndex);
		String type = "HTTP";
		List<Pack> listing = distroBot.getListing();
		AbstractDistroBot newDistroBot = DistroBotFactory.create(_myBot, _updatepackList.getMyDAO(), name, url, type, listing, 0, 0, "unknown", PackParseFunctions.PARSER_NORMAL_ID);
		
		newDistroBot.addNewBotToDatabase();
		
		_updatepackList.getBotsMap().remove(distroBot.getId());
		_updatepackList.getReverseBotsMap().remove(distroBot.getName().toLowerCase());
		
		_updatepackList.getBotsMap().put(newDistroBot.getId(), newDistroBot);
		_updatepackList.getReverseBotsMap().put(newDistroBot.getName().toLowerCase(), newDistroBot.getId());

		newDistroBot.getPackListFromBot();
		//newDistroBot.processPackListFromBot();
	}
	
	void save(SendChat session, String botIDorName) throws IOException{
		
		AbstractDistroBot distroBot = getBot(botIDorName);

		if( distroBot == null ){
			session.sendLine("Bot " + botIDorName + " unable to save bot to database.");
			return;
		}
		
		String name = distroBot.getName();
		String url = "http://plxchangeme";
		String type = "HTTP";
		List<Pack> listing = distroBot.getListing();
		AbstractDistroBot newDistroBot = DistroBotFactory.create(_myBot, _updatepackList.getMyDAO(), name, url, type, listing, 0, 0, "unknown", PackParseFunctions.PARSER_NORMAL_ID);
		
		newDistroBot.addNewBotToDatabase();
		
		_updatepackList.getBotsMap().remove(distroBot.getId());
		_updatepackList.getReverseBotsMap().remove(distroBot.getName().toLowerCase());
		
		_updatepackList.getBotsMap().put(newDistroBot.getId(), newDistroBot);
		_updatepackList.getReverseBotsMap().put(newDistroBot.getName().toLowerCase(), newDistroBot.getId());
		
		session.sendLine("Bot " + botIDorName + " saved to database.");
	}
	
	void delete(SendChat session, String botIDorName) throws IOException {
		AbstractDistroBot bot = getBot(botIDorName);
		
		if( bot == null ){
			session.sendLine("Unable to delete " + botIDorName + " from database.");
			return;
		}
		
		// Remove from memory
		_updatepackList.getBotsMap().remove( bot.getId() );
		_updatepackList.getReverseBotsMap().remove( bot.getName().toLowerCase() );
		// Remove from DB
		_updatepackList.getMyDAO().deleteBot( bot.getId() );
		
		session.sendLine("Bot " + botIDorName + " deleted from database");
		
	}
	
	void hop(SendChat session, String botIDorName) throws IOException {
		AbstractDistroBot bot = getBot(botIDorName);

		if( bot == null ){
			session.sendLine("Unable to hop " + botIDorName);
			return;
		}

		for(Channel chan : _myBot.getBot().getUserBot().getChannels() ){
			chan.send().setMode("+h", bot.getName());
		}
		
		session.sendLine(botIDorName + " hopped.");
	}
	
	void showOnline(SendChat session) throws IOException {
		String title = String.format("%4s %20s %5s %2s %7s", "id", "name", "type", "info", "url");
		session.sendLine(title);
		for (AbstractDistroBot distroBot : _updatepackList.getBotsMap().values()) {
			if( distroBot.getStatusId() == 1 ){
				String informative = "" + distroBot.getInformative();
				if( distroBot.isInformative() ){
					informative = Colors.RED + distroBot.getInformative() + Colors.NORMAL;
				}
				
				session.sendLine(String.format("%4s %20s %5s %2s %7s", distroBot.getId(), distroBot.getName(), distroBot.getType(), informative, distroBot.getURL()));
			}
		}
	}
	
	void showAll(SendChat session) throws IOException {
		String title = String.format("%4s %25s %5s %2s %7s", "id", "name", "type", "info", "url");
		session.sendLine(title);
		for (AbstractDistroBot distroBot : _updatepackList.getBotsMap().values()) {
			String informative = "" + distroBot.getInformative();
			if( distroBot.isInformative() ){
				informative = Colors.RED + distroBot.getInformative() + Colors.NORMAL;
			}
			
			session.sendLine(String.format("%4d %20s %5s %2s %7s", distroBot.getId(), distroBot.getName(), distroBot.getType(), informative, distroBot.getURL()));
		}
	}
	
	void showBot(SendChat session, String botname) throws IOException {
		AbstractDistroBot tempBot = getBot(botname);
		
		if(tempBot != null){
			session.sendLine(String.format("%4s %15s %6s %2s %8s %7s %10s", "id", "name", "type", "inform", "owner", "url", "count"));
			String informative = "" + tempBot.getInformative();
			if( tempBot.isInformative() ){
				informative = Colors.RED + tempBot.getInformative() + Colors.NORMAL;
			}
			
			session.sendLine(String.format("%4d %15s %6s %2s %8s %7s %10s", tempBot.getId(), tempBot.getName(), tempBot.getType(), informative, tempBot.getOwner(), tempBot.getURL(), tempBot.getListing().size()+" Packs"));
		}else{
			session.sendLine("Could not find " + botname);
		}
	}
	
	public void setURL(SendChat session, String args) throws IOException {
		int end = args.indexOf(' ');
		String url = args.substring(end).trim();
		AbstractDistroBot bot = getBot( args.substring(0, end) );
		if( bot == null ){
			session.sendLine("Correct ID?");
			return;
		}
			
		bot.setURL(url); //update memory
		int result = _updatepackList.getMyDAO().updateBotURL(bot.getId(), url); //update database
		if (result > 0)
			session.sendLine("Updated " + bot.getId() + " with url = " + url );
		else
			session.sendLine("Correct ID?");
	}
	
	public void setInformative(SendChat session, String args) throws IOException {
		int end = args.indexOf(' ');
		String informative = args.substring(end).trim();
		AbstractDistroBot bot = getBot( args.substring(0, end) );
		if( bot == null ){
			session.sendLine("Correct ID?");
			return;
		}
		
		try{
			Integer inform = Integer.parseInt(informative);
			if( inform != 0 && inform != 1 ){
				throw new Exception();
			}
			
			_updatepackList.getMyDAO().updateBotInformative(bot.getId(), inform);
			bot.setInformative(inform);
			
			session.sendLine("Updated " + bot.getId() + " informative = " + inform );
			
		}catch(Exception e){
			session.sendLine("Informative must be 0 or 1.");
		}
	}
	
	public void setBatch(SendChat session, String args) throws IOException {
		int end = args.indexOf(' ');
		String batchInput = args.substring(end).trim();
		AbstractDistroBot bot = getBot( args.substring(0, end) );
		if( bot == null ){
			session.sendLine("Correct ID?");
			return;
		}
		
		try{
			Integer batch = Integer.parseInt(batchInput);
			if( batch != 0 && batch != 1 ){
				throw new Exception();
			}
			
			_updatepackList.getMyDAO().updateBotBatch(bot.getId(), batch);
			bot.setInformative(batch);
			
			session.sendLine("Updated " + bot.getId() + " batch = " + batch );
			
		}catch(Exception e){
			session.sendLine("Batch must be 0 or 1.");
		}
	}

	public void setType(SendChat session, String args) throws IOException, SQLException {
		int end = args.indexOf(' ');
		String type = args.substring(end).trim().toUpperCase();
		if (testType(type)) {
			AbstractDistroBot bot = getBot(args.substring(0, end));
			
			if( bot == null ){
				session.sendLine("Correct ID?");
				return;
			}
			
			int result = _updatepackList.getMyDAO().updateBotType(bot.getId(),type); //update database
			if (result > 0){
				session.sendLine("Updated " + bot.getId() + " with type = " + type);
				_updatepackList.getBotsMap().put(bot.getId(), _updatepackList.getMyDAO().buildDistroBot(bot.getId(),
						bot.getName(),bot.getURL(),type,bot.getStatusId(),bot.getLastSeen(),bot.getLastProcessed(),
						bot.getInformative(),bot.getExternal(), bot.getOwner(), bot.getParserId()));
			}else
				session.sendLine("Correct ID?");
		} else {
			session.sendLine("Incorrect type only XDCC NOTICE HTTP JS HTML");
		}
	}
	
	public void getList(SendChat session, String args) throws IOException{
		AbstractDistroBot bot = getBot(args);
		if(bot==null){
			session.sendLine("Correct ID?");
			return;
		}
		session.sendLine("Lets see what happens with " + bot.getName());
		
		bot.getPackListFromBot();
	}
	
	public void remake(SendChat session, String botName) throws IOException, SQLException{
		if(!_updatepackList.isIterating()){
			
			
			AbstractDistroBot trashBot = getBot(botName); // Get bot in memory
			AbstractDistroBot tempBot = null;
			if( null == trashBot ){ // bot not found in memory, so grab new one from DB
				tempBot = _updatepackList.getMyDAO().getBotFromDatabase(botName);
			} else {// bot found in memory so grab new one using old name
				tempBot = _updatepackList.getMyDAO().getBotFromDatabase(trashBot.getName());
			}
			
			if( tempBot == null ){
				session.sendLine("Unable to remake bot: " + botName + ". Not in database?");
			}
			
			if( null != trashBot ){
				_updatepackList.getBotsMap().remove(trashBot.getId());
				_updatepackList.getReverseBotsMap().remove(trashBot.getName().toLowerCase());
			}
			
			_updatepackList.getBotsMap().remove(tempBot.getId());
			_updatepackList.getReverseBotsMap().remove(tempBot.getName().toLowerCase());
			
			_updatepackList.getBotsMap().put(tempBot.getId(), tempBot);
			_updatepackList.getReverseBotsMap().put(tempBot.getName().toLowerCase(), tempBot.getId());
			
			
		}else{
			_myBot.getLogger().warn("UpdatePackList is iterating, please try after completion!");
		}
	}
	
	public void countPacks(SendChat session, String args) throws IOException {
		AbstractDistroBot bot = getBot(args);
		if(bot==null){
			session.sendLine("Correct ID?");
			return;
		}
		session.sendLine(bot.getName() + " has " + bot.getListing().size() + " packs");			
	}
	
	/**
	 * Find any:
	 * 		invalid status_id's
	 * 		bots that are not hopped
	 * 		bots that have no packs
	 * 
	 * @param session
	 * @throws IOException
	 */
	public void findErrorBots(SendChat session) throws IOException{
		
		if(!_updatepackList.isIterating()){
			
			LinkedList<User> invalidStatus = new LinkedList<User>();
			HashSet<User> unHoppedBots = findUnHopBots();
			HashSet<AbstractDistroBot> noPacks = findNoPackBots();
	
			StringBuilder sb = new StringBuilder();
			sb.append(_updatepackList.getPrefix() + "List of online Hops status: (Offline bots are in an Invalid State or Between Iterations) \r\n");
			for(User user : findHops()){
				if( getBot(user.getNick()) == null ){
					sb.append(_updatepackList.getPrefix() + Colors.RED + user.getNick() + " is possibly new (fixing)\r\n");
					addNewBot(user.getNick());
					getBot(user.getNick()).getPackListFromBot();
				}else if(getBot(user.getNick().toLowerCase()).isOnlne()){
					sb.append(_updatepackList.getPrefix() + Colors.BLUE + user.getNick() + " is Online\r\n");
				}else{
					if( getBot(user.getNick()).isInformative() || getBot(user.getNick()).getListing().size() == 0 ){
						continue;
					}
					sb.append(_updatepackList.getPrefix() + Colors.RED + user.getNick() + " is marked Offline (fixing)\r\n");
					
					try{
						getBot(user.getNick()).setStatusId(1);
					}catch(Exception e){
						session.sendLine("Couldn't set property for bot: " + user.getNick());
					}
					
					invalidStatus.add(user);
				}
			}
			
			sb.append("\r\n");
			sb.append(_updatepackList.getPrefix() + "Questionable Bots: \r\n");
			if(invalidStatus.size()>0){
				for(User temp : invalidStatus){
					sb.append(_updatepackList.getPrefix() + Colors.RED + temp.getNick() + " has invalid status.\r\n");
				}
			}else{
				sb.append(_updatepackList.getPrefix() + Colors.GREEN + "Cant find any bots with Invalid status.\r\n");
			}
			
			if(unHoppedBots.size()>0){
				for(User temp : unHoppedBots){
					sb.append(_updatepackList.getPrefix() + Colors.RED + temp.getNick() + " is not hopped (fixing)\r\n");
					hop(session, temp.getNick());
				}
			}else{
				sb.append(_updatepackList.getPrefix() + Colors.GREEN + "Cant find any unHopped bots.\r\n");
			}
			
			if(noPacks.size()>0){
				for(AbstractDistroBot tempBot : noPacks){
					sb.append(_updatepackList.getPrefix() + Colors.RED + "No packs found for: "+ getBot(tempBot.getName().toLowerCase()).getId() + " " + tempBot.getName() + "\r\n");
					
					if( tempBot.getId() < 0 ){ // If the bot doesn't have an ID, that probably means it isn't responding and can't be made into an HTTP bot.
						addNewBotWithHTTPListingToDatabase("http://changemeplx", tempBot.getName()); // Add as an HTTP bot
						sb.append(_updatepackList.getPrefix() + Colors.RED + "Bot Added to DB as HTTP: "+ getBot(tempBot.getName().toLowerCase()).getId() + " " + tempBot.getName() + "\r\n");
					}
				}
			}else{
				sb.append(_updatepackList.getPrefix() + Colors.GREEN + "All bots have packs.\r\n");
			}
			
			session.sendLine(sb.toString());
		}else{
			_myBot.getLogger().warn("UpdatePackList is iterating, please try after completion!");
		}
		
	}
	
	public void passMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode){
		if(mode.startsWith("+h")){
			if(!_updatepackList.isIterating()){
				
				for(String botName : mode.replaceAll("\\+\\w+", "").trim().split(" ")){
					
					if( getBot(botName) != null ){
						try{
							getBot(botName).setStatusId(1);
						}catch(Exception e){
							_myBot.sendMessageFair("#" + _myBot.getProperty("admin_channel"), "passMode failed; Bot: " + botName);
						}
					}else{
						addNewBot(botName);						
						_myBot.getLogger().info("Found new possible bot : " + botName);
					}
					
				}
				
			}else{
				//leave it alone until next iteration
			}
			
		}else if(mode.startsWith("-h")){
			
			if(!_updatepackList.isIterating()){
				for(String botName : mode.replaceAll("\\-\\w+", "").trim().split(" ")){
					if(_updatepackList.getBotsMap().containsKey(botName.toLowerCase())){
						getOfflineBots().add(botName.toLowerCase());//maybe
						_updatepackList.getMyDAO().setOfflineBots(getOfflineBots());
					}
				}
			}else{
				//leave it alone until next iteration
			}
			
		}
	}
	
	private boolean testType(String type) {
		return type.equals("XDCC") || type.equals("NOTICE") || type.equals("HTTP") || type.equals("HTML") || type.equals("JS");
	}
	
	public HashSet<String> getOfflineBots(){
		return _offlineBots;
	}
	
	/**
	 * This method will delete xdcc list files, so there are no stale lists.
	 * Should wait a couple seconds
	 */
	public void deleteXDCCFiles(){
		File dir = new File(_myBot.getProperty("download_dir").replaceAll("---", "/")); 
		String[] files = dir.list();
		for (int i=0; i<files.length; i++) {
			String filename = files[i];
			if(!(new File(_myBot.getProperty("download_dir").replaceAll("---", "/") + "/" + filename)).delete())
				_myBot.getLogger().info("Failed to delete " + filename + " after update!");
		}
	}
	
	public void cancelAnnounceHandlers(){
		for(AbstractDistroBot curBot : _updatepackList.getBotsMap().values())
			curBot.cancelAnnounceHandler();
	}
	
	public static AbstractDistroBot getBot(String botIDorName){
		AbstractDistroBot bot = null;
		
		try{
			try{
				bot = UpdatePackList._botsMap.get( Integer.parseInt(botIDorName) );
			}catch(Exception e){
				bot = UpdatePackList._botsMap.get( UpdatePackList._botsReverseMap.get(botIDorName.toLowerCase()) );
			}
		}catch(NullPointerException ex){ }
		
		return bot;
	}
	
	public void getOwner(String botIDorName){
		AbstractDistroBot bot = UpdatePackListFunctions.getBot(botIDorName);
		if( null == bot ){
			_myBot.getLogger().dccChatSend("Unknown bot: " + botIDorName, _updatepackList.getAccessLevel());
		} else {
			_myBot.getLogger().dccChatSend("Sending xdcc owner message to " + bot.getName(), _updatepackList.getAccessLevel());
			this._myBot.sendMessageFair(bot.getName(), "xdcc owner");
		}
	}

	public void setOwner(String botIDorName, String botOwner){
		AbstractDistroBot bot = UpdatePackListFunctions.getBot(botIDorName);
		if( null == bot ){
			_myBot.getLogger().dccChatSend("Unknown bot: " + botIDorName, _updatepackList.getAccessLevel());
		} else {
			try {
				_updatePackListDAO.updateOwner(bot, botOwner);
				_myBot.getLogger().dccChatSend("Bot owner for " + bot.getName() + " updated", _updatepackList.getAccessLevel());
			} catch (SQLException e) {
				_myBot.getLogger().dccChatSend("Bot owner for " + bot.getName() + " failed to update.", _updatepackList.getAccessLevel());
			}
		}
	}
	
	public void setAllOwners(){
		
		int counter = 0;
		for (AbstractDistroBot distroBot : UpdatePackList._botsMap.values()) {
			if(distroBot.isOnlne()){
				counter++;
				this.getOwner(distroBot.getName());
				counter = counter%3;
				if(counter == 0){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public void getUnknownOwners(){
		for (AbstractDistroBot distroBot : UpdatePackList._botsMap.values()) {
			if( distroBot.isOnlne() && distroBot.getOwner().equals("unknown") ) {
				_myBot.getLogger().dccChatSend("Unknown owner for " + distroBot.getName() + " :ID " + distroBot.getId(), _updatepackList.getAccessLevel());
			}
		}
	}
}
