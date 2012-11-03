/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLHandshakeException;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxRequest;
import net.tsz.afinal.http.AjaxStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;

public class FinalHttp {
	
	private static final String CHARSET_UTF8 = "UTF-8";

	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
		public boolean retryRequest(IOException exception, int executionCount,HttpContext context) {
			if (executionCount >= 3) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// Do not retry on SSL handshake exception
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}
	};
	
	private static ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String charset = EntityUtils.getContentCharSet(entity) == null ? CHARSET_UTF8 : EntityUtils.getContentCharSet(entity);
				return new String(EntityUtils.toByteArray(entity), charset);
			} else {
				return null;
			}
		}
	};
	
	
	private static ResponseHandler<AjaxStatus> ajaxHandler = new ResponseHandler<AjaxStatus>() {
		public AjaxStatus handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			
			AjaxStatus ajaxResponse = new AjaxStatus();
			
			HttpEntity entity = response.getEntity();
			if(entity!=null)
				ajaxResponse.setContent(EntityUtils.toByteArray(entity));
			
			ajaxResponse.setStatus(response.getStatusLine().getStatusCode());
			
			return ajaxResponse;
		}
	};
	
	
	private static ExecutorService ajaxExecutor = Executors.newFixedThreadPool(5,new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			// 设置线程的优先级别，让线程先后顺序执行（级别越高，抢到cpu执行的时间越多）
			t.setPriority(Thread.NORM_PRIORITY - 1);
			return t;
		}
	});
	

	public static String get(String url) {
		return get(url, null, null);
	}

	public static String get(String url, Map<String, String> params) {
		return get(url, params, null);
	}

	public static String get(String url, Map<String, String> params,String charset) {
		if (url == null || url.trim().length()==0) {
			return null;
		}
		DefaultHttpClient httpclient = null;
		HttpGet hg = null;
		try {
			url = patchUrl(url);
			List<NameValuePair> qparams = getParamsList(params);
			if (qparams != null && qparams.size() > 0) {
				charset = (charset == null ? CHARSET_UTF8 : charset);
				String formatParams = URLEncodedUtils.format(qparams, charset);
				url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams) : (url.substring(0, url.indexOf("?") + 1) + formatParams);
			}
			httpclient= getDefaultHttpClient(charset);
			hg = new HttpGet(url);
			return httpclient.execute(hg, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			abortConnection(hg, httpclient);
		}
		return null;
	}
	
	
	
	
	public static String post(String url, Map<String, String> params) {
		return post(url, params, null);
	}

	public static String post(String url, Map<String, String> params,String charset) {
		if (url == null || url.trim().length()==0) {
			return null;
		}
		url = patchUrl(url);
		DefaultHttpClient httpclient = getDefaultHttpClient(charset);
		UrlEncodedFormEntity formEntity = null;
		try {
			if (charset == null || charset.trim().length()==0) {
				formEntity = new UrlEncodedFormEntity(getParamsList(params));
			} else {
				formEntity = new UrlEncodedFormEntity(getParamsList(params),
						charset);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		HttpPost hp = new HttpPost(url);
		hp.setEntity(formEntity);
		try {
			return  httpclient.execute(hp, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			abortConnection(hp, httpclient);
		}
		return null;
	}
	
	
	public static void ajax(String url, AjaxCallBack callBack) {
		AjaxRequest request = new AjaxRequest(url);
		ajaxExecutor.submit(new AjaxTask(callBack, request));
	}
	
	private static AjaxStatus ajax(AjaxRequest request) {
		if (request == null ) {
			return null;
		}
		String url = patchUrl(request.getUrl());
		String charset = request.getCharset();
		DefaultHttpClient httpclient = getDefaultHttpClient(charset);
		UrlEncodedFormEntity formEntity = null;
		try {
			List<NameValuePair> paramList = getParamsList(request.getParams());
			if(paramList!=null && paramList.size()>0){
				if (charset == null || charset.trim().length()==0) {
					formEntity = new UrlEncodedFormEntity(getParamsList(request.getParams()));
				} else {
					formEntity = new UrlEncodedFormEntity(getParamsList(request.getParams()),charset);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		HttpPost hp = new HttpPost(url);
		hp.setEntity(formEntity);
		try {
			return httpclient.execute(hp,ajaxHandler).setUrl(request.getUrl());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			abortConnection(hp, httpclient);
		}
		return null;
	}
	
	
	public static String postSend(String url,String data,String charset) {
		if (url == null || url.trim().length()==0) {
			return null;
		}
		DefaultHttpClient httpclient = getDefaultHttpClient(charset);
		StringEntity formEntity = null;
		try {
			if (charset == null || charset.trim().length()==0) {
				formEntity = new StringEntity(data);
			} else {
				formEntity = new StringEntity(data,charset);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		HttpPost hp = new HttpPost(url);
		hp.setEntity(formEntity);
		try {
			return httpclient.execute(hp, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			abortConnection(hp, httpclient);
		}
		return null;
	}
	
	
	
	private static DefaultHttpClient getDefaultHttpClient(final String charset) {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,charset == null ? CHARSET_UTF8 : charset);
		httpclient.setHttpRequestRetryHandler(requestRetryHandler);
		
		return httpclient;
	}

	private static void abortConnection(final HttpRequestBase hrb,final HttpClient httpclient) {
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}

	private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
		if (paramsMap == null || paramsMap.size() == 0) {
			return null;
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> map : paramsMap.entrySet()) {
			params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
		}
		return params;
	}
	
	
	private static String patchUrl(String url){
		return url.replaceAll(" ", "%20").replaceAll("\\|", "%7C");
	}
	
	
	
	static class AjaxTask implements Runnable{
		final private AjaxCallBack mCallBack;
		final private AjaxRequest request;
		
		final private Handler mHandler= new Handler(){
			public void handleMessage(Message msg) {
				mCallBack.callBack((AjaxStatus)msg.obj);
			}
		};
		
		public AjaxTask(AjaxCallBack callBack, AjaxRequest request) {
			this.mCallBack = callBack;
			this.request = request;
		}

		public void run() {
			Message msg = new Message();
			msg.obj = ajax(request);
			mHandler.sendMessage(msg);
		}
	}
}