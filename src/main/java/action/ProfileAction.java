package action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

import beans.AboutUsBean;
import beans.EventBean;
import beans.NgoBean;
import beans.PhotoBean;
import config.DBConnection;
import constants.Constants;
import constants.ResultConstants;
import dao.AboutUsDao;
import dao.AppreciationDao;
import dao.NgoDao;
import dao.PhotoDao;


public class ProfileAction extends ActionSupport implements SessionAware{
	SessionMap<String,Object> sessionMap;
	
	private Integer pageOwnerCode;
	private int noOfAppreciations;
	private int noOfEvents;
	private int noOfPhotos;
	private NgoBean pageOwnerBean;
	private List<EventBean> eventList;
	private List<PhotoBean> photoList = new ArrayList<PhotoBean>();
	private String id;
	private String appTo;
	

	private String appByEmail;
	private String appByName = "";
	
	private List<AboutUsBean> aboutUsList = new ArrayList<AboutUsBean>();
	private String tourName;
	private String aboutUsPageNo= "0";
	private boolean endReached;
	
	private boolean firstVisit;
	public String ajaxAppreciateAction()
	{
		Integer ato = 0;
		String aby="";
		ato = pageOwnerCode;
		aby = ""+sessionMap.get("userCode");
		if(aby==null || "".equals(aby))
			aby = appByEmail;
		Connection con = null;
		
		try{
			con = DBConnection.getConnection();
			AppreciationDao.appreciate(con, aby, ato, appByName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				setNoOfAppreciations(AppreciationDao.getNoOfAppreciations(con, ato));
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sessionMap.put("isUserAppreciated", true);
		return ResultConstants.SUCCESS;
	}
	public String home() throws SQLException{
		setPageOwnerCode(Integer.parseInt(""+sessionMap.get("userCode")));
		return ResultConstants.SUCCESS;
	}
	public String openProfile()
	{
		try(Connection conn = DBConnection.getConnection()) {
			Integer ngoUid = null;
			Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
			if(""!=id && null!=id)
			{
				ngoUid = NgoDao.getNgoUidFromAlias(conn, id);
				if(ngoUid == 0)
					return ResultConstants.PAGE_NOT_FOUND;
				setPageOwnerCode(ngoUid);
				NgoBean sessionBean = NgoDao.getSessionNgoBeanFromId(conn, ngoUid);
    			sessionMap.put("pageOwnerBean", sessionBean);
    			sessionMap.put("isUserModified", false);
				if(ngoUid.equals(Integer.parseInt("" + sessionMap.get("userCode")))){
					sessionMap.put("guest", false);
					sessionMap.put("owner", true);
					sessionMap.put("visitor", false);
				}
				sessionMap.put("isUserAppreciated", NgoDao.isAppreciated(conn, userCode, pageOwnerCode));
				
			}
			else
				ngoUid = pageOwnerCode;
			ArrayList<String> selectables = new ArrayList<String>();
			selectables.add("all");
			NgoBean ngoBean = new NgoBean();
			
			ngoBean.setListOfCoverPhotos(PhotoDao.getListOfCoverPhotos(conn, ngoUid, 0, 10));
			boolean isOwner = ngoUid.equals(Integer.parseInt("" + sessionMap.get("userCode")));
			List<EventBean> eventList = NgoDao.getListOfEvents(conn, ngoUid, isOwner, -1, -1, 0, 5);
			ngoBean.setNgoEventBeanList(eventList);
			setPageOwnerBean(ngoBean);
			//NgoBean sessionNgoBean = NgoDao.getNgoBeanFromId(conn, ngoUid, selectables);
			//sessionMap.put("pageOwnerBean", ngoBean);
			setNoOfEvents(1);
			setNoOfPhotos(NgoDao.getCountOfCoverPhotos(conn, ngoUid));
			photoList = PhotoDao.getListOfPhotos(conn, ngoUid, "ngo", 0, 20, true);
			if(!sessionMap.containsKey("firstVisit") || (boolean)sessionMap.get("firstVisit")){
				sessionMap.put("firstVisit", false);
				setFirstVisit(true);
			} else {
				setFirstVisit(false);
			}
			
			setTourName("TOUR_PROFILE");
			
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			return ResultConstants.PAGE_NOT_FOUND;
		}
		catch (SQLException sqle){
			sqle.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	public String ajaxGetAboutUsList(){
		try(Connection conn = DBConnection.getConnection()) {
			aboutUsList = AboutUsDao.getAboutUsList(conn, getPageOwnerCode(), Integer.parseInt(aboutUsPageNo)*4, 4);
			setEndReached(aboutUsList.size()==0);
		}
		catch (SQLException sqle){
			sqle.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAppTo() {
		return appTo;
	}
	public void setAppTo(String appTo) {
		this.appTo = appTo;
	}
	
	public int getNoOfEvents() {
		return noOfEvents;
	}
	public void setNoOfEvents(int noOfEvents) {
		this.noOfEvents = noOfEvents;
	}
	public int getNoOfPhotos() {
		return noOfPhotos;
	}
	public void setNoOfPhotos(int noOfPhotos) {
		this.noOfPhotos = noOfPhotos;
	}
	public NgoBean getPageOwnerBean() {
		return pageOwnerBean;
	}
	public void setPageOwnerBean(NgoBean pageOwnerBean) {
		this.pageOwnerBean = pageOwnerBean;
	}
	@Override
	public void setSession(Map<String, Object> sessionMap) {
		// TODO Auto-generated method stub
		this.sessionMap=(SessionMap)sessionMap;
	}
	
	public Integer getPageOwnerCode() {
		return pageOwnerCode;
	}
	public void setPageOwnerCode(Integer pageOwnerCode) {
		this.pageOwnerCode = pageOwnerCode;
	}
	public int getNoOfAppreciations() {
		return noOfAppreciations;
	}
	public void setNoOfAppreciations(int noOfAppreciations) {
		this.noOfAppreciations = noOfAppreciations;
	}
	public List<EventBean> getEventList() {
		return eventList;
	}
	public void setEventList(List<EventBean> eventList) {
		this.eventList = eventList;
	}
	public List<PhotoBean> getPhotoList() {
		return photoList;
	}
	public void setPhotoList(List<PhotoBean> photoList) {
		this.photoList = photoList;
	}
	public String getAppByEmail() {
		return appByEmail;
	}
	public void setAppByEmail(String appByEmail) {
		this.appByEmail = appByEmail;
	}
	public String getAppByName() {
		return appByName;
	}
	public void setAppByName(String appByName) {
		this.appByName = appByName;
	}
	public List<AboutUsBean> getAboutUsList() {
		return aboutUsList;
	}
	public void setAboutUsList(List<AboutUsBean> aboutUsList) {
		this.aboutUsList = aboutUsList;
	}
	public String getTourName() {
		return tourName;
	}
	public void setTourName(String tourName) {
		this.tourName = tourName;
	}
	public String getAboutUsPageNo() {
		return aboutUsPageNo;
	}
	public void setAboutUsPageNo(String aboutUsPageNo) {
		this.aboutUsPageNo = aboutUsPageNo;
	}
	public boolean isEndReached() {
		return endReached;
	}
	public void setEndReached(boolean endReached) {
		this.endReached = endReached;
	}
	public boolean isFirstVisit() {
		return firstVisit;
	}
	public void setFirstVisit(boolean firstVisit) {
		this.firstVisit = firstVisit;
	}
}
