#Afinal简介
Afinal 是一个android的 orm 和 ioc 框架。同时封装了android中的httpClient，使其更加简单易用；使用finalBitmap，无需考虑bitmap在android中加载的时候oom的问题和快速滑动的时候图片加载位置错位等问题。
Afinal的宗旨是简洁，快速。约定大于配置的方式。尽量一行代码完成所有事情。

#afinal交流QQ群：192341294

##目前Afinal主要有四大模块：

1、FinalDB模块：android中的orm框架，一行代码就可以进行增删改查。支持一对多，多对一等查询。

2、FinalActivity模块：android中的ioc框架，完全注解方式就可以进行UI绑定和事件绑定。无需findViewById和setClickListener等。

3、FinalHttp模块：通过httpclient进行封装http数据请求，支持ajax方式加载。

4、FinalBitmap模块：通过FinalBitmap，imageview加载bitmap的时候无需考虑bitmap加载过程中出现的oom和android容器快速滑动时候出现的图片错位等现象。FinalBitmap可以配置线程加载线程数量，缓存大小，缓存路径，加载显示动画等。FinalBitmap的内存管理使用lru算法，没有使用弱引用（android2.3以后google已经不建议使用弱引用，android2.3后强行回收软引用和弱引用，详情查看android官方文档），更好的管理bitmap内存。FinalBitmap可以自定义下载器，用来扩展其他协议显示网络图片，比如ftp等。同时可以自定义bitmap显示器，在imageview显示图片的时候播放动画等（默认是渐变动画显示）。



#使用afinal快速开发框架需要有以下权限：

>uses-permission android:name="android.permission.INTERNET" 
>
>uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
>
>第一个是访问网络
>
>第二个是访问sdcard
>
>（访问网络是请求网络图片的时候需要或者是http数据请求时候需要，访问sdcard是图片缓存的需要）。


##FinalDB使用方法：

>FinalDb db = FinalDb.create(this);
>                        
>User user = new User();
>
>user.setEmail("mail@tsz.net");
>
>user.setId(1);
>
>user.setName("michael yang");
>
>db.save(user);

##FinalActivity使用方法：


>public class AfinalDemoActivity extends FinalActivity {
>
>     //无需调用findViewById和setOnclickListener等
>
>     @ViewInject(id=R.id.button,click="btnClick") Button button;
>
>     @ViewInject(id=R.id.textView) TextView textView;
>       
>    public void onCreate(Bundle savedInstanceState) {
>
>        super.onCreate(savedInstanceState);
>
>        setContentView(R.layout.main);
>
>    }
>  
>    public void btnClick(View v){
>
>        textView.setText("text set form button");
>
>   }
>
>}

##FinalHttp使用方法：

>FinalHttp.ajax("http://www.yangfuhai.com/topic/7.html", new AjaxCallBack() {
>
>	@Override
>
>	public void callBack(AjaxStatus status) {
>
>		textView.setText(status.getContentAsString());
>
>	}
>
>});

##FinalBitmap 使用方法 (加载网络图片就一行代码 fb.display(imageView,url) )：

>    private GridView gridView;
>
>	private FinalBitmap fb;
>
>	@Override
>
>	protected void onCreate(Bundle savedInstanceState) {
>
>		super.onCreate(savedInstanceState);
>
>		setContentView(R.layout.images);
>		
>		  gridView = (GridView) findViewById(R.id.gridView);
>
>		gridView.setAdapter(mAdapter);
>		
>		  fb = new FinalBitmap(this).init();//必须调用init初始化FinalBitmap模块
>
>		fb.configLoadingImage(R.drawable.downloading);
>
>		//这里可以进行其他十几项的配置，也可以不用配置，配置之后必须调用init()函数,才生效
>
>		//fb.configBitmapLoadThreadSize(int size)
>
>		//fb.configBitmapMaxHeight(bitmapHeight)
>
>	}
>
>
>///////////////////////////adapter getView////////////////////////////////////////////
>
> public View getView(int position, View convertView, ViewGroup parent) {
>
>	ImageView iv;
>
>	if(convertView == null){
>
>	    convertView = View.inflate(BitmapCacheActivity.this,R.layout.image_item, null);
>
>	    iv = (ImageView) convertView.findViewById(R.id.imageView);
>
>	    iv.setScaleType(ScaleType.CENTER_CROP);
>
>	    convertView.setTag(iv);
>
>	}else{
>
>	    iv = (ImageView) convertView.getTag();
>
>	}
>
>	//bitmap加载就这一行代码，display还有其他重载，详情查看源码
>
>	fb.display(iv,Images.imageUrls[position]);
>
>	return convertView;
>
>}
>

