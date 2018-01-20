package interceptors;

import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import constants.ResultConstants;
public class VisitorAuthInterceptor  extends AbstractInterceptor {
	private Integer pageOwnerCode;
        @Override
        public String intercept(ActionInvocation invocation) throws Exception {
        		System.out.println("Visitor Auth called");
        		final ActionContext context = invocation.getInvocationContext();
                Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
                Map params = ActionContext.getContext().getParameters();
                String result = "";
                if(sessionMap.get("userCode") == null || params.get("pageOwnerCode") == null ){
                	return ResultConstants.ILLEGAL_ACCESS;
                } else {
                	Integer userCode = Integer.parseInt(""+sessionMap.get("userCode"));
                	pageOwnerCode = Integer.parseInt((String)params.get("pageOwnerCode"));
                	if(pageOwnerCode.equals(userCode)){
                		return ResultConstants.ILLEGAL_ACCESS;
                	}
                	sessionMap.put("guest", false);
					sessionMap.put("owner", false);
					sessionMap.put("visitor", true);
                	result = invocation.invoke();
                }
                return result;
        }
		public Integer getPageOwnerCode() {
			return pageOwnerCode;
		}
		public void setPageOwnerCode(Integer pageOwnerCode) {
			this.pageOwnerCode = pageOwnerCode;
		}
}