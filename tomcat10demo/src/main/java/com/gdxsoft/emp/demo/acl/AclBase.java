package com.gdxsoft.emp.demo.acl;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;

public class AclBase {
    private RequestValue _RequestValue;
    private String _XmlName;
    private String _ItemName;
    private String _GoToUrl;
    private HtmlCreator htmlCreator;

    public HtmlCreator getHtmlCreator() {
        return htmlCreator;
    }

    public void setHtmlCreator(HtmlCreator htmlCreator) {
        this.htmlCreator = htmlCreator;
    }

    public String getDenyMessage() {
        return null;
    }

    public String getGoToUrl() {
        return _GoToUrl;
    }

    public String getItemName() {
        return this._ItemName;
    }

    public RequestValue getRequestValue() {
        return this._RequestValue;
    }

    public String getXmlName() {
        return _XmlName;
    }

    public void setGoToUrl(String url) {
        _GoToUrl = url;
    }

    public void setItemName(String itemName) {
        _XmlName = itemName;
    }

    public void setRequestValue(RequestValue requestValue) {
        _RequestValue = requestValue;
    }

    public void setXmlName(String xmlName) {
        this._XmlName = xmlName;
    }

    public static int getId(RequestValue rv, String key) {
        String v = getString(rv, key);
        if (v == null) return -1;
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return -1;
        }
    }

    public static long getLongId(RequestValue rv, String key) {
        String v = getString(rv, key);
        if (v == null) return -1;
        try {
            return Long.parseLong(v);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getString(RequestValue rv, String key) {
        PageValue pv = rv.getPageValues().getPageValue(key);
        if (pv == null) return null;
        if (pv.getPVTag() != PageValueTag.COOKIE_ENCYRPT && pv.getPVTag() != PageValueTag.SESSION) return null;
        try {
            return pv.getStringValue();
        } catch (Exception e) {
            return null;
        }
    }
}
