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
package net.tsz.afinal.reflect;

import java.lang.reflect.Method;

public class ReflectUtils {

	public static Object invokeMethod(Object handler, String strMethod,Class<?>[] cls, Object... params) {

		if (handler == null || strMethod == null)
			return null;
		Method method = null;
		try {
			if (cls == null)
				cls = new Class[0];
			method = handler.getClass().getMethod(strMethod, cls);
			return method.invoke(handler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}
