package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import beans.AboutUsBean;

public class AboutUsDao {

	public static List<AboutUsBean> getAboutUsList(Connection con, Integer uid, int start, int count) throws SQLException {
		PreparedStatement stmt = null;
		List<AboutUsBean> aboutUsList = new ArrayList<AboutUsBean>();
		stmt = con.prepareStatement("select au_code, au_ngo_uid_fk, au_heading, au_content, au_created_on, au_is_pinned, "
				+ "p.url, p.owner_id"
				+ " from about_us_table "
				+ "left join photo_table p on au_code = p.owner_id and category = 'AU' "
				+ "where au_ngo_uid_fk=? order by au_code limit "+start+","+count);
		stmt.setInt(1, uid);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String content = rs.getString("au_content");
			Calendar createdOn = new GregorianCalendar();
			createdOn.setTimeInMillis(rs.getDate("au_created_on").getTime());
			aboutUsList.add(new AboutUsBean(rs.getInt("au_code"), rs.getString("au_ngo_uid_fk"),
					rs.getString("au_heading"),content, 
					rs.getString("url"), 
					createdOn, rs.getBoolean("au_is_pinned")));
		}
		//aboutUsList.sort(AboutUsBean.DEFAULT_SORT);
		stmt.close();
		return aboutUsList;
	}

	public static boolean updateList(Connection con, List<AboutUsBean> aboutUsList) throws SQLException {
		PreparedStatement stmt = null;
		stmt = con.prepareStatement("update about_us_table set au_heading = ?, au_content=? where au_code=?");
		Iterator<AboutUsBean> iter = aboutUsList.iterator();
		while(iter.hasNext())
		{
			AboutUsBean aboutUsBean = (AboutUsBean)iter.next();
			stmt.setString(1, aboutUsBean.getHeading());
			stmt.setString(2, aboutUsBean.getContent());
			stmt.setInt(3, aboutUsBean.getCode());
			stmt.executeUpdate();
		}
		stmt.close();
		return true;
	}

	public static int addNewAboutUs(Connection con, String newAboutUsHeading, String newAboutUsContent, String ngoEmail, Calendar createdOn) throws SQLException {
		PreparedStatement stmt = null;
		int result = -1;
		java.sql.Date sqlDate = new java.sql.Date(createdOn.getTime().getTime());
		stmt = con.prepareStatement("insert into about_us_table(au_heading, au_content, au_ngo_uid_fk, au_created_on) "
				+ "values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS  );
		stmt.setString(1, newAboutUsHeading);
		stmt.setString(2, newAboutUsContent);
		stmt.setString(3, ngoEmail);
		stmt.setDate(4, sqlDate);
		stmt.execute();
		ResultSet rsGetAutoId = stmt.getGeneratedKeys();
		if (rsGetAutoId.next())
			result = rsGetAutoId.getInt(1);
		stmt.close();
		return result;
	}
	public static void pinThisAboutUs(Connection con, String toBePinnedCode, short pin, String userCode) throws SQLException{
		PreparedStatement stmt = null;
		stmt = con.prepareStatement("update about_us_table set au_is_pinned = "+pin+" where au_code = ? and au_ngo_uid_fk=?");
		stmt.setString(1, toBePinnedCode);
		stmt.setString(2, userCode);
		stmt.executeUpdate();
	}
	public static boolean updateThisAboutUs(Connection con, Integer toBeUpdatedCode, String newAboutUsHeading, String newAboutUsContent, Calendar createdOn, Integer userCode) throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Date sqlDate = new java.sql.Date(createdOn.getTime().getTime());
		PreparedStatement stmt = null;
		stmt = con.prepareStatement("update about_us_table set au_heading = ?, au_content=?, au_created_on=? where au_code=? and au_ngo_uid_fk=?");
		stmt.setString(1, newAboutUsHeading);
		stmt.setString(2, newAboutUsContent);
		stmt.setDate(3, sqlDate);
		stmt.setInt(4, toBeUpdatedCode);
		stmt.setInt(5, userCode);
		stmt.executeUpdate();
		stmt.close();
		return true;
	}

	public static boolean deleteThisAboutUs(Connection con, Integer toBeDeletedCode, Integer userCode) throws SQLException {
		PreparedStatement stmt = null;
		stmt =  con.prepareStatement("delete from about_us_table where au_code=? and au_ngo_uid_fk=?");
		stmt.setInt(1, toBeDeletedCode);
		stmt.setInt(2, userCode);
		stmt.execute();
		return true;
	}

	public static String getPhotoPublicId(Connection conn, Integer auCode) throws SQLException {
		String logoId = "";
		PreparedStatement stmt = conn.prepareStatement("select public_id, id, owner_id, category "
				+ " from photo_table where owner_id=? and category = 'AU'");
		stmt.setInt(1, auCode);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			logoId = rs.getString("public_id");
		}
		return logoId;
	}
	

}
