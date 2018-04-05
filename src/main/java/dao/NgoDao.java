package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import beans.AddressBean;
import beans.CauseBean;
import beans.EventBean;
import beans.NgoBean;
import constants.ConfigConstants;
import constants.Constants;
import security.SecurityUtil;
import util.CloudinaryUtils;

public class NgoDao {

	public static Integer createNew(Connection con, String ngoEmail, String ngoName,
			String ngoDescription, String ngoPhone, int ngoNoOfVolunteers, String alias)
			throws SQLException {
		Integer ngoUid = 0;
		try {
			String sql = "insert into ngos_table(ngo_email, ngo_name, ngo_description, ngo_phone_number, ngo_no_of_volunteers, ngo_alias)"
					+ " values ( ?, ?, ?, ?, ?, ?)";
					
			PreparedStatement pStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pStmt.setString(1, ngoEmail);
			pStmt.setString(2, ngoName);
			pStmt.setString(3, ngoDescription);
			pStmt.setString(4, ngoPhone);
			pStmt.setInt(5, ngoNoOfVolunteers);
			pStmt.setString(6, alias);
			pStmt.execute();
			ResultSet rsGetAutoId = pStmt.getGeneratedKeys();
			if (rsGetAutoId.next())
				ngoUid = rsGetAutoId.getInt(1);
			
			setNgoAlias(con, ngoUid, alias);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return ngoUid;
	}

	public static void updateNgo(Connection con, Integer ngoUid, String ngoName, String ngoAlias, String ngoDescription, String ngoPhone,
			Integer ngoNoOfVolunteers) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("update ngos_table set ngo_name = ?, ngo_alias=?, ngo_description= ?, ngo_no_of_volunteers=?"
				+", ngo_phone_number=? where ngo_uid=?");
			// TODO Auto-generated catch block
		stmt.setString(1, ngoName);
		stmt.setString(2, ngoAlias);
		stmt.setString(3, ngoDescription);
		stmt.setInt(4, ngoNoOfVolunteers);
		stmt.setString(5, ngoPhone);
		stmt.setInt(6, ngoUid);
		stmt.execute();
		stmt.close();
	
	}

