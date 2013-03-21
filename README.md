#afinal交流QQ群：192341294
----
# ![mahua](http://code.google.com/p/afinal/logo?cct=1351516535) Afinal简介 
* Afinal 是一个android的sqlite orm 和 ioc 框架。同时封装了android中的http框架，使其更加简单易用；
* 使用finalBitmap，无需考虑bitmap在android中加载的时候oom的问题和快速滑动的时候图片加载位置错位等问题。
* Afinal的宗旨是简洁，快速。约定大于配置的方式。尽量一行代码完成所有事情。


##目前Afinal主要有四大模块：

* FinalDB模块：android中的orm框架，一行代码就可以进行增删改查。支持一对多，多对一等查询。

* FinalActivity模块：android中的ioc框架，完全注解方式就可以进行UI绑定和事件绑定。无需findViewById和setClickListener等。

* FinalHttp模块：通过httpclient进行封装http数据请求，支持ajax方式加载。

* FinalBitmap模块：通过FinalBitmap，imageview加载bitmap的时候无需考虑bitmap加载过程中出现的oom和android容器快速滑动时候出现的图片错位等现象。FinalBitmap可以配置线程加载线程数量，缓存大小，缓存路径，加载显示动画等。FinalBitmap的内存管理使用lru算法，没有使用弱引用（android2.3以后google已经不建议使用弱引用，android2.3后强行回收软引用和弱引用，详情查看android官方文档），更好的管理bitmap内存。FinalBitmap可以自定义下载器，用来扩展其他协议显示网络图片，比如ftp等。同时可以自定义bitmap显示器，在imageview显示图片的时候播放动画等（默认是渐变动画显示）。


---
## 使用afinal快速开发框架需要有以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
* 第一个是访问网络
* 第二个是访问sdcard
* 访问网络是请求网络图片的时候需要或者是http数据请求时候需要，访问sdcard是图片缓存的需要。

----
##FinalDB使用方法：
```java
FinalDb db = FinalDb.create(this);
User user = new User(); //这里需要注意的是User对象必须有id属性，或者有通过@ID注解的属性
user.setEmail("mail@tsz.net");
user.setName("michael yang");
db.save(user);
```
* 关于finalDb的更多介绍，请点击[这里](http://my.oschina.net/yangfuhai/blog/87459)
----
##FinalActivity使用方法：
```java
public class AfinalDemoActivity extends FinalActivity {

    //无需调用findViewById和setOnclickListener等
    @ViewInject(id=R.id.button,click="btnClick") Button button;
    @ViewInject(id=R.id.textView) TextView textView;

    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
    }
    
    public void btnClick(View v){
       textView.setText("text set form button");
    }
}
```
----
##FinalHttp使用方法：
```java
FinalHttp fh = new FinalHttp();
fh.get("http://www.yangfuhai.com", new AjaxCallBack(){

    @Override
	public void onLoading(long count, long current) { //每1秒钟自动被回调一次
			textView.setText(current+"/"+count);
	}

	@Override
	public void onSuccess(String t) {
			textView.setText(t==null?"null":t);
	}

	@Override
	public void onStart() {
		//开始http请求的时候回调
	}

	@Override
	public void onFailure(Throwable t, String strMsg) {
		//加载失败的时候回调
	}
});
```
----
##上传文件 或者提交数据：
```java
  AjaxParams params = new AjaxParams();
  params.put("username", "michael yang");
  params.put("password", "123456");
  params.put("email", "test@tsz.net");
  params.put("profile_picture", new File("/mnt/sdcard/pic.jpg")); // 上传文件
  params.put("profile_picture2", inputStream); // 上传数据流
  params.put("profile_picture3", new ByteArrayInputStream(bytes)); // 提交字节流
 
  FinalHttp fh = new FinalHttp();
  fh.post("http://www.yangfuhai.com", params, new AjaxCallBack(){
  		@Override
 		public void onLoading(long count, long current) {
 				textView.setText(current+"/"+count);
 		}
 
 		@Override
 		public void onSuccess(String t) {
 			textView.setText(t==null?"null":t);
 		}
  });
```
文件上传到服务器，服务器如何接收，请查看[这里](http://www.oschina.net/question/105836_85825)
----
##使用FinalHttp下载文件：
```java
    FinalHttp fh = new FinalHttp();  
    fh.download("http://www.xxx.com/下载路径/xxx.apk", //这里是下载的路径
    "/mnt/sdcard/testapk.apk", //这是保存到本地的路径
    new AjaxCallBack() {  
                @Override  
                public void onLoading(long count, long current) {  
                     textView.setText("下载进度："+current+"/"+count);  
                }  
  
                @Override  
                public void onSuccess(File t) {  
                    textView.setText(t==null?"null":t.getAbsoluteFile().toString());  
                }  
  
            });  
```
----
##FinalBitmap 使用方法 
* 加载网络图片就一行代码 fb.display(imageView,url) 
```java
private GridView gridView;
	private FinalBitmap fb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.images);
		
		gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(mAdapter);
		
		fb = FinalBitmap.create(this);//初始化FinalBitmap模块
		fb.configLoadingImage(R.drawable.downloading);
		//这里可以进行其他十几项的配置，也可以不用配置，配置之后必须调用init()函数,才生效
		//fb.configBitmapLoadThreadSize(int size)
		//fb.configBitmapMaxHeight(bitmapHeight)
	}


///////////////////////////adapter getView////////////////////////////////////////////

public View getView(int position, View convertView, ViewGroup parent) {
	ImageView iv;
	if(convertView == null){
	    convertView = View.inflate(BitmapCacheActivity.this,R.layout.image_item, null);
	    iv = (ImageView) convertView.findViewById(R.id.imageView);
	    iv.setScaleType(ScaleType.CENTER_CROP);
	    convertView.setTag(iv);
	}else{
	    iv = (ImageView) convertView.getTag();
	}
	//bitmap加载就这一行代码，display还有其他重载，详情查看源码
	fb.display(iv,Images.imageUrls[position]);
```

----
#关于作者无为
* 个人博客：[http://www.yangfuhai.com](http://www.yangfuhai.com)
* 交流网站：[http://www.devchina.com](http://www.devchina.com)
* afinal交流QQ群 ： 192341294


