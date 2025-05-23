# RESTLite 原生RESTful开发框架
中文 | [English](README-EN.md)

RESTLite是基于Java语言的现代化WEB开发框架，其设计目标是创建一个简单易用、功能强大、轻量可扩展的WEB框架。

```gradle
dependencies {
    implementation 'io.github.yeamy:restlite-gson:1.0-RC1' // 谷歌json解析器
    implementation 'io.github.yeamy:restlite-gson:1.0-RC3' // google json
//    implementation 'io.github.yeamy:restlite-jackson:1.0-RC3' // jackson json 解析器
//    implementation 'io.github.yeamy:restlite-jacksonxml:1.0-RC3' // jackson xml 解析器
//    implementation 'io.github.yeamy:restlite-tomcat:1.0-RC3' // 内嵌式 tomcat

    annotationProcessor 'io.github.yeamy:restlite-apt:1.0-RC3' // servlet生成器
//    annotationProcessor 'io.github.yeamy:restlite-tomcat:1.0-RC3' 
}
```

## 有何不同
- 采用与servlet接近的 “资源(接口)” + “方法” 的设计理念，更符合RESTful的设计风格；
- 采用APT生成代码而非反射和动态代理；
- 采用注解配置而非xml配置；
- 内嵌Tomcat支持properties文件替换jar包内配置；

## 如何使用
注意：
*代码内包含的RESTful相关内容，此处不展开讲解。*
*本框架并不遵循JAX-RS，如来自UrlPath的Param与来自UrlQuery的Param具有相同的地位。*
*来自其他Module的代码在编译前会被编译器提前编译，导致APT注解失效*
### 1.配置
```java
package example;
import yeamy.restlite.addition.GsonResponse;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.TomcatConfig;

@TomcatConfig(connector = @Connector(port = 80))// 需要内嵌tomcat时，添加此配置
@Configuration(response = GsonResponse.class,   // 配置默认HttpResponse类
        responseAllType = false) // int等基本类型,String,BigDecimal,InputStream不通过response()序列化
public class Config {
}
```
### 2.创建资源，并为其添加方法
```java
package example;
import yeamy.restlite.annotation.*;

@RESTfulResource("apple")// RESTful注解，该资源名为apple
public class ExampleMain {

    /* 添加注解，声明其HTTP方法，支持GET, DELETE, PUT, PATCH, POST。
     * 其中PUT, PATCH, POST支持http body请求数据
     */
    @POST
    public String getColor(@Param String p,    // 来自URI的请求数据
                           String p2,          // 无注解，当必要Param处理
                           @Param(required=false) String p3, // 可选（非必要）Param
                           @Param(processor="MaxTo15") int p4, // 通过processor读取
                           @Cookies String c,  // 来自cookie的数据
                           @Header String h,   // 来自header的数据
                           @Body String b) {   // 来自body的数据
        return "This is getColor";
    }

    @GET
    public String post3(@Param String p) {
        return "This is post3";
    }
}
```
### 3.@Inject添加成员变量
```java
@RESTfulResource("apple")
public class ExampleMain {
    @Inject InjectBean injectBean;            // 添加(注入)单例

    @GET
    public String get(@Inject InjectBean i, // 添加(注入)新建对象
                      @Param String p) {
        return "This is get";
    }
}
```
注意：@Inject只在@RESTfulResource资源类有效，且创建的静态函数或构造函数必须为无参函数。

**成员变量**  
使用@Inject注解为@RESTfulResource资源对象添加成员变量，成员变量默认为RESTLite创建并缓存的单例，@Inject注解的创建顺序如下：
1. 当成员变量的creator()不为空时，使用creator()提供的类，当tag()不为空时，查找带有对应@LinkTag注解的函数，否则查找静态无参函数、类变量或者无参构造函数。
2. 当成员变量的类带有@Inject注解时，如果creator()不为空时，通过与成员变量相同的方法查找。
3. 查找@InjectProvider注解提供的单例
4. 查找类的公开当前类常量、公开静态无参函数、公开无参构造函数。

**请求参数**  
使用@Inject注解为@RESTfulResource方法添加参数，参数默认为方法创建新对象，支持@Header，@Cookies，@Param作为参数；  
若@Inject(singleton = true)注解为单例，其创建方式与成员变量相同；  

