package com.truemove.msoc.downstream;


import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : dmc
 * @Date    : Mar 6, 2016 11:25:59 PM
 */

public class dmc {
    
    private static final httpUtil httpUtil = new httpUtil();
    
    // Define field parameter
    private static final String path = "/dmc_mobile_e2e/get_response.php";
    private static final String xpath = "//dmc_response/*";
    
    // XML tag
    public static String xml(String msisdn) {
        
        String cmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<dmc_request>" +
                "<command>get_customer_info</command>" +
                "<transaction_id>12345</transaction_id>" +
                "<msisdn>" + msisdn + "</msisdn>" +
                "<channel>E2E</channel>" +
                "</dmc_request>";
        return cmd;
    }
    
    // Execute http
    public static HashMap run(String ne,String ip,String port,String msisdn) {
        
        HashMap<String, String> data = new HashMap<String, String>();
        
        // Generate XML by MSISDN
        String tagxml = xml("66" + msisdn);
        // Generate URL
        String url = "http://" + ip + ":" + port + path;
        
        try {
            HashMap<String,String> result = (httpUtil.httpreq(url, tagxml));
            data.put(ne + ":rs",result.get("rs"));
            //System.out.println(result.get("xml"));
            if ("success".equals(result.get("rs"))) {
                data.putAll(httpUtil.parserXMLelement(result.get("xml"),ne,xpath));
            }
        } catch (Exception ex) {
            Logger.getLogger(dmc.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }
}
