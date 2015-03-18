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
package net.tsz.afinal.annotation.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.tsz.afinal.exception.ViewException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class EventListener implements OnClickListener, OnLongClickListener, OnItemClickListener, OnItemSelectedListener,OnItemLongClickListener {

	private Object handler;
	
	public EventListener(Object handler) {
		this.handler = handler;
	}
	
	public boolean onLongClick(View v) {
		return invokeMethod(v);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return invokeMethod(parent, view, position, id);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		invokeMethod(parent, view, position, id);
	}

	public void onNothingSelected(AdapterView<?> parent) {
		invokeMethod(parent);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		invokeMethod(parent, view, position, id);
	}

	public void onClick(View v) {
		invokeMethod(v);
	}

	private boolean invokeMethod(Object... params) throws RuntimeException {
		if (handler == null)
			return false;
		Method method = null;
		String methodName = null;
		try {
			/**
			 * Method name is onClick,onItemClick etc
			 * see http://lmbj.net/blog/get-method-name-by-stack-trace/
			 */
			methodName = Thread.currentThread().getStackTrace()[3]
					.getMethodName();
			Class<?>[] parameterTypes = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				parameterTypes[i] = params[i].getClass();
				if (Integer.class.equals(parameterTypes[i])) {
					parameterTypes[i] = int.class;
				} else if (Long.class.equals(parameterTypes[i])) {
					parameterTypes[i] = long.class;
				} else if (AdapterView.class
						.isAssignableFrom(parameterTypes[i])) {
					parameterTypes[i] = AdapterView.class;
				} else if (View.class.isAssignableFrom(parameterTypes[i])) {
					parameterTypes[i] = View.class;
				}
			}
			method = handler.getClass().getDeclaredMethod(methodName,
					parameterTypes);
			Object obj = method.invoke(handler, params);
			return obj == null ? false : Boolean.valueOf(obj.toString());
		} catch (IllegalArgumentException e) {
			Log.e("EventListener", "IllegalArgumentException", e);
		} catch (NoSuchMethodException e) {
			Log.e("EventListener", "NoSuchMethodException", e);
			Toast.makeText(((View) params[0]).getContext(),
					"Please implements the method:" + methodName,
					Toast.LENGTH_LONG).show();
			throw new ViewException("Please implements the method:"
					+ methodName);
		} catch (IllegalAccessException e) {
			Log.e("EventListener", "IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			Log.e("EventListener", "InvocationTargetException", e);
		}
		return false;
	}
}
