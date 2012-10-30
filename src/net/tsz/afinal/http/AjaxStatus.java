/**
 * Copyright (c) 2012-2013, Michael Yang Ñî¸£º£ (www.yangfuhai.com).
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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AjaxStatus {

	private int status;
	private String url ; 
	private byte[] content;
    private Map<String, String> headers;
	private Map<String, String> cookies;
    
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getUrl() {
		return url;
	}
	public AjaxStatus setUrl(String url) {
		this.url = url;
		return this;
	}
	public byte[] getContent() {
		return content;
	}
	
	public String getContentAsString() {
		return content==null?null:new String(content);
	}
	
	public AjaxStatus setContent(byte[] content) {
		this.content = content;
		return this;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	public AjaxStatus setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}
	public Map<String, String> getCookies() {
		return cookies;
	}
	public AjaxStatus setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}
	
	
	public AjaxStatus cookie(String name, String value){
		if(cookies == null){
			cookies = new HashMap<String, String>();
		}
		cookies.put(name, value);
		return this;
	}
	
	
	public String makeCookie(){
		if(cookies == null || cookies.size() == 0) return null;
		Iterator<String> iter = cookies.keySet().iterator();
		
		StringBuilder sb = new StringBuilder();
		
		while(iter.hasNext()){
			String key = iter.next();
			String value = cookies.get(key);
			sb.append(key);
			sb.append("=");
			sb.append(value);
			if(iter.hasNext()){
				sb.append("; ");
			}
		}
		return sb.toString();
	}
	
}