	public static NgoBean getNgoBeanFromId(Connection con, Integer uid) throws SQLException{
		NgoBean ngoBean = new NgoBean();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select * from ngos_table where ngo_uid ='" + uid + "'");
		while (rs.next()) {
			ngoBean.setUid(uid);
			ngoBean.setNgoEmail(rs.getString("ngo_email"));
			ngoBean.setNgoName(rs.getString("ngo_name"));
			ngoBean.setNgoDescription(rs.getString("ngo_description"));
			ngoBean.setNgoLogoUrl(PhotoDao.getUrlFromPhotoId(con, rs.getInt("ngo_logo_p_id"), true));
			ngoBean.setNgoPhone(rs.getString("ngo_phone_number"));
			ngoBean.setAlias(rs.getString("ngo_alias"));
			ngoBean.setNoOfVolunteers(rs.getInt("ngo_no_of_volunteers"));
			ngoBean.setNoOfAppreciations(rs.getInt(Constants.NGOBEAN_NO_OF_APPRECIATIONS));
			ngoBean.setType(rs.getString(Constants.NGOBEAN_TYPE));
		}
		return ngoBean;
	}
	public static NgoBean getNgoBeanFromId(Connection con, Integer uid, ArrayList<String> selectables)  throws SQLException {
		NgoBean ngoBean = new NgoBean();
		if(selectables.contains("all")){
			selectables.remove("all");
			selectables.add(Constants.NGOBEAN_ADDRESS_LIST);
			selectables.add(Constants.NGOBEAN_CAUSE_LIST);
			selectables.add(Constants.NGOBEAN_DESCRIPTION);
			selectables.add(Constants.NGOBEAN_EMAIL);
			selectables.add(Constants.NGOBEAN_LOGO_P_ID);
			selectables.add(Constants.NGOBEAN_NAME);
			selectables.add(Constants.NGOBEAN_NO_OF_APPRECIATIONS);
			selectables.add(Constants.NGOBEAN_NO_OF_VOLUNTEERS);
			selectables.add(Constants.NGOBEAN_PHONE);
			selectables.add(Constants.NGOBEAN_TYPE);
			selectables.add(Constants.NGOBEAN_UID);
		}
		//selectables = new ArrayList<String>(selectables);
		String joinQuery = "";
		if(!selectables.contains(Constants.NGOBEAN_UID))
			selectables.add(Constants.NGOBEAN_UID);
		if(selectables.contains(Constants.NGOBEAN_ADDRESS_LIST)){
			
			selectables.remove(Constants.NGOBEAN_ADDRESS_LIST);
			selectables.add("nat.add_code_pk");
			selectables.add("amt.add_state");
			selectables.add("nat.add_text");
			selectables.add("amt.add_city");
			selectables.add("amt.add_area");
			selectables.add("add_pincode");
			joinQuery += " left join ngo_address_table nat on nat.add_ngo_code_fk=T1.ngo_uid"
					+ " left join address_master_table amt on amt.add_code_pk=nat.add_master_code_fk";
		}
		if(selectables.contains(Constants.NGOBEAN_CAUSE_LIST)){
			
			selectables.remove(Constants.NGOBEAN_CAUSE_LIST);
			selectables.add("cmt.cause_name");
			selectables.add("cmt.cause_icon");
			selectables.add("nct.nct_code_pk");
			joinQuery += " left join ngo_causes_table nct on nct.nct_ngo_uid_fk = T1.ngo_uid "
					+ " left join causes_master_table cmt on nct.nct_cause_code_fk=cmt.cause_code_pk";
		}
		String selectString = "";
		selectString = selectables.toString();
		selectString = selectString.substring(1, selectString.length()-1);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select "+selectString+" from ngos_table T1 "+joinQuery+" where ngo_uid ='" + uid + "'");
		HashSet<Integer> hsAdd = null;
		HashSet<Integer> hsCause = null;
		AddressBean adBean = null;
		CauseBean cBean = null;
		List<AddressBean> ngoAddressBeanList = null;
		List<CauseBean> ngoCauseBeanList = null;
		int counter=0;
		long fetchTime = System.currentTimeMillis()/1000l;
		while (rs.next()) {
			if (counter==0) {
				ngoBean.setUid(uid);
				hsAdd = new HashSet<Integer>();
				hsCause = new HashSet<Integer>();
				ngoCauseBeanList = new ArrayList<CauseBean>();
				ngoAddressBeanList = new ArrayList<AddressBean>();
				ngoBean.setNgoAddressBeanList(ngoAddressBeanList);
				ngoBean.setNgoCauseBeanList(ngoCauseBeanList);
				
			}
			if (selectables.contains(Constants.NGOBEAN_EMAIL))
					ngoBean.setNgoEmail(rs.getString("ngo_email"));
			if (selectables.contains(Constants.NGOBEAN_NAME))
				ngoBean.setNgoName(rs.getString("ngo_name"));
			if (selectables.contains(Constants.NGOBEAN_DESCRIPTION))
				ngoBean.setNgoDescription(rs.getString("ngo_description"));
			if (selectables.contains(Constants.NGOBEAN_LOGO_P_ID))
				ngoBean.setNgoLogoUrl(PhotoDao.getUrlFromPhotoId(con, rs.getInt("ngo_logo_p_id"), true));
			if (selectables.contains(Constants.NGOBEAN_PHONE))
				ngoBean.setNgoPhone(rs.getString("ngo_phone_number"));
			if (selectables.contains(Constants.NGOBEAN_ALIAS))
				ngoBean.setAlias(rs.getString("ngo_alias"));
			if (selectables.contains(Constants.NGOBEAN_NO_OF_VOLUNTEERS))
				ngoBean.setNoOfVolunteers(rs.getInt("ngo_no_of_volunteers"));
			if (selectables.contains(Constants.NGOBEAN_NO_OF_APPRECIATIONS))
				ngoBean.setNoOfAppreciations(rs.getInt(Constants.NGOBEAN_NO_OF_APPRECIATIONS));
			if (selectables.contains(Constants.NGOBEAN_TYPE))
				ngoBean.setType(rs.getString(Constants.NGOBEAN_TYPE));
			if(selectables.contains("nat.add_code_pk")){
				if(!hsAdd.contains(rs.getInt("nat.add_code_pk"))){
				 adBean = new AddressBean(rs.getInt("nat.add_code_pk"),
							 	rs.getString("amt.add_area"),
							rs.getString("amt.add_city"),
								rs.getString("amt.add_state"),
								rs.getInt("amt.add_pincode"),
								rs.getString("nat.add_text"));
				 hsAdd.add(rs.getInt("nat.add_code_pk"));
				 ngoAddressBeanList.add(adBean);
				
				}
			}
			if(selectables.contains("nct.nct_code_pk")){
				if(!hsCause.contains(rs.getInt("nct.nct_code_pk"))){
				cBean = new CauseBean(rs.getInt("nct.nct_code_pk"),
						rs.getString("cmt.cause_name"),
						rs.getString("cmt.cause_icon"));
				hsCause.add(rs.getInt("nct.nct_code_pk"));
				ngoCauseBeanList.add(cBean);
				
				}
			}
			counter = 1;
		}
		
		System.out.println("ngo details fetched in  "+( System.currentTimeMillis()/1000l-fetchTime));
		return ngoBean;
	}

