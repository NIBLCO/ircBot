package com.nibl.bot.plugins.npp;

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
CREATE TABLE IF NOT EXISTS `nibl_npp` (
  `user_name` varchar(30) NOT NULL,
  `npts` int(11) DEFAULT 0 NOT NULL,
  `loli` int(11) DEFAULT 0 NOT NULL,
  `shta` int(11) DEFAULT 0 NOT NULL,
  `trap` int(11) DEFAULT 0 NOT NULL,
  `moeh` int(11) DEFAULT 0 NOT NULL,
  `cows` int(11) DEFAULT 0 NOT NULL,
  `peng` int(11) DEFAULT 0 NOT NULL,
  `n_flag` BOOL DEFAULT 0 NOT NULL,
  `l_flag` BOOL DEFAULT 0 NOT NULL,
  `s_flag` BOOL DEFAULT 0 NOT NULL,
  `t_flag` BOOL DEFAULT 0 NOT NULL,
  `m_flag` BOOL DEFAULT 0 NOT NULL,
  PRIMARY KEY  (`user_name`)
) ENGINE=InnoDB;
 * 
 */
		 
public class NppDAO extends DataAccessObject {
	
	public NppDAO(Bot myBot) {
		super(myBot);
	}

	@Override
	public void createTables() {
		if (!tableExists("nibl_npp")) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS `nibl_npp` (`user_name` varchar(30) NOT NULL, `npts` INT(11) DEFAULT 0 NOT NULL, `loli` int(11) DEFAULT 0 NOT NULL, `shta` int(11) DEFAULT 0 NOT NULL, `trap` int(11) DEFAULT 0 NOT NULL, `moeh` int(11) DEFAULT 0 NOT NULL, `cows` int(11) DEFAULT 0 NOT NULL, `peng` int(11) DEFAULT 0 NOT NULL, `n_flag` BOOL DEFAULT 0 NOT NULL, `l_flag` BOOL DEFAULT 0 NOT NULL, `s_flag` BOOL DEFAULT 0 NOT NULL, `t_flag` BOOL DEFAULT 0 NOT NULL, `m_flag` BOOL DEFAULT 0 NOT NULL, PRIMARY KEY  (`user_name`)) ENGINE=InnoDB;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Tables Created for Nibl++");
		}
		
	}

	@Override
	public void dropTables() {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("DROP TABLE IF EXISTS nibl_npp;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will attempt to give a user money if a 
	 * 24 hour period has passed
	 * @param username, channel, type
	 * type will be either npts, loli, shta, trap, moeh, all
	 * @return $$ munny
	 */
	public void add(User user, Channel channel, String type){
			try {
				Statement statement = getConnection().createStatement();
				if(!type.equalsIgnoreCase("all")){ //input checking is handled elsewhere
					ResultSet rs = statement.executeQuery("SELECT "+ type +","+type.substring(0,1)+ "_flag from nibl_npp where user_name='" + user.getNick() + "';");
					int amount = 0;
					if(rs.next()){
						amount = rs.getInt(1);
						if(rs.getInt(2)==1){	//check today flag				
							StringBuilder sb = new StringBuilder();
							sb.append(Colors.OLIVE + user.getNick() + Colors.NORMAL + ", you have already recieved your ");
							sb.append(Colors.TEAL + type + Colors.NORMAL + " today");
							
							_myBot.sendMessageFair(channel, sb.toString());
							return;
						}
					}
					
					Random rand = new Random();
					int newAmount = 0;
					if(type.equals("npts")){
						newAmount = rand.nextInt(100);
					}else if(type.equals("loli") || type.equals("shta")){
						newAmount = rand.nextInt(25);
					}else{
						newAmount = rand.nextInt(10);
					}
					
					StringBuilder sb = new StringBuilder();
					sb.append("INSERT INTO nibl_npp (user_name, "+type+", "+type.substring(0,1)+"_flag) values (");
					sb.append("'" + user.getNick() + "'," + (amount+newAmount) + ",1) ON DUPLICATE KEY ");
					sb.append("UPDATE "+type+"=" + (amount+newAmount) + ", "+type.substring(0,1)+"_flag=1;");
					statement.execute(sb.toString());
					
					sb = new StringBuilder();
					sb.append(Colors.OLIVE + user.getNick() + Colors.NORMAL + " obtained " + Colors.TEAL + newAmount + " ");
					sb.append(Colors.NORMAL + type + " today. " + Colors.OLIVE + user.getNick() + Colors.NORMAL);
					sb.append(" has a total of " + Colors.TEAL + (amount+newAmount) + Colors.NORMAL);
					sb.append(" " + type + " .");
					_myBot.sendMessageFair(channel, sb.toString());
				}else{ //all
					//check if any flags are set, then give them everything else
					int[] flag = new int[5];
					int[] value = new int[7];
					boolean allSet = true;
					ResultSet rs = statement.executeQuery("SELECT n_flag, l_flag, s_flag, t_flag, m_flag, npts, loli, shta, trap, moeh, cows, peng from nibl_npp where user_name='" + user.getNick() + "';");
					if(rs.next()){
						for(int i=0;i<5;i++){
							flag[i] = rs.getInt(i+1);
							value[i] = rs.getInt(i+6);
							if(flag[i] == 0)
								allSet = false;
						}
						value[5] = rs.getInt(11);
						value[6] = rs.getInt(12);
						if(allSet){
							StringBuilder sb = new StringBuilder();
							sb.append(Colors.OLIVE + user.getNick() + Colors.NORMAL + ", you have already recieved your ");
							sb.append(Colors.TEAL + "npts, loli, shta, trap, and moeh" + Colors.NORMAL + " today");
							
							_myBot.sendMessageFair(channel, sb.toString());
							return;
						}
					}
	
					int[] newVal = new int[7];
					Random rand = new Random();
					for(int i=0;i<5;i++){
						if(flag[i] == 0){
							newVal[i] = rand.nextInt(mapping(i));
						}
					}
					
					// 1/100 chance to get cows
					if( 10 == rand.nextInt(100) ){
						newVal[5] = rand.nextInt(mapping(5))+1;
					} else {
						newVal[5] = 0;
					}
					
					// 1/1000 chance to get penguins
					if( 10 == rand.nextInt(1000) ){
						newVal[6] = rand.nextInt(mapping(6))+1;
					} else {
						newVal[6] = 0;
					}
					
					StringBuilder sb = new StringBuilder();
					sb.append("INSERT INTO nibl_npp (user_name,npts,loli,shta,trap,moeh,n_flag,l_flag,s_flag,t_flag,m_flag) values (");
					sb.append("'" + user.getNick() + "',");
					for(int i=0;i<5;i++){
						sb.append(value[i]+newVal[i] + ",");
					}
					sb.append("1,1,1,1,1) ON DUPLICATE KEY ");
					sb.append("UPDATE npts=" + (value[0]+newVal[0]) + ", n_flag=1,");
					sb.append("loli=" + (value[1]+newVal[1]) + ", l_flag=1,");
					sb.append("shta=" + (value[2]+newVal[2]) + ", s_flag=1,");
					sb.append("trap=" + (value[3]+newVal[3]) + ", t_flag=1,");
					sb.append("moeh=" + (value[4]+newVal[4]) + ", m_flag=1");
					if( newVal[5] != 0 ){
						sb.append(",cows=" + (value[5]+newVal[5]));
					}
					if( newVal[6] != 0 ){
						if( newVal[5] != 0 ){
							 sb.append(",");
						}
						sb.append("peng=" + (value[6]+newVal[6]));
					}
					statement.execute(sb.toString());
					
					
					sb = new StringBuilder();
					sb.append(Colors.OLIVE + user.getNick() + Colors.NORMAL + " obtained " + Colors.TEAL + newVal[0] + Colors.NORMAL + " nibl points, ");
					sb.append(Colors.TEAL + newVal[1] + Colors.NORMAL + " loli, " + Colors.TEAL + newVal[2] + Colors.NORMAL + " shota, ");
					sb.append(Colors.TEAL + newVal[3] + Colors.NORMAL + " traps, and " + Colors.TEAL + newVal[4] + Colors.NORMAL + " moehawks");
					
					if( newVal[5] != 0 ){
						sb.append(", " + Colors.BOLD + Colors.RED + newVal[5] + Colors.NORMAL + " cows");
					}
					
					if( newVal[6] != 0 ){
						if( newVal[5] != 0 ){
							sb.append(", ");
						}
						sb.append(Colors.BOLD + Colors.RED + newVal[6] + Colors.NORMAL + " penguins");
					}
					
					sb.append(" today. ");
					sb.append(Colors.OLIVE + user.getNick() + Colors.NORMAL + " has a total of " + Colors.TEAL + (value[0] + newVal[0]) + Colors.NORMAL + " nibl points, ");
					sb.append(Colors.TEAL + (value[1] + newVal[1]) + Colors.NORMAL + " loli, " + Colors.TEAL + (value[2] + newVal[2]) + Colors.NORMAL + " shota, ");
					sb.append(Colors.TEAL + (value[3] + newVal[3]) + Colors.NORMAL + " traps, " + Colors.TEAL + (value[4] + newVal[4]) + Colors.NORMAL + " moehawks, ");
					sb.append(Colors.TEAL + (value[5] + newVal[5]) + Colors.NORMAL + " cows, and " + Colors.TEAL + (value[6] + newVal[6]) + Colors.NORMAL + " penguins.");
					_myBot.sendMessageFair(channel, sb.toString());
				}
			} catch (SQLException e) {
				_myBot.getLogger().error("SQL error in nibl++/child trigger",e);
			}
	}
	
	private int mapping(int i){
		switch(i){
		case 0:
			return 100;
		case 1:
		case 2:
			return 25;
		case 3:
		case 4:
			return 10;
		case 5:
			return 5;
		case 6:
			return 2;
		default:
			return 10;
		}
	}
	
	public void resetFlags(){
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("UPDATE nibl_npp SET n_flag=0, l_flag=0, s_flag=0, m_flag=0, t_flag=0");
		} catch (SQLException e) {
			_myBot.getLogger().error("SQL error resetting nibl++ flags",e);
		}
	}
	
	public String checkType(String username, String type){
		Statement statement;
		int amount;
		String output = "";
		try {
			statement = getConnection().createStatement();
			if(!type.equals("all")){
				ResultSet rs = statement.executeQuery("SELECT "+type+" from nibl_npp where user_name='" + username + "';");
				rs.next();
				amount = rs.getInt(1);
				output = Colors.OLIVE + username + Colors.NORMAL + " has " + Colors.TEAL + amount + Colors.NORMAL + " " + type + ".";
				rs.close();
			}else{
				ResultSet rs = statement.executeQuery("SELECT npts,loli,shta,trap,moeh,cows,peng from nibl_npp where user_name='" + username + "';");
				rs.next();
				StringBuilder sb = new StringBuilder();
				sb.append(Colors.OLIVE + username + Colors.NORMAL + " has " + Colors.TEAL + rs.getInt(1) + Colors.NORMAL + " nibl points, ");
				sb.append(Colors.TEAL + rs.getInt(2) + Colors.NORMAL + " loli, " + Colors.TEAL + rs.getInt(3) + Colors.NORMAL + " shota, ");
				sb.append(Colors.TEAL + rs.getInt(4) + Colors.NORMAL + " traps, " + Colors.TEAL + rs.getInt(5) + Colors.NORMAL + " moehawks, ");
				sb.append(Colors.TEAL + rs.getInt(6) + Colors.NORMAL + " cows, and " + Colors.TEAL + rs.getInt(7) + Colors.NORMAL + " penguins.");
				output = sb.toString();
			}
			statement.close();
		} catch (SQLException e) {
			output = username + " not found in teh datanets";
		}
		return output;
	}
	
	public String topTen(String type){
		Statement statement;
		StringBuilder sb = new StringBuilder();
		String output = "";
		try {
			statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery("SELECT user_name,"+type+" from nibl_npp ORDER BY "+type+" DESC LIMIT 10;");
			while(rs.next()){
				sb.append(Colors.OLIVE + rs.getString(1) + Colors.NORMAL + " has " + Colors.TEAL + rs.getInt(2) + Colors.NORMAL + " " + type +", ");
			}
			output = sb.toString().substring(0, sb.toString().length()-2)+ ".";
			
		} catch (SQLException e) {
			output = "Error in top10, Jenga";
			_myBot.getLogger().error("Error in top10",e);
		}

		return output;
		
	}
	
	public String give(String info, String type){
		Statement statement;
		String username = info.split(" ")[0];
		int amount = Integer.parseInt(info.split(" ")[1]);
		String output = "";
		try {
			statement = getConnection().createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("update nibl_npp set "+type+"=");
			sb.append(amount);
			sb.append(" + (select "+type+" from (select * from nibl_npp) as x where user_name='");
			sb.append(username);
			sb.append("') where user_name='" + username + "';");
			statement.executeUpdate(sb.toString());
			//System.out.println(sb.toString());
			output = "Gave " + username + " " + amount + " " + type;
			
		} catch (SQLException e) {
			output = "invalid syntax";
		}

		return output;
	}
	
	public String take(String info, String type){
		Statement statement;
		String username = info.split(" ")[0];
		int amount = Integer.parseInt(info.split(" ")[1]);
		String output = "";
		try {
			statement = getConnection().createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("update nibl_npp set "+type+"=");
			sb.append("(select "+type+" from (select * from nibl_npp) as x where user_name='");
			sb.append(username);
			sb.append("') - ");
			sb.append(amount + " where user_name='" + username + "';");
			statement.executeUpdate(sb.toString());
			output = "Took " + amount + " " + type + " from " + username;
			
		} catch (SQLException e) {
			output = "invalid syntax";
		}

		return output;
	}

}
