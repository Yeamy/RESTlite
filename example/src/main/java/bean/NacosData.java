package bean;

import yeamy.restlite.annotation.NacosExecutor;
import yeamy.restlite.annotation.NacosDiscovery;
import yeamy.restlite.annotation.NacosPullValue;
import yeamy.restlite.annotation.NacosRemoteServer;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@NacosRemoteServer(
        enableDiscovery = @NacosDiscovery(
                serviceName = "defaultServer",
                servicePort = 8848)
)
public interface NacosData {

    @NacosExecutor
    ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 1000L,
            TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());

    @NacosPullValue(group = "g", dataId = "d1", timeoutMs = 1000L)
    int getStr1();

    @NacosPullValue(group = "g", dataId = "d2", timeoutMs = 1000L, autoRefreshed = true)
    Integer getStr2();
}
