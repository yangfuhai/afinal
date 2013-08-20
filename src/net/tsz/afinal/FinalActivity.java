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
import net.tsz.afinal.annotation.view.Select;
import net.tsz.afinal.annotation.view.ViewInject;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

public class FinalActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	

	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
        FinalActivity.initInjectedView(this);
	}


	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
        FinalActivity.initInjectedView(this);
	}



	public void setContentView(View view) {
		super.setContentView(view);
		FinalActivity.initInjectedView(this);
	}

    /**
     * 初始化Actvity中的注入属性
     * 可用于与其他框架合用（如ActionBarShelock）
     * <p>
     * *必须在setContentView之后调用:
     * <pre>
     * protected void onCreate(Bundle savedInstanceState) {
     *  super.onCreate(savedInstanceState);
     *   setContentView(view);
     *   FinalActivity.initInjectedView(this);
     * }
     * </pre>
     * @param sourceActivity
     */
    public static void initInjectedView(Activity sourceActivity){
        initInjectedView(sourceActivity,sourceActivity.getWindow().getDecorView());
    }

    /**
     * 初始化指定View中的注入属性
     * 可用于Fragment内使用InjectView<p>
     * 示例：<p>
     * 在onCreateView中:
     * <pre>
     * public View onCreateView(LayoutInflater inflater, ViewGroup container,
     *      Bundle savedInstanceState) {
     *  View viewRoot = inflater.inflate(R.layout.map_frame, container, false);
     *  FinalActivity.initInjectedView(this,viewRoot);
     * }
     * </pre>
     * @param sourceView
     */
	public static void initInjectedView(Object injectedSource,View sourceView){
		Field[] fields = injectedSource.getClass().getDeclaredFields();
		if(fields!=null && fields.length>0){
			for(Field field : fields){
				ViewInject viewInject = field.getAnnotation(ViewInject.class);
				if(viewInject!=null){
					int viewId = viewInject.id();
					try {
						field.setAccessible(true);
                        /*当已经被赋值时，不在重复赋值，用于include，inflate情景下的viewinject组合*/
                        if(field.get(injectedSource)==null){
						    field.set(injectedSource,sourceView.findViewById(viewId));
                        }else{
                            continue;
                        }
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					String clickMethod = viewInject.click();
					if(!TextUtils.isEmpty(clickMethod))
						setViewClickListener(injectedSource,field,clickMethod);
					
					String longClickMethod = viewInject.longClick();
					if(!TextUtils.isEmpty(longClickMethod))
						setViewLongClickListener(injectedSource,field,longClickMethod);
					
					String itemClickMethod = viewInject.itemClick();
					if(!TextUtils.isEmpty(itemClickMethod))
						setItemClickListener(injectedSource,field,itemClickMethod);


					String itemLongClickMethod = viewInject.itemLongClick();
					if(!TextUtils.isEmpty(itemLongClickMethod))
						setItemLongClickListener(injectedSource,field,itemLongClickMethod);
					
					Select select = viewInject.select();
					if(!TextUtils.isEmpty(select.selected()))
						setViewSelectListener(injectedSource,field,select.selected(),select.noSelected());
					
				}
			}
		}
	}
	
	
	private static void setViewClickListener(Object injectedSource,Field field,String clickMethod){
		try {
			Object obj = field.get(injectedSource);
			if(obj instanceof View){
				((View)obj).setOnClickListener(new EventListener(injectedSource).click(clickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setViewLongClickListener(Object injectedSource,Field field,String clickMethod){
		try {
			Object obj = field.get(injectedSource);
			if(obj instanceof View){
				((View)obj).setOnLongClickListener(new EventListener(injectedSource).longClick(clickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setItemClickListener(Object injectedSource,Field field,String itemClickMethod){
		try {
			Object obj = field.get(injectedSource);
			if(obj instanceof AbsListView){
				((AbsListView)obj).setOnItemClickListener(new EventListener(injectedSource).itemClick(itemClickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setItemLongClickListener(Object injectedSource,Field field,String itemClickMethod){
		try {
			Object obj = field.get(injectedSource);
			if(obj instanceof AbsListView){
				((AbsListView)obj).setOnItemLongClickListener(new EventListener(injectedSource).itemLongClick(itemClickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setViewSelectListener(Object injectedSource,Field field,String select,String noSelect){
		try {
			Object obj = field.get(injectedSource);
			if(obj instanceof View){
				((AbsListView)obj).setOnItemSelectedListener(new EventListener(injectedSource).select(select).noSelect(noSelect));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
