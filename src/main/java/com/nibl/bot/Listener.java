package com.nibl.bot;

import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import com.nibl.bot.command.Command;
import com.nibl.bot.plugins.admin.AdminSession;
import com.nibl.bot.plugins.admin.BotUser;
import com.nibl.bot.service.Service;

public class Listener extends ListenerAdapter {
	
	private Bot _myBot;
	
	public Listener(Bot myBot){
		_myBot = myBot;
	}
	
	@Override
	public void onConnect(ConnectEvent event){
		// setBotAdmins
		_myBot.getServiceGate().countDown(); // start services
	}
	
	@Override
	public void onMessage(MessageEvent event){
		
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsMessages())
				service.onMessage(event);
		}
		
		String message = event.getMessage();
		
		if (message.charAt(0) == '!' || message.charAt(0) == '@') {
			Command command = null;
			try {
				command = _myBot.getCommandFactory().create(event.getChannel(), event.getUser(), event.getUser().getLogin(), event.getUser().getHostmask(), message);
			} catch (Exception e) {
				_myBot.getLogger().error(event.getMessage(),e);
			}
			if (command != null)
				_myBot.getCommandExecutor().submit(command);
		}
		
	}
	
	@Override
	public void onNotice(NoticeEvent event){
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsNotices()) {
				service.onNotice(event);
			}
		}
	}
	
	@Override
	public void onPrivateMessage(PrivateMessageEvent event){
		String message = event.getMessage();
		
		if (message.charAt(0) == '!' || message.charAt(0) == '@') {
			Command command = null;
			try {
				command = _myBot.getCommandFactory().create(null, event.getUser(), event.getUser().getLogin(), event.getUser().getHostmask(), message);
			} catch (Exception e) {
				_myBot.getLogger().error(event.getMessage(),e);
			}
			if (command != null){
				if( command.acceptPrivateMessage() ){
					command.setOriginatePrivateMessage();
					_myBot.getCommandExecutor().submit(command);
				} else {
					_myBot.sendMessageFair(event.getUser().getNick(), "Cannot run this command via private message");
				}
				return;
			}
		}
		
		BotUser botUser = _myBot.getBotUsers().get(event.getUser().getNick().toLowerCase());
		if ( botUser != null && botUser.getAccessLevel() > 0 ) {
			if( event.getUser().isVerified() ){
				try {
					AdminSession adminSession = new AdminSession(_myBot, botUser, event.getUser(), message.replace("admin ", ""));
					_myBot.getAdminExecutor().execute(adminSession);
				} catch (Exception e) {
					_myBot.sendMessageFair(event.getUser().getNick(), "Problem using DccChat session: " + e.getMessage());
				}
			} else {
				_myBot.sendMessageFair(event.getUser().getNick(), "You don't seem to be registered with nickserv");
			}
		} else{ // some other user messaged bot
			_myBot.getLogger().info(Colors.BROWN + "PRIVMSG: " + Colors.NORMAL + event.getUser().getNick() + " " + event.getUser().getLogin() + "@" + event.getUser().getHostmask() + " -- " + event.getMessage() );
		}
		
	}
	
	@Override
	public void onIncomingChatRequest(IncomingChatRequestEvent event){
		BotUser botUser = _myBot.getBotUsers().get(event.getUser().getNick().toLowerCase());
		if ( botUser != null && botUser.getAccessLevel() > 0 ) {
			if( event.getUser().isVerified() ){
				try {
					AdminSession adminSession = new AdminSession(_myBot, botUser, event.getUser(), null);
					_myBot.getAdminExecutor().execute(adminSession);
				} catch (Exception e) {
					_myBot.sendMessageFair(event.getUser().getNick(), "Problem using DccChat session: " + e.getMessage());
				}
			} else {
				_myBot.sendMessageFair(event.getUser().getNick(), "You don't seem to be registered with nickserv");
			}
		}
	}
	
	/**
	 * If a service accepts the transfer, that transfer will be used.
	 * Subsequent services wont' be able to get the file.. what do? 
	 */
	public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsIncomingFileTransfers()) {
				service.onIncomingFileTransfer(event);
			}
		}
	}
	

	public void onTopic(TopicEvent event) throws Exception {
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsOnTopic()) {
				service.onTopic(event);
			}
		}
	}

	public void onPart(PartEvent event){
		String sender = event.getUser().getNick();
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsOnPart()) {
				service.onPart(sender);
			}
		}
	}
	
	public void onJoin(JoinEvent event){
			String sender = event.getUser().getNick();
			for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
				if (service.isRunning() && service.needsOnJoin()) {
					service.onJoin(sender);
				}
			}
	}

	public void onMode(ModeEvent event) {
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsOnMode()) {
				service.onMode(event);
			}
		}
	}
	
	public void onHalfOp(HalfOpEvent event){
		for (Service service : _myBot.getServiceFactory().getRegisteredService().values()) {
			if (service.isRunning() && service.needsOnHop()) {
				service.onHalfOp(event);
			}
		}
	}
	
	public void onAction(ActionEvent event){}
	public void onBanList(BanListEvent event){}
	public void onChannelInfo(ChannelInfoEvent event){}
	public void onConnectAttemptFailed(ConnectAttemptFailedEvent event){}
	public void onDisconnect(ActionEvent event){}
	public void onAction(DisconnectEvent event){}
	public void onFinger(FingerEvent event){}
	public void onInvite(InviteEvent event){}
	public void onKick(KickEvent event){}
	public void onMotd(MotdEvent event){}
	public void onNickAlreadyInUse(NickAlreadyInUseEvent event){}
	public void onNickChange(NickChangeEvent event){}
	public void onOp(OpEvent event){}
	public void onOutput(OutputEvent event){}
	public void onOwner(OwnerEvent event){}
	public void onPing(PingEvent event){}
	public void onQuit(QuitEvent event){}
	public void onRemoveChannelBan(RemoveChannelBanEvent event){}
	public void onRemoveChannelKey(RemoveChannelKeyEvent event){}
	public void onRemoveChannelLimit(RemoveChannelLimitEvent event){}
	public void onRemoveInviteOnly(RemoveInviteOnlyEvent event){}
	public void onRemoveModerated(RemoveModeratedEvent event){}
	public void onRemoveNoExternalMessages(RemoveNoExternalMessagesEvent event){}
	public void onRemovePrivate(RemovePrivateEvent event){}
	public void onRemoveSecret(RemoveSecretEvent event){}
	public void onRemoveTopicProtection(RemoveTopicProtectionEvent event){}
	public void onServerPing(ServerPingEvent event){}
	public void onServerResponse(ServerResponseEvent event){}
	public void onSetChannelBan(SetChannelBanEvent event){}
	public void onSetChannelKey(SetChannelKeyEvent event){}
	public void onSetChannelLimit(SetChannelLimitEvent event){}
	public void onSetInviteOnly(SetInviteOnlyEvent event){}
	public void onSetModerated(SetModeratedEvent event){}
	public void onSetNoExternalMessages(SetNoExternalMessagesEvent event){}
	public void onSetPrivate(SetPrivateEvent event){}
	public void onSetSecret(SetSecretEvent event){}
	public void onSetTopicProtection(SetTopicProtectionEvent event){}
	public void onSocketConnect(SocketConnectEvent event){}
	public void onSuperOp(SuperOpEvent event){}
	public void onTime(TimeEvent event){}
	public void onUnknown(UnknownEvent event){}
	public void onUserList(UserListEvent event){}
	public void onUserMode(UserModeEvent event){}
	public void onVersion(VersionEvent event){}
	public void onVoice(VoiceEvent event){}
	public void onWhois(WhoisEvent event){}
	
}
 