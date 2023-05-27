# RESTLite: RESTful style web framework
English | [中文](README.md)

RESTLite is a modern Java WEB framework, It's design to create a simple, powerful, lightweight and extensible WEB framework.

```gradle
dependencies {
    implementation 'io.github.yeamy:restlite-gson:1.0-RC1' // google json
//    implementation 'io.github.yeamy:restlite-permission:1.0-RC1' // permission manage
//    implementation 'io.github.yeamy:restlite-jackson:1.0-RC1' // fastjson json
//    implementation 'io.github.yeamy:restlite-jacksonxml:1.0-RC1' // fastjson xml
//    implementation 'io.github.yeamy:restlite-sentinel:1.0-RC1' // alibaba sentinel
//    implementation 'io.github.yeamy:restlite-nacos:1.0-RC1' // alibaba nacos
//    implementation 'io.github.yeamy:restlite-tomcat:1.0-RC1' // tomcat embed

    annotationProcessor 'io.github.yeamy:restlite-apt:1.0-RC1' // apt code generator
//    annotationProcessor 'io.github.yeamy:restlite-nacos:1.0-RC1'
//    annotationProcessor 'io.github.yeamy:restlite-tomcat:1.0-RC1' 
}
```

## What's different
- Adopting the design concept of "Resource"+"Method", which is similar to servlet and more in line with the design style of RESTful;
- Use Java APT(since jdk1.6) to generate code instead of reflection and dynamic proxy;
- configure with annotation rather than xml;
- Embedded Tomcat supports replacing annotation configuration with outside properties file;

## How to use
note: *no more explain about RESTful below, RESTlite not following JAX-RS, for example Param from Url Path is the same with Param from Url Query.*
### 1.Configuration
```java
package example;
import yeamy.restlite.addition.GsonResponse;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.TomcatConfig;

@TomcatConfig(connector = @Connector(port = 80))// necessary, for embed tomcat
@Configuration(response = GsonResponse.class,   // class of HttpResponse
        responseAllType = false, // String,BigDecimal,InputStream and base type like int, long~ not serialize with response()
        supportPatch = SupportPatch.tomcat)     // allow http PATCH，PUT, PATCH, POST to support body
public class Config {
}
```
### 2.Create Resources and add methods
```java
package example;
import yeamy.restlite.annotation.*;

@Resource("apple")// name of resource
public class ExampleMain {

    /* annotation of HTTP method: support GET, DELETE, POST, PUT, PATCH.
     * Only POST, PUT, PATCH support body
     */
    @POST
    public String getColor(@Param String p, // param data
                           String p2,          // no annotation, take it as necessary Param
                           @Param(required=false)String p3, // optional Param
                           @Param(processor=Max15.class)int p4, // read via processor
                           @Cookies String c,  // cookie data
                           @Header String h,   // header data
                           @GsonBody Bean b) { // deserialize body with GSON
        return "This is getColor";
    }

    @GET
    public String post3(@Param String p) {
        return "This is post3";
    }
}
```
### 3.Adding instance by @Inject
```java
@Resource("apple")
public class ExampleMain {
    @Inject InjectBean injectBean;          // inject field (default is singleton)

    @GET
    public String get(@Inject InjectBean i, // inject param (default create new instance)
                      @Param String p) {
        return "This is get";
    }
}
```
note: @Inject only work in @Resource, and constructor/static-method of singleton must with no parameter.

**For field:**  
create field of @Resource with @Inject, create singleton by default:
1. param's creator() not empty, if tag() not empty lookup the math @LinkTag in target class; else if tag() is empty lookup static no-param-method, static field, constructor without param.
2. lookup type's @Inject, if creator() is not empty, follow step as upside.
3. lookup @InjectProvider.
4. lookup public static field, public static no-parameter-method, public none-parameter-constructor.

