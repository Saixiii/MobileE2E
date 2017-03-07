package com.truemove.msoc.downstream;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : ccbs
 * @Date    : Mar 16, 2016 4:52:41 PM
 */

public class ccbs {
    
    private static final httpUtil httpUtil = new httpUtil();
    
    // Define field parameter
    private static final String path = "/TrueMoveCustInfoESB/http/TrueMoveCustInfo/service";
    private static final String xpath = "//@*";
    
    // XML tag
    public static String xml(String msisdn) {
        
        String cmd = "<?xml version=\"1.0\"?>" +
                "<request cmd=\"Query\" mobile_no=\"" + msisdn + "\"  limit_result=\"no\"/>";
        
        return cmd;
    }
    
    // Execute http
    public static HashMap run(String ne,String ip,String port,String msisdn) {
        
        HashMap<String, String> data = new HashMap<String, String>();
        
        // Generate XML by MSISDN
        String tagxml = xml("0" + msisdn);
        // Generate URL
        String url = "http://" + ip + ":" + port + path;
        
        try {
            HashMap<String,String> result = (httpUtil.httpreq(url, tagxml));
            data.put(ne + ":rs",result.get("rs"));
            //System.out.println(result.get("xml"));
            if ("success".equals(result.get("rs"))) {
                data.putAll(httpUtil.parserXMLattribute(result.get("xml"),ne,xpath));
                data.put(ne + ":name",String.valueOf(data.get(ne + ":name_title"))
                        + String.valueOf(data.get(ne + ":first_name"))
                        + " " + String.valueOf(data.get(ne + ":last_name")));
                data.put(ne + ":addr",String.valueOf(data.get(ne + ":house_num"))
                        + " " + String.valueOf(data.get(ne + ":moo_ban"))
                        + " " + String.valueOf(data.get(ne + ":street"))
                        + " " + String.valueOf(data.get(ne + ":sub_district"))
                        + " " + String.valueOf(data.get(ne + ":district"))
                        + " " + String.valueOf(data.get(ne + ":city"))
                        + " " + String.valueOf(data.get(ne + ":zip_code")));
                data.put(ne + ":name",data.get(ne + ":name").replaceAll("null",""));
                data.put(ne + ":addr",data.get(ne + ":addr").replaceAll("null",""));
            }
        } catch (Exception ex) {
            Logger.getLogger(dmc.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }
}
