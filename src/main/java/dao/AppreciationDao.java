package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AppreciationDao {

	public static void appreciate(Connection con, String aby, Integer ato, String abyName) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("insert into appreciation_table"
				+ "(app_by_uid, app_to_ngo_uid_fk, app_by_name) values(?, ?, ?)");
		stmt.setString(1, aby);
		stmt.setInt(2, ato);
		stmt.setString(3, abyName);
		stmt.execute();
		stmt.close();
		stmt = con.prepareStatement("update ngos_table set ngo_no_of_appreciations = ngo_no_of_appreciations+1 where ngo_uid=?");
		stmt.setInt(1, ato);
		stmt.executeUpdate();
		stmt.close();
	}

	public static int getNoOfAppreciations(Connection con, Integer ato) throws SQLException {
		int count = 0;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt
				.executeQuery("select ngo_no_of_appreciations, ngo_uid from ngos_table where ngo_uid = " + ato + "");
		if (rs.next())
			count = rs.getInt("ngo_no_of_appreciations");
		rs.close();
		stmt.close();
		return count;
	}
}
