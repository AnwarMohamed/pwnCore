package com.anwarelmakrahy.pwncore;


import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tMap;
import static org.msgpack.template.Templates.TValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.msgpack.template.Templates.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MsfRpcClient {
	
	private Context context;
	
	private String host = "";
	private String port;
	private String uri;
	private boolean ssl;
	
	private String url;
	private boolean authenticated;
	private String token;
	private HttpClient client;
	
	MsfRpcClient(Context c, Map<String, Object> opts) {
		
		this.context = c;
		
		host = opts.containsKey("host") ? opts.get("host").toString() : "127.0.0.1";
		port = opts.containsKey("port") ? opts.get("port").toString() : "55553";
		uri = opts.containsKey("uri") ? opts.get("uri").toString() : "/api/";
		ssl = opts.containsKey("ssl") ? (Boolean)opts.get("ssl") : false;
		
		url = (ssl ? "https://": "http://") + host + ":" + port + uri; 
		
		authenticated = false;
		token = null;
		
		
		HttpParams params = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(params, 5000);
	    HttpConnectionParams.setSoTimeout(params, 5000);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
	    
		client = (ssl ? sslClient(new DefaultHttpClient(cm, params)) : new DefaultHttpClient(cm, params));	
	}
		
	public static List<Object> singleOptCallList(String s) {
		List<Object> tmp = new ArrayList<Object>();
		tmp.add(s);
		return tmp;
	}
	
	public boolean login(String username, String password) {
		HttpPost httpPost = newHttpPost(encode(new String[] { "auth.login", username, password }));
		Map<String, Value> res = getResponse(httpPost);
		
		if (res != null) {
			if (res.containsKey("result") && 
					res.get("result").asRawValue().getString().equals("success")) {
				token = res.get("token").asRawValue().getString();
				
				httpPost = newHttpPost(encode(new String[] { "auth.token_generate", token}));
				res = getResponse(httpPost);
				
				if (res != null && 
						res.containsKey("result") && 
						res.get("result").asRawValue().getString().equals("success")) {
					token = res.get("token").asRawValue().getString();
					authenticated = true;
					return true;
				}
				
				return false;
			}
			
			Intent tmpIntent = new Intent();
			tmpIntent.setAction(StaticsClass.PWNCORE_AUTHENTICATION_FAILED);
			context.sendBroadcast(tmpIntent);
			return false;
		}
		return false;
	}
	
	private Map<String, Value> getResponse(HttpPost p) {
		try {
			
			HttpResponse response = client.execute(p);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				Map<String, Value> res = decode(entity.getContent());
				entity.consumeContent();
				return res;
			}
			
		} catch (ClientProtocolException e) {
			Log.e("GetResponse", "ClientProtocolException: " + e);
		} catch (ConnectTimeoutException e) {
			Log.e("GetResponse", "ConnectTimeoutException: " + e);
			
			Intent tmpIntent = new Intent();
			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
			context.sendBroadcast(tmpIntent);
			
		} catch (HttpHostConnectException e) {
			Log.e("GetResponse", "HttpHostConnectException: " + e);
			
			Intent tmpIntent = new Intent();
			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			context.sendBroadcast(tmpIntent);
			
		} catch (IOException e) {
			Log.e("GetResponse", "IOException: " + e);
			
			Intent tmpIntent = new Intent();
			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			context.sendBroadcast(tmpIntent);
		}
		return null;
	}
	
	public Map<String, Value> call(List<Object> data) {
		if (authenticated) {
			data.add(1, token);
			HttpPost httpPost = newHttpPost(encode(data));
			return getResponse(httpPost);
		}
		
		Log.d("RpcCall", "Not Authenticated");
		return null;
	}
	
	
	public String[] getModules(String type) {
		String moduleTag = "module.";
		
		if (type.contains(StaticsClass.PWNCORE_LOAD_EXPLOITS))
			moduleTag += "exploits";
		else if (type.contains(StaticsClass.PWNCORE_LOAD_PAYLOADS))
			moduleTag += "payloads";
		else if (type.contains(StaticsClass.PWNCORE_LOAD_AUXILIARY))
			moduleTag += "auxiliary";
		else if (type.contains(StaticsClass.PWNCORE_LOAD_POSTS))
			moduleTag += "post";
		else if (type.contains(StaticsClass.PWNCORE_LOAD_ENCODERS))
			moduleTag += "encoders";
		else if (type.contains(StaticsClass.PWNCORE_LOAD_NOPS))
			moduleTag += "nops";
		else return null;

		List<Object> opts = new ArrayList<Object>();
		opts.add(moduleTag);
		
		Map<String, Value> res = call(opts);
		
		if (res != null &&
				res.containsKey("modules")) {
			List<String> list = null;
			
			try {
				list = new Converter(res.get("modules")).read(tList(TString));
			} catch (IOException e) {
				Log.e("GetModules", "IOException: " + e);
			}
			
			return list.toArray(new String[list.size()]);
		}
			
		return null;
	}
	
	private Template<Map<String, Value>> decodeAsMap = tMap(TString, TValue);
	private Template<Map<Integer, Value>> decodeAsIntMap = tMap(TInteger, TValue);
	
	private Map<String, Value> decode(InputStream in) throws IOException {
		MessagePack msgpack = new MessagePack();
		Value tmpResult = msgpack.read(in);
		
        try {
        	
        	return new Converter(tmpResult).read(decodeAsMap);
        } catch (Exception e) {
        	Map<Integer, Value> before = new Converter(tmpResult).read(decodeAsIntMap);
        	Map<String, Value> after = new HashMap<String, Value>();
        	
        	Integer[] keys = before.keySet().toArray(new Integer[before.size()]);
        	Value[] values = before.values().toArray(new Value[before.size()]);
        	for (int i=0; i<before.size(); i++)
        		after.put(keys[i].toString(), values[i]);
 
        	return after;
        }
	}
	
	private byte[] encode(String[] s) {
		MessagePack msgpack = new MessagePack();			
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);		      	        
        try {
			packer.write(s);
		} catch (IOException e) {
			Log.e("MsgpackEncode", "IOException: " + e);
		}		        
        return out.toByteArray();
	}
	
	private byte[] encode(List<Object> l) {
		MessagePack msgpack = new MessagePack();			
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);		      	        
        try {
			packer.write(l);
		} catch (IOException e) {
			Log.e("MsgpackEncode", "IOException: " + e);
		}		        
        return out.toByteArray();
	}
	
	private HttpPost newHttpPost(byte[] postData) {
		HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "binary/message-pack");       
        ByteArrayEntity entity = new ByteArrayEntity(postData);
        httpPost.setEntity(entity);     
        return httpPost;
	}
	
	public class MySSLSocketFactory extends SSLSocketFactory {
	     SSLContext sslContext = SSLContext.getInstance("TLS");	  
	     public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, 
	     KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	         super(truststore);
	         TrustManager tm = new X509TrustManager() {
	             public void checkClientTrusted(X509Certificate[] chain, String authType) 
	            		 throws CertificateException {}
	             public void checkServerTrusted(X509Certificate[] chain, String authType) 
	            		 throws CertificateException {}
	             public X509Certificate[] getAcceptedIssuers() { return null; } 
             };
	         sslContext.init(null, new TrustManager[] { tm }, null);
	     }	   
	     public MySSLSocketFactory(SSLContext context) throws KeyManagementException, 
	     	NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }
     
	     @Override public Socket createSocket(Socket socket, String host, int port, boolean autoClose) 
	    		 throws IOException, UnknownHostException {
	         return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	     }
    
	     @Override public Socket createSocket() throws IOException {
	         return sslContext.getSocketFactory().createSocket();
	     }
	}
	
	private HttpClient sslClient(HttpClient client) {
		try {
			X509TrustManager tm = new X509TrustManager() { 
				public void checkClientTrusted(X509Certificate[] xcs, String string) 
            		throws CertificateException {}
            
				public void checkServerTrusted(X509Certificate[] xcs, String string) 
            		throws CertificateException {}
            
				public X509Certificate[] getAcceptedIssuers() {return null; }
			};		        
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = client.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, client.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	public String getHost() {	
		return host;
	}
}
