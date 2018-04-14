package com.nibl.bot.plugins.checknameregistered;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.pircbotx.User;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class CheckNameRegisteredDAO extends DataAccessObject{
	
	public CheckNameRegisteredDAO(Bot myBot) {
		super(myBot);
	}

	public Integer getUserAccessLevel(User user){
		
		try {
				if( user.isVerified() ){
				//if(user.isIdentified()){
					Statement statement = getConnection().createStatement();
					ResultSet rs = statement.executeQuery("select access from bot_admins where user_name = '" + user.getNick().toLowerCase() + "';");
					if(rs.next()){
						return rs.getInt(1);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		return -1;
	}

	@Override
	public void createTables() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropTables() {
		// TODO Auto-generated method stub
		
	}
	
}
