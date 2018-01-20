package interceptors;

import java.sql.Connection;
import java.util.Map;

import org.apache.struts2.dispatcher.Parameter;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import beans.NgoBean;
import config.DBConnection;
import dao.NgoDao;

public class OwnerAuthInterceptor  extends AbstractInterceptor {
        @Override
        public String intercept(ActionInvocation invocation) {
        		System.out.println("Owner Auth called");
        		final ActionContext context = invocation.getInvocationContext();
                Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
                Map<String, Parameter> params = invocation.getInvocationContext().getParameters();
                String role = "";
                String result = "";
                
                try {
	                if(!sessionMap.isEmpty()){
	                	
	                	if(sessionMap.get("userCode")==null)
	                    	return "IllegalAccess";
	                    else{
	                    	Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
	                    	try(Connection conn = DBConnection.getConnection()) {
	                    		if(((NgoBean)sessionMap.get("pageOwnerBean")).getUid() != (userCode)
	                    				|| Boolean.parseBoolean(""+sessionMap.get("isUserModified")))
	                    		{
	                    			NgoBean sessionBean = NgoDao.getSessionNgoBeanFromId(conn, userCode);
	                    			sessionMap.put("pageOwnerBean", sessionBean);
	                    			sessionMap.put("isUserModified", false);
	                    		}
	                    		// parametersCopy.put("pageOwnerCode", userCode);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    	result = invocation.invoke();
	                    }
	                }
	                else
	                	return "IllegalAccess";
                } catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return "IllegalAccess";
				}
                return result;
        }
}