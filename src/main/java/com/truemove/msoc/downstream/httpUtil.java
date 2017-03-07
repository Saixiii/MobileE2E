package com.truemove.msoc.downstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;


/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : downstream
 * @Class   : httpUtil
 * @Date    : Mar 7, 2016 12:52:59 AM
 */

public class httpUtil {
    
    private final int timeout = 10 * 1000;
    
    
    public HashMap<String, String> httpreq (String url,String reqxml) throws Exception {
        
        HashMap<String, String> res = new HashMap<String, String>();
        
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Jakarta Commons-HttpClient/4.5.1");
        httpPost.setHeader("Content-Type", "application/xml");
        httpPost.setHeader("SOAPAction", "");
        httpPost.setHeader("Connection", "close");
        
        try {
            StringEntity xmlEntity = new StringEntity(reqxml);
            httpPost.setEntity(xmlEntity);
            httpResponse = httpClient.execute(httpPost);
            
            if("200".equals(Integer.toString(httpResponse.getStatusLine().getStatusCode()))) {
                res.put("xml",EntityUtils.toString(httpResponse.getEntity(),"UTF-8"));
                res.put("rs","success");
            } else {
                res.put("rs",Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            }
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(httpUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            res.put("rs","Timeout");
        } finally {
            httpClient.close();
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return res;
    }
    
    public HashMap<String, String> httpsreq (String url,String reqxml,String trustkey,String password) throws Exception {
        
        HashMap<String, String> res = new HashMap<String, String>();
        
        KeyStore keyStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File(trustkey));
        try {
            keyStore.load(instream, password.toCharArray());
        } finally {
            instream.close();
        }
        
        
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, password.toCharArray())
                .loadTrustMaterial(keyStore)
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslsf)
                .build();
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Jakarta Commons-HttpClient/4.5.1");
        httpPost.setHeader("Content-Type", "application/xml");
        httpPost.setHeader("SOAPAction", "");
        httpPost.setHeader("Connection", "close");
        
        try {
            StringEntity xmlEntity = new StringEntity(reqxml);
            httpPost.setEntity(xmlEntity);
            httpResponse = httpClient.execute(httpPost);
            
            if("200".equals(Integer.toString(httpResponse.getStatusLine().getStatusCode()))) {
                res.put("xml",EntityUtils.toString(httpResponse.getEntity(),"UTF-8"));
                res.put("rs","success");
            } else {
                res.put("rs",Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            }
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(httpUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(httpUtil.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            res.put("rs","Timeout");
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
            httpClient.close();
        }
        return res;
    }
    
    private static Document convertStringToDocument(String xmlStr) {
        
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new StringReader(xmlStr));
            return doc;
        } catch (DocumentException ex) {
            Logger.getLogger(httpUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static HashMap<String, String> parserXMLelement (String xml,String ne,String path) throws JaxenException {
    
        HashMap<String, String> data = new HashMap<String, String>();
        XPath xpath = new Dom4jXPath(path);
        Document doc = convertStringToDocument(xml.trim());
        List<Element> results = xpath.selectNodes(doc);
        for (Element element : results) {
            String key = String.valueOf(ne + ":" + element.getName());
            if(data.containsKey(key))
                data.put(key,data.get(key) + "," + String.valueOf(element.getData()));
            else
                data.put(key,String.valueOf(element.getData()));
        }
        
        return data;
    }
    
    public static HashMap<String, String> parserXMLattribute (String xml,String ne,String path) throws JaxenException {
    
        HashMap<String, String> data = new HashMap<String, String>();
        Document doc = convertStringToDocument(xml.trim());
        List<Attribute> results = doc.selectNodes(path);
        for (Attribute attribute : results) {
            String key = String.valueOf(ne + ":" + attribute.getName());
            if(data.containsKey(key))
                data.put(key,data.get(key) + "," + String.valueOf(attribute.getData()));
            else
                data.put(key,String.valueOf(attribute.getData()));
        }
        
        return data;
    }
    
    public static HashMap<String, String> parserXMLpair(String xml,String ne,String path) throws JaxenException {
    
        HashMap<String, String> data = new HashMap<String, String>();
        Document doc = convertStringToDocument(xml.trim());
        List<Element> results = doc.selectNodes(path);
        for (Element element : results) {
            String key = ne + ":" + element.elementTextTrim("key");
            String val = element.elementTextTrim("value");
            if(data.containsKey(key))
                data.put(key,data.get(key) + "," + val);
            else
                data.put(key,val);    
        }
        
        return data;
    }
    
}
