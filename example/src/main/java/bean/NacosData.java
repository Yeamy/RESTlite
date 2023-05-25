package bean;

import com.alibaba.nacos.api.exception.NacosException;
import jakarta.servlet.ServletException;
import yeamy.restlite.annotation.*;

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

    @NacosGet(group = "g", dataId = "d1", timeoutMs = 1000L)
    int getStr1();

    @NacosGet(group = "g", dataId = "d2", timeoutMs = 1000L, autoRefreshed = true)
    Integer getStr2();

    @NacosSet(group = "g", dataId = "d2")
    void setStr2(Integer v) throws ServletException;
}
