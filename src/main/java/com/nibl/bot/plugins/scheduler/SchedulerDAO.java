package com.nibl.bot.plugins.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nibl.bot.Bot;
import com.nibl.bot.command.Command;
import com.nibl.bot.dao.DataAccessObject;

public class SchedulerDAO extends DataAccessObject {

    private final String _SCHEDULER_TABLE = "schedules";

    public SchedulerDAO(Bot bot) {
        super(bot);
    }

    @Override
    public void createTables() {
        if (tablesDoesNotExist()) {
            try {
                Statement statement = getConnection().createStatement();
                statement.execute(
                        "CREATE TABLE `schedules` (`commandName` varchar(50) NOT NULL,`lastRun` timestamp DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`commandName`)) ENGINE=MyISAM DEFAULT CHARSET=latin1;");
                _myBot.getLogger().info("created schedules tables");
                statement.close();
            } catch (SQLException e) {
                _myBot.getLogger().error("createTables", e);
            }
        }
    }

    private boolean tablesDoesNotExist() {
        return !tableExists(_SCHEDULER_TABLE);
    }

    @Override
    public void dropTables() {
        try {
            Statement statement = getConnection().createStatement();
            statement.execute("DROP TABLE IF EXISTS `schedules`;");
            _myBot.getLogger().info("dropped schedules table");
            statement.close();
        } catch (SQLException e) {
            _myBot.getLogger().error("dropTables", e);
        }
    }

    public Date getTime(Command command, int scheduleNumber) {
        try {
            Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `schedules` WHERE `commandName` = '"
                    + command.getName() + "_" + scheduleNumber + "';");
            if (rs.next()) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    String butts = rs.getString(2);
                    Date corn = df.parse(butts);
                    return corn;
                } catch (Exception e) {
                    _myBot.getLogger().error("getTime", e);
                }
            }
            statement.close();
        } catch (SQLException e) {
            _myBot.getLogger().error("getTime", e);
        }
        return null;
    }

    public void setTime(Command command, int scheduleNumber) {
        try {
            Statement statement = getConnection().createStatement();
            String commandName = command.getName() + "_" + scheduleNumber;
            statement.execute("INSERT INTO `schedules` (commandName) VALUES ('" + commandName
                    + "') ON DUPLICATE KEY UPDATE `lastRun` = CURRENT_TIMESTAMP;");
            statement.close();
        } catch (SQLException e) {
            _myBot.getLogger().error("setTime", e);
        }
    }

}
