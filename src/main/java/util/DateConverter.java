package util;

import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

public class DateConverter extends StrutsTypeConverter {
	public Object convertFromString(Map context, String[] values, Class toClass) {
		String calendar = values[0];
		Integer year = new Integer(calendar.substring(0, calendar.indexOf("-")));
		calendar = calendar.substring(calendar.indexOf("-")+1);
		Integer month = new Integer(calendar.substring(0, calendar.indexOf("-")));
		calendar = calendar.substring(calendar.indexOf("-")+1);
		Integer day = new Integer(calendar);
		return new GregorianCalendar (year, month-1, day);

	}

	public String convertToString(Map context, Object o) {
		
		return o.toString();
	}
}