**@InjectProvider:**
```java
@InjectProvider // 生成B类实例
public class B implements A {
}
```
```java
@InjectProvider(provideFor=A.class) // 为A类和B类生成实例
public class B implements A {
}
```

### 4.预处理请求参数
在@Param, @Header, @Cookies, @Body和@Inject等注解中为了预处理请求参数，可以使用processor()或provider()来指定预处理函数，当为空时，
processor()或provider()指定的类内必须存在唯一一个返回该类型的静态方法或构造函数；否则必须存在与@Inject.provider()返回值相同的@InjectProvider.value()注解。如:
```java
@Inject(provider="xx")
public class A {
}
```
```java
public class B {
    @InjectProvider("xx")
    public static final A XX = new A();
}
```

### 5.对JSON的支持
RESTLite提供了GSON跟Jackson两套解决方案，通过选择添加`restlite-gson`或者`restlite-jackson`依赖实现。  
- 分别提供解析request body为JSON格式的@GsonBody和@JacksonBody注解，  
- 返回JSON格式的GsonResponse和JacksonResponse（可以作为函数返回值，或者在@Configuration声明为默认返回值）  
- 默认时间格式为“yyyy-MM-dd HH:mm:ss”的GsonParser和JacksonParser类，支持替换为自定义的GSON/Jackson实体。
```java
@RESTfulResource("apple")// RESTful注解，该资源名为apple
public class ExampleMain {
    @POST
    public GsonResponse getColor(@GsonBody Bean b) { // 使用GSON解析body
        return new GsonResponse("return");
    }
}
```
### 6.对sentinel的支持
RESTLite提供了对sentinel的支持方案，通过添加`restlite-sentinel`依赖实现。  
该依赖包修改自sentinel对javax-servlet的支持包，并保留了CommonTotalFilter。
```java
import jakarta.servlet.annotation.WebFilter;
import yeamy.restlite.sentinel.CommonFilter;

@WebFilter(urlPatterns = "/*",
          filterName = "sentinelFilter",
          dispatcherTypes = DispatcherType.FORWARD)
public class SentinelFilter extends CommonFilter {
}
```
### 7.对权限管理的支持
由于Shiro暂未升级到Jakarta-Servlet，且对RESTful支持并不完善。RESTLite提供了`restlite-permission`依赖，实现对权限管理的支持。  

```java
import yeamy.restlite.permission.SimplePermissionFilter;

@WebFilter
public class MyPermissionFilter extends SimplePermissionFilter {
    // 也可继承子类 PermissionFilter 以设置更多自定义参数
}
```

### 8.内嵌Tomcat的支持
添加@TomcatConfig注解配置Tomcat服务器参数
```java
package example;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.TomcatConfig;

// 内嵌Tomcat最少需要开放一个端口，支持内嵌CA证书和端口转发
@TomcatConfig(connector = {
        @Connector(port = 80,
                redirectPort = 443),     // 开启Tomcat端口转发
        @Connector(port = 443,           // 监听端口
                secure = true,           // 开启证书验证
                keyStoreType = "PKCS12", // 证书类型
                keyStoreFile = "!a.pfx", // 感叹号开头表示jar包内的证书
                keyStorePass = "1234")}) // 证书密码
@Configuration(response = GsonResponse.class)
public class Config {
}
```
内嵌Tomcat允许使用 **@Priority** 对 **@WebListener** 和 **@WebFilter** 进行排序
```java
@Priority(2)
@WebFilter
public class MyFilter implements Filter {
}
```
支持TomcatConfig参数保存到properties文件替换注解(注意：参数名必须与注解相同；connector需要添加序号，序号从0开始)
```properties
# tomcat.properties
connector0.port=80
connector0.redirectPort=443
connector1.port=443
```
添加启动参数 -tomcat *.properties
```shell
java -jar abc.jar -tomcat tomcat.properties
```

### 9.HTTP客户端
使用`io.github.yeamy:httpclient-apt-gson`或者`io.github.yeamy:httpclient-apt-jackson`依赖，配合@Inject。  
版本号 >= 1.0.1
```java
@RESTfulResource("apple")
public class ExampleMain {
    @Inject              // httpclient-apt默认生成Impl结尾的实现类
    DemoClient client;   // 实现httpclient-apt注解的接口类
}
```
### 10.对国际化的支持
使用IDEA插件：[RESTLite-i18n](https://plugins.jetbrains.com/plugin/20268-restlite-i18n) 实现。