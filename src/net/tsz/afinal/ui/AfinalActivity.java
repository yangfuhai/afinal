package net.tsz.afinal.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.R;
import net.tsz.afinal.annotation.view.Select;
import net.tsz.afinal.annotation.view.ViewInject;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxStatus;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AfinalActivity extends FinalActivity {
	
	@ViewInject(id=R.id.listView,select=@Select(selected="onItemSelected"))
	ListView mListView;
	
	@ViewInject(id=R.id.addButton,click="onClick")
	Button btnAdd;
	
	@ViewInject(id=R.id.delButton,click="onClick")
	Button btnDel;
	
	@ViewInject(id=R.id.refButton,click="onClick")
	Button btnRef;
	
	@ViewInject(id=R.id.imagesButton,click="onClick")
	Button btnImages;
	
	List<User> mUserList = new ArrayList<User>();
	
	FinalDb db;
	
	@ViewInject(id=R.id.urlTextView)
	TextView textView;
	UserType userType;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        mListView = (ListView) findViewById(R.id.listView);
//        btnAdd = (Button) findViewById(R.id.addButton);
//        btnDel = (Button) findViewById(R.id.delButton);
//        btnRef = (Button) findViewById(R.id.refButton);

//        btnAdd.setOnClickListener(this);
//        btnDel.setOnClickListener(this);
//        btnRef.setOnClickListener(this);
//        mListView.setOnItemClickListener(this);
        db = FinalDb.create(this,true);
        
        mListView.setAdapter(mAdapter);
        
//        userType = new UserType();
//        userType.setTypeName("���111");
//        db.save(userType);
        userType = db.findWihtOneToManyById(1, UserType.class);
        
        User user = db.findWihtManyToOneById(23, User.class);
//        System.out.println(user.getEmail());
    }
    
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3){
    	Toast.makeText(this, "onItemSelected", 0).show();
    }
    
    public OnClickListener butClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			User u = mUserList.get(position);
			db.delete(u);
			mAdapter.notifyDataSetChanged();
		}
	};
    
    BaseAdapter mAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View itemView = View.inflate(AfinalActivity.this, R.layout.myitem, null);
			
			TextView idTextView = (TextView) itemView.findViewById(R.id.idTextView);
			TextView nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
			TextView emailTextView = (TextView) itemView.findViewById(R.id.mailTtextView);
			Button delBtn = (Button) itemView.findViewById(R.id.delButton);
			
			User u = mUserList.get(position);
			
			idTextView.setText(u.getId()+"");
			nameTextView.setText(u.getName());
			emailTextView.setText(u.getEmail());
			
			delBtn.setTag(position);
			delBtn.setOnClickListener(butClick);
			
			System.out.println("-------:"+u.getRegisterDate()+"-----dd:"+u.getDdtest()+"----ff:"+u.getFftest());
			
			return itemView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return mUserList.get(position);
		}
		
		@Override
		public int getCount() {
			return mUserList.size();
		}
	};

	public void onClick(View v) {
		if(v == btnAdd){
			
			FinalDb db = FinalDb.create(this);
			
			User user = new User();
			user.setEmail("mail@tsz.net");
			user.setId(1);
			user.setFftest(0.33f);
			user.setDdtest(12.01);
			user.setName("michael yang");
			user.setRegisterDate(new Date());
			
			db.save(user);
			
//			db.deleteByWhere(User.class, "id = 0 OR id = NULL");
			
			FinalHttp.ajax("http://www.yangfuhai.com/topic/7.html", new AjaxCallBack() {
				@Override
				public void callBack(AjaxStatus status) {
					if(status!=null)
					 textView.setText(status.getContentAsString());
				}
			});
			
			
		}else if(v == btnDel){
			db.deleteByWhere(User.class, null);
		}else if(v == btnRef){
			long l = System.currentTimeMillis();
//			List<User> ulist = db.findAll(User.class);
			List<User> ulist = db.findAll(User.class,"id");
			
			System.out.println("-----count:"+ulist.size()+"-----time:"+(System.currentTimeMillis()-l)+"--------------");
			
			mUserList.clear();
			mUserList.addAll(ulist);
			mAdapter.notifyDataSetChanged();
		}else if(v == btnImages){
//			Intent intent = new Intent(this, ImagesActivity.class);
//			startActivity(intent);
		}
		
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		// TODO Auto-generated method stub
		return false;
	}
}