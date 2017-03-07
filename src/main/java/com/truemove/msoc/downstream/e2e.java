package com.truemove.msoc.downstream;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : e2e
 * @Date    : Mar 23, 2016 2:33:41 PM
 */

public class e2e {
    
    // Global variable
    private static final String head = "📌";
    private static final String ok = "🔅";
    private static final String nok = "🚫";
    private static final String step = "--------------------------";
    private static final String noklst[] = {"No","NOTPROV","SIM","Bar","Bypass","Suspend","suspend",
        "Disconnect","Valid","1-Way Block","2-Way Block","Deactive","Purge","Frozen","PortOut",
        "CrossPort"};
    
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
    private static final String PCRF_PORT = "8080";
    //private static final String PCRF_KEY = "D:\\1-Work\\1-Script\\JAVA\\cer\\UPCC_client.store";
    private static final String PCRF_KEY = "/home/mstm/script/java/lib/UPCC_client.store";
    private static final String PCRF_PASS = "123456";
    
    
    public static void main (String args[]) {
        
        String MSISDN = "";
        //String MSISDN = "0865532270";
        
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
        printe2e(data);
    }
    
    public static void usage() {
        System.out.println("Usage: [MSISDN - 0XXXXXXXXX,66XXXXXXXXX]");
        System.exit(0);
    }
    
    
    public static void prtlst (String ne,HashMap<String,String> data,Map<String,String> chklist) {
        
        System.out.println(step);
        System.out.println(head + " [" + ne.toUpperCase() + "]");
        if("success".equals(data.get(ne + ":rs"))) {
            for (Map.Entry<String,String> entry : chklist.entrySet()) {
                String idx = entry.getKey();
                String key = entry.getValue();
                String val = data.get(idx);
                
                if(val != null && !val.isEmpty()) {
                    String vallst[] = data.get(idx).split("\\W+");
                    boolean chknok = true;
                    for(String nokword: noklst )
                        for(String valword : vallst)
                            if(nokword == null ? valword == null : nokword.equals(valword))
                                chknok = false;
                    
                    if(chknok)
                        System.out.println(ok + key + " : " + val);
                    else
                        System.out.println(nok + key + " : " + val);
                }
            }
        } else {
            System.out.println(data.get(ne + ":rs"));
        }
    }
    
    
    public static void sps (HashMap<String,String> data) {
        
        String ne = "sps";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("sps:portingStatus","Status");
            chklist.put("sps:routingCode","Routing");
        
        HashMap<String,String> routingcode = new HashMap<String,String>() {{
            put("06800","ACeS");
            put("06801","AIS");
            put("06802","CAT-BFKT");
            put("06803","AWN");
            put("06804","DPC");
            put("06805","DTAC");
            put("06806","RFT");
            put("06807","TOT");
            put("06808","DTN");
            put("06810","TMV");
            put("06888","RMV");
            put("06889","CAT");
        }};
        
        HashMap<String,String> porting = new HashMap<String,String>() {{
            put("1","PortOut");
            put("2","PortIn");
            put("3","CrossPort");
            put("10","OwnPortOut");
            put("30","ForeignPortIn");
            put("31","ForeignPortIn-ChangeZone");
            put("40","CrossPort");
            put("41"," CrossPort-ChangeZone");
            put("99","N/A");
        }};
        
        data.put("sps:routingCode",data.get("sps:routingCode") + "(" + routingcode.get(data.get("sps:routingCode").substring(0, 5)) + ")");
        data.put("sps:portingStatus",data.get("sps:portingStatus") + "(" + porting.get(data.get("sps:portingStatus")) + ")");
        
        prtlst(ne,data,chklist);
    }
    
