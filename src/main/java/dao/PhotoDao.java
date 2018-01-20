package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import beans.PhotoBean;
import constants.Constants;
import util.ImageUtil;

public class PhotoDao {
	public static String getUrlFromPhotoId(Connection con, int id, boolean thumb) throws SQLException
	{
		Statement stmt=null;
		ResultSet rs=null;
		String url="";
		stmt=con.createStatement();
		rs=stmt.executeQuery("SELECT * from photo_table where id="+id);
		if(rs.next()){
			if(!thumb)
				url = rs.getString("url");
			else 
				url = rs.getString("thumbnail_url");
		}
		rs.close();
		stmt.close();
		return url;
	}

	public static void updatePhoto(Connection con, Integer ownerId, String pFExt, String catg) throws SQLException  {
		Statement stmt=con.createStatement();
		stmt.execute("update photo_table set p_file_extension='"+pFExt+"' where p_owner_id ="+ownerId+" and p_category='"+catg+"'");
		stmt.close();
	}
	public static List<PhotoBean> getListOfPhotos(Connection con, Integer id, String from, int start, int count, boolean thumb)
	{
		List<PhotoBean> listOfPhotos = new ArrayList<PhotoBean>();
		String limit="";
		if(count>0)
			limit = "order by created_at desc limit "+start+","+count;
		try {
			
			ResultSet rs = null;
			String query = "";
			String cat = "";
			String comnQuery = "SELECT id, public_id, url, thumbnail_url, created_at, category, owner_id, original_filename FROM photo_table ";
			if(from.equalsIgnoreCase("ngo")){
				query = "("+comnQuery + "where owner_id = ? and category = 'ngoOthers' )"
						+ " union "
						+ "("+comnQuery + "pt join events_table et on pt.owner_id = et.evt_code_pk "
						+ "where et.evt_organizer_code_fk = ? and category<>'eventDp' and category<>'AU' ) "+limit;
			} else if(from.equalsIgnoreCase("event")){
				query = comnQuery + "where owner_id = ? and category<>'eventDp' and category<>'AU' "+limit; 
			} else if(from.equalsIgnoreCase("wall")){
				query = comnQuery + "where category<>'eventDp' and category<>'AU' and category not like 'default%' "+limit; 
			} 
			PreparedStatement stmt = con.prepareStatement(query);
			if(!from.equalsIgnoreCase("wall"))
				stmt.setInt(1, id);
			if(from.equalsIgnoreCase("ngo"))
				stmt.setInt(2, id);
			rs = stmt.executeQuery();
			while(rs.next())
			{
				PhotoBean pBean = new PhotoBean(rs.getInt("id"), rs.getString("public_id"), rs.getString("url"), 
						rs.getString("thumbnail_url"), rs.getString("original_filename"), rs.getDate("created_at"), rs.getString("category"), rs.getInt("owner_id"));
				listOfPhotos.add(pBean);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return listOfPhotos;
	}
	public static List<PhotoBean> getListOfCoverPhotos(Connection con, Integer id, int start, int count)
	{
		List<PhotoBean> listOfPhotos = new ArrayList<PhotoBean>();
		String limit="";
		if(count>0)
			limit = "order by created_at desc limit "+start+","+count;
		try {
			String query = "select * from photo_table where is_cover = 1 and owner_id = ? "+limit;
			PreparedStatement stmt = con.prepareStatement(query);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				PhotoBean pBean = new PhotoBean(rs.getInt("id"), rs.getString("public_id"), rs.getString("url"), 
						rs.getString("thumbnail_url"), rs.getString("original_filename"), rs.getDate("created_at"), rs.getString("category"), rs.getInt("owner_id"));
				listOfPhotos.add(pBean);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return listOfPhotos;
	}
	
	public static String getLogoFileURL(Connection con, String type, Integer id) throws SQLException
	{
		String path = "";
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		if(type.equals("ngo"))
			rs = stmt.executeQuery("select ngo_uid, ngo_logo_p_id, url, category from ngos_table join photo_table on ngo_uid = owner_id and category = 'ngoLogo' where ngo_uid="+id);
		else
			rs = stmt.executeQuery("select evt_code_pk, evt_dp_p_id, category, url from events_table join photo_table on evt_code_pk = owner_id and category = 'eventDp' where evt_code_pk="+id);
		if(rs.next()){
			path = rs.getString("url");
		}
		return path;
	}

	public static void deletePhotos(Connection con, String[] deletePhotosIdArray) throws SQLException {
		Statement stmt = con.createStatement();
		StringBuffer ids = new StringBuffer("");
		for(String strCurrId : deletePhotosIdArray)
		{
			ids.append(strCurrId+"','");
		}
		ids = new StringBuffer(ids.substring(0, ids.length()-2));
		stmt = con.createStatement();
		stmt.execute("delete from photo_table where public_id in ('"+ids+")");
		stmt.close();
	}
	public static String[] getPublicIds(Connection con, String[] photosIdArray) throws SQLException {
		Statement stmt = con.createStatement();
		StringBuffer ids = new StringBuffer("");
		for(String strCurrId : photosIdArray)
		{
			ids.append(strCurrId+",");
		}
		ids = new StringBuffer(ids.substring(0, ids.length()-1));
		String sql = "SELECT id, public_id from photo_table where id in ("+ids+")";
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		int i = 0;
		while(rs.next()){
			photosIdArray[i] = rs.getString("public_id");
			i++;
		}
		rs.close();
		stmt.close();
		return photosIdArray;
	}
	public static void deleteAboutUsPhoto(Connection con, Integer toBeDeletedCode) throws SQLException{
		PreparedStatement stmt =  con.prepareStatement("delete from photo_table where owner_id=? and category='AU'");
		stmt.setInt(1, toBeDeletedCode);
		stmt.execute();
		stmt.close();
	}
	public static void addToCover(Connection conn, String[] imgsArr, String ownerId) throws SQLException{
		StringBuffer statement = new StringBuffer("");
		for(String strCurrId : imgsArr)
		{
			statement.append(strCurrId+",");
		}
		statement = new StringBuffer(statement.substring(0, statement.length()-1));
		PreparedStatement stmt = conn.prepareStatement("update photo_table set is_cover = 1 where id in("+statement.toString()+") and owner_id = ?");
		stmt.setString(1, ownerId);
		stmt.executeUpdate();
	}
	public static void removeFromCover(Connection conn, String[] imgsArr, String ownerId) throws SQLException{
		StringBuffer statement = new StringBuffer("");
		for(String strCurrId : imgsArr)
		{
			statement.append(strCurrId+",");
		}
		statement = new StringBuffer(statement.substring(0, statement.length()-1));
		PreparedStatement stmt = conn.prepareStatement("update photo_table set is_cover = 0 where id in("+statement.toString()+") and owner_id = ?");
		stmt.setString(1, ownerId);
		stmt.execute();
	}
	
	public static String getAboutUsPhotoExt(Connection con, Integer toBeDeletedCode) throws SQLException {
		PreparedStatement stmt = null;
		String url = "";
		stmt =  con.prepareStatement("select p_file_extension from photo_table where p_owner_id=? and p_category=?");
		stmt.setInt(1, toBeDeletedCode);
		stmt.setString(2, "AU");
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			url = rs.getString("p_file_extension");
		stmt.close();
		return url;
	}

	public static Integer create(Connection con, PhotoBean upPhoto) throws SQLException {
		int pId = 0;
		String strStmt = "insert into photo_table("
				+ "public_id, url, thumbnail_url, original_filename, created_at, category, owner_id) "
				+ "values(?,?,?,?,?,?,?)";
		PreparedStatement stmt = con.prepareStatement(strStmt, PreparedStatement.RETURN_GENERATED_KEYS);
		stmt.setString(1, upPhoto.getPublicId());
		stmt.setString(2, upPhoto.getUrl());
		stmt.setString(3, upPhoto.getThumbUrl());
		stmt.setString(4, upPhoto.getFileName());
		stmt.setDate(5, new java.sql.Date(upPhoto.getCreatedAt().getTime()));
		stmt.setString(6, upPhoto.getCategory());
		stmt.setInt(7, upPhoto.getOwnerId());
		stmt.execute();
		ResultSet rsGetAutoId = stmt.getGeneratedKeys();
		if (rsGetAutoId.next())
			pId = rsGetAutoId.getInt(1);
		return pId;
		
	}
	public static void update(Connection con, PhotoBean upPhoto, String oldPubId) throws SQLException {
		String strStmt = "update photo_table set public_id = ?, url = ?, thumbnail_url = ?, original_filename = ?, created_at = ? where public_id = ?";
		PreparedStatement stmt = con.prepareStatement(strStmt);
		stmt.setString(1, upPhoto.getPublicId());
		stmt.setString(2, upPhoto.getUrl());
		stmt.setString(3, upPhoto.getThumbUrl());
		stmt.setString(4, upPhoto.getFileName());
		stmt.setDate(5, new java.sql.Date(upPhoto.getCreatedAt().getTime()));
		stmt.setString(6, oldPubId);
		stmt.executeUpdate();
	}
}
