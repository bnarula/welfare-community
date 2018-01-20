package action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import beans.CauseBean;
import beans.EventBean;
import beans.NgoBean;
import config.DBConnection;
import constants.Constants;
import constants.ResultConstants;
import dao.CauseDao;
import dao.EventDao;
import dao.NgoDao;

public class IndexAction {
	private List<NgoBean> currCityNgoList = new ArrayList<NgoBean>();
	private List<EventBean> currCityEventList = new ArrayList<EventBean>();
	private List<CauseBean> causesList = new ArrayList<CauseBean>();
	private String currentCity;
	
	public String indexGetCausesList(){
		try(Connection conn = DBConnection.getConnection()) {
			causesList = (CauseDao.getCauseBeanMasterList(conn));
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}
	
	public String indexGetCityNGOList(){
		try(Connection conn = DBConnection.getConnection()) {
			//String cities[] = currentCity.split(",");
			List<Integer> ngoIds = new ArrayList<Integer>();
			/*for(String city: cities){
				if(city.equals("Bengaluru"))
					city="Bangalore";
				ngoIds.addAll(NgoDao.searchByLocation(conn, "city", city, "user", 0, 10));
				currCityEventList.addAll(EventDao.searchByLocation(conn, city, 0, 10));
			}*/
			if(currCityEventList.size()<5){
				ArrayList<EventBean> otherEvents = (ArrayList<EventBean>) NgoDao.getListOfEvents(conn, 0, false, -1, -1, 0, 10);
				ArrayList<Integer> otherEventIds = new ArrayList<Integer>();
				Iterator<EventBean> oEIter = currCityEventList.iterator();
				while(oEIter.hasNext()){
					EventBean nxtEvent = oEIter.next();
					otherEventIds.add(nxtEvent.getId());
				}
				oEIter = otherEvents.iterator();
				while(oEIter.hasNext()){
					EventBean nxtEvent = oEIter.next();
					if(!otherEventIds.contains(nxtEvent.getId()))
							currCityEventList.add(nxtEvent);
				}
				
			}
			List<Integer> ngoIds2 = new ArrayList<Integer>();
			ArrayList<Integer> otherNGOs = (ArrayList<Integer>) NgoDao.listAllUserNgos(conn, 0, 20);
			Iterator<Integer> oEIter = otherNGOs.iterator();
			while(oEIter.hasNext()){
				Integer nxtNgoId = oEIter.next();
				if(!ngoIds.contains(nxtNgoId))
					ngoIds2.add(nxtNgoId);
				
			}
			if(ngoIds2.size()>0){
				ArrayList<String> selectables = new ArrayList<String>();
				selectables.add(Constants.NGOBEAN_NAME);
				selectables.add(Constants.NGOBEAN_LOGO_P_ID);
				selectables.add(Constants.NGOBEAN_ADDRESS_LIST);
				selectables.add(Constants.NGOBEAN_ALIAS);
				Integer array[] = new Integer[ngoIds2.size()];
				for(int i = 0 ; i < array.length ; i++){
					array[i] = ngoIds2.get(i);
				}
				currCityNgoList.addAll(NgoDao.getNgosFromIds(conn, array, selectables));
			}
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResultConstants.SUCCESS;
	}

	public List<NgoBean> getCurrCityNgoList() {
		return currCityNgoList;
	}

	public void setCurrCityNgoList(List<NgoBean> currCityNgoList) {
		this.currCityNgoList = currCityNgoList;
	}

	public List<EventBean> getCurrCityEventList() {
		return currCityEventList;
	}

	public void setCurrCityEventList(List<EventBean> currCityEventList) {
		this.currCityEventList = currCityEventList;
	}
	public List<CauseBean> getCausesList() {
		return causesList;
	}

	public void setCausesList(List<CauseBean> causesList) {
		this.causesList = causesList;
	}

	public String getCurrentCity() {
		return currentCity;
	}

	public void setCurrentCity(String locationResponse) {
		this.currentCity = locationResponse;
	}



	
}
