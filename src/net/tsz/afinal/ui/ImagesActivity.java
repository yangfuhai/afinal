//package net.tsz.afinal.ui;
//
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.ImageView;
//import net.tsz.afinal.FinalActivity;
//import net.tsz.afinal.FinalBitmap;
//import net.tsz.afinal.R;
//import net.tsz.afinal.annotation.view.ViewInject;
//
//public class ImagesActivity extends FinalActivity {
//
//	@ViewInject(id=R.id.gridView)
//	GridView gridView;
//	
//	FinalBitmap fb;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.images);
//		fb = FinalBitmap.create(this);
//		fb.setDownLoadFailBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.loadfail));
//		fb.setDownLoadingBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.downloading));
//		gridView.setAdapter(mAdapter);
//	}
//	
//    BaseAdapter mAdapter = new BaseAdapter() {
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			Log.e("tag", "==============");
//			View itemView = View.inflate(ImagesActivity.this, R.layout.image_item, null);
//			ImageView iv = (ImageView) itemView.findViewById(R.id.imageView);
//			fb.display(iv, Images.imageUrls[position]);
////			iv.setImageResource(R.drawable.ic_launcher);
//			return itemView;
//		}
//		
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//		
//		@Override
//		public Object getItem(int position) {
//			return Images.imageUrls[position];
//		}
//		
//		@Override
//		public int getCount() {
//			return Images.imageUrls.length;
//		}
//	};
//}
