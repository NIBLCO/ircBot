package com.nibl.bot;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;

import com.nibl.bot.command.CommandFactory;
import com.nibl.bot.dao.DAOFactory;
import com.nibl.bot.database.Database;
import com.nibl.bot.database.DatabaseFactory;
import com.nibl.bot.plugins.admin.AdminSession;
import com.nibl.bot.plugins.admin.BotUser;
import com.nibl.bot.service.ServiceFactory;
import com.nibl.bot.util.BotProperties;
import com.nibl.bot.util.Logbook;

public class Bot {
	
	private PircBotX pircBotX;
	private String configFile;
	private BotProperties properties;
	private Database database;
	private DAOFactory daoFactory;
	private CommandFactory commandFactory;
	private ServiceFactory serviceFactory;
	private CountDownLatch serviceGate = new CountDownLatch(1);
	private ExecutorService commandExecutor;
	private ExecutorService serviceExecutor;
	private ExecutorService adminExecutor;
	private Channel adminChannel;
	private LinkedList<AdminSession> _adminSessions = new LinkedList<AdminSession>();
	private Logbook logbook = new Logbook(this);
	
	private Hashtable<String,BotUser> botUsers = new Hashtable<String,BotUser>();
	// TODO set this up private Hashtable<String,Boolean> noSendNotice = new Hashtable<String,Boolean>();
	
	public Bot(String configFile) throws IOException, IrcException {
		this.configFile = configFile;
		this.loadEssentials();
    	pircBotX = new PircBotX( this.createConfiguration() );
    	pircBotX.startBot();
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("need a path to config arg.");
			System.exit(2);
		}
		
