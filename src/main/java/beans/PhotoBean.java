package beans;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class PhotoBean {

	private Integer id;
	private String publicId;
	private String url;
	private String thumbUrl;
	private String fileName;
	private Date createdAt;
	private String category;
	private Integer ownerId;
	private boolean cover;
	public static final String PHOTOBEAN_TYPE_POST = "AU";
	public static final String PHOTOBEAN_TYPE_EVENT = "event";
	public static final String PHOTOBEAN_TYPE_EVENT_DP = "eventDp";
	public static final String PHOTOBEAN_TYPE_NGO_LOGO = "ngoLogo";
	public static final String PHOTOBEAN_TYPE_NGO = "ngoOthers";
	//public static final String PHOTOBEAN_TYPE_EVENT_DP = "eventDp";
	public PhotoBean(){
		
	}
	public PhotoBean(Map<String, String> result, String category, Integer owner){
		DateFormat df = new SimpleDateFormat(); 
		Date createdAt = new Date();
		try {
			createdAt = df.parse(result.get("created_at"));
		} catch (ParseException e) {
		    //e.printStackTrace();
		    createdAt = new Date();
		}
		String url = result.get("secure_url");
		String thumbUrl = url.replace("upload/", "upload/c_limit,h_60,w_90/");
		this.id = 0; 
		this.publicId = result.get("public_id");
		this.url = url;
		this.thumbUrl = thumbUrl;
		this.createdAt = createdAt;
		this.fileName = result.get("original_filename");
		this.category = category;
		this.ownerId = owner;
	}
	public PhotoBean(Integer id, String url) {
		super();
		this.id = id;
		this.url = url;
	}

	public PhotoBean(Integer id, String publicId, String url, String thumbUrl, String fileName, Date createdAt,
			String category, Integer ownerId) {
		super();
		this.id = id;
		this.publicId = publicId;
		this.url = url;
		this.thumbUrl = thumbUrl;
		this.fileName = fileName;
		this.createdAt = createdAt;
		this.category = category;
		this.ownerId = ownerId;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	public boolean isCover() {
		return cover;
	}
	public void setCover(boolean cover) {
		this.cover = cover;
	}

	
}
