package loader.monitor.util;

import org.apache.log4j.Logger;
import java.sql.*;

public class SQLHelper
{
    private Connection con = null;
    private Statement stm = null;
    private static Logger logger;

    static {
        logger = Logger.getLogger(SQLHelper.class);
    }
    /**
     *
     * @param hostName
     * @param user
     * @param password
     * @param defaultDB
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SQLHelper(String hostName, int port, String user, String password, String defaultDB) throws ClassNotFoundException, SQLException {
        String conString = "";
        Class.forName("com.mysql.jdbc.Driver");
        if(password==null)
            password="";
        conString = "jdbc:mysql://"+hostName+":"+ port;

        if(defaultDB != null && !defaultDB.trim().equals(""))
            conString += "/"+defaultDB;
        conString += "?user="+user+"&password="+password+"&useUnicode=true&characterEncoding=UTF8";
        logger.debug("Connection String:'"+conString+"'");
        try{
            this.con = DriverManager.getConnection(conString);
        }
        catch (Exception e) {
            logger.info("Eating SQL Exception. Test");
            this.con = DriverManager.getConnection(conString);
        }
        this.stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     *
     * @param query Execute Query and return the result set.
     * @return Returns ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery(String query) throws SQLException {
        logger.debug("Query :'"+query+"'");
        ResultSet rs = stm.executeQuery(query);
        return rs;
    }

    /**
     * Closes SQL Connection
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        stm.close();
        con.close();
   }

}