**For parameter:**  
Parameter with @Inject in http-method(annotation with @GET, @POST...), create new instance by default, support parameter with @Header，@Cookies，@Param;  
if singleton @Inject(singleton = true) limit same with field.  

**@InjectProvider:**
```java
@InjectProvider // provide for B.class
public class B implements A {
}
```
```java
@InjectProvider(provideFor=A.class) // provide for A.class and B.class
public class B implements A {
}
```

### 4.preprocess request parameter
Preprocess request parameter(such as @Param, @Header, @Cookies, @Body, @Inject) by processor() or creator() and mark executor with tag(), if tag() is empty, there must only one executor(static-method or constructor) return same type, otherwise there must be only one executor with @LinkTag.value() match the tag():
```java
@Inject(creator=B.class, tag="xx")
public class A {
}
```
```java
public class B {
    @LinkTag("xx")
    public static final A XX = new A();
}
```

### 5.Support of JSON
To support JSON with `restlite-gson` or `restlite-jackson`.  
- Deserialization request body with @GsonBody or @JacksonBody,  
- Response JSON with @GsonResponse or @JacksonResponse (return by method or configure the default response with @Configuration)  
- Default time format is “yyyy-MM-dd HH:mm:ss” of class GsonParser/JacksonParser, support to replace GSON/Jackson instance.

### 6.Support of sentinel
To support sentinel with `restlite-sentinel`.  
The dependency package is modified from Sentinel's support package for javax-servlets, and the CommonTotalFilter is retained.
```java
import jakarta.servlet.annotation.WebFilter;
import yeamy.restlite.sentinel.CommonFilter;

@WebFilter(urlPatterns = "/*",
          filterName = "sentinelFilter",
          dispatcherTypes = DispatcherType.FORWARD)
public class SentinelFilter extends CommonFilter {
}
```

### 7.Support of sentinel permission management
Cause Shiro not support Jakarta-Servlet yet, and not support RESTful well. RESTLite provides `restlite-permission` to support permission management. 

```java
import yeamy.restlite.permission.SimplePermissionFilter;

@WebFilter
public class MyPermissionFilter extends SimplePermissionFilter {
    // or extends PermissionFilter to set more custom parameters 
}
```

### 8.Embedded Tomcat
Configure Tomcat with @TomcatConfig
```java
package example;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.TomcatConfig;

// Embedded Tomcat needs at least one port, supports embedded CA certificate and port forwarding
@TomcatConfig(connector = {
        @Connector(port = 80,
                redirectPort = 443),     // Enable port forwarding
        @Connector(port = 443,           // Listening port
                secure = true,           // Turn on certificate validation
                keyStoreType = "PKCS12", // Key store type
                keyStoreFile = "!a.pfx", // Start with exclamatory mark means embedded file(in jar file)
                keyStorePass = "1234")}) // Password of key store
@Configuration(response = GsonResponse.class)
public class Config {
}
```
Embedded Tomcat allow to order **@WebListener** and **@WebFilter** with **@Position**
```java
@Position(2)
@WebFilter
public class MyFilter implements Filter {
}
```
support to save configuration in a properties file and run as an argument replace the annotation configuration in jar file(note: option name same with annotation; connector need to add number, number start from 0)
```properties
# tomcat.properties
connector0.port=80
connector0.redirectPort=443
connector1.port=443
```
start with "-tomcat *.properties"
```shell
java -jar abc.jar -tomcat tomcat.properties
```

### 9.HTTP client
Using `io.github.yeamy:httpclient-apt-gson` or `io.github.yeamy:httpclient-apt-jackson` with @Inject.  
version >= 1.0.1
```java
@Resource("apple")
public class ExampleMain {
    @Inject              // httpclient-apt create implementation class with @InjectProvider
    DemoClient client;   // interface with annotation of httpclient-apt
}
```
### 10.Support for internationalization
using IDEA plugin: [RESTLite-i18n](https://plugins.jetbrains.com/plugin/20268-restlite-i18n)。