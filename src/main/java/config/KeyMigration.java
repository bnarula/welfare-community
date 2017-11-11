package config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import util.Constants;

public class KeyMigration {

	public static void main(String args[]) throws SQLException{
		
		correctDele();
	}
	public static void readDeliveries1() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ipl?user=root&password=root")){
			conn.setAutoCommit(false);
			PreparedStatement stmtSelect0 = conn.prepareStatement("select batsman, non_striker, bowler, fielder from deliveries_dump limit 0,3000");
			ResultSet rs0 = stmtSelect0.executeQuery();
			while(rs0.next()){
				String batsman = "", non_striker = "", bowler = "", fielder = "";
				batsman= rs0.getString(1);
				non_striker= rs0.getString(2);
				bowler= rs0.getString(3);
				fielder= rs0.getString(4);
				HashSet<String> players = new HashSet<String>();
				if(!players.contains(batsman)){
					String seasonSQL = "insert into player(name) values(?)";
					PreparedStatement seasonPSQL = conn.prepareStatement(seasonSQL);
					seasonPSQL.setString(1, batsman);
					players.add(batsman);
					seasonPSQL.execute();
					seasonPSQL.close();
				}
				if(!players.contains(non_striker)){
					String seasonSQL = "insert into player(name) values(?)";
					PreparedStatement seasonPSQL = conn.prepareStatement(seasonSQL);
					seasonPSQL.setString(1, non_striker);
					players.add(non_striker);
					seasonPSQL.execute();
					seasonPSQL.close();
				}
				if(!players.contains(bowler)){
					String seasonSQL = "insert into player(name) values(?)";
					PreparedStatement seasonPSQL = conn.prepareStatement(seasonSQL);
					seasonPSQL.setString(1, bowler);
					players.add(bowler);
					seasonPSQL.execute();
					seasonPSQL.close();
				}
				if(!players.contains(fielder)){
					String seasonSQL = "insert into player(name) values(?)";
					PreparedStatement seasonPSQL = conn.prepareStatement(seasonSQL);
					seasonPSQL.setString(1, fielder);
					players.add(fielder);
					seasonPSQL.execute();
					seasonPSQL.close();
				}
				
			}
			rs0.close();
			stmtSelect0.close();
			
