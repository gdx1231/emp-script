package com.gdxsoft.easyweb.define;

import java.util.HashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.utils.UPath;

/**
 *
 */
public class SyncRemotes {

    private static long LAST_TIME;
    public static HashMap<String, SyncRemote> MAP_REMOTE;
    private static Logger LOOGER = LoggerFactory.getLogger(SqlCached.class);

    public SyncRemotes() {
        initCfgs();
    }

    public void initCfgs() {
        boolean fileChanged = UPath.getPropTime() != LAST_TIME;
        if (!fileChanged && MAP_REMOTE != null) {
            return;
        }


        Document doc;
        try {
            doc = UPath.getCfgXmlDoc();
            initCfgs(doc);
        } catch (Exception e) {
            LOOGER.error("SyncRemotes.initCfgs, " + e.getMessage());
        }

    }

    /**
     * 初始化同步对象,放到缓存中(MAP_REMOTE)
     *
     * @param doc
     */
    private void initCfgs(Document doc) {
        MAP_REMOTE = new HashMap<String, SyncRemote>();
        NodeList nl = doc.getElementsByTagName("remote_syncs");
        int inc = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Element ele = (Element) nl.item(i);

            String code = ele.getAttribute("code");
            if (code == null || code.trim().length() == 0) {
                LOOGER.warn("配置项: code 没定义");
                continue;
            }
            String key = this.getKey(code);
            inc++;
            SyncRemote remote = new SyncRemote();
            remote.loadCfgs(ele);
            remote.setCfgKey(key);

            if (MAP_REMOTE.containsKey(key)) {
                LOOGER.warn("配置项:" + code + "重复");
            }
            MAP_REMOTE.put(key, remote);

        }
        LOOGER.debug("加载了: " + inc);
        System.out.println("加载了: " + inc);
    }

    /**
     * 根据 code 获取key
     *
     * @param code the AES key of SYNC
     * @return the "CFG_" + code.hashCode
     */
    public String getKey(String code) {
        String key = "CFG_" + code.hashCode();
        return key;
    }

    /**
     * 根据key获取同步对象
     *
     * @param key the "CFG_" + code.hashCode
     * @return the remote
     */
    public SyncRemote getRemoteInstance(String key) {
        return MAP_REMOTE.get(key);
    }

}