    public static void cdb (HashMap<String,String> data) {
        
        String ne = "cdb";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("cdb:spImsi","IMSI");
            chklist.put("cdb:spAccountType","AccountType");
            chklist.put("cdb:spSubStatus","Status");
            chklist.put("cdb:spAccountCateg","Category");
            chklist.put("cdb:spPricePlan","PricePlan");
            chklist.put("cdb:appINChain","IN Chain");
            chklist.put("cdb:appINStatus","IN Status");
            chklist.put("cdb:appINProfile","IN Profile");
            chklist.put("cdb:gprsAPNList","APN List");
            chklist.put("cdb:gprsType","GPRS Type");
            chklist.put("cdb:spMobileVas","MobileVas");
            chklist.put("cdb:spConvergence","Convergence");
            chklist.put("cdb:spProvDate","ProvDate");
            chklist.put("cdb:appFirstCallDate","FirstCallDate");
            chklist.put("cdb:SIM Type","SIM Type");
            chklist.put("cdb:Data Speed","Data Speed");
            chklist.put("cdb:RBT","RBT");
            chklist.put("cdb:Language","Language");
            chklist.put("cdb:IOU","IOU");
            chklist.put("cdb:Roaming","Roaming");
            chklist.put("cdb:FCR","FCR");
            chklist.put("cdb:Bill Cycle","Bill Cycle");
            chklist.put("cdb:ISIM","iSIM");
        
        HashMap<String,Integer> mobilevas = new HashMap<String,Integer>() {{
            put("cdb:SIM Type",1);
            put("cdb:Data Speed",2);
            put("cdb:RBT",4);
            put("cdb:Language",7);
            put("cdb:IOU",8);
            put("cdb:Roaming",10);
            put("cdb:FCR",11);
            put("cdb:Bill Cycle",12);
            put("cdb:iSIM",13);
        }};
        
        HashMap<String,String> speed = new HashMap<String,String>() {{
            put("0","No");
            put("1","2G");
            put("2","1.0Mbps");
            put("3","2.0Mbps");
            put("4","3.6Mbps");
            put("5","7.2Mbps");
            put("6","10.2Mbps");
            put("7","14.4Mbps");
            put("8","21.5Mbps");
            put("9","42.0Mbps");
        }};
        
        HashMap<String,String> flag = new HashMap<String,String>() {{
            put("0","No");
            put("1","Yes");
        }};
        
        HashMap<String,String> roaming = new HashMap<String,String>() {{
            put("0","No");
            put("1","PROROAM2");
            put("2","PROROAM4");
            put("3","IR Partial");
            put("4","Roaming(SMS MT PRE)");
            put("5","Roaming(SMS MT POS)");
        }};
        
        HashMap<String,String> billcycle = new HashMap<String,String>() {{
            put("A","2");
            put("B","10");
            put("C","13");
            put("D","16");
            put("E","19");
            put("F","22");
            put("G","25");
            put("H","28");
        }};
        
        HashMap<String,String> lang = new HashMap<String,String>() {{
            put("0","TH");
            put("1","EN");
            put("2","MM");
        }};
        
        HashMap<String,String> iou = new HashMap<String,String>() {{
            put("0","NoOwe");
            put("1","Owe");
        }};
        
        HashMap<String,String> acccat = new HashMap<String,String>() {{
            put("I","Indy");
            put("C","Corp");
            put("B","Biz");
            put("A","A");
        }};
        
        HashMap<String,String> acctype = new HashMap<String,String>() {{
            put("A","Active");
            put("S","Suspend");
        }};
        
        
        if(null != data.get("cdb:spMobileVas")) {
            String mbvas = data.get("cdb:spMobileVas");
            for(String key : mobilevas.keySet()) {
                String val = mbvas.substring(mbvas.length() - mobilevas.get(key), mbvas.length() - mobilevas.get(key) + 1);
                data.put(key,val);
            }
            
            // Update Sim type
            if("U".equals(data.get("cdb:SIM Type")))
                data.put("cdb:SIM Type","USIM");
            else
                data.put("cdb:SIM Type","SIM");
            // Update Data Speed
            data.put("cdb:Data Speed",speed.get(data.get("cdb:Data Speed")));
            // Update RBT
            data.put("cdb:RBT",flag.get(data.get("cdb:RBT")));
            // Update IOU
            data.put("cdb:IOU",iou.get(data.get("cdb:IOU")));
            // Update ISIM
            data.put("cdb:ISIM",flag.get(data.get("cdb:ISIM")));
            // Update Roaming
            data.put("cdb:Roaming",roaming.get(data.get("cdb:Roaming")));
            // Update Bill Cycle
            data.put("cdb:Bill Cycle",billcycle.get(data.get("cdb:Bill Cycle")));
            // Update Language
            data.put("cdb:Language",lang.get(data.get("cdb:Language")));
            // Update AccountCateg
            data.put("cdb:spAccountCateg",acccat.get(data.get("cdb:spAccountCateg")));
            // Update SubStatus
            data.put("cdb:spSubStatus",acctype.get(data.get("cdb:spSubStatus")));
            // Update FCR
            if("0".equals(data.get("cdb:FCR")))
                data.put("cdb:FCR","NoFCR");
            else if("1".equals(data.get("cdb:FCR")))
                data.put("cdb:FCR","Bar FCR");
            else if("2".equals(data.get("cdb:FCR")))
                data.put("cdb:FCR","Unbar FCR");
            else
                data.put("cdb:FCR","N/A");
        }
        
        prtlst(ne,data,chklist);
    }
    
