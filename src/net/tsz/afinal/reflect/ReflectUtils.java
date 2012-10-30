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
