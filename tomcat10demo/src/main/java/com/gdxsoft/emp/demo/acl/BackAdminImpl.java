package com.gdxsoft.emp.demo.acl;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.script.RequestValue;

public class BackAdminImpl extends AclBase implements IAcl {

    public static boolean isLogined(RequestValue rv) {
        int iAdmId = AclBase.getId(rv, "G_ADM_ID");
        int iSupId = AclBase.getId(rv, "G_SUP_ID");
        return iAdmId >= 0 && iSupId >= 0;
    }

    @Override
    public boolean canRun() {
        if (isLogined(super.getRequestValue())) {
            return true;
        }
        // 未登录，跳转到登录页
        RequestValue rv = super.getRequestValue();
        String loginUrl = rv.s("EWA.CP") + "/back_admin/login.jsp";

        // 记录来源页
        if (rv.getRequest() != null) {
            String ref = rv.getRequest().getContextPath() + rv.getRequest().getServletPath();
            if (rv.getRequest().getQueryString() != null) {
                ref += "?" + rv.getRequest().getQueryString();
            }
            try {
                loginUrl += "?ref=" + java.net.URLEncoder.encode(ref, "utf-8");
            } catch (java.io.UnsupportedEncodingException e) {
                loginUrl += "?ref=" + ref;
            }
        }

        super.setGoToUrl(loginUrl);
        return false;
    }
}
