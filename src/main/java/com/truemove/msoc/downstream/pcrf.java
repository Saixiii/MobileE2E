package com.truemove.msoc.downstream;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : pcrf
 * @Date    : Mar 18, 2016 10:49:49 AM
 */

public class pcrf {
    
    private static final httpUtil httpUtil = new httpUtil();
    
    // Define field parameter
    private static final String path = "/axis/services/ScfPccSoapServiceEndpointPort";
    private static final String node[] = {"subscriber","subscribedService","servicePackage"};
    
    // XML tag
    public static String getSubscriberAllInf(String msisdn) {
        
        String cmd = "<?xml version=\"1.0\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:rm=\"rm:soap\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<rm:getSubscriberAllInf>" +
                "<inPara>" +
                "<subscriber>" +
                "<attribute>" +
                "<key>usrIdentifier</key>" +
                "<value>" + msisdn + "</value>" +
                "</attribute>" +
                "</subscriber>" +
                "</inPara>" +
                "</rm:getSubscriberAllInf>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
                
        return cmd;
    }
    
    public static String getSubscriberAllQuota(String msisdn) {
        
        String cmd = "<?xml version=\"1.0\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:rm=\"rm:soap\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<rm:getSubscriberAllQuota>" +
                "<inPara>" +
                "<subscriber>" +
                "<attribute>" +
                "<key>usrIdentifier</key>" +
                "<value>" + msisdn + "</value>" +
                "</attribute>" +
                "</subscriber>" +
                "</inPara>" +
                "</rm:getSubscriberAllQuota>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
                
        return cmd;
    }
    
    // Execute http
    public static HashMap run(String ne,String ip,String port,String trustkey,String pass,String msisdn) {
        
        HashMap<String, String> data = new HashMap<String, String>();
        
        // Generate XML by MSISDN
        String tagxml = getSubscriberAllInf("66" + msisdn);
        // Generate URL
        String url = "https://" + ip + ":" + port + path;
        
        try {
            HashMap<String,String> result = (httpUtil.httpsreq(url, tagxml, trustkey, pass));
            data.put(ne + ":rs",result.get("rs"));
            if ("success".equals(result.get("rs"))) {
                for(String n : node) {
                    data.putAll(httpUtil.parserXMLpair(result.get("xml"),ne + ":" + n,"//result/" + n + "/*"));
                }
            }
            
            tagxml = getSubscriberAllQuota("66" + msisdn);
            result = httpUtil.httpsreq(url, tagxml, trustkey, pass);
            //System.out.println(result.get("xml"));
            data.put(ne + ":rsquota",result.get("rs"));
            if ("success".equals(result.get("rs"))) {
                data.putAll(httpUtil.parserXMLpair(result.get("xml"),ne + ":subscriberQuota","//result/subscriberQuota/*"));
                data.put(ne + ":subscribedService:SRVNAME",data.get(ne + ":subscribedService:SRVNAME").replace(',','\n'));
                data.put(ne + ":servicePackage:SRVPKGNAME",data.get(ne + ":servicePackage:SRVPKGNAME").replace(',','\n'));
                if("0".equals(data.get(ne + ":subscriberQuota:QTACLASS"))) {
                    String val = String.valueOf(Integer.parseInt((String)data.get(ne + ":subscriberQuota:QTAVALUE")) / 1024);
                    String bal = String.valueOf(Integer.parseInt((String)data.get(ne + ":subscriberQuota:QTABALANCE")) / 1024);
                    data.put(ne + ":subscriberQuota:BALANCE",bal + "/" + val + " MB");
                }
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(dmc.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }
    
}
