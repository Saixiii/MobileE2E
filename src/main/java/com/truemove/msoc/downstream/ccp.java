package com.truemove.msoc.downstream;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : ccp
 * @Date    : Mar 10, 2016 5:50:10 PM
 */

public class ccp {
    
    private static final httpUtil httpUtil = new httpUtil();
    
    // Define field parameter
    private static final String path = "/ocswebservices/services/TrueWebServices";
    private static final String xpath = "//*[name()='queryUserProfileReturn']/child::* | //*[name()='AcctResCode' and text()='0']/following-sibling::* | //*[name()='PricePlanCode'] | //*[name()='BalDto']/child::* ";
    
    // XML tag
    public static String xml(String msisdn,String user,String pass) {
        
        String cmd = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tha=\"http://thaitrue.customization.ws.bss.zsmart.ztesoft.com\"> lns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tha=\"http://thaitrue.customization.ws.bss.zsmart.ztesoft.com\">" +
                        "<soapenv:Header>" +
                        "<AuthHeader>" +
                        "<username>" + user + "</username>" +
                        "<password>" + pass + "</password>" +
                        "</AuthHeader>" +
                        "</soapenv:Header>" +
                        "<soapenv:Body>" +
                        "<tha:queryUserProfile>" +
                        "<tha:QueryUserProfileReqDto>" +
                        "<tha:MSISDN>" + msisdn + "</tha:MSISDN>" +
                        "<tha:UserPwd></tha:UserPwd>" +
                        "<tha:RequestID>INOPS0000000</tha:RequestID>" +
                        "</tha:QueryUserProfileReqDto>" +
                        "</tha:queryUserProfile>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>";
        return cmd;
    }
    
    // Execute http
    public static HashMap run(String ne,String ip,String port,String msisdn) {
        
        HashMap<String, String> data = new HashMap<String, String>();
        
        String user = "INOPS";
        String pass = "SPONI";
        
        // Generate XML by MSISDN
        String tagxml = xml("66" + msisdn,user,pass);
        // Generate URL
        String url = "http://" + ip + ":" + port + path;
        
        try {
            HashMap<String,String> result = (httpUtil.httpreq(url, tagxml));
            data.put(ne + ":rs",result.get("rs"));
            //System.out.println(result.get("xml"));
            if ("success".equals(result.get("rs"))) {
                data.putAll(httpUtil.parserXMLelement(result.get("xml"),ne,xpath));
                //data.put(ne + ":Balance",String.format("%.2f",Long.parseLong((String)data.get(ne + ":Balance")) * -0.0001) + " ฿");
                String name[] = data.get(ne + ":AcctResName").split(",");
                String eff[] = data.get(ne + ":EffDate").split(",");
                String exp[] = data.get(ne + ":ExpDate").split(",");
                String bal[] = data.get(ne + ":Balance").split(",");
                data.put(ne + ":Bundle","");
                for(int i=0;i < name.length;i++) {
                    if("Main Balance".equals(name[i])) {
                        data.put(ne + ":ValidDate",eff[i]);
                        data.put(ne + ":InactDate",exp[i]);
                        data.put(ne + ":Balance",String.format("%.2f",Long.parseLong(bal[i]) * -0.0001) + " ฿");
                    }
                    else if(i == name.length - 1)
                        data.put(ne + ":Bundle",data.get(ne + ":Bundle") + name[i] + "(" + bal[i] + ")");
                    else
                        data.put(ne + ":Bundle",data.get(ne + ":Bundle") + name[i] + "(" + bal[i] + ")" + "\n");
                }
                
                data.put(ne + ":PricePlan",String.valueOf(data.get(ne + ":PricePlanCode").split(",",2)[0]));
                if(data.get(ne + ":PricePlanCode").split(",",2).length > 1)
                    data.put(ne + ":Package",String.valueOf(data.get(ne + ":PricePlanCode").split(",",2)[1].replace(',','\n')));
                else
                    data.put(ne + ":Package","");
                data.put(ne + ":Refill",String.valueOf(data.get(ne + ":RefillAble")) 
                        + " (" + String.valueOf(data.get(ne + ":RefillTempErrorTimes")) 
                        + "/" + String.valueOf(data.get(ne + ":RefillErrorTimes")) + ")");
            }
        } catch (Exception ex) {
            Logger.getLogger(dmc.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }
    
}
