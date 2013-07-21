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
package net.tsz.afinal.bitmap.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.util.Log;

/**
 * @title 根据 图片url地址下载图片  可以是本地和网络
 * @author 杨福海（michael）  www.yangfuhai.com
 */
public class SimpleDownloader implements Downloader{
	
	private static final String TAG = "SimpleHttpDownloader";
	private static final int IO_BUFFER_SIZE = 8 * 1024; //8k
	
	
	public byte[] download(String urlString) {
		 	if(urlString == null)
		 		return null;
		 	
		 	if(urlString.trim().toLowerCase().startsWith("http")){
		 		return getFromHttp(urlString);
		 	}else if(urlString.trim().toLowerCase().startsWith("file")){
		 		try {
					return getFromFile(new URI(urlString).getPath());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
		 	}else if(urlString.trim().toLowerCase().startsWith("/")){
		 		return getFromFile(urlString);
		 	}
		 	
		 	return null;
	    }


	private byte[] getFromFile(String urlString) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileReader f = new FileReader(new File(urlString));
			int b;
		    while ((b = f.read()) != -1) {
		    	baos.write(b);
		    }
		    return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	private byte[] getFromHttp(String urlString) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		FlushedInputStream in = null;

		try {
		    final URL url = new URL(urlString);
		    urlConnection = (HttpURLConnection) url.openConnection();
		    in = new FlushedInputStream(new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE));
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    int b;
		    while ((b = in.read()) != -1) {
		    	baos.write(b);
		    }
		    return baos.toByteArray();
		} catch (final IOException e) {
		    Log.e(TAG, "Error in downloadBitmap - "+urlString +" : " + e);
		} finally {
		    if (urlConnection != null) {
		        urlConnection.disconnect();
		    }
		    try {
		        if (out != null) {
		            out.close();
		        }
		        if (in != null) {
		            in.close();
		        }
		    } catch (final IOException e) {}
		}
		return null;
	}

	    
	    public class FlushedInputStream extends FilterInputStream {

	    	public FlushedInputStream(InputStream inputStream) {
	    		super(inputStream);
	    	}

	    	@Override
	    	public long skip(long n) throws IOException {
	    		long totalBytesSkipped = 0L;
	    		while (totalBytesSkipped < n) {
	    			long bytesSkipped = in.skip(n - totalBytesSkipped);
	    			if (bytesSkipped == 0L) {
	    				int by_te = read();
	    				if (by_te < 0) {
	    					break; // we reached EOF
	    				} else {
	    					bytesSkipped = 1; // we read one byte
	    				}
	    			}
	    			totalBytesSkipped += bytesSkipped;
	    		}
	    		return totalBytesSkipped;
	    	}
	    }
}
