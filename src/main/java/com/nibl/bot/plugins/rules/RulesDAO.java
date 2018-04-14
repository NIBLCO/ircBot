package com.nibl.bot.plugins.rules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class RulesDAO extends DataAccessObject{
	
	public RulesDAO(Bot bot) {
		super(bot);
	}

	@Override
	public void createTables() {
/*
 CREATE TABLE `ooinuza`.`rules`(  
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `channel` VARCHAR(255) NOT NULL,
  `number` INT(11) NOT NULL,
  `rule` TEXT NOT NULL,
  PRIMARY KEY (`id`)
);
 */
	}

	@Override
	public void dropTables() {}
	
	public LinkedHashMap<Integer, String> getChannelRules(String channel){
		LinkedHashMap<Integer, String> rules = new LinkedHashMap<Integer, String>();
		try {
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery("SELECT number, rule FROM rules WHERE channel = '" + channel.toLowerCase() + "'");
			while(rs.next()){
				rules.put(rs.getInt("number"), rs.getString("rule"));
			}
		} catch (SQLException e) {
			_myBot.getLogger().error("Could not select from rules where channel = " + channel, e);
		}
		return rules;
	}
	
}
