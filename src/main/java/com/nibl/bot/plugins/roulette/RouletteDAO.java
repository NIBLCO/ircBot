package com.nibl.bot.plugins.roulette;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.pircbotx.User;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class RouletteDAO extends DataAccessObject{
	
	public RouletteDAO(Bot bot) {
		super(bot);
	}

	@Override
	public void createTables() {}

	@Override
	public void dropTables() {}
	
	public Integer getAccessLevel(User user){
		
		try {
			if(user.isVerified()){
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

}
