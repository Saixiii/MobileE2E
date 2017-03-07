package com.truemove.msoc.downstream;

import java.util.HashMap;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : sps
 * @Date    : Mar 17, 2016 12:08:06 PM
 */

public class sps {
    
    private static final String CONT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String SE_AUTHEN = "simple";
    private static String PROVIDER_URL = "";
    private static String SE_PRINCIPAL = "";
    private static String SE_CREDENTIALS = "";
    
    private static HashMap query(String msisdn) {
        
        //Attr for query
        String[] seekAttrs = null;
        //Create search
        SearchControls searchCtl = new SearchControls();
        searchCtl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtl.setReturningAttributes(seekAttrs);
        
        //The name of the context or object to search
        String name = "msisdn=" + msisdn + ",domainName=mnp,O=True,C=TH";
        //Filter cn start with TA
        String filter = "(objectclass=*)";
        HashMap cusattr = new HashMap();
        NamingEnumeration namingEnum = null;
        DirContext dirCtx = null;
        try {
            dirCtx = getDirContext();
            namingEnum = dirCtx.search(name, filter , searchCtl);
            //namingEnum = dirCtx.search(name, filter, null); 
            while (namingEnum.hasMore()) {
                SearchResult searchRs = (SearchResult) namingEnum.next();
                Attributes attrs = searchRs.getAttributes();
                NamingEnumeration namingAttr = attrs.getAll();
                while (namingAttr.hasMoreElements()) {
                    Attribute attr = (Attribute) namingAttr.next();
                    if(cusattr.get("sps:" + attr.getID()) == null)
                        cusattr.put("sps:" + attr.getID() , attr.get(0));
                    else
                        cusattr.put("sps:" + attr.getID() , cusattr.get("sps:" + attr.getID()) + "|" + attr.get(0));
                }
            }
            namingEnum.close();
            cusattr.put("sps:rs","success");
            
        } catch (NamingException ne) {
            cusattr.put("sps:rs","Account is not found in SPS");
        } catch (Exception e) {
            cusattr.put("sps:rs",e);
            e.printStackTrace();
        } finally {
            if (dirCtx != null) {
                try {
                    dirCtx.close();
                } catch (NamingException e) {}
            }
            if(namingEnum != null){
                try {
                    namingEnum.close();
                    namingEnum = null;
                } catch (NamingException e) {}
            }
            return cusattr;
        }
    }
    
    private static DirContext getDirContext() {
        Properties pros = new Properties();
        DirContext dirCtx = null;
        try {
            pros.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONT_FACTORY);
            pros.setProperty(Context.SECURITY_AUTHENTICATION, SE_AUTHEN);
            pros.setProperty(Context.PROVIDER_URL, PROVIDER_URL);
            pros.setProperty(Context.SECURITY_PRINCIPAL, SE_PRINCIPAL);
            pros.setProperty(Context.SECURITY_CREDENTIALS, SE_CREDENTIALS);
            
            //Optional
            pros.setProperty("com.sun.jndi.ldap.connect.pool", "true");
            pros.setProperty("com.sun.jndi.ldap.connect.pool.initsize", "2");
            pros.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", "5");
            pros.setProperty("com.sun.jndi.ldap.connect.pool.prefsize", "2");
            
            dirCtx = new InitialDirContext(pros);
        } catch (Exception e) {}
        return dirCtx;
    }
    
    public static HashMap run(String ne,String ip,String port,String user,String pass,String msisdn) {
        
        PROVIDER_URL = "LDAP://" + ip + ":" + port;
        SE_PRINCIPAL =  user;
        SE_CREDENTIALS = pass;
        
        HashMap<String,String> data = query("66" + msisdn);
        
        return data;
    }
}
