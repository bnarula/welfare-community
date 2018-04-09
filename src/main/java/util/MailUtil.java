package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

import beans.NgoBean;
import config.DBConnection;
import constants.Constants;
import security.SecurityUtil;

public class MailUtil {
	public static ClientResponse SendSimpleMessage(String toEmail, String toName, String subject, String msg) {
	    Client client = Client.create();
	    client.addFilter(new HTTPBasicAuthFilter("api",
	                "key-dfef560674b4ec55206662779a98bc22"));
	    WebResource webResource =
	        client.resource("https://api.mailgun.net/v3/admin.welfarecommunity.org/messages");
	    MultivaluedMapImpl formData = new MultivaluedMapImpl();
	    formData.add("from", "Welfare Community <postmaster@admin.welfarecommunity.org>");
	    formData.add("to", toName+" <"+toEmail+">");
	    formData.add("subject", subject);
	    formData.add("text", msg);
	    return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
	                                                post(ClientResponse.class, formData);
	}
	public static ClientResponse GetValidate() {
	       Client client = new Client();
	       client.addFilter(new HTTPBasicAuthFilter("api",
	                       "pubkey-5ogiflzbnjrljiky49qxsiozqef5jxp7"));
	       WebResource webResource =
	               client.resource("https://api.mailgun.net/v3" +
	                               "/address/validate");
	       MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
	       queryParams.add("address", "foo@mailgun.net");
	       return webResource.queryParams(queryParams).get(ClientResponse.class);
	}
	public static ClientResponse sendMimeMessage(String toEmail,  String subject, String msg) {
	       Client client = Client.create();
	       ClientConfig cc = new DefaultClientConfig();

	       cc.getClasses().add(MultiPartWriter.class);
	       client = Client.create(cc);
	       client.addFilter(new HTTPBasicAuthFilter("api","key-dfef560674b4ec55206662779a98bc22"));
	       final WebResource webResource =
	               client.resource("https://api.mailgun.net/v3/welfarecommunity.org/"+"messages");
	       final FormDataMultiPart form = new FormDataMultiPart();
	       form.field("from", "Welfare Community <postmaster@welfarecommunity.org>");
	       form.field("to", toEmail);
	       form.field("subject", subject);
	       form.field("html", msg);
	       Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, form);
						System.out.println("mail sending complete");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t1.start();
	       return null;
	}
	public static ClientResponse sendMimeMessage(String toEmail, String bcc, String subject, String msg) {
	       Client client = Client.create();
	       ClientConfig cc = new DefaultClientConfig();
	       cc.getClasses().add(MultiPartWriter.class);
	       client = Client.create(cc);
	       client.addFilter(new HTTPBasicAuthFilter("api","key-dfef560674b4ec55206662779a98bc22"));
	       WebResource webResource =
	               client.resource("https://api.mailgun.net/v3/welfarecommunity.org/"+"messages");
	       FormDataMultiPart form = new FormDataMultiPart();
	       form.field("from", "Welfare Community <postmaster@admin.welfarecommunity.org>");
	       form.field("to", toEmail);
	       form.field("bcc", bcc);
	       form.field("subject", subject);
	       form.field("html", msg);
	       return webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, form);
	}
	public static String readMailHTML(String mailCode){
		String html = "";
		
		String fileName = "";
		if(mailCode.equalsIgnoreCase("welcomeNGO"))
			fileName = Constants.HTML_FILE_WELCOME_NGO;
		if(mailCode.equalsIgnoreCase("forgotPassword"))
			fileName = Constants.HTML_FILE_FORGOT_PASSWORD;
		if(mailCode.equalsIgnoreCase("welcomeVolunteer"))
			fileName = Constants.HTML_FILE_WELCOME_VOLUNTEER;
		if(mailCode.equalsIgnoreCase("appAccept"))
			fileName = Constants.HTML_FILE_APPLICATION_ACCEPT;
		if(mailCode.equalsIgnoreCase("appReject"))
			fileName = Constants.HTML_FILE_APPLICATION_REJECT;
		if(mailCode.equalsIgnoreCase("autoMail"))
			fileName = Constants.HTML_FILE_AUTO_MAIL;
		if(mailCode.equalsIgnoreCase("promoMail"))
			fileName = Constants.HTML_FILE_PROMO_MAIL;
		if(mailCode.equalsIgnoreCase("accountDetailsMail"))
			fileName = Constants.HTML_FILE_ACCOUNT_DETAILS_MAIL;
		File htmlFile = new File(fileName);
		try(FileInputStream fis = new FileInputStream(htmlFile)){
			int c = 0;
			while((c=fis.read())!=-1){
				html+=(char)c;
			}
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		return html;
	}
	
	public static void createMailingLists(){
		System.out.println("Started to create Mailing Lists");
		Connection con;
		int lSize = 6000;
		int i = 1;
		try {
			while(true){
				con = DBConnection.getConnection();
				PreparedStatement pStmt = con.prepareStatement("select ngo_uid, ngo_type from ngos_table where ngo_type = 'auto' order by ngo_uid limit "+i*lSize+", "+ lSize);
				i++;
				ResultSet rs = pStmt.executeQuery();
				boolean isRSEmpty = true;
				System.out.println("got the list of auto ngos-"+i);
				int j = 1;
				while (rs.next()) {
					
					if(isRSEmpty){
						PreparedStatement pStmt2 = con.prepareStatement("insert into mailing_list_table(id) values(?)");
						pStmt2.setInt(1, i);
						pStmt2.execute();
						pStmt2.close();
						System.out.println("new list created- "+i);
					}
					isRSEmpty = false;
					PreparedStatement pStmt2 = con.prepareStatement("insert into ml_member_table(ngo_uid, ml_id) values(?,?)");
					pStmt2.setInt(1, rs.getInt("ngo_uid"));
					pStmt2.setInt(2, i);
					pStmt2.execute();
					pStmt2.close();
					System.out.println(j++ +"th member added to list- "+i);
				}
				System.out.println(j+" members added to the list- "+i);
				rs.close();
				pStmt.close();
				if(isRSEmpty){
					System.out.println("no more members left to add, total lists created - "+i);
					break;
				}
			}
			System.out.println();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public static void addUsersToMailingLists(){
		/*Client client = Client.create();
       ClientConfig cc = new DefaultClientConfig();
       cc.getClasses().add(MultiPartWriter.class);
       client = Client.create(cc);
       client.addFilter(new HTTPBasicAuthFilter("api","key-dfef560674b4ec55206662779a98bc22"));*/
	    Connection con = null;
	    String mlPrefix = "ml_";
	    String mlSuffic = "@welfarecommunity.org";
	    try {
	    	con = DBConnection.getConnection();
	    	con.setAutoCommit(false);
	    	int mlId = 1;
	    	while(mlId < 16) {
	    		int i = 0;
				while(true){
					JsonArray arr = new JsonArray();
					PreparedStatement pStmt = con.prepareStatement("select mlt.id, mlt.ngo_uid, mlt.ml_id, nt.ngo_name, nt.ngo_email from ml_member_table mlt "
							+ "join ngos_table nt on mlt.ngo_uid = nt.ngo_uid where ml_id = "+mlId+" order by id limit "+i*1000+", 1000");
					ResultSet rs = pStmt.executeQuery();
					boolean hasRS = false;
					while(rs.next()){
						hasRS = true;
						int ngoId = rs.getInt("mlt.ngo_uid");
						String email = rs.getString("nt.ngo_email");
						String name = rs.getString("nt.ngo_name");
						String passcode = SecurityUtil.encrypt(email);
						JsonObject obj = new JsonObject();
						obj.addProperty("name", name);
						obj.addProperty("address", email);
						obj.addProperty("subscribed", true);
						JsonObject vars = new JsonObject();
						vars.addProperty("passcode", passcode);
						vars.addProperty("uid", ngoId);
						obj.add("vars", vars);
						arr.add(obj);
						PreparedStatement pStmt2 = con.prepareStatement("insert into passcode_table(pt_user_code_fk, pt_user_passcode, pt_type) values(?,?,?)");
						pStmt2.setInt(1, ngoId);
						pStmt2.setString(2, passcode);
						pStmt2.setString(3, "claim");
						pStmt2.execute();
						pStmt2.close();
						
					}
					pStmt.close();
					if(!hasRS)
						break;
					Gson gson = new Gson();
					String output = gson.toJson(arr);
					HttpResponse <String> request = Unirest.post("https://api.mailgun.net/v3/lists/"+mlPrefix
							+mlId+mlSuffic+"/members.json")
							.header("accept", "application/json")
		                    .basicAuth("api", "key-dfef560674b4ec55206662779a98bc22")
		                   .field("upsert", true)
		                   .field("members", output).asString();

					System.out.println(request.getBody());
					i++;
				}
				mlId++;
	    	}
	    	con.commit();
	    }
	    catch(Exception e){
	    	try {
	    		e.printStackTrace();
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
	    } 
		
	}
	public static void main(String arg[]){
		sendAutoMails();
	}
	public static void sendAutoMails() {
		String msg = readMailHTML("autoMail");
		sendMimeMessage("test@welfarecommunity.org", "Welcome to Welfare Community - your free social profile..", msg);
		
	}
	public static void sendAccountDetails(NgoBean ngoBean, String password) {
		String msg = readMailHTML("accountDetailsMail");
		msg = msg.replaceAll("%#ngoName#%", ngoBean.getNgoName());
		msg = msg.replaceAll("%#email#%", ngoBean.getNgoEmail());
		msg = msg.replaceAll("%#password#%", password);
		sendMimeMessage(ngoBean.getNgoEmail(), "Account Details for Welfare Community", msg);
		
	}
	    
}
