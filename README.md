#afinal交流平台
* QQ群：192341294（群1，1000已满）    246710918（群2，1000未满）
* 网址：[http://www.afinal.org](http://www.afinal.org)


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
关于finalDb的更多介绍，请点击[这里](http://my.oschina.net/yangfuhai/blog/87459)

```java
FinalDb db = FinalDb.create(this);
User user = new User(); //这里需要注意的是User对象必须有id属性，或者有通过@ID注解的属性
user.setEmail("mail@tsz.net");
user.setName("michael yang");
db.save(user);
```

----
##FinalDB OneToMany懒加载使用方法：
模型定义：
```java
public class Parent{
    private int id;
    @OneToMany(manyColumn = "parentId")
    private OneToManyLazyLoader<Parent ,Child> children;
    /*....*/
}
public class Child{
    private int id;
    private String text;
    @ManyToOne(column = "parentId")
    private  Parent  parent;
    /*....*/
}
```
使用：
```java
List<Parent> all = db.findAll(Parent.class);
        for( Parent  item : all){
            if(item.getChildren ().getList().size()>0)
                Toast.makeText(this,item.getText() + item.getChildren().getList().get(0).getText(),Toast.LENGTH_LONG).show();
        }
```
----
##FinalActivity使用方法：
* 完全注解方式就可以进行UI绑定和事件绑定
* 无需findViewById和setClickListener等

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
*在其他侵入式框架下使用（如ActionBarShelock）
```java
     protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(view);
        FinalActivity.initInjectedView(this);
     }
```
*在Fragment中使用
```java
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
       View viewRoot = inflater.inflate(R.layout.map_frame, container, false);
       FinalActivity.initInjectedView(this,viewRoot);
    }
```
##FinalHttp使用方法：
###普通get方法

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

### 使用FinalHttp上传文件 或者 提交数据 到服务器（post方法）
文件上传到服务器，服务器如何接收，请查看[这里](http://www.oschina.net/question/105836_85825)

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


----

###使用FinalHttp下载文件：
* 支持断点续传，随时停止下载任务 或者 开始任务

```java
    FinalHttp fh = new FinalHttp();  
    //调用download方法开始下载
    HttpHandler handler = fh.download("http://www.xxx.com/下载路径/xxx.apk", //这里是下载的路径
    true,//true:断点续传 false:不断点续传（全新下载）
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

	
   //调用stop()方法停止下载
   handler.stop();

   
```


##FinalBitmap 使用方法 
加载网络图片就一行代码 fb.display(imageView,url) ,更多的display重载请看[帮助文档](https://github.com/yangfuhai/afinal/tree/master/doc)

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


---
**>>> Add by fantouch**

#### 配置成Android Library Project
*解决需求:有多个项目依赖afinal,并且想修改afinal源码*
>  
* clone到本地
* 添加AndroidManifest.xml文件:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="net.tsz.afinal" >
    <uses-sdk
        android:minSdkVersion="5"
        android:targetSdkVersion="7" />
</manifest>
```
>  
* 导入到 Eclipse:  
`Import => Android => Existing Android Code Into Workspace`
* 工程上按右键 => Properties => Android => √ Is Library
* 完成,你的项目可以引用这个afinal Library了.

#### 排除不需要Git管理的文件
*解决需求:想修改源码,但不想让Eclipse把工程弄脏*
>  
* 忽略已经被Git管理的`./bin`目录:  
导入Eclipse前执行:  ` git update-index --assume-unchanged ./bin/* `
>  
* 忽略未被Git管理的文件和目录: 添加`/.gitignore` 文件:  
```
/gen
/assets
/bin
/res
/.classpath
/.project
/AndroidManifest.xml
/project.properties
/.gitignore
```  
* 导入到Eclipse,`git status`可见Repository依旧干净.
  
**<<< Add by fantouch**

---

#关于作者michael
* 个人博客：[http://www.yangfuhai.com](http://www.yangfuhai.com)
* afinal捐助：[http://me.alipay.com/yangfuhai](http://me.alipay.com/yangfuhai) （为了感谢捐助者，michael将会把捐助者将公布在afinal官方网站[afinal.org](http://www.afinal.org)上,不让公布的同学说明下）
* afinal交流QQ群 ： 192341294

