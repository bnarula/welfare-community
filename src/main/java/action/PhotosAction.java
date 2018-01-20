package action;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;

import beans.EventBean;
import beans.PhotoBean;
import config.DBConnection;
import constants.ConfigConstants;
import constants.Constants;
import constants.ResultConstants;
import dao.EventDao;
import dao.PhotoDao;
import util.CloudinaryUtils;

public class PhotosAction extends ActionSupport implements SessionAware {

	SessionMap<String, Object> sessionMap;
	List<PhotoBean> listOfPhotos = new ArrayList<PhotoBean>();
	private File[] imgFile;
	private String[] imgFileContentType;
	private String[] imgFileFileName;
	private int photoId;
	private String header;
	private String from;
	private int eventId;
	private String[] addToSlideshow;
	private String[] removeFromSlideshow;
	private String deletePhotosIdArray;
	private String album = "All";
	private Integer pageOwnerCode;
	private String ajaxResponseDummyMsg; 
	private String imgPageNo = "0";
	private boolean endReached;
	 
	private PhotoBean[] upPhotos;
	
	//private String newImgCloudinaryUrl;
	


	public String openPhotosPage(){
		try(Connection con = DBConnection.getConnection()) {
			if(getFrom().equals("ngo"))
			{
				setHeader("Photos");
			}
			if(getFrom().equals("event"))
			{
				ArrayList<String> selectables = new ArrayList<String>();
				selectables.add(Constants.EVENTBEAN_NAME);
				selectables.add(Constants.EVENTBEAN_CALENDAR);
				EventBean evtBean = EventDao.getEventBean(con, eventId, selectables);
				setHeader("Photos for the event "+evtBean.getName());
			}
			if(getFrom().equals("slideshow"))
			{
				setHeader("Add/Remove photos to slideshow on the profile:");
			}
			if(getFrom().equals("all"))
			{
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	public String listPhotosAction() {
		
		Integer ngoUid = null;
		ngoUid = getPageOwnerCode();
		int pageNo = Integer.parseInt(imgPageNo);
		int count = 8;
		try(Connection con = DBConnection.getConnection()) {
			if(getFrom().equals("ngo"))
			{
				if(album.equalsIgnoreCase("Slideshow"))
				{
					listOfPhotos = PhotoDao.getListOfCoverPhotos(con, ngoUid, pageNo*count, count);
				} else {
					listOfPhotos = PhotoDao.getListOfPhotos(con, ngoUid, getFrom(), pageNo*count, count, true);
				}
			}
			if(getFrom().equals("event"))
			{
				listOfPhotos = PhotoDao.getListOfPhotos(con, eventId , getFrom(), pageNo*count, count, true);
			}
			
			if(getFrom().equals("wall"))
			{
				listOfPhotos = PhotoDao.getListOfPhotos(con, 0, "wall", pageNo*count, count, true);
			}
			setEndReached(listOfPhotos.size() == 0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}

	/*public String addNewPhoto(){
		String ngoUid = ""+sessionMap.get("userCode");
		
	}*/
	public String uploadPhotosAction() throws JSONException, JsonParseException, IOException
	{
		Integer ngoUid = Integer.parseInt(""+sessionMap.get("userCode"));
		
		Integer owner = 1;
		String cat = "";
		if(from.equals("ngo"))
		{
			owner = ngoUid;
			cat = "ngoOthers";
		}
		if(from.equals("event"))
		{
			owner = eventId;
			cat = "event";
		}
		try(Connection con = DBConnection.getConnection()) {
			HttpServletRequest request = ServletActionContext.getRequest();
			String upPhotos = request.getParameter("upPhotos");
			Gson g = new Gson();
			PhotoBean[] pbArr = g.fromJson(upPhotos, PhotoBean[].class);
			for(int i = 0; i<pbArr.length; i++){
				PhotoBean upPhoto = pbArr[i];
				upPhoto.setOwnerId(owner);
				upPhoto.setCategory(cat);
				PhotoDao.create(con, upPhoto);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	public String ajaxDeletePhotoAction()
	{
		try(Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false);
			String[] arr = deletePhotosIdArray.split(",");
			arr = PhotoDao.getPublicIds(con, arr);
			PhotoDao.deletePhotos(con, arr);
			try {
				CloudinaryUtils.deleteImages(arr, null);
			} catch (Exception e) {
				ajaxResponseDummyMsg = "Deletion Failed";
				con.rollback();
			}
			con.commit();
			ajaxResponseDummyMsg = "Successfully deleted!";
			con.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	public String ajaxAddToCoverAction(){
		try(Connection conn = DBConnection.getConnection()) {
			PhotoDao.addToCover(conn, addToSlideshow, ""+sessionMap.get("userCode"));
			ajaxResponseDummyMsg = "Selected images are added to your Profile Slideshow!";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}

	public String ajaxRemoveFromCoverAction(){
		try(Connection conn = DBConnection.getConnection()) {
			PhotoDao.removeFromCover(conn, removeFromSlideshow, ""+sessionMap.get("userCode"));
			ajaxResponseDummyMsg = "Selected images are removed from your Profile Slideshow!";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	

	public Integer getPageOwnerCode() {
		return pageOwnerCode;
	}

	public void setPageOwnerCode(Integer pageOwnerCode) {
		this.pageOwnerCode = pageOwnerCode;
	}
	public static String getDestinationPath(String category, String ngoUid, String eventId){
		String rootpath = ConfigConstants.get("ROOTPATH")+"/images/";
		return category.startsWith("ngo")?rootpath+ngoUid+"/":category.startsWith("event")?rootpath+ngoUid+"/"+eventId:rootpath+"volunteers"+"/";
	}
	
	public List<PhotoBean> getListOfPhotos() {
		return listOfPhotos;
	}

	public void setListOfPhotos(List<PhotoBean> listOfPhotos) {
		this.listOfPhotos = listOfPhotos;
	}

	@Override
	public void setSession(Map<String, Object> sessionMap) {
		// TODO Auto-generated method stub
		this.sessionMap = (SessionMap) sessionMap;
	}

	public File[] getImgFile() {
		return imgFile;
	}

	public void setImgFile(File[] imgFile) {
		this.imgFile = imgFile;
	}

	public String[] getImgFileContentType() {
		return imgFileContentType;
	}

	public void setImgFileContentType(String[] imgFileContentType) {
		this.imgFileContentType = imgFileContentType;
	}

	public String[] getImgFileFileName() {
		return imgFileFileName;
	}

	public void setImgFileFileName(String[] imgFileFileName) {
		this.imgFileFileName = imgFileFileName;
	}

	public int getPhotoId() {
		return photoId;
	}

	public void setPhotoId(int photoId) {
		this.photoId = photoId;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String[] getAddToSlideshow() {
		return addToSlideshow;
	}

	public void setAddToSlideshow(String[] addToSlideshow) {
		this.addToSlideshow = addToSlideshow;
	}

	public String getDeletePhotosIdArray() {
		return deletePhotosIdArray;
	}

	public void setDeletePhotosIdArray(String deletePhotosIdArray) {
		this.deletePhotosIdArray = deletePhotosIdArray;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String[] getRemoveFromSlideshow() {
		return removeFromSlideshow;
	}

	public void setRemoveFromSlideshow(String[] removeFromSlideshow) {
		this.removeFromSlideshow = removeFromSlideshow;
	}
	public String getImgPageNo() {
		return imgPageNo;
	}
	public void setImgPageNo(String imgPageNo) {
		this.imgPageNo = imgPageNo;
	}
	public boolean isEndReached() {
		return endReached;
	}
	public void setEndReached(boolean endReached) {
		this.endReached = endReached;
	}
	public String getAjaxResponseDummyMsg() {
		return ajaxResponseDummyMsg;
	}
	public void setAjaxResponseDummyMsg(String ajaxResponseDummyMsg) {
		this.ajaxResponseDummyMsg = ajaxResponseDummyMsg;
	}

	public PhotoBean[] getUpPhoto() {
		return upPhotos;
	}

	public void setUpPhoto(PhotoBean[] upPhotos) {
		this.upPhotos = upPhotos;
	}
}
