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
package net.tsz.afinal.bitmap.display;

import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SimpleDisplayer implements Displayer{
	
	public void loadCompletedisplay(ImageView imageView,Bitmap bitmap,BitmapDisplayConfig config){
		switch (config.getAnimationType()) {
		case BitmapDisplayConfig.AnimationType.fadeIn:
			fadeInDisplay(imageView,bitmap);
			break;
		case BitmapDisplayConfig.AnimationType.userDefined:
			animationDisplay(imageView,bitmap,config.getAnimation());
			break;
		default:
			break;
		}
	}
	
	
	public void loadFailDisplay(ImageView imageView,Bitmap bitmap){
		imageView.setImageBitmap(bitmap);
	}
	
	
	
	private void fadeInDisplay(ImageView imageView,Bitmap bitmap){
		final TransitionDrawable td =
                new TransitionDrawable(new Drawable[] {
                        new ColorDrawable(android.R.color.transparent),
                        new BitmapDrawable(imageView.getResources(), bitmap)
                });
        imageView.setImageDrawable(td);
        td.startTransition(300);
	}
	
	
	private void animationDisplay(ImageView imageView,Bitmap bitmap,Animation animation){
		animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());		
        imageView.setImageBitmap(bitmap);
        imageView.startAnimation(animation);
	}

}
