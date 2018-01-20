package interceptors;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import beans.NgoBean;
import config.DBConnection;
import dao.NgoDao;

public class GuestAuthInterceptor  extends AbstractInterceptor {
        @Override
        public String intercept(ActionInvocation invocation) throws Exception {
        		System.out.println("Guest Auth called");
        		final ActionContext context = invocation.getInvocationContext();
                Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
                Map<String, Parameter> params = ActionContext.getContext().getParameters();
                
                Integer pageOwnerCode = 0;
                try(Connection conn = DBConnection.getConnection()) {
                	String role = "";
                	Integer userCode = 0;
                	
                	if(params.get("pageOwnerCode").getValue()==null)
                	{
                		if(!sessionMap.containsKey("pageOwnerCode"))
                    	{
                			pageOwnerCode = 0;
                    	}
                	}
                	else
                	{
                		pageOwnerCode = Integer.parseInt(params.get("pageOwnerCode").getValue());
                		if(!sessionMap.containsKey("pageOwnerBean") 
                				|| !(((NgoBean)sessionMap.get("pageOwnerBean")).getUid().equals(pageOwnerCode))
                				|| Boolean.parseBoolean(""+sessionMap.get("isUserModified")))
                		{
                			NgoBean sessionBean = NgoDao.getSessionNgoBeanFromId(conn, pageOwnerCode);
                			sessionMap.put("pageOwnerBean", sessionBean);
                			sessionMap.put("isUserModified", false);
                		}
                	}
                	
                	if(sessionMap.get("userCode") == null){
                		role = "Guest";
                		sessionMap.put("isUserAppreciated", false);
                	} else {
                		userCode = Integer.parseInt(""+sessionMap.get("userCode"));
                		sessionMap.put("isUserAppreciated", NgoDao.isAppreciated(conn, userCode, pageOwnerCode));
    					if(userCode.equals(pageOwnerCode))
    						role = "Owner";
    					else
    						role = "Visitor";
                	}
                 	
					
					if(role.equalsIgnoreCase("Guest"))
					{
						sessionMap.put("guest", true);
						sessionMap.put("owner", false);
						sessionMap.put("visitor", false);
					}
					if(role.equalsIgnoreCase("visitor"))
					{
						sessionMap.put("guest", false);
						sessionMap.put("owner", false);
						sessionMap.put("visitor", true);
					}
					if(role.equalsIgnoreCase("owner"))
					{
						sessionMap.put("guest", false);
						sessionMap.put("owner", true);
						sessionMap.put("visitor", false);
					}
					//context.put(ActionContext.PARAMETERS, parametersCopy);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
                return invocation.invoke();
        }
}