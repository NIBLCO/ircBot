package com.nibl.bot.plugins.money;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

/**
 * 
CREATE TABLE IF NOT EXISTS `nibl_money` (
  `user_name` varchar(30) NOT NULL,
  `amount` int(11) NOT NULL,
  `today_flag` BOOL NOT NULL,
  PRIMARY KEY  (`user_name`)
) ENGINE=InnoDB;
 * 
 */
public class MoneyDAO extends DataAccessObject{
	
	public MoneyDAO(Bot myBot) {
		super(myBot);
	}

	@Override
	public void createTables() {
		if (!tableExists("nibl_money")) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS `nibl_money` (`user_name` varchar(30) NOT NULL,`amount` int(11) NOT NULL, `today_flag` BOOL NOT NULL, PRIMARY KEY  (`user_name`)) ENGINE=InnoDB;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Tables Created for Money");
		}
		
	}

	@Override
	public void dropTables() {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("DROP TABLE IF EXISTS nibl_money;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will attempt to give a user money if a 
	 * 24 hour period has passed
	 * @param username, channel
	 * @return $$ munny
	 */
	public void add(User user, Channel channel){
			try {
				Statement statement = getConnection().createStatement();
				
				ResultSet rs = statement.executeQuery("SELECT amount,today_flag from nibl_money where user_name='" + user.getNick().toLowerCase() + "';");
				int amount = 0;
				if(rs.next()){
					amount = rs.getInt(1);
					if(rs.getInt(2)==1){	//check today flag				
						StringBuilder sb = new StringBuilder();
						sb.append(user.getNick() + ", you have already recieved your money today");
						
						_myBot.sendMessageFair(channel.getName(), sb.toString());
						return;
					}
				}
				
				Random rand = new Random();
				int newAmount = rand.nextInt(100);
				
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO nibl_money(user_name,amount,today_flag) values (");
				sb.append("'" + user.getNick() + "'," + (amount+newAmount) + ",1) ON DUPLICATE KEY ");
				sb.append("UPDATE amount=" + (amount+newAmount) + ", today_flag=1;");
				statement.execute(sb.toString());
				_myBot.sendMessageFair(channel.getName(), "You get $" + newAmount + " , your total is $" + (amount+newAmount));
			} catch (SQLException e) {
				_myBot.getLogger().error("SQL error in !money add", e);
			}
	}
	
	public void resetFlags(){
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("UPDATE nibl_money SET today_flag=0");
		} catch (SQLException e) {
			_myBot.getLogger().error("SQL error resetting money flags");
		}
	}
	
	public String checkMoney(String username){
		Statement statement;
		int amount;
		String output = "";
		try {
			statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery("SELECT amount from nibl_money where user_name='" + username + "';");
			rs.next();
			amount = rs.getInt(1);
			output = username + ", You have $" + amount;
			rs.close();
			statement.close();
		} catch (SQLException e) {
			output = username + " not found in teh datanets";
		}
		return output;
	}
	
	public String topTen(){
		Statement statement;
		StringBuilder sb = new StringBuilder();
		String output = "";
		int counter = 0;
		try {
			statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery("SELECT user_name,amount from nibl_money ORDER BY amount DESC LIMIT 10;");
			while(rs.next()){
				counter++;
				if(counter<4){
					sb.append(Colors.DARK_GREEN + rs.getString(1));
				}else if(counter<8){
					sb.append(Colors.TEAL + rs.getString(1));
				}else{
					sb.append(Colors.RED + rs.getString(1));
				}
				sb.append(Colors.NORMAL + " has $" + rs.getInt(2) + ", ");
			}
			output = sb.toString().substring(0, sb.toString().length()-2)+ ".";
			
		} catch (SQLException e) {
			output = "Error in top10";
			_myBot.getLogger().error("Error in top10", e);
		}

		return output;
		
	}
	
	public String give(String info){
		Statement statement;
		String username = info.split(" ")[0];
		int amount = Integer.parseInt(info.split(" ")[1]);
		String output = "";
		try {
			statement = getConnection().createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("update nibl_money set amount=");
			sb.append(amount);
			sb.append(" + (select amount from (select * from nibl_money) as x where user_name='");
			sb.append(username);
			sb.append("') where user_name='" + username + "';");
			statement.executeUpdate(sb.toString());
			//System.out.println(sb.toString());
			output = "Gave " + username + " $" + amount;
			
		} catch (SQLException e) {
			output = "invalid syntax";
		}

		return output;
	}
	
	public String take(String info){
		Statement statement;
		String username = info.split(" ")[0];
		int amount = Integer.parseInt(info.split(" ")[1]);
		String output = "";
		try {
			statement = getConnection().createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("update nibl_money set amount=");
			sb.append("(select amount from (select * from nibl_money) as x where user_name='");
			sb.append(username);
			sb.append("') - ");
			sb.append(amount + " where user_name='" + username + "';");
			statement.executeUpdate(sb.toString());
			output = "Took $" + amount + " from " + username;
			
		} catch (SQLException e) {
			output = "invalid syntax";
		}

		return output;
	}

}
