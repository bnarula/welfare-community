package action;

import constants.ResultConstants;
import util.AutoMailer;

public class MailAction {

	private String password;
	private String city;
	private int limit;
	private int start;
	private String email;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private String bcc;
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String sendPromoMails(){
		if(password.equals("08-Jan-2014")){
			if(!"".equals(email))
				AutoMailer.send(email, name, bcc);
			else
				AutoMailer.send(city, limit, start);
		}
		return ResultConstants.SUCCESS;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
}