    public static void dmc (HashMap<String,String> data) {
        
        String ne = "dmc";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("dmc:imsi","IMSI");
            chklist.put("dmc:status","Status");
            chklist.put("dmc:sub_type","Type");
            chklist.put("dmc:sub_cat","Category");
            chklist.put("dmc:priceplan","Priceplan");
            chklist.put("dmc:bill_cycle","Bill Cycle");
            chklist.put("dmc:charge_type","Charge Type");
            chklist.put("dmc:billing_plan","Charge Plan");
            chklist.put("dmc:pref_language","Language");
            chklist.put("dmc:sso_sub","SSO Status");
            chklist.put("dmc:gprs_usage","Usage");
            chklist.put("dmc:wifi_status","WIFI Status");
            chklist.put("dmc:user_wifi","WIFI User");
            //chklist.put("dmc:ac_pass","WIFI Pass");
            chklist.put("dmc:wifi_concurrent","WIFI Concurrent");
            chklist.put("dmc:domain","WIFI Domain");
        
        HashMap<String,String> chargetype = new HashMap<String,String>() {{
            put("T","Time");
            put("V","Volumn");
        }};   
        
        HashMap<String,String> wifistatus = new HashMap<String,String>() {{
            put("A","Active");
            put("S","Suspend");
            put("D","Disconnect");
        }};
        
        HashMap<String,String> billing = new HashMap<String,String>() {{
            put("BP_POST","Bypass DMC");
            put("INT_TIME","Time Base");
            put("INT_VOL","Volumn Base");
        }};
        
        data.put("dmc:charge_type",chargetype.get(data.get("dmc:charge_type")));
        data.put("dmc:billing_plan",billing.get(data.get("dmc:billing_plan")));
        data.put("dmc:wifi_status",wifistatus.get(data.get("dmc:wifi_status")));
        data.put("dmc:gprs_usage",data.get("dmc:gprs_usage") + " ฿");
        
        prtlst(ne,data,chklist);
    }
    
    
    public static void ccp (HashMap<String,String> data) {
        
        String ne = "ccp";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("ccp:IMSI","IMSI");
            chklist.put("ccp:State","Status");
            chklist.put("ccp:PricePlan","Priceplan");
            chklist.put("ccp:Balance","Balance");
            chklist.put("ccp:DefLang","Language");
            chklist.put("ccp:GPRSChargeFlag","GPRS charge flag");
            chklist.put("ccp:Refill","Refill Flag");
            chklist.put("ccp:Package","Package");
            chklist.put("ccp:Bundle","Bundle");
            chklist.put("ccp:ValidDate","Valid");
            chklist.put("ccp:CompletedDate","Active");
            chklist.put("ccp:InactDate","1-Way Block");
            chklist.put("ccp:SuspendStopDate","2-Way Block");
            chklist.put("ccp:DisableStopDate","Deact");
        
        HashMap<String,String> state = new HashMap<String,String>() {{
            put("G","Valid");
            put("A","Active");
            put("D","1-Way Block");
            put("E","2-Way Block");
            put("B","Deactive");
            put("F","Purged");
        }};
        
        HashMap<String,String> lang = new HashMap<String,String>() {{
            put("1","TH");
            put("2","EN");
            put("4","MM");
        }};
        
        data.put("ccp:State",data.get("ccp:State") + " (" + state.get(data.get("ccp:State")) + ")");
        data.put("ccp:DefLang",data.get("ccp:DefLang") + " (" + lang.get(data.get("ccp:DefLang")) + ")");
        prtlst(ne,data,chklist);
    }
    
