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
package net.tsz.afinal.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import net.tsz.afinal.common.AsyncTask;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import net.tsz.afinal.http.entityhandler.FileEntityHandler;
import net.tsz.afinal.http.entityhandler.StringEntityHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;


public class  AjaxRequestHandler extends  AsyncTask<Object, Object, Object> {

	private final AbstractHttpClient client;
	private final HttpContext context;
	
	private final static StringEntityHandler mStrEntityHandler = new StringEntityHandler();
	private final static FileEntityHandler mFileEntityHandler = new FileEntityHandler();
	
	@SuppressWarnings("rawtypes")
	private final AjaxCallBack callback;
	
	private int executionCount = 0;
	private String targetUrl = null;
	private String charset;

	public AjaxRequestHandler(AbstractHttpClient client, HttpContext context, AjaxCallBack<?> callback,String charset) {
		this.client = client;
		this.context = context;
		this.callback = callback;
		this.charset = charset;
	}


	private void makeRequestWithRetries(HttpUriRequest request) throws ConnectException {
		
		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
		while (retry) {
			try {
				if (!isCancelled()) {
					HttpResponse response = client.execute(request, context);
					if (!isCancelled()) {
						handleResponse(response);
					} 
				}
				return;
			} catch (UnknownHostException e) {
				publishProgress(Update_failure, e,"can't resolve host");
				return;
			} catch (IOException e) {
				cause = e;
				retry = retryHandler.retryRequest(cause, ++executionCount,context);
			} catch (NullPointerException e) {
				// HttpClient 4.0.x 之前的一个bug
				// http://code.google.com/p/android/issues/detail?id=5255
				cause = new IOException("NPE in HttpClient" + e.getMessage());
				retry = retryHandler.retryRequest(cause, ++executionCount,context);
			}
		}
		
		//没有能读取到网络数据
		ConnectException ex = new ConnectException();
		ex.initCause(cause);
		throw ex;
	}

	@Override
	protected Object doInBackground(Object... params) {
		if(params!=null && params.length == 2)
			targetUrl = String.valueOf(params[1]);
		
		try {
			publishProgress(Update_start); // 开始
			
			makeRequestWithRetries((HttpUriRequest)params[0]);
			
			publishProgress(Update_failure,null,null); // 结束
		} catch (IOException e) {
			publishProgress(Update_failure,e,null); // 结束
		}

		return null;
	}

	private final static int Update_start = 1;
	private final static int Update_loading = 2;
	private final static int Update_failure = 3;
	private final static int Update_success = 4;

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(Object... values) {
		int update = Integer.valueOf(String.valueOf(values[0]));
		switch (update) {
		case Update_start:
			if(callback!=null)
				callback.onStart();
			break;
		case Update_loading:
			if(callback!=null)
				callback.onLoading(Long.valueOf(String.valueOf(values[1])),Long.valueOf(String.valueOf(values[2])));
			break;
		case Update_failure:
			if(callback!=null)
				callback.onFailure((Throwable)values[1],(String)values[2]);
			break;
		case Update_success:
			if(callback!=null)
				callback.onSuccess(values[1]);
			break;
		default:
			break;
		}
		super.onProgressUpdate(values);
	}

	private void handleResponse(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() >= 300) {
			publishProgress(Update_failure,new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()),
					"response status code:"+status.getStatusCode());
		} else {
			try {
				HttpEntity temp = response.getEntity();
				Object responseBody = null;
				if (temp != null) {
					if(targetUrl!=null)
						responseBody = mFileEntityHandler.handleEntity(temp,new EntityCallBackImpl(),targetUrl);
					else
						responseBody = mStrEntityHandler.handleEntity(temp,new EntityCallBackImpl(),charset);
				}
				publishProgress(Update_success,responseBody);
				
			} catch (IOException e) {
				publishProgress(Update_failure,e,(String)null);
			}
			
		}
	}
	
	class EntityCallBackImpl implements EntityCallBack{
		private long time;
		public EntityCallBackImpl() {
			time = SystemClock.uptimeMillis();
		}
		@Override
		public void callBack(long count, long current) {
			if(callback!=null && callback.isProgress()){
				long thisTime = SystemClock.uptimeMillis();
				if(thisTime - time > callback.getRate()){
					time = thisTime ;
					publishProgress(Update_loading,count,current);
				}
			}
		}
		
	}
	

}