	public static NgoBean getSessionNgoBeanFromId(Connection con, Integer uid)  throws SQLException{
		NgoBean ngoBean = new NgoBean();
		ArrayList<String> selectables = new ArrayList<String>();
		selectables.add("all");
		ngoBean = NgoDao.getNgosFromIds(con, new Integer[]{uid}, selectables).get(0);
		/*Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select ngo_email, ngo_name, ngo_logo_p_id, ngo_phone_number, ngo_alias from ngos_table where ngo_uid ='" + uid + "'");
		while (rs.next()) {
			ngoBean.setUid(uid);
			ngoBean.setNgoEmail(rs.getString("ngo_email"));
			ngoBean.setNgoName(rs.getString("ngo_name"));
			ngoBean.setNgoLogoUrl(PhotoDao.getUrlFromPhotoId(con, rs.getInt("ngo_logo_p_id"), true));
			ngoBean.setNgoPhone(rs.getString("ngo_phone_number"));
		}*/
		return ngoBean;
	}

	public static Integer getNgoUidFromAlias(Connection con, String alias)  throws SQLException{
		Integer uid = 0;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select ngo_uid_fk, ngo_alias from ngo_alias_table where ngo_alias ='" + alias + "'");
		if (rs.next()) {
			uid = rs.getInt("ngo_uid_fk");
		}
		return uid;
	}