    public static void hlr (HashMap<String,String> data) {
        
        String ne = "hlr";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("hlr:IMSI","IMSI");
            chklist.put("hlr:Dynamic Status Information For S4SGSN:IMEI","IMEI");
            chklist.put("hlr:CardType","Card Type");
            chklist.put("hlr:EPS Data:CHARGE_GLOBAL","4G Service");
            chklist.put("hlr:Dynamic Status Information For GSM:VlrNum","VLR");
            chklist.put("hlr:Dynamic Status Information For GSM:MscNum","MSC");
            chklist.put("hlr:Dynamic Status Information For GPRS:SgsnNum","SGSN");
            chklist.put("hlr:Dynamic Status Information For GSM:MsPurgedForNonGprs","MS Purge");
            chklist.put("hlr:Dynamic Status Information For GPRS:MsPurgedForGprs","GPRS Purge");
            chklist.put("hlr:Dynamic Status Information For MME:PURGEDONMME","MME Purge");
            chklist.put("hlrlu:Dynamic Status Information For GSM:CSUPLTIME","LU CS");
            chklist.put("hlrlu:Dynamic Status Information For GPRS:PSUPLTIME","LU PS");
            chklist.put("hlr:Dynamic Status Information For MME:MME-UpdateLocation-Time","LU MME");
            chklist.put("hlr:U-CSI:TPLNAME","Service Name");
            chklist.put("hlr:GPRS APN","GPRS APN");
            chklist.put("hlr:EPS APN","EPS APN");
            chklist.put("hlr:Dynamic Status Information For MME:VPLMN","VPLMN");
            chklist.put("hlr:Dynamic Status Information For MME:MMEHOST","MME Host");
            chklist.put("hlr:Dynamic Status Information For MME:MMEREALM","MME Realm");
            chklist.put("hlr:SS Data:CFU","โอนสายทุกกรณี(CFU)");
            chklist.put("hlr:SS Data:CFB","โอนสายกรณี Busy(CFB)");
            chklist.put("hlr:SS Data:CFNRC","โอนสายกรณีไม่มีสัญญาณ(CFNRC)");
            chklist.put("hlr:SS Data:CFNRY","โอนสายกรณีไม่รับสาย(CFNRY)");
            chklist.put("hlr:ODB Data:ODBIC","ระงับโทรเข้า(ODBIC)");
            chklist.put("hlr:ODB Data:ODBOC","ระงับโทรออก(ODBOC)");
            chklist.put("hlr:ODB Data:ODBROAM","ระงับโรมมิ่ง(ODBROAM)");
            
        HashMap<String,String> apn = new HashMap<String,String>() {{
            put("3","IMS");
            put("4","INTERNET");
            put("5","HMMS");
            put("6","FUT2");
            put("7","FUT1");
            put("8","blackberry.net");
            put("9","FTP");
            put("10","HINTERNET");
            put("11","MMS");
            put("12","WAP");
            put("13","TGMICS");
            put("14","MINSERT01");
            put("15","POC1");
            put("16","POC2");
            put("17","BBL.TRUE");
            put("18","TUC.TEST");
            put("19","testbed1");
            put("20","testbed2");
            put("21","TRUEH.TEST");
            put("22","AF");
            put("23","Test01");
            put("24","Test02");
            put("25","Test03");
            put("26","Test04");
            put("27","Test05");
            put("28","Test06");
            put("29","testmms");
            put("30","BAY.TRUE");
            put("31","KBANK.TRUE");
            put("32","CIMB.TRUE");
            put("33","CORP.TUC");
            put("35","FORTHSMART");
            put("36","CORP.BAAC");
            put("37","RMV.CORPTEST");
            put("39","FIXIP.SIT.TEST");
            put("40","FIXIP.SIT.TEST2");
            put("41","EDCNETWORK.TRUEMH");
            put("42","KORVACEDC");
            put("43","CORP.SCB");
            put("44","CORP.GSB");
            put("45","RMV.CORP1");
            put("46","ICSOMC");
            put("47","TRANSPORTATION");
            put("48","CORP.LHB");
            put("49","RFT.MG");
            put("50","testtyn");
            put("51","MATT");
            put("52","RFT.CORPTEST");
            put("53","MWA.SCADA");
            put("54","SAMART.AMR");
            put("55","BENCHAMAS");
            put("56","TANTAWAN");
            put("57","PLATONG");
            put("58","SATUN");
            put("59","FUNAN");
            put("60","NPAILIN");
            put("61","SPAILIN");
            put("62","ERAWAN");
            put("63","GIL");
            put("64","PWA.SCADA");
            put("65","CIMB.TRUE2");
            put("66","KCS.TRUE");
            put("67","DESIGN");
            put("68","BBL.EDC");
            put("69","uih.nonatm");
            put("70","uih.kcs");
            put("71","uih.kbank");
            put("99","FTP_TEMP");
        }};
        
        HashMap<String,String> odboc = new HashMap<String,String>() {{
            put("NOBOC","NO");
            put("BAOC","Bar All");
            put("BOIC","Bar Inter");
            put("BOICEXHC","Bar Inter (except those directed to the home PLMN country)");
            put("BOCROAM","Bar All (when the subscriber roams outside the home PLMN country)");
        }};
        
        HashMap<String,String> odbic = new HashMap<String,String>() {{
            put("NOBIC","NO");
            put("BAIC","Bar All");
            put("BICROAM","Bar All (when the subscriber roams outside the home PLMN country)");
        }};
        
        HashMap<String,String> odbroam = new HashMap<String,String>() {{
            put("NOBAR","NO");
            put("BROHPLMN","Bar (when the subscriber roams outside the home PLMN)");
            put("BROHPLMNC","Bar (when the subscriber roams outside the home PLMN country)");
            put("BROHPLMNCGPRS","Bar GPRS (when the subscriber roams outside the home PLMN country)");
        }};
        
        data.put("hlr:ODB Data:ODBOC",odboc.get(data.get("hlr:ODB Data:ODBOC")));
        data.put("hlr:ODB Data:ODBIC",odbic.get(data.get("hlr:ODB Data:ODBIC")));
        data.put("hlr:ODB Data:ODBROAM",odbroam.get(data.get("hlr:ODB Data:ODBROAM")));
            
        if(!data.containsKey("hlr:EPS Data:CHARGE_GLOBAL"))
            data.put("hlr:EPS Data:CHARGE_GLOBAL","No");
        
        if(data.containsKey("hlr:Dynamic Status Information For GSM:MsPurgedForNonGprs") && "TRUE".equals(data.get("hlr:Dynamic Status Information For GSM:MsPurgedForNonGprs")))
            data.put("hlr:Dynamic Status Information For GSM:MsPurgedForNonGprs","TRUE(Purge)");
        
        if(data.containsKey("hlr:Dynamic Status Information For GPRS:MsPurgedForGprs") && "TRUE".equals(data.get("hlr:Dynamic Status Information For GPRS:MsPurgedForGprs")))
            data.put("hlr:Dynamic Status Information For GPRS:MsPurgedForGprs","TRUE(Purge)");
        
        String gprs = data.get("hlr:GPRS Data:APNTPLID");
        String eps = data.get("hlr:EPS Data:APNTPLID");
        
        if(gprs != null && !gprs.isEmpty()) {
            String apnlst[] = gprs.split(",");
            for(String n : apnlst) {
                if(data.containsKey("hlr:GPRS APN"))
                    data.put("hlr:GPRS APN",data.get("hlr:GPRS APN") + "\n" + apn.get(n) + "(" + n + ")");
                else
                    data.put("hlr:GPRS APN",apn.get(n) + "(" + n + ")");
            }
        }
        
        if(eps != null && !eps.isEmpty()) {
            String apnlst[] = eps.split(",");
            for(String n : apnlst) {
                if(data.containsKey("hlr:EPS APN"))
                    data.put("hlr:EPS APN",data.get("hlr:EPS APN") + "\n" + apn.get(n) + "(" + n + ")");
                else
                    data.put("hlr:EPS APN",apn.get(n) + "(" + n + ")");
            }
        }
        
        prtlst(ne,data,chklist);
    }
    
    
    public static void ccbs (HashMap<String,String> data) {
        
        String ne = "ccbs";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("ccbs:tao_ban","BAN");
            chklist.put("ccbs:customer_no","Customer No");
            chklist.put("ccbs:category","Category");
            chklist.put("ccbs:priceplan","Priceplan");
            chklist.put("ccbs:bill_cycle","Bill Cycle");
            chklist.put("ccbs:pp_desc","Promotion");
            chklist.put("ccbs:ar_balance","Debt");
            chklist.put("ccbs:credit_limit","Credit Limit");
            //chklist.put("ccbs:payment_method","Payment Method");
            //chklist.put("ccbs:id","Id");
            chklist.put("ccbs:name","Name");
            chklist.put("ccbs:type_desc","Desc");
            chklist.put("ccbs:addr","Address");
        
        if("I".equals(data.get("ccbs:category")))
            data.put("ccbs:category","Indy");
        else if("C".equals(data.get("ccbs:category")))
            data.put("ccbs:category","Corp");
        else if("B".equals(data.get("ccbs:category")))
            data.put("ccbs:category","Biz");
        
        data.put("ccbs:ar_balance",data.get("ccbs:ar_balance") + " ฿");
        data.put("ccbs:credit_limit",data.get("ccbs:credit_limit") + " ฿");
        
        prtlst(ne,data,chklist);
    }
    
