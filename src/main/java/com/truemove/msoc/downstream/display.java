package com.truemove.msoc.downstream;


import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : display
 * @Date    : Mar 4, 2016 5:02:40 PM
 */

public class display {
    
    // --- Configuration --
    // CDB
    private static final String CDB_IP = "10.95.78.12";
    private static final String CDB_PORT = "16611";
    private static final String CDB_USER = "ussdbuffet";
    private static final String CDB_PASS = "q1w2e3r4";
    
    // SPS
    private static final String SPS_IP = "10.80.193.21";
    private static final String SPS_PORT = "389";
    //private static final String SPS_IP = "127.0.0.1";
    //private static final String SPS_PORT = "10014";
    private static final String SPS_USER = "rmvdmp";
    private static final String SPS_PASS = "rmvdmp_RFT";
    
    // HLR
    private static final String HLRRMV_IP = "10.95.20.68";
    private static final String HLRRMV_PORT = "7776";
    //private static final String HLRRMV_IP = "127.0.0.1";
    //private static final String HLRRMV_PORT = "10015";
    private static final String HLRRMV_USER = "amhlrbf";
    private static final String HLRRMV_PASS = "amhlRBF";
    private static final String HLRRFT_IP = "10.80.118.108";
    private static final String HLRRFT_PORT = "7776";
    //private static final String HLRRFT_IP = "127.0.0.1";
    //private static final String HLRRFT_PORT = "10016";
    private static final String HLRRFT_USER = "rftHLR";
    private static final String HLRRFT_PASS = "rftHLR01";
    
    // DMC
    private static final String DMC_IP = "10.4.85.137";
    private static final String DMC_PORT = "80";
    //private static final String DMC_IP = "127.0.0.1";
    //private static final String DMC_PORT = "10017";
    
    // CCP
    private static final String CCP1_IP = "10.80.65.33";
    private static final String CCP2_IP = "10.80.193.33";
    private static final String CCP7_IP = "10.95.84.135";
    private static final String CCP8_IP = "10.95.78.16";
    private static final String CCP_PORT = "8090";
    
    // CCBS
    private static final String CCBS_IP = "172.19.136.56";
    private static final String CCBS_PORT = "80";
    
    // PCRF
    private static final String PCRFRMV_IP = "10.95.234.36";
    private static final String PCRFRFT_IP = "10.80.116.16";
    //private static final String PCRFRFT_IP = "127.0.0.1";
    private static final String PCRF_PORT = "8080";
    //private static final String PCRF_KEY = "D:\\1-Work\\1-Script\\JAVA\\cer\\UPCC_client.store";
    private static final String PCRF_KEY = "/home/mstm/script/java/lib/UPCC_client.store";
    private static final String PCRF_PASS = "123456";
    
    
    public static void main (String args[]) {
        
        //String MSISDN = "66957482448";
        String MSISDN = "";
        
        if(args.length > 0)
            MSISDN = args[0];
        else
            usage();
        
        
        // Veridate input number
        if("66".equals(MSISDN.substring(0,2)))
            MSISDN = MSISDN.substring(2);
        else if("0".equals(MSISDN.substring(0,1)))
            MSISDN = MSISDN.substring(1);
        else if(MSISDN.length() != 9)
            usage();
            
        
        
        //MSISDN = "829013088";
        //MSISDN = "610400152";
        //MSISDN = "891033575";
        //MSISDN = "941524633";
        
        HashMap<String,String> data = new HashMap<String,String>();
        data.putAll(cdb.run("cdb", CDB_IP, CDB_PORT, CDB_USER, CDB_PASS, MSISDN));
        data.putAll(sps.run("sps", SPS_IP, SPS_PORT, SPS_USER, SPS_PASS, MSISDN));
        if("success".equals(data.get("cdb:rs"))){
            data.putAll(dmc.run("dmc", DMC_IP, DMC_PORT, MSISDN));
            if("PRE".equals(data.get("cdb:spAccountType"))) switch (data.get("cdb:appINChain")) {
                case "1":
                    data.putAll(ccp.run("ccp", CCP1_IP, CCP_PORT, MSISDN));
                    data.put("ccbs:rs","account did not found");
                    break;
                case "2":
                    data.putAll(ccp.run("ccp", CCP2_IP, CCP_PORT, MSISDN));
                    data.put("ccbs:rs","account did not found");
                    break;
                case "7":
                    data.putAll(ccp.run("ccp", CCP7_IP, CCP_PORT, MSISDN));
                    data.put("ccbs:rs","account did not found");
                    break;
                case "8":
                    data.putAll(ccp.run("ccp", CCP8_IP, CCP_PORT, MSISDN));
                    data.put("ccbs:rs","account did not found");
                    break;
            } else if("POS".equals(data.get("cdb:spAccountType"))) {
                data.putAll(ccbs.run("ccbs", CCBS_IP, CCBS_PORT, MSISDN));
                data.put("ccp:rs","account did not found");
            }
            
            if(null != data.get("cdb:spImsi").substring(0,5)) switch (data.get("cdb:spImsi").substring(0,5)) {
                case "52000":
                    data.putAll(hlr.run("hlr", HLRRMV_IP, HLRRMV_PORT, HLRRMV_USER, HLRRMV_PASS, MSISDN));
                    data.putAll(hlrlu.run("hlrlu", HLRRMV_IP, HLRRMV_PORT, HLRRMV_USER, HLRRMV_PASS, MSISDN));
                    data.putAll(pcrf.run("pcrf", PCRFRMV_IP, PCRF_PORT, PCRF_KEY, PCRF_PASS, MSISDN));
                    break;
                case "52004":
                    data.putAll(hlr.run("hlr", HLRRFT_IP, HLRRFT_PORT, HLRRFT_USER, HLRRFT_PASS, MSISDN));
                    data.putAll(hlrlu.run("hlrlu", HLRRFT_IP, HLRRFT_PORT, HLRRFT_USER, HLRRFT_PASS, MSISDN));
                    data.putAll(pcrf.run("pcrf", PCRFRFT_IP, PCRF_PORT, PCRF_KEY, PCRF_PASS, MSISDN));
                    break;
            }
        }
            
        System.out.println("MSISDN: 0" + MSISDN);
        //prtdata(data);
        e2e.printe2e(data);
    }
    
    public static void usage() {
        System.out.println("Usage: [MSISDN - 0XXXXXXXXX,66XXXXXXXXX]");
        System.exit(0);
    }
    
    public static void prtdata (HashMap<String,String> data) {
        SortedSet<String> keys = new TreeSet<String>(data.keySet());
        for (String key : keys) {
            System.out.println("Key : " + key + " = " + data.get(key));
        }
    }
    
    
    public static void prtall (HashMap<String,String> data) {
        
        SortedSet<String> keys = new TreeSet<String>(data.keySet());
        String chk = "";
        for (String key : keys) {
            String arraykey[] = key.split(":");
            String node = arraykey[0];
            String attr = arraykey[arraykey.length-1];
            String val = data.get(key);
            //if(val != null && !val.isEmpty() && !"rs".equals(attr)) {
            if(!"rs".equals(attr)) {
                if(!chk.equals(node)) {
                    System.out.println("----------------");
                    System.out.println(" 🎉  [" + node + "]");
                    chk = node;
                }
                System.out.println("🔅" + attr + ": " + val);
            }
        }
    }
    
}
