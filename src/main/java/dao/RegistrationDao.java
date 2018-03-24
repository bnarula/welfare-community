package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import config.DBConnection;

public class RegistrationDao {

	public static boolean checkDuplicateAlias(Connection con, String alias) throws SQLException {
		boolean result= false;
		PreparedStatement stmt = con.prepareStatement("select ngo_alias from ngo_alias_table where ngo_alias=?");
		stmt.setString(1, alias);
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			result= true;
		else
			result= false;
		rs.close();
		stmt.close();
		return result;
	}

	public static boolean checkDuplicateEmail(Connection con, String ngoEmail) throws SQLException {
		boolean result= false;
		Statement stmt=con.createStatement();
		ResultSet rs = stmt.executeQuery("select ngo_email from ngos_table where ngo_email='"+ngoEmail+"'");
		if(rs.next())
			result= true;
		else
			result= false;
		rs.close();
		stmt.close();
		return result;
	}
	public static boolean validatePasscode(Connection con, Integer ngoUid, String passcode) throws SQLException {
		boolean result = false;
		PreparedStatement stmt = con.prepareStatement("select pt_user_code_fk, pt_user_passcode, pt_type from passcode_table where "
				+ "pt_user_code_fk = ? and pt_user_passcode = ?");
		stmt.setInt(1, ngoUid);
		stmt.setString(2, passcode);
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			result= true;
		else
			result= false;
		rs.close();
		stmt.close();
		return result;
	}
	   

}