    public static void pcrf (HashMap<String,String> data) {
        
        String ne = "pcrf";
        
        Map<String,String> chklist = Collections.synchronizedMap (new LinkedHashMap());
            chklist.put("pcrf:subscriber:USRSTATE","Status");
            chklist.put("pcrf:subscriber:USRPAIDTYPE","Account Type");
            chklist.put("pcrf:subscriber:USRSTATION","Station");
            chklist.put("pcrf:subscriber:USRMASTERIDENTIFIER","Master");
            chklist.put("pcrf:subscriber:USRBILLCYCLEDATE","Bill Cycle");
            chklist.put("pcrf:subscriber:USRLATESTOFFLINETIME","Last Online");
            chklist.put("pcrf:servicePackage:SRVPKGNAME","Package");
            chklist.put("pcrf:subscribedService:SRVNAME","Service");
            chklist.put("pcrf:subscriberQuota:BALANCE","Quota");
            chklist.put("pcrf:subscriberQuota:QTANAME:","Quota Name");
            chklist.put("pcrf:subscriberQuota:SRVNAME","Quota Service");
        
        HashMap<String,String> state = new HashMap<String,String>() {{
            put("1","normal");
            put("2","frozen");
            put("65","SubStatusA");
            put("66","SubStatusB");
            put("67","SubStatusC");
            put("68","SubStatusD");
            put("69","SubStatusE");
            put("70","SubStatusF");
        }};
        
        HashMap<String,String> type = new HashMap<String,String>() {{
            put("0","PRE");
            put("1","POS");
        }};
        
        HashMap<String,String> station = new HashMap<String,String>() {{
            put("1","master");
            put("2","slave");
        }};
        
        data.put("pcrf:subscriber:USRSTATE",data.get("pcrf:subscriber:USRSTATE") + " (" + state.get(data.get("pcrf:subscriber:USRSTATE")) + ")");
        data.put("pcrf:subscriber:USRPAIDTYPE",data.get("pcrf:subscriber:USRPAIDTYPE") + " (" + type.get(data.get("pcrf:subscriber:USRPAIDTYPE")) + ")");
        data.put("pcrf:subscriber:USRSTATION",data.get("pcrf:subscriber:USRSTATION") + " (" + station.get(data.get("pcrf:subscriber:USRSTATION")) + ")");
        prtlst(ne,data,chklist);
    }
    
    public static void printe2e (HashMap<String,String> data) {
        
        sps(data);
        cdb(data);
        if("success".equals(data.get("cdb:rs"))) {
            hlr(data);
            dmc(data);
            pcrf(data);
            if("PRE".equals(data.get("cdb:spAccountType")))
                ccp(data);
            else
                ccbs(data);
        }
        
    }
    
}
