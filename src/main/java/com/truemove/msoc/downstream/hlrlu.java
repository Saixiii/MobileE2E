package com.truemove.msoc.downstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : hlrlu
 * @Date    : Mar 25, 2016 10:14:41 AM
 */

public class hlrlu {
    
    // HLR MML API display
    public static HashMap run (String ne,String ip,String port,String user,String pass,String msisdn) {
        
        String login = "-1";
        HashMap<String, String> data = new HashMap<String, String>();
        
        try {
        
            // Socket HLR-RMV engine.
            Socket sock = new Socket(ip, Integer.parseInt(port));
            sock.setSoTimeout(10000);
            
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            
            // MML login
            wr.write("LGI: OPNAME=\"" + user + "\", PWD=\"" + pass + "\";\r\n");
            wr.flush();
            Thread.sleep(2000);
            
            String line;
            
            // Result login
            while(!(line=rd.readLine()).startsWith("---")) {
                if(line.startsWith("RETCODE")) {
                    String splitlogin[] = line.split("\\s");
                    login = splitlogin[2];
                    data.put(ne + ":rs",login);
                }
            }
            
            // Display list msisdn
            if(login.equals("0")) {
                
                String group = ne + ":";
                        
                // MML display
                wr.write("LST DYNSUB: ISDN=\"66" + msisdn + "\";\r\n");
                wr.flush();
                
                // Read output data
                while(!(line=rd.readLine()).startsWith("---")) {
                    // Read display result
                    if(line.startsWith("RETCODE")) {
                        String splitdiscode[] = line.split("\\s");
                        String splitdisdesc[] = line.split("=");
                        if("0".equals(splitdiscode[2]))
                            data.put(ne + ":rs","success");
                        else
                            data.put(ne + ":rs",splitdisdesc[1]);
                    }
                    
                    // Read display group
                    if(line.startsWith("\"") && line.endsWith("\""))
                        group = ne + ":" + line.replaceAll("\"","") + ":";
                    
                    // Read display data
                    if(line.length() > 1 && Character.isWhitespace(line.charAt(0))) {
                        String splitdata[] = line.trim().split("=");
                        String key = group + splitdata[0].trim(); 
                        if(splitdata.length > 1)
                            if(data.containsKey(key))
                                data.put(key,data.get(key) + "," + splitdata[1].trim());
                            else
                                data.put(key,splitdata[1].trim());
                        else
                            data.put(key,"");
                        
                    }
                }
            }
            
            
        } catch (SocketException ex) {
            Logger.getLogger(hlr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(hlr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(hlr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(hlr.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }
    
}