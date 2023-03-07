import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.dubbo.rpc.model.ModuleModel;

public class DemoProvider {
    public static void main(String[] args) {
        // 服务实现
        DemoService demoService = new DemoServiceImpl();

        // 当前应用配置
        ApplicationModel app = new ApplicationModel(new FrameworkModel());
        ModuleModel model = new ModuleModel(app);
        app.setModelName("demo-provider");
//        model.setAttribute();

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://10.20.130.230:2181");

        // 服务提供者协议配置
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(12345);
        protocol.setThreads(200);

        // 配置中心
        ConfigCenterConfig configCenter = new ConfigCenterConfig();
        configCenter.setAddress("zookeeper://192.168.10.2:2181");
        // 元数据中心
        MetadataReportConfig metadataReport = new MetadataReportConfig();
        metadataReport.setAddress("zookeeper://192.168.10.3:2181");
        // Metrics
        MetricsConfig metrics = new MetricsConfig();
        metrics.setProtocol("dubbo");
        // SSL
        SslConfig ssl = new SslConfig();
        ssl.setServerKeyCertChainPath("/path/ssl/server-key-cert-chain");
        ssl.setServerPrivateKeyPath("/path/ssl/server-private-key");
        // Provider配置（ServiceConfig默认配置）
        ProviderConfig provider = new ProviderConfig();
        provider.setGroup("demo");
        provider.setVersion("1.0.0");
        // Consumer配置（ReferenceConfig默认配置）
        ConsumerConfig consumer = new ConsumerConfig();
        consumer.setGroup("demo");
        consumer.setVersion("1.0.0");
        consumer.setTimeout(2000);

        DubboBootstrap.getInstance()
                .application("demo-app")
                .registry(registry)
                .protocol(protocol)
                .configCenter(configCenter)
                .metadataReport(metadataReport)
                .module(new ModuleConfig("module"))
                .metrics(metrics)
                .ssl(ssl)
                .provider(provider)
                .consumer(consumer)
                .start();




//        // 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口
//        // 服务提供者暴露服务配置
//        ServiceConfig<DemoService> service = new ServiceConfig<>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
//        service.setScopeModel(model);
////        service.setApplication(application);
//        service.setRegistry(registry); // 多个注册中心可以用setRegistries()
//        service.setProtocol(protocol); // 多个协议可以用setProtocols()
//        service.setInterface(DemoService.class);
//        service.setRef(demoService);
//        service.setVersion("1.0.0");
//
//        // 暴露及注册服务
//        service.export();

        System.out.println("DemoProvider ok");
        try {
            // 挂起等待(防止进程退出）
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}