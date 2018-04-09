package action;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.SessionAware;

import beans.AboutUsBean;
import beans.PhotoBean;
import config.DBConnection;
import constants.Constants;
import constants.ResultConstants;
import dao.AboutUsDao;
import dao.EventDao;
import dao.NgoDao;
import dao.PhotoDao;
import util.CloudinaryUtils;
import util.ImageUtil;

public class AboutUsAction implements SessionAware {

	private SessionMap<String, Object> sessionMap;
	
	private List<AboutUsBean> aboutUsList = new ArrayList<AboutUsBean>();
	private AboutUsBean aboutUsBean = new AboutUsBean();
	private String newAboutUsHeading;
	private String newAboutUsContent;
	private int newAboutUsCode;
	private String newAboutUsPhoto;
	private long newAboutUsCreatedOn;
	private File imgFile;
	private String imgFileContentType;
	private String imgFileFileName;
	
	private Integer toBeUpdatedCode;
	private Integer toBeDeletedCode;
	private String toBePinnedCode;
	
	private String ajaxResponseDummyMsg;
	
	private Integer pageOwnerCode;
	
	private String defaultContent;
	
	public String ajaxUpdateThisAboutUs()
	{
		try(Connection conn = DBConnection.getConnection()) {
			Calendar createdOn = new GregorianCalendar();
			setNewAboutUsCreatedOn(createdOn.getTimeInMillis());
			
			Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
			if(AboutUsDao.updateThisAboutUs(conn, toBeUpdatedCode, newAboutUsHeading, newAboutUsContent, createdOn, userCode))
			{
				if (imgFile!=null) {
					String existingLogoId = AboutUsDao.getPhotoPublicId(conn, toBeUpdatedCode); 
			    	CloudinaryUtils.deleteImage(existingLogoId, null);
			    	Map<String, String> result = CloudinaryUtils.uploadImage(imgFile, null);
			    	PhotoBean upPhoto = new PhotoBean(result, "AU", toBeUpdatedCode);
					PhotoDao.update(conn, upPhoto, existingLogoId);
					newAboutUsPhoto = result.get("secure_url");
				}
				ajaxResponseDummyMsg = "Successfully Updated!";
				return ResultConstants.SUCCESS;
			}
			else 
			{
				ajaxResponseDummyMsg = "Failed to Update!";
				return ResultConstants.FAILURE;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.FAILURE;
	}
	public String ajaxDeleteThisAboutUs()
	{
		try(Connection conn = DBConnection.getConnection()) {
			Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
			if(AboutUsDao.deleteThisAboutUs(conn, toBeDeletedCode, userCode))
			{
				List<String> list = PhotoDao.deleteAllPhotos(conn, PhotoBean.PHOTOBEAN_TYPE_POST, toBeDeletedCode);
				CloudinaryUtils.deleteImages(list, null);
				//TODO delete photo from cloud
				ajaxResponseDummyMsg = "Successfully Deleted!";
				return ResultConstants.SUCCESS;
			}
			else 
				return ResultConstants.FAILURE;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.FAILURE;
	}
	
	
	public String ajaxSaveNewAboutUs()
	{
		Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
		try(Connection conn = DBConnection.getConnection()) {
			Calendar createdOn = new GregorianCalendar();
			setNewAboutUsCreatedOn(createdOn.getTimeInMillis());
			int result = AboutUsDao.addNewAboutUs(conn, newAboutUsHeading, newAboutUsContent, ""+userCode, createdOn);
			if(result!=-1)
			{
				setNewAboutUsCode(result);
				newAboutUsPhoto = saveImgFile(conn, result);
				ajaxResponseDummyMsg = "Successfully Added!";
				aboutUsBean = new AboutUsBean(result, ""+userCode, newAboutUsHeading, newAboutUsContent, newAboutUsPhoto, createdOn, false);
				return ResultConstants.SUCCESS;
			}
			else 
				return ResultConstants.FAILURE;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ResultConstants.FAILURE;
	}
	public String ajaxPinThisAboutUs()
	{
		Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
		try(Connection conn = DBConnection.getConnection()) {
			AboutUsDao.pinThisAboutUs(conn, toBePinnedCode, (short)1, ""+userCode);
			ajaxResponseDummyMsg = "Post Pinned!";
			return ResultConstants.SUCCESS;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ResultConstants.FAILURE;
	}
	public String ajaxRemovePinThisAboutUs()
	{
		Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
		try(Connection conn = DBConnection.getConnection()) {
			AboutUsDao.pinThisAboutUs(conn, toBePinnedCode, (short)0, ""+userCode);
			ajaxResponseDummyMsg = "Pin removed!";
			return ResultConstants.SUCCESS;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.FAILURE;
	}
	private String saveImgFile(Connection con, Integer auId) throws SQLException, IOException {
		String url = "";
		Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
		try {
			Map<String, String> result = CloudinaryUtils.uploadImage(imgFile, null);
			url = result.get("secure_url");
			PhotoBean upPhoto = new PhotoBean(result, "AU", auId);
			int pId = PhotoDao.create(con, upPhoto);
		} catch (NullPointerException npe) {
			
		}
		return url;
	}
	
	@Override
	public void setSession(Map<String, Object> session) {
		// TODO Auto-generated method stub
		this.sessionMap = (SessionMap<String, Object>)session;
	}
	public List<AboutUsBean> getAboutUsList() {
		return aboutUsList;
	}
	public void setAboutUsList(List<AboutUsBean> aboutUsList) {
		this.aboutUsList = aboutUsList;
	}
	public AboutUsBean getAboutUsBean() {
		return aboutUsBean;
	}
	public void setAboutUsBean(AboutUsBean aboutUsBean) {
		this.aboutUsBean = aboutUsBean;
	}
	public String getNewAboutUsHeading() {
		return newAboutUsHeading;
	}
	public void setNewAboutUsHeading(String newAboutUsHeading) {
		this.newAboutUsHeading = newAboutUsHeading;
	}
	public String getNewAboutUsContent() {
		return newAboutUsContent;
	}
	public void setNewAboutUsContent(String newAboutUsContent) {
		this.newAboutUsContent = newAboutUsContent;
	}
	public String getAjaxResponseDummyMsg() {
		return ajaxResponseDummyMsg;
	}
	public void setAjaxResponseDummyMsg(String ajaxResponseDummyMsg) {
		this.ajaxResponseDummyMsg = ajaxResponseDummyMsg;
	}
	public Integer getToBeUpdatedCode() {
		return toBeUpdatedCode;
	}
	public void setToBeUpdatedCode(Integer toBeUpdatedCode) {
		this.toBeUpdatedCode = toBeUpdatedCode;
	}
	public Integer getToBeDeletedCode() {
		return toBeDeletedCode;
	}
	public void setToBeDeletedCode(Integer toBeDeletedCode) {
		this.toBeDeletedCode = toBeDeletedCode;
	}
	public int getNewAboutUsCode() {
		return newAboutUsCode;
	}
	public void setNewAboutUsCode(int newAboutUsCode) {
		this.newAboutUsCode = newAboutUsCode;
	}
	public Integer getPageOwnerCode() {
		return pageOwnerCode;
	}
	public void setPageOwnerCode(Integer pageOwnerCode) {
		this.pageOwnerCode = pageOwnerCode;
	}
	public String getNewAboutUsPhoto() {
		return newAboutUsPhoto;
	}
	public void setNewAboutUsPhoto(String newAboutUsPhoto) {
		this.newAboutUsPhoto = newAboutUsPhoto;
	}
	public String getDefaultContent() {
		return defaultContent;
	}
	public void setDefaultContent(String defaultContent) {
		this.defaultContent = defaultContent;
	}
	public File getImgFile() {
		return imgFile;
	}
	public void setImgFile(File imgFile) {
		this.imgFile = imgFile;
	}
	public String getImgFileContentType() {
		return imgFileContentType;
	}
	public void setImgFileContentType(String imgFileContentType) {
		this.imgFileContentType = imgFileContentType;
	}
	public String getImgFileFileName() {
		return imgFileFileName;
	}
	public void setImgFileFileName(String imgFileFileName) {
		this.imgFileFileName = imgFileFileName;
	}
	public long getNewAboutUsCreatedOn() {
		return newAboutUsCreatedOn;
	}
	public void setNewAboutUsCreatedOn(long newAboutUsCreatedOn) {
		this.newAboutUsCreatedOn = newAboutUsCreatedOn;
	}
	public String getToBePinnedCode() {
		return toBePinnedCode;
	}
	public void setToBePinnedCode(String toBePinnedCode) {
		this.toBePinnedCode = toBePinnedCode;
	}
	
}
