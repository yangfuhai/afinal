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
package net.tsz.afinal.http.entityhandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.http.HttpEntity;

import android.text.TextUtils;

public class FileEntityHandler {

	public Object handleEntity(HttpEntity entity, EntityCallBack callback,String target) throws IOException {

		if (TextUtils.isEmpty(target) || target.trim().length() == 0)
			return null;

		File targetFile = new File(target);

		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}

		RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
		raf.seek(0);
		InputStream input = entity.getContent();
		long count = entity.getContentLength();
		long current = 0;
		byte[] bt = new byte[1024];
		int nRead = 0;
		while ((nRead = input.read(bt, 0, 1024)) >0) {
			raf.write(bt, 0, nRead);
			current += nRead;
			callback.callBack(count, current);
		}
		return targetFile;
	}

}