        try{
        	new Bot( args[0] );
        } catch (Exception e){
        	e.printStackTrace();
        }
        
	}
	
	private Configuration createConfiguration(){
        Builder cb = new Configuration.Builder();
        cb.addListener( new Listener(this) );
        // Set bot information
        cb.setMessageDelay( 500 );
        cb.setName( this.getProperty("bot_name") );
        cb.setAutoNickChange( this.getProperty("auto_nick_change").equals("true") );
        cb.setVersion( "GomuGomu" );
        cb.setFinger( "Watch where you are poking" );
        cb.setLogin( this.getProperty("login") );
        cb.setAutoReconnect( true );
        cb.setNickservPassword( this.getProperty("bot_pass") ); // TODO test if this works
        cb.setAutoReconnect(true);
        // Set server information
        cb.addServer(this.getProperty("server"), Integer.parseInt(this.getProperty("port")));
        cb.setSocketFactory( new UtilSSLSocketFactory().trustAllCertificates() );
        
        // Set channel information
		String[] channels = this.getProperty("channels").split("\\s");
		for (String channel : channels) {
			String[] namePass = channel.split(":");
			String name = "#" + namePass[0];
			if (namePass.length > 1) {
				String pass = namePass[1];
				cb.addAutoJoinChannel(name, pass);
			} else
				cb.addAutoJoinChannel(name);
		}
		
        return cb.buildConfiguration();
	}
	
	public void loadEssentials() {
		this.properties = new BotProperties( this, this.configFile );
		
		commandExecutor = Executors.newFixedThreadPool(Integer.parseInt(this.properties.getProperty("bot_conc_request")));
		serviceExecutor = Executors.newCachedThreadPool();
		adminExecutor = Executors.newFixedThreadPool(Integer.parseInt(this.properties.getProperty("bot_conc_admin")));
		
		database = DatabaseFactory.create(
				this,
				this.getProperty("db"), this.getProperty("db_user"), 
				this.getProperty("db_pass"), this.getProperty("db_server"), 
				this.getProperty("db_port"), this.getProperty("db_name")
				);
		
		daoFactory = new DAOFactory(this);
		serviceFactory = new ServiceFactory(this);
		commandFactory = new CommandFactory(this);
	}
	
	public PircBotX getBot(){
		return this.pircBotX;
	}
	
	public String getProperty(String key) {
		String property = this.properties.getProperty(key);
		if( property == null ) {
			this.getLogger().warn("No property found for " + key);
		}
		return property;
	}
	
	public void setProperty(String key, String value) {
		this.properties.setProperty(key, value);
	}
	
	public Database getDatabase(){
		return this.database;
	}
	
	public DAOFactory getDAOFactory(){
		return this.daoFactory;
	}
	
	public CommandFactory getCommandFactory() {
		return this.commandFactory;
	}

	public ServiceFactory getServiceFactory() {
		return this.serviceFactory;
	}
	
	public final CountDownLatch getServiceGate(){
		return this.serviceGate;
	}
	
	public ExecutorService getCommandExecutor() {
		return this.commandExecutor;
	}
	
	public ExecutorService getServiceExecutor() {
		return this.serviceExecutor;
	}
	
	public ExecutorService getAdminExecutor() {
		return this.adminExecutor;
	}
	
	/**
	 * This is the prefered way to output Messages <b>DO NOT USE</b> sendMessage
	 * makes the outputs fair won't hog
	 */
	public void sendMessageFair(String target, String message) {
		this.getBot().sendIRC().message(target, message);
		/*
		try {
			Thread.sleep(this.getBot().getConfiguration().getMessageDelay());
		} catch (InterruptedException e) {
			this.getLogger().error("sendMessageFair interrupted.",e);
			Thread.currentThread().interrupt();
		}
		*/
	}
	
	public void sendMessageFair(Object target, String message) {
		if( null == target ){
			this.getLogger().warn("No target passed in");
			return;
		}
		String targetString = "";
		if( target instanceof org.pircbotx.User ){
			targetString = ((org.pircbotx.User)target).getNick();
		} else if ( target instanceof org.pircbotx.Channel ){
			targetString = ((org.pircbotx.Channel)target).getName();
		}
		this.getBot().sendIRC().message(targetString, message);
		/*
		try {
			Thread.sleep(this.pircBotX.getConfiguration().getMessageDelay());
		} catch (InterruptedException e) {
			this.getLogger().error("sendMessageFair interrupted. ",e);
			Thread.currentThread().interrupt();
		}
		*/
	}
	
	public void sendMessageFairToAdmin(String message) {
		if( null == adminChannel ) {
			for( Channel channel : this.getBot().getUserBot().getChannels() ){
				if( channel.getName().replace("#", "").toLowerCase().equals( 
						this.getProperty("admin_channel").replace("#", "").toLowerCase() ) ){
					adminChannel = channel;
				}
			}
		}
		this.getBot().sendIRC().message(adminChannel.getName(), message);
		/*
		try {
			Thread.sleep(this.getBot().getConfiguration().getMessageDelay());
		} catch (InterruptedException e) {
			this.getLogger().error("sendMessageFair interrupted.", e);
			Thread.currentThread().interrupt();
		}
		*/
	}
	
	public void sendNoticeFair(String target, String notice) {
		this.getBot().sendIRC().notice(target, notice);
		/*
		try {
			Thread.sleep(this.pircBotX.getConfiguration().getMessageDelay());
		} catch (InterruptedException e) {
			this.getLogger().error("sendMessageFair interrupted.", e);
			Thread.currentThread().interrupt();
		}
		*/
	}
	
	public void sendNoticeFair(Object target, String notice) {
		if( null == target ){
			this.getLogger().warn("No target passed in");
			return;
		}
		String targetString = "";
		if( target instanceof org.pircbotx.User ){
			targetString = ((org.pircbotx.User)target).getNick();
		} else if ( target instanceof org.pircbotx.Channel ){
			targetString = ((org.pircbotx.Channel)target).getName();
		}
		this.getBot().sendIRC().notice(targetString, notice);
		/*
		try {
			Thread.sleep(this.pircBotX.getConfiguration().getMessageDelay());
		} catch (InterruptedException e) {
			this.getLogger().error("sendMessageFair interrupted.", e);
			Thread.currentThread().interrupt();
		}
		*/
	}
	
	public Channel stringToChannel(String channelString){
		if( !channelString.startsWith("#") ) {
			channelString = "#" + channelString;
		}
		for( Channel channel : this.getBot().getUserBot().getChannels() ){
			if( channel.getName().toLowerCase().equals( channelString.toLowerCase() ) ){
				return channel;
			}
		}
		return null;
	}
	
	public void setBotAdmins(LinkedList<BotUser> users){
		botUsers.clear();
		for( BotUser user : users ){
			botUsers.put(user.getUserName().toLowerCase(), user);
		}
	}
	
	public Hashtable<String,BotUser> getBotUsers(){
		return botUsers;
	}
	
	public void setAdminSession(AdminSession session){
		_adminSessions.add(session);
	}
	
	public void delAdminSession(AdminSession session){
		_adminSessions.remove(session);
	}
	
	public LinkedList<AdminSession> getAdminSession(){
		return _adminSessions;
	}
	
	public Logbook getLogger(){
		return this.logbook;
	}
}