	public static List<NgoBean> getNgosFromIds(Connection con, Integer uid[], List<String> selectables)  throws SQLException{
		//long startTime = System.currentTimeMillis()/1000l;
		List<NgoBean> ngoBeans = new ArrayList<NgoBean>();
		String codeString = "";
		for (int i = 0; i < uid.length; i++) {
			codeString =  uid[i] + " , " + codeString;
		}
		
		String joinQuery = "";
		if(selectables.contains("all")){
			selectables.remove("all");
			selectables.add(Constants.NGOBEAN_ALIAS);
			selectables.add(Constants.NGOBEAN_ADDRESS_LIST);
			selectables.add(Constants.NGOBEAN_CAUSE_LIST);
			selectables.add(Constants.NGOBEAN_DESCRIPTION);
			selectables.add(Constants.NGOBEAN_EMAIL);
			selectables.add(Constants.NGOBEAN_LOGO_P_ID);
			selectables.add(Constants.NGOBEAN_NAME);
			selectables.add(Constants.NGOBEAN_NO_OF_APPRECIATIONS);
			selectables.add(Constants.NGOBEAN_NO_OF_VOLUNTEERS);
			selectables.add(Constants.NGOBEAN_PHONE);
			selectables.add(Constants.NGOBEAN_TYPE);
			selectables.add(Constants.NGOBEAN_UID);
		}
		if(!selectables.contains(Constants.NGOBEAN_UID))
			selectables.add(Constants.NGOBEAN_UID);
		if(selectables.contains(Constants.NGOBEAN_ADDRESS_LIST)){
			
			selectables.remove(Constants.NGOBEAN_ADDRESS_LIST);
			selectables.add("nat.add_code_pk");
			selectables.add("amt.add_state");
			selectables.add("nat.add_text");
			selectables.add("amt.add_city");
			selectables.add("amt.add_area");
			selectables.add("add_pincode");
			joinQuery += "  left join ngo_address_table nat on nat.add_ngo_code_fk=T1.ngo_uid"
					+ " left join address_master_table amt on amt.add_code_pk=nat.add_master_code_fk";
		}
		if(selectables.contains(Constants.NGOBEAN_CAUSE_LIST)){
			
			selectables.remove(Constants.NGOBEAN_CAUSE_LIST);
			selectables.add("cmt.cause_icon");
			selectables.add("cmt.cause_name");
			selectables.add("nct.nct_code_pk");
			joinQuery += "  left join ngo_causes_table nct on nct.nct_ngo_uid_fk = T1.ngo_uid "
					+ " left join causes_master_table cmt on nct.nct_cause_code_fk=cmt.cause_code_pk";
		}
		String selectString = "";
		selectString = selectables.toString();
		selectString = selectString.substring(1, selectString.length()-1);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select "+selectString+" from ngos_table T1 "+joinQuery+" where ngo_uid in ("
				+ codeString.substring(0, codeString.length() - 2) + ") order by ngo_type desc");
		NgoBean ngoBean = new NgoBean();
		ngoBean.setUid(-1);
		HashSet<String> hsNgo = new HashSet<String>();
		HashSet<Integer> hsAdd = null;
		HashSet<Integer> hsCause = null;
		AddressBean adBean = null;
		CauseBean cBean = null;
		List<AddressBean> ngoAddressBeanList = null;
		List<CauseBean> ngoCauseBeanList = null;
		while (rs.next()) {
			if(!hsNgo.contains(rs.getString("ngo_uid"))){
				ngoBean = new NgoBean();
				ngoBean.setUid(rs.getInt("ngo_uid"));
				hsNgo.add(rs.getString("ngo_uid"));
				ngoBeans.add(ngoBean);
				hsAdd = new HashSet<Integer>();
				hsCause = new HashSet<Integer>();
				ngoCauseBeanList = new ArrayList<CauseBean>();
				ngoAddressBeanList = new ArrayList<AddressBean>();
				ngoBean.setNgoAddressBeanList(ngoAddressBeanList);
				ngoBean.setNgoCauseBeanList(ngoCauseBeanList);
			}
				
			if (selectables.contains(Constants.NGOBEAN_EMAIL))
				ngoBean.setNgoEmail(rs.getString("ngo_email"));
			if (selectables.contains(Constants.NGOBEAN_NAME))
				ngoBean.setNgoName(rs.getString("ngo_name"));
			if (selectables.contains(Constants.NGOBEAN_DESCRIPTION))
				ngoBean.setNgoDescription(rs.getString("ngo_description"));
			if (selectables.contains(Constants.NGOBEAN_LOGO_P_ID))
				ngoBean.setNgoLogoUrl(PhotoDao.getUrlFromPhotoId(con, rs.getInt("ngo_logo_p_id"), true));
			if (selectables.contains(Constants.NGOBEAN_PHONE))
				ngoBean.setNgoPhone(rs.getString("ngo_phone_number"));
			if (selectables.contains(Constants.NGOBEAN_ALIAS))
				ngoBean.setAlias(rs.getString("ngo_alias"));
			if (selectables.contains(Constants.NGOBEAN_NO_OF_VOLUNTEERS))
				ngoBean.setNoOfVolunteers(rs.getInt("ngo_no_of_volunteers"));
			if (selectables.contains(Constants.NGOBEAN_NO_OF_APPRECIATIONS))
				ngoBean.setNoOfAppreciations(rs.getInt(Constants.NGOBEAN_NO_OF_APPRECIATIONS));
			if (selectables.contains(Constants.NGOBEAN_TYPE))
				ngoBean.setType(rs.getString(Constants.NGOBEAN_TYPE));
			if(selectables.contains("nat.add_code_pk")){
				int addCode = rs.getInt("nat.add_code_pk");
				if(!hsAdd.contains(addCode) && addCode!=0){
					 adBean = new AddressBean(addCode,
							 	rs.getString("amt.add_area"),
								rs.getString("amt.add_city"),
								rs.getString("amt.add_state"),
								rs.getInt("amt.add_pincode"),
								rs.getString("nat.add_text"));
					 hsAdd.add(addCode);
					 ngoAddressBeanList.add(adBean);
					
				 }
			}
			if(selectables.contains("nct.nct_code_pk")){
				int causeCode = rs.getInt("nct.nct_code_pk");
				if(!hsCause.contains(causeCode) && causeCode!=0){
					cBean = new CauseBean(causeCode,
							rs.getString("cmt.cause_name"),
							rs.getString("cmt.cause_icon"));
					hsCause.add(rs.getInt("nct.nct_code_pk"));
					ngoCauseBeanList.add(cBean);
					
				}
			}
		}
		return ngoBeans;
	}
	public static List<Integer> listAllNgos(Connection con, String type, int start, int count)  throws SQLException{
		List<Integer> resultList = new ArrayList<Integer>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"SELECT ngo_uid, ngo_name, ngo_type from ngos_table where ngo_type='"+type+"' order by ngo_type, ngo_name limit "+start+","+count);
		while (rs.next()) {
			resultList.add(rs.getInt("ngo_uid"));
		}
		rs.close();
		stmt.close();
		return resultList;
	}
	public static List<Integer> searchByName(Connection con, String searchQuery, boolean isUser, int start, int count)  throws SQLException{

		List<Integer> resultList = new ArrayList<Integer>();
		Statement stmt = con.createStatement();
		String strWhere = "";
		if(isUser)
			strWhere = " and ngo_type='user' ";
		ResultSet rs = stmt.executeQuery(
				"SELECT ngo_uid, ngo_name, ngo_type from ngos_table where ngo_name like '" + searchQuery + "%' "+strWhere+"order by ngo_type, ngo_name limit "+start+","+count);
		while (rs.next()) {
			resultList.add(rs.getInt("ngo_uid"));
		}
		rs.close();
		stmt.close();
		return resultList;
	}

	public static List<Integer> searchByCause(Connection con, boolean searchByName, String searchQuery, String ngoType, int start, int count)  throws SQLException{
		List<Integer> resultList = new ArrayList<Integer>();
		if(!searchByName){
			//searchQuery = searchQuery.replace(",", "','");
			searchQuery = " cmt.cause_code_pk in ("+searchQuery+")";
		}
		else
			searchQuery = " cmt.cause_name like '%"+ searchQuery + "%'";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select cmt.cause_code_pk, nct.nct_ngo_uid_fk, nct.nct_cause_code_fk"
						+ " from causes_master_table cmt "
						+ "join ngo_causes_table nct on nct.nct_cause_code_fk=cmt.cause_code_pk "
						+ "where "+ searchQuery + " and nct.nct_ngo_type = '"+ngoType+"' order by nct.nct_ngo_uid_fk limit "+start+","+count);
		while (rs.next())
			resultList.add(rs.getInt("nct.nct_ngo_uid_fk"));
		rs.close();
		stmt.close();
		return resultList;
	}
	public static List<Integer> searchByCauseAndLocation(Connection con, String causeQuery, String locationQuery, String mode, String ngoType, int start, int count) 
			 throws SQLException{
		List<Integer> resultList = new ArrayList<Integer>();
		String locationWhere = "";
		if(causeQuery.indexOf(",")!=-1){
			//causeQuery = causeQuery.replace(",", "','");
			causeQuery = "in ("+causeQuery+")";
		}
		else
			causeQuery = "like '%"+ causeQuery + "%'";
		if (mode.equalsIgnoreCase("city"))
			locationWhere = " amt.add_city = '" + locationQuery + "'";
		if (mode.equalsIgnoreCase("state"))
			locationWhere = " amt.add_state ='" + locationQuery + "'";
		
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select cmt.cause_code_pk, nct.nct_ngo_uid_fk"
						+ " from causes_master_table cmt join ngo_causes_table nct on nct.nct_cause_code_fk=cmt.cause_code_pk "
						+ "join ngo_address_table nat on nat.add_ngo_code_fk = nct.nct_ngo_uid_fk "
						+ "join address_master_table amt on amt.add_code_pk = nat.add_master_code_fk where "+locationWhere
						+ " and cmt.cause_code_pk "+ causeQuery + " and nct.nct_ngo_type = '"+ngoType+"' order by nct.nct_ngo_uid_fk limit "+start+",5");
						//						+ " and cmt.cause_name "+ causeQuery + /*" and nct.nct_ngo_type = '"+ngoType+"' */" order by nct.nct_ngo_uid_fk, nct.nct_ngo_type desc limit "+start+",5");
	
		while (rs.next())
			resultList.add(rs.getInt("nct.nct_ngo_uid_fk"));
		rs.close();
		stmt.close();
		return resultList;
	}
	public static List<Integer> searchByLocation(Connection con, String mode, String searchQuery, String ngoType, int start, int count)  throws SQLException{
		List<Integer> resultList = new ArrayList<Integer>();
		String strWhere = "";
		if (mode.equalsIgnoreCase("all"))
			strWhere = "amt.add_area like '%" + searchQuery + "%' or amt.add_city like '%" + searchQuery
					+ "%' or amt.add_state like '%" + searchQuery + "%'"
					+ " or nat.add_text like '%" + searchQuery + "%'";
		String and = !searchQuery.equals("")?"and":"";
		String pincodeWhere = ngoType.equals("auto")?" "+and+" amt.add_pincode=0":" "+and+" amt.add_pincode<>0";
			
		if (mode.equalsIgnoreCase("city") && !searchQuery.equals(""))
			strWhere = " amt.add_city = '" + searchQuery + "'";
		if (mode.equalsIgnoreCase("state"))
			strWhere = " amt.add_state ='" + searchQuery + "'";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select nat.add_ngo_code_fk, amt.add_pincode"
				+ " from ngo_address_table nat join address_master_table amt "
				+ "on amt.add_code_pk=nat.add_master_code_fk "
				+ "where " + strWhere +pincodeWhere+" order by nat.add_ngo_code_fk limit "+start+","+count);
		while (rs.next())
			resultList.add(rs.getInt("add_ngo_code_fk"));
		rs.close();
		stmt.close();
		return resultList;
	}

	public static List<AddressBean> getAddressListOfNgo(Connection con, String ngoUid)  throws SQLException{
		List<AddressBean> addressBeanList = new ArrayList<AddressBean>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select nat.add_code_pk, nat.add_text, nat.add_master_code_fk, nat.add_ngo_code_fk,"
				+ " amt.add_code_pk, amt.add_state, amt.add_city, amt.add_pincode, amt.add_area"
				+ " from ngo_address_table nat join address_master_table amt "
				+ "on amt.add_code_pk=nat.add_master_code_fk "
				+ "where nat.add_ngo_code_fk='"+ngoUid+"'");
		while (rs.next()) {
			addressBeanList.add(new AddressBean(rs.getInt("nat.add_code_pk"), rs.getString("amt.add_area"),
					rs.getString("amt.add_city"), rs.getString("amt.add_state"), rs.getInt("amt.add_pincode"),
					rs.getString("nat.add_text")));
		}
		rs.close();
		stmt.close();
		return addressBeanList;

	}
	public static List<AddressBean> getCityListOfNgo(Connection con, String ngoUid)  throws SQLException{
		List<AddressBean> addressBeanList = new ArrayList<AddressBean>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct nat.add_master_code_fk, nat.add_ngo_code_fk,"
				+ " amt.add_code_pk, amt.add_city"
				+ " from ngo_address_table nat join address_master_table amt "
				+ "on amt.add_code_pk=nat.add_master_code_fk "
				+ "where nat.add_ngo_code_fk='"+ngoUid+"'");
		while (rs.next()) {
			addressBeanList.add(new AddressBean(0, "",
					rs.getString("amt.add_city"), "", 0,
					""));
		}
		rs.close();
		stmt.close();
		return addressBeanList;

	}

	public static List<CauseBean> getCauseListOfNgo(Connection con, String ngoUid)  throws SQLException{
		List<CauseBean> causeBeanList = new ArrayList<CauseBean>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select cmt.cause_code_pk, cmt.cause_name, cmt.cause_icon, nct.nct_code_pk, nct.nct_ngo_uid_fk, nct.nct_cause_code_fk"
				+ " from causes_master_table cmt join ngo_causes_table nct on nct.nct_cause_code_fk=cmt.cause_code_pk "
				+ "where nct.nct_ngo_uid_fk='"+ ngoUid + "'");
		while (rs.next()) {
			causeBeanList.add(new CauseBean(rs.getInt("nct.nct_cause_code_fk"), rs.getString("cmt.cause_name"),
					rs.getString("cmt.cause_icon")));
		}
		rs.close();
		stmt.close();
		return causeBeanList;

	}

	public static List<EventBean> getListOfEvents(Connection con, Integer ngoUid, boolean isOwner, int eventMonth, int eventYear, int start, int count) throws SQLException {
		Statement stmt = null;
		List<EventBean> eventList = new ArrayList<EventBean>();
		stmt = con.createStatement();
		String dateSelectQuery = "";
		String dateHavingClause = "";
		String whereClause = "where";
		if(!isOwner)
			whereClause += " (evt_status<>'"+Constants.EVENTBEAN_STATUS_CREATE+"')";
		if(ngoUid != 0){
			if(!isOwner)
				whereClause += " and ";
			whereClause += " evt_organizer_code_fk='" + ngoUid + "'";
		}
			
		if(eventMonth!=-1){
			dateSelectQuery += "EXTRACT(MONTH FROM evt_date) AS eventMonth, ";
			dateHavingClause += " having eventMonth="+(eventMonth+1);
		}
		if(eventYear!=-1){
			dateSelectQuery += "EXTRACT(YEAR FROM evt_date) AS eventYear,";
			if(eventMonth==-1)
				dateHavingClause += " having";
			else
				dateHavingClause += " and";
			dateHavingClause += " eventYear="+eventYear;
		}
		String orderClause = " order by (evt_date < CURDATE()),"
				+ " (case when evt_date > CURDATE() then evt_date end) ASC,"
				+ " (case when evt_date < CURDATE() then evt_date end) DESC ";
		ResultSet rs = stmt.executeQuery(
				"select  "+dateSelectQuery
				+ "evt_code_pk, evt_name, evt_dp_p_id, evt_date, evt_time, evt_address_code_fk, evt_details, "
				+ "evt_venue, evt_status, evt_organizer_code_fk, "
				+ "add_area, add_city, add_state, add_pincode from events_table et join address_master_table amt on et.evt_address_code_fk = "
				+ "amt.add_code_pk  "
				+ whereClause +  dateHavingClause + orderClause +" limit "+ start+","+count);
		while (rs.next()) {
			java.sql.Date d  = rs.getDate("evt_date");
			Calendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(d.getTime());
			AddressBean evtAddressBean = new AddressBean(rs.getInt("evt_address_code_fk"), rs.getString("add_area"), rs.getString("add_city"),
					rs.getString("add_state"), rs.getInt("add_pincode"), rs.getString("evt_venue"));
			evtAddressBean.setStreet(rs.getString("evt_venue"));
			eventList.add(new EventBean(rs.getInt("evt_code_pk"), rs.getString("evt_name"),
					rs.getString("evt_details"), calendar, evtAddressBean,
					rs.getString("evt_time"), rs.getInt("evt_organizer_code_fk"),
					PhotoDao.getUrlFromPhotoId(con, rs.getInt("evt_dp_p_id"), true),"", rs.getString("evt_status")));
		}

		return eventList;
	}


	

	public static int getCountOfCoverPhotos(Connection con, Integer ngoUid) throws SQLException {
		// TODO Auto-generated method stub
		Statement stmt = null;
		int count = 0;
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select count(id) as pcount from photo_table where owner_id=" + ngoUid
				+ " and is_cover=1");
		if (rs.next()) {
			count = rs.getInt("pcount");
		}
		return count;
	}
	public static Map<String, String> getLogoId(Connection conn, Integer ngoUid) throws SQLException{
		HashMap<String, String> map = new HashMap<String, String>();
		PreparedStatement stmt = conn.prepareStatement("select ngo_uid, ngo_logo_p_id, public_id"
				+ " from ngos_table join photo_table on ngo_logo_p_id = id where ngo_uid=? ");
		stmt.setInt(1, ngoUid);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			map.put("publicId", rs.getString("public_id"));
			map.put("id", rs.getString("ngo_logo_p_id"));
		}
		return map;
	}
	public static void updateLogo(Connection conn, Integer ngoUid, int pId) throws SQLException{
		PreparedStatement stmt = conn.prepareStatement("update ngos_table set ngo_logo_p_id =? where ngo_uid=?");
		stmt.setInt(1, pId);
		stmt.setInt(2, ngoUid);
		stmt.executeUpdate();
	}

	public static boolean isAppreciated(Connection conn, Integer userCode, Integer pageOwnerCode) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("select app_by_uid, app_to_ngo_uid_fk from appreciation_table"
				+ " where app_by_uid = ? and app_to_ngo_uid_fk = ?");
		stmt.setInt(1, userCode);
		stmt.setInt(2, pageOwnerCode);
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}

	public static List<NgoBean> getListOfAutoGenNgosNames(Connection conn, String query, int count) throws SQLException {
		List<NgoBean> list = new ArrayList<NgoBean>();
		PreparedStatement stmt = conn.prepareStatement("select ngo_uid, ngo_name, ngo_type"
				+ " from ngos_table where ngo_name like ? and ngo_type = ? order by ngo_name limit 0,10");
		stmt.setString(1, query+"%");
		stmt.setString(2, "auto");
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			NgoBean nb = new NgoBean();
			nb.setNgoName(rs.getString("ngo_name"));
			nb.setUid(rs.getInt("ngo_uid"));
			list.add(nb);
		}
		return list;
	}

	public static void deleteNgo(Connection conn, Integer ngoUid) throws Exception {
		PreparedStatement stmt = conn.prepareStatement("select evt_code_pk, evt_organizer_code_fk from events_table where evt_organizer_code_fk = ?");
		stmt.setInt(1, ngoUid);
		ResultSet rs = stmt.executeQuery();
		String folderPath = "";
		ArrayList<String> eventPhotos = new ArrayList<String>();
		while(rs.next()){
			int eventId = rs.getInt("evt_code_pk");
			eventPhotos = EventDao.getPublicIdForPhotos(conn, eventId);
			EventDao.deleteEvent(conn, eventId, ngoUid);
		}
		stmt.close();
		ArrayList<String> ngoPhotos = NgoDao.getPublicIdForAllPhotos(conn, ngoUid);
		
		stmt = conn.prepareStatement("delete pt from photo_table pt left join about_us_table aut on pt.owner_id = aut.au_code  where owner_id=? or aut.au_ngo_uid_fk=?");
		stmt.setInt(1, ngoUid);
		stmt.setInt(2, ngoUid);
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement("delete from ngos_table where ngo_uid=?");
		stmt.setInt(1, ngoUid);
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement("delete from users_table where usr_uid=?");
		stmt.setInt(1, ngoUid);
		stmt.execute();
		stmt.close();
		stmt = conn.prepareStatement("delete from appreciation_table where app_by_uid=?");
		stmt.setInt(1, ngoUid);
		stmt.execute();
		stmt.close();
		conn.commit();
		if(eventPhotos.size() > 0){
			CloudinaryUtils.deleteImages(eventPhotos, null);
		}
		if(ngoPhotos.size() > 0){
			CloudinaryUtils.deleteImages(ngoPhotos, null);
		}
	}

	private static ArrayList<String> getPublicIdForAllPhotos(Connection conn, Integer ngoUid) throws SQLException {
		ArrayList<String> photoPubIds = new ArrayList<String>();
		PreparedStatement stmt = conn.prepareStatement("select pt.id, pt.public_id, pt.owner_id, pt.category, aut.au_ngo_uid_fk from photo_table pt "
				+ " left join about_us_table aut on pt.owner_id = aut.au_code where (pt.owner_id=? or aut.au_ngo_uid_fk=?) and "
				+ "(category = 'ngoOthers' or category = 'ngoLogo'  or category = 'AU')");
		stmt.setInt(1, ngoUid);
		stmt.setInt(2, ngoUid);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			photoPubIds.add(rs.getString("public_id"));
		}
		return photoPubIds;
	}

	public static void deleteAutoGenNgo(Connection con, String autoGenId) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("delete from ngos_table where ngo_uid =? and ngo_type='auto'");
		stmt.setString(1, autoGenId);
		stmt.execute();
		stmt.close();
		
	}

	public static void setNgoAlias(Connection con, Integer ngoUid, String alias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("insert into ngo_alias_table (ngo_uid_fk, ngo_alias) values(?, ?)");
		stmt.setInt(1, ngoUid);
		stmt.setString(2, alias);
		stmt.execute();
		stmt.close();
		
	}

	public static void claimProfile(Connection con, Integer claimUserId, String ngoEmail, String password) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("update ngos_table set ngo_type = 'user' where ngo_uid = ?");
		stmt.setInt(1, claimUserId);
		stmt.execute();
		stmt = con.prepareStatement("update ngo_causes_table set nct_ngo_type = 'user' where nct_ngo_uid_fk = ?");
		stmt.setInt(1, claimUserId);
		stmt.execute();
		stmt = con.prepareStatement("insert into users_table (usr_email, usr_password, usr_type, usr_uid, usr_verified) values(?, ?,?,?,?)");
		stmt.setString(1, ngoEmail);
		stmt.setString(2, SecurityUtil.encrypt(password));
		stmt.setString(3, "n");
		stmt.setInt(4, claimUserId);
		stmt.setInt(5, 1);
		stmt.execute();
		stmt.close();		
	}
}
