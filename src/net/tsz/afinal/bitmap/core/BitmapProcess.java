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
package net.tsz.afinal.bitmap.core;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import net.tsz.afinal.bitmap.download.Downloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapProcess {
	private static final String TAG = "BitmapProcess";
	private boolean mHttpDiskCacheStarting = true;
	private int cacheSize;
	private static final int DEFAULT_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

	private LruDiskCache mOriginalDiskCache;//原始图片的路径，不进行任何的压缩操作
	private final Object mHttpDiskCacheLock = new Object();
	private static final int DISK_CACHE_INDEX = 0;

	private File mOriginalCacheDir;
	private Downloader downloader;
	
	private boolean neverCalculate = false;

	public BitmapProcess(Downloader downloader,String filePath,int cacheSize) {
		this.mOriginalCacheDir = new File(filePath+"/original");
		this.downloader = downloader;
		if(cacheSize<=0)
			cacheSize = DEFAULT_CACHE_SIZE;
		this.cacheSize = cacheSize;
	}
	
	public void configCalculateBitmap(boolean neverCalculate){
		this.neverCalculate = neverCalculate;
	}

	public Bitmap processBitmap(String data, BitmapDisplayConfig config) {
		final String key = FileNameGenerator.generator(data);
		FileDescriptor fileDescriptor = null;
		FileInputStream fileInputStream = null;
		LruDiskCache.Snapshot snapshot;
		synchronized (mHttpDiskCacheLock) {
			// Wait for disk cache to initialize
			while (mHttpDiskCacheStarting) {
				try {
					mHttpDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}

			if (mOriginalDiskCache != null) {
				try {
					snapshot = mOriginalDiskCache.get(key);
					if (snapshot == null) {
						LruDiskCache.Editor editor = mOriginalDiskCache.edit(key);
						if (editor != null) {
							if (downloader.downloadToLocalStreamByUrl(data,editor.newOutputStream(DISK_CACHE_INDEX))) {
								editor.commit();
							} else {
								editor.abort();
							}
						}
						snapshot = mOriginalDiskCache.get(key);
					}
					if (snapshot != null) {
						fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
						fileDescriptor = fileInputStream.getFD();
					}
				} catch (IOException e) {
					Log.e(TAG, "processBitmap - " + e);
				} catch (IllegalStateException e) {
					Log.e(TAG, "processBitmap - " + e);
				} finally {
					if (fileDescriptor == null && fileInputStream != null) {
						try {
							fileInputStream.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}

		Bitmap bitmap = null;
		if (fileDescriptor != null) {
			if(neverCalculate)
				bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
			else
				bitmap = BitmapDecoder.decodeSampledBitmapFromDescriptor(fileDescriptor, config.getBitmapWidth(),config.getBitmapHeight());
		}
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
			}
		}
		return bitmap;
	}

	public void initHttpDiskCache() {
		if (!mOriginalCacheDir.exists()) {
			mOriginalCacheDir.mkdirs();
		}
		synchronized (mHttpDiskCacheLock) {
			if (BitmapCommonUtils.getUsableSpace(mOriginalCacheDir) > cacheSize) {
				try {
					mOriginalDiskCache = LruDiskCache.open(mOriginalCacheDir, 1, 1,cacheSize);
				} catch (IOException e) {
					mOriginalDiskCache = null;
				}
			}
			mHttpDiskCacheStarting = false;
			mHttpDiskCacheLock.notifyAll();
		}
	}

	public void clearCacheInternal() {
		synchronized (mHttpDiskCacheLock) {
			if (mOriginalDiskCache != null && !mOriginalDiskCache.isClosed()) {
				try {
					mOriginalDiskCache.delete();
				} catch (IOException e) {
					Log.e(TAG, "clearCacheInternal - " + e);
				}
				mOriginalDiskCache = null;
				mHttpDiskCacheStarting = true;
				initHttpDiskCache();
			}
		}
	}

	public void flushCacheInternal() {
		synchronized (mHttpDiskCacheLock) {
			if (mOriginalDiskCache != null) {
				try {
					mOriginalDiskCache.flush();
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	public void closeCacheInternal() {
		synchronized (mHttpDiskCacheLock) {
			if (mOriginalDiskCache != null) {
				try {
					if (!mOriginalDiskCache.isClosed()) {
						mOriginalDiskCache.close();
						mOriginalDiskCache = null;
					}
				} catch (IOException e) {
					Log.e(TAG, "closeCacheInternal - " + e);
				}
			}
		}
	}
	
	
}
