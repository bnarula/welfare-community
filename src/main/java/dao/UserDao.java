package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import config.DBConnection;
import security.SecurityUtil;
public class UserDao {
	public static String validateByUid(Connection con, Integer uid, String pass) throws SQLException{
		return validate(con, "usr_uid", ""+uid, pass);
	}
	public static String validateByEmail(Connection con, String email, String pass) throws SQLException{
		return validate(con, "usr_email", email, pass);
	}
	public static String validate(Connection con, String validateBy, String email, String password) throws SQLException
	{
		String result="invalid";
		PreparedStatement stmt=con.prepareStatement("select usr_email, usr_password, usr_uid, usr_verified from users_table where "+validateBy+"=?");
		if(validateBy.equals("usr_email")){
			stmt.setString(1, email);
		} else {
			stmt.setInt(1, Integer.parseInt(email));
		}
		password = SecurityUtil.encrypt(password);
		ResultSet rs=stmt.executeQuery();
		boolean emailExists = false;
		while(rs.next())
		{
			emailExists = true;
			if(rs.getString("usr_password").equals(password))
			{
				if(rs.getInt("usr_verified")==1)
					result = rs.getString("usr_uid");
				else
					result = "userNotVerified";
				break;
			}
		}
		if(!emailExists)
			result = "emailDoesNotExist";
		return result;
		
	}

	public static String getUserPassword(Connection con, String ngoEmail) throws SQLException {
		String result = "";
		Statement stmt=con.createStatement();
		ResultSet rs=stmt.executeQuery("select usr_email, usr_password from users_table where usr_email='"+ngoEmail+"'");
		if(rs.next())
			result= rs.getString("usr_password");
		else
			result= "";
		rs.close();
		stmt.close();
		return result;
	}
	public static String getUserEmail(Connection con, Integer ngoUid) throws SQLException {
		String result = "";
		Statement stmt=con.createStatement();
		ResultSet rs=stmt.executeQuery("select usr_email, usr_uid from users_table where usr_uid="+ngoUid+"");
		if(rs.next())
			result= rs.getString("usr_email");
		else
			result= "";
		rs.close();
		stmt.close();
		return result;
	}
	public static void createUser(Connection con, Integer uid, String email, String password, String passcode) throws SQLException
	{
		password = SecurityUtil.encrypt(password);
		PreparedStatement stmt = con.prepareStatement("insert into users_table(usr_uid, usr_email, usr_password, usr_type) values(?,?,?,?)");
		stmt.setInt(1, uid);
		stmt.setString(2,  email);
		stmt.setString(3, password);
		stmt.setString(4, "n");
		stmt.execute();
		stmt.close();
		VerificationDao.generateNewPasscode(con, uid, passcode, "v");
			
	}
	public static void updateUser(Connection con, Integer uid, String password) throws SQLException
	{
		if(con==null)
		con = DBConnection.getConnection();
		Statement stmt = con.createStatement();
		password = SecurityUtil.encrypt(password);
		stmt.execute("update users_table set usr_password='"
				+ password + "' where usr_uid = "
				+ uid );
		stmt.close();
	}
	public static void setUserAsVerified(Connection con, Integer userCode) throws SQLException{
		PreparedStatement stmt = con.prepareStatement("update users_table set usr_verified = 1 where usr_uid=?");
		stmt.setInt(1, userCode);
		stmt.executeUpdate();
		stmt.close();
	}
	public static boolean getUserVerificationStatus(Connection con, Integer userCode) throws SQLException{
		PreparedStatement stmt = con.prepareStatement("select usr_uid, usr_verified from users_table where usr_uid=?");
		boolean isVerified = false;
		stmt.setInt(1, userCode);
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			isVerified = rs.getInt("usr_verified")==1;
		rs.close();
		stmt.close();
		return isVerified;
	}
	public static Integer getUserId(Connection con, String email) throws SQLException{
		PreparedStatement stmt = con.prepareStatement("select usr_uid, usr_email from users_table where usr_email=?");
		Integer result = 0;
		stmt.setString(1, email);
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			result = rs.getInt("usr_uid");
		rs.close();
		stmt.close();
		return result;
	}

}
