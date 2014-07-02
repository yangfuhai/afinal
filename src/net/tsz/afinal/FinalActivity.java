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

import java.lang.reflect.Field;

import net.tsz.afinal.annotation.view.EventListener;
import net.tsz.afinal.annotation.view.ViewInject;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;

public abstract class FinalActivity extends Activity {


	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initInjectedView(this);
	}


	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		initInjectedView(this);
	}


	public void setContentView(View view) {
		super.setContentView(view);
		initInjectedView(this);
	}
	

	public static void initInjectedView(Activity activity){
		initInjectedView(activity, activity.getWindow().getDecorView());
	}
	
	public static void initInjectedView(Object injectedSource, View sourceView) {
		Field[] fields = injectedSource.getClass().getDeclaredFields();
		View view;
		if (fields != null && fields.length > 0) {
			try {
				for (Field field : fields) {
					field.setAccessible(true);

					// just inject view and view's subclass, otherwise skip
					if (!View.class.isAssignableFrom(field.getType())
							|| field.get(injectedSource) != null) {
						continue;
					}

					ViewInject viewInject = field
							.getAnnotation(ViewInject.class);
					if (viewInject != null) {

						int viewId = viewInject.id();
						view = sourceView.findViewById(viewId);
						field.set(injectedSource, view);

						EventListener listener = new EventListener(
								injectedSource);
						if (viewInject.click()) {
							view.setOnClickListener(listener);
						}

						if (viewInject.longClick()) {
							view.setOnLongClickListener(listener);
						}

						//AdapterView be in common use
						if (viewInject.itemClick()) {
							if (view instanceof AdapterView<?>) {
								((AdapterView<?>) view)
										.setOnItemClickListener(listener);
							}
						}

						if (viewInject.itemLongClick()) {
							if (view instanceof AdapterView<?>) {
								((AdapterView<?>) view)
										.setOnItemLongClickListener(listener);
							}
						}

						if (viewInject.select()) {
							if (view instanceof AdapterView<?>) {
								((AdapterView<?>) view)
										.setOnItemSelectedListener(listener);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
