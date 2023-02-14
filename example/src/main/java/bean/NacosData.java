package bean;

import yeamy.restlite.annotation.NacosExecutor;
import yeamy.restlite.annotation.NacosDiscovery;
import yeamy.restlite.annotation.NacosRemoteServer;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@NacosDiscovery(
        serviceName = "defaultServer",
        servicePort = 8848)
@NacosRemoteServer
public interface NacosData {

    @NacosExecutor
    ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 1000L,
            TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());

}