			conn.commit();
			conn.close();
			}
			catch(SQLException sqle){
				sqle.printStackTrace();
			}
			
		}
	public static void correctMatchID() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ipl?user=root&password=root")){
			conn.setAutoCommit(false);
			int newI = 1;
			int oldI = 2015;
			while(newI<577){
				PreparedStatement psTeams = conn.prepareStatement("update match_data set id = "+newI+" where id ="+oldI);
				psTeams.executeUpdate();
				psTeams.close();
				newI++;
				oldI++;
			}
			conn.commit();
			conn.close();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
		}
		
	}
	public static void correctDele() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ipl?user=root&password=root")){
			conn.setAutoCommit(false);
			
			/*HashMap<String, Integer> teams = new HashMap<String, Integer>();
			PreparedStatement psTeams = conn.prepareStatement("select * from team");
			ResultSet rsTeams = psTeams.executeQuery();
			while(rsTeams.next()){	
				teams.put(rsTeams.getString(2), rsTeams.getInt(1));
			}
			rsTeams.close();
			psTeams.close();
			
			
			HashMap<String, Integer> players = new HashMap<String, Integer>();
			PreparedStatement psPlayers = conn.prepareStatement("select id, name from player");
			ResultSet rsPlayers = psPlayers.executeQuery();
			while(rsPlayers.next()){	
				players.put(rsPlayers.getString(2), rsPlayers.getInt(1));
			}
			rsPlayers.close();
			psPlayers.close();
			
			HashMap<Integer, Integer> seasons = new HashMap<Integer, Integer>();
			PreparedStatement psSeasons = conn.prepareStatement("select id, year from season");
			ResultSet rsSeasons = psSeasons.executeQuery();
			while(rsSeasons.next()){	
				seasons.put(rsSeasons.getInt(2), rsSeasons.getInt(1));
			}
			rsSeasons.close();
			psSeasons.close();*/
			
			
			
			int i = 1;
			while(i<=136598){
				/*PreparedStatement psDELE = conn.prepareStatement("update deliveries_data d "
						+"join team t1 on t1.name = d.bat_team_id "
						+ "join team t2 on t2.name = d.bowl_team_id "
						+ "join player p1 on p1.name = d.batsman_id "
						+ "join player p2 on p2.name = d.non_striker_id "
						+ "join player p3 on p3.name = d.bowler_id "
						+ " set d.bat_team_id = t1.id, "
						+ " d.bowl_team_id = t2.id,"
						+ " d.batsman_id = p1.id,"
						+ " d.non_striker_id = p2.id,"
						+ " d.bowler_id = p3.id "
						+ "where d.id = "+i);
				psDELE.executeUpdate();
				psDELE.close();*/
				
				PreparedStatement psDELE = conn.prepareStatement("update deliveries_data d "
						+ "join player p4 on p4.name = trim(substring_index(d.fielder_id, '(sub)', 1)) "
						+ " set d.fielder_id = p4.id "
						+ "where d.id = "+i+" and locate('(sub)',d.fielder_id) <> 0");
				psDELE.executeUpdate();
				psDELE.close();
				i++;
				
			}
			conn.commit();
			conn.close();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
		}
		
	}
		
	public static void readMatches() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ipl?user=root&password=root")){
			conn.setAutoCommit(false);
			HashMap<String, Integer> teams = new HashMap<String, Integer>();
			PreparedStatement psTeams = conn.prepareStatement("select * from team");
			ResultSet rsTeams = psTeams.executeQuery();
			while(rsTeams.next()){	
				teams.put(rsTeams.getString(2), rsTeams.getInt(1));
			}
			rsTeams.close();
			psTeams.close();
			
			
			HashMap<String, Integer> players = new HashMap<String, Integer>();
			PreparedStatement psPlayers = conn.prepareStatement("select id, name from player");
			ResultSet rsPlayers = psPlayers.executeQuery();
			while(rsPlayers.next()){	
				players.put(rsPlayers.getString(2), rsPlayers.getInt(1));
			}
			rsPlayers.close();
			psPlayers.close();
			
			HashMap<Integer, Integer> seasons = new HashMap<Integer, Integer>();
			PreparedStatement psSeasons = conn.prepareStatement("select id, year from season");
			ResultSet rsSeasons = psSeasons.executeQuery();
			while(rsSeasons.next()){	
				seasons.put(rsSeasons.getInt(2), rsSeasons.getInt(1));
			}
			rsSeasons.close();
			psSeasons.close();
		
			PreparedStatement stmtSelect1 = conn.prepareStatement("select * from matches_dump");
			ResultSet rs = stmtSelect1.executeQuery();
			while(rs.next()){
				int season, win_by_runs, win_by_wickets;
				java.sql.Date playedOn;
				String city, team1, team2, toss_winner, toss_decision, result,
				winner, player_of_match, venue, umpire1, umpire2, umpire3;
				int id = rs.getInt(1);
				season= rs.getInt(2);
				city= rs.getString(3);
				playedOn = rs.getDate(4);
				team1= rs.getString(5);
				team2= rs.getString(6);
				toss_winner= rs.getString(7);
				toss_decision= rs.getString(8);
				result= rs.getString(9);
				winner= rs.getString(11);
				win_by_runs= rs.getInt(12);
				win_by_wickets= rs.getInt(13);
				player_of_match = rs.getString(14);
				venue = rs.getString(15);
				umpire1 = rs.getString(16);
				umpire2 = rs.getString(17);
				umpire3 = rs.getString(18);
				if(umpire3==null)
					umpire3 = "";
				//season_id team1_id, team2_id played_on toss_team_id venue toss_decision winner_team_id win_by_runs win_by_wickets player_of_match_id city umpire1 umpire2
				
				String matchSQL = "insert into match_data(season_id, team1_id, team2_id, played_on,"
						+ " toss_team_id, venue, toss_decision, result,"
						+ " winner_team_id, win_by_runs, win_by_wickets, player_of_match_id,"
						+ " city, umpire1, umpire2, umpire3) "
						+ "values(?,?,?,?"
						+ ",?,?,?,?"
						+ ",?,?,?,?"
						+ ",?,?,?,?)";
				PreparedStatement psMatch = conn.prepareStatement(matchSQL);
				psMatch.setInt(1, seasons.get(season));
				psMatch.setInt(2, teams.get(team1));
				psMatch.setInt(3, teams.get(team2));
				psMatch.setDate(4, playedOn);
				psMatch.setInt(5, teams.get(toss_winner));
				psMatch.setString(6, venue);
				psMatch.setString(7, toss_decision);
				psMatch.setString(8, result);
				if(winner==null)
				{
					psMatch.setNull(9, java.sql.Types.INTEGER);
				} else {
					psMatch.setInt(9, teams.get(winner));
				}
			
				psMatch.setInt(10, win_by_runs);
				psMatch.setInt(11, win_by_wickets);
				
				if(player_of_match==null || player_of_match.equals(""))
				{
					psMatch.setNull(12, java.sql.Types.VARCHAR);
				} else {
					psMatch.setInt(12, players.get(player_of_match));
				}
				if(city==null || city.equals(""))
				{
					psMatch.setString(13, "-");
				} else {
					psMatch.setString(13, city);
				}
				psMatch.setString(14, umpire1);
				psMatch.setString(15, umpire2);
				psMatch.setString(16, umpire3);
				psMatch.execute();
				System.out.println("writing for match "+id);	
				
			}
			rs.close();
			stmtSelect1.close();
			
			conn.commit();
			conn.close();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
		}
		
	}
	public static void migrateAutoNgos() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/welfarecommunity?user=root&password=root")){
			conn.setAutoCommit(false);
			PreparedStatement stmtSelect = conn.prepareStatement("select ngo_uid, ngo_type from ngos_table where ngo_type='auto' ");
			ResultSet rs = stmtSelect.executeQuery();
			int counter = 10000001;
			while(rs.next()){
				
				String thisUid = rs.getString("ngo_uid");
				PreparedStatement stmtNgos = conn.prepareStatement("update ngos_table set ngo_uid =? where ngo_uid = ?");
				stmtNgos.setString(1, ""+counter);
				stmtNgos.setString(2, thisUid);
				stmtNgos.execute();
				stmtNgos.close();
				System.out.println("writing for "+counter);
				counter++;
			}
			conn.commit();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
		}
	}
	public static void migrateUserNgos() throws SQLException{
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/welfarecommunity?user=root&password=root")){
			conn.setAutoCommit(false);
			PreparedStatement stmtSelect = conn.prepareStatement("select ngo_uid, ngo_type from ngos_table where ngo_type='user' ");
			ResultSet rs = stmtSelect.executeQuery();
			int counter = 10057684;
			while(rs.next()){
				
				String thisUid = rs.getString("ngo_uid");
				
				String strPhotoOwners = thisUid;
				
				PreparedStatement stmtEvents = conn.prepareStatement("select evt_code_pk, evt_organizer_code_fk from events_table where evt_organizer_code_fk = ?");
				stmtEvents.setString(1, thisUid);
				ResultSet rsEvents = stmtEvents.executeQuery();
				while(rsEvents.next()){
					strPhotoOwners += ","+rsEvents.getInt("evt_code_pk");
				}
				
				stmtEvents.close();
				PreparedStatement stmtAUs = conn.prepareStatement("select au_code, au_ngo_uid_fk from about_us_table where au_ngo_uid_fk = ?");
				stmtAUs.setString(1, thisUid);
				ResultSet rsAUs = stmtAUs.executeQuery();
				while(rsAUs.next()){
					strPhotoOwners += ","+rsAUs.getInt("au_code");
				}
				stmtAUs.close();
				
				PreparedStatement stmtPhotoPath = conn.prepareStatement("update photo_table "
						+ "set p_file_path =replace(p_file_path, '"+thisUid+"', '"+counter+"') where p_owner_id in("+strPhotoOwners+")");
				stmtPhotoPath.execute();
				stmtPhotoPath.close();
				
				PreparedStatement stmtNgos = conn.prepareStatement("update ngos_table set ngo_uid =? where ngo_uid = ?");
				stmtNgos.setString(1, ""+counter);
				stmtNgos.setString(2, thisUid);
				stmtNgos.execute();
				stmtNgos.close();
				
					
				PreparedStatement stmtApp = conn.prepareStatement("update appreciation_table set app_by_uid =? where app_by_uid = ?");
				stmtApp.setString(1, ""+counter);
				stmtApp.setString(2, thisUid);
				stmtApp.execute();
				stmtApp.close();
				
				PreparedStatement stmtPhoto = conn.prepareStatement("update photo_table set p_owner_id =? where p_owner_id = ?");
				stmtPhoto.setString(1, ""+counter);
				stmtPhoto.setString(2, thisUid);
				stmtPhoto.execute();
				stmtPhoto.close();
				
				PreparedStatement stmtUsers = conn.prepareStatement("update users_table set usr_uid =? where usr_uid = ?");
				stmtUsers.setString(1, ""+counter);
				stmtUsers.setString(2, thisUid);
				stmtUsers.execute();
				stmtUsers.close();
				
				
				File folder = new File(Constants.ROOTPATH+"/images/"+thisUid);
				folder.renameTo(new File(Constants.ROOTPATH+"/images/"+counter));
				//rename folders
				
				System.out.println("writing for "+counter);
				counter++;
					
				
			}
			conn.commit();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
		}
	}
}
