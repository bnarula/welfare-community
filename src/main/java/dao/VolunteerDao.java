package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import beans.VolunteerBean;
import constants.Constants;

public class VolunteerDao {

	public static Integer createNew(Connection conn, VolunteerBean volunteerBean) throws SQLException {
		Integer id = 0;
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("insert into volunteer_table(v_email, v_name, v_phone, v_phone, v_gender) "
					+ "values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS  );
			stmt.setString(1, volunteerBean.getEmail());
			stmt.setString(2, volunteerBean.getName());
			stmt.setString(3, volunteerBean.getContact());
			stmt.setInt(4, volunteerBean.getAge());
			stmt.setString(5, volunteerBean.getGender());
			
			stmt.execute();
			ResultSet rsGetAutoId = stmt.getGeneratedKeys();
			if (rsGetAutoId.next())
				id = rsGetAutoId.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public static boolean newApplication(Connection conn, int eventId, Integer vId) throws SQLException {
		try {	
			Statement stmt = conn.createStatement();
			stmt.execute("insert into event_volunteer_table (e_v_event_id_fk, e_v_vol_id_fk, e_v_status) values("
					+eventId+", "+vId+", '"+Constants.APPLICATION_STATUS_UNVERIFIED+"')");
			stmt.close();
			return true;
			} catch (SQLIntegrityConstraintViolationException e) {
				return false;
		}
	}

	public static VolunteerBean getVolunteerBean(Connection conn, Integer vId) throws SQLException {
		// TODO Auto-generated method stub
		Statement stmt = conn.createStatement();
		VolunteerBean vBean = null;
		ResultSet rs = stmt.executeQuery("select v_email, v_uid, v_name from volunteer_table where v_uid = '"+vId+"'");
		if(rs.next())
			vBean = new VolunteerBean(vId, rs.getString("v_name"), rs.getString("v_email"), "", 0, "", "");
		rs.close();
		stmt.close();
		return vBean;
		
	}

	public static boolean checkVolunteerAlreadyExists(Connection conn, Integer eventId, String email) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select e_v_event_id_fk, e_v_vol_id_fk, v_email from event_volunteer_table "
				+ "join volunteer_table on v_uid = e_v_vol_id_fk "
				+ "where v_email='"+email+"' and e_v_event_id_fk="+eventId);
		if(rs.next())
			return true;
		else
			return false;
	}

	public static void updatePhoto(Connection conn, Integer vId, int pId) {

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("update volunteer_table set v_photo_id = ? where v_uid = ?");
			stmt.setInt(1, pId);
			stmt.setInt(2, vId);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
