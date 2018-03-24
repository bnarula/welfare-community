package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
class NGOCause {
	String email;
	Integer causeCode;
	Integer ngoCode;
	public NGOCause(String email, Integer causeCode) {
		super();
		this.email = email;
		this.causeCode = causeCode;
	}
	
}
public class Migration {
	static ArrayList<NGOCause> list = new ArrayList<NGOCause>();
	static ArrayList<Integer> lostCauses = new ArrayList<Integer>();
	public static void main(String[] args) {
		try {
			lostCauses0();
			lostCauses1();
			lostCauses2();
			lostCauses3();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*ArrayList<NGOCause> temp = new ArrayList<NGOCause>();
		NGOCause nc = new NGOCause("abc", 2);
		temp.add(nc);
		NGOCause nc2 = temp.iterator().next();
		nc2.ngoCode = 33;
		NGOCause nc3 = temp.iterator().next();
		System.out.println(nc3.causeCode +"----"+ nc3.email +"----"+ nc3.ngoCode);*/
		
		
	}
	public static void lostCauses0() throws SQLException, ClassNotFoundException {
		System.out.println("running part 1\n\n\n");
		try {
			Class.forName("com.mysql.jdbc.Driver"); 
			Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/welfarecommunity","root","root");  
			PreparedStatement stmt = null;
			int i = 1;
			while(i<36){
				stmt = con.prepareStatement("select count(*) as c from ngo_causes_table where nct_cause_code_fk="+i);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					if(rs.getInt("c") == 0){
						lostCauses.add(i);
						System.out.println("inserting.."+i);
					}
					i++;
				}
			}
			con.close();

		} catch(SQLException sqle) {

		}
		System.out.println("\n\n\nfinish part 1 with "+lostCauses.size()+" objects");
	}
	public static void lostCauses1() throws SQLException, ClassNotFoundException {
		System.out.println("running part 1\n\n\n");
		try {
			Class.forName("com.mysql.jdbc.Driver"); 
			Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/ngocrawleddata","root","root");  
			PreparedStatement stmt = null;
			stmt = con.prepareStatement("select nct_cause_code_fk, nct_ngo_uid_fk, ngo_email from ngo_causes_table "
					+ "join ngos_table on nct_ngo_uid_fk = ngo_uid where nct_cause_code_fk in "+(lostCauses.toString().replace("[", "(").replace("]", ")")));
			ResultSet rs = stmt.executeQuery();
			int i = 1;
			while(rs.next()){
				list.add(new NGOCause(rs.getString("ngo_email"), rs.getInt("nct_cause_code_fk")));
				System.out.println("inserting.."+i++);
			}
			con.close();

		} catch(SQLException sqle) {

		}
		
		System.out.println("\n\n\nfinish part 1 with "+list.size()+" objects");
	}

	public static void lostCauses2() throws SQLException, ClassNotFoundException {
		System.out.println("starting part 2\n\n\n");
		try {
			Class.forName("com.mysql.jdbc.Driver"); 
			Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/welfarecommunity","root","root");  
			PreparedStatement stmt = null;
			Iterator<NGOCause> iter = list.iterator();
			int i = 1;
			while(iter.hasNext()){
				NGOCause nc = iter.next();
				String email = nc.email;
				stmt = con.prepareStatement("select ngo_email, ngo_uid from ngos_table where ngo_email = ?");
				stmt.setString(1, email);
				ResultSet rs = stmt.executeQuery();
				
				if(rs.next()){
					nc.ngoCode = rs.getInt("ngo_uid");
					System.out.println("setting ngoCode.."+i++);
				}
			}
			con.close();

		} catch(SQLException sqle) {

		}
		System.out.println("\n\n\n\nfinish part 2 with "+list.size()+" objects");
	}
	public static void lostCauses3() throws SQLException, ClassNotFoundException {
		System.out.println("starting part 3");
		try {
			Class.forName("com.mysql.jdbc.Driver"); 
			Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/temp_cause_mig","root","root");  
			PreparedStatement stmt = null;
			Iterator<NGOCause> iter = list.iterator();
			while(iter.hasNext()){
				NGOCause nc = iter.next();
				Integer ngoCode = nc.ngoCode;
				Integer causeCode = nc.causeCode;
				stmt = con.prepareStatement("insert into ngo_causes_table(nct_ngo_uid_fk, nct_cause_code_fk, nct_ngo_type) values(?,?,?)");
				stmt.setInt(1, ngoCode);
				stmt.setInt(2,causeCode);
				stmt.setString(3, "auto");
				try {
					stmt.execute();
					System.out.println("new insertion for cause -"+causeCode);
				} catch(SQLException sqle) {
					System.out.println(sqle.getMessage());
				}
			}
			con.close();

		} catch(SQLException sqle) {

		}
	}
}