package com.jd.jecipts.config;

import com.jcloud.jss.Credential;
import com.jcloud.jss.JingdongStorageService;
import com.jcloud.jss.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置操作oss对象存储的sdk对象
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
@Configuration
public class JssConfig {
    private static final Logger logger = LoggerFactory.getLogger(JssConfig.class);

    /**
     * jss 连接参数
     */
    @Value("${jss.connect.timeout}")
    private int connectTimeout;
    @Value("${jss.connect.socket.timeout}")
    private int socketConnectTimeout;
    @Value("${jss.connect.maxCount}")
    private int maxConnects;
    @Value("${jss.transfer.partSize}")
    private int transferPartSize;

    /**
     * 公有云配置
     */
    @Value("${jss.public.accessKeyId}")
    private String publicJssAccessKeyId;
    @Value("${jss.public.secretAccessKey}")
    private String publicJssSecretAccessKey;
    @Value("${jss.public.endPoint}")
    private String publicJssEndpoint;

    /**
     * 私有云配置
     */
    @Value("${jss.private.accessKeyId}")
    private String privateJssAccessKeyId;
    @Value("${jss.private.secretAccessKey}")
    private String privateJssSecretAccessKey;
    @Value("${jss.private.endPoint}")
    private String privateJssEndpoint;

    /**
     * 印尼云配置
     */
    @Value("${jss.indonesia.accessKeyId}")
    private String indonesiaJssAccessKeyId;
    @Value("${jss.indonesia.secretAccessKey}")
    private String indonesiaJssSecretAccessKey;
    @Value("${jss.indonesia.endPoint}")
    private String indonesiaJssEndpoint;

    /**
     * 返回操作公有云JSS的服务实例
     * @return
     */
    @Bean(name = "publicJssService")
    public JingdongStorageService publicStorageService() {
        logger.info("Init public jss client : access_key_id(" + publicJssAccessKeyId + "), " +
                "secret_access_key(" + publicJssSecretAccessKey + "), " +
                "endpoint(" + publicJssEndpoint + ")");
        return getJssServiceInstance(publicJssAccessKeyId, publicJssSecretAccessKey, publicJssEndpoint);
    }

    /**
     * 返回操作私有云JSS的服务实例
     * @return
     */
    @Bean(name = "privateJssService")
    public JingdongStorageService privateStorageService() {
        logger.info("Init private jss client : access_key_id(" + privateJssAccessKeyId + "), " +
                "secret_access_key(" + privateJssSecretAccessKey + "), " +
                "endpoint(" + privateJssEndpoint + ")");
        return getJssServiceInstance(privateJssAccessKeyId, privateJssSecretAccessKey, privateJssEndpoint);
    }

    /**
     * 返回操作印尼云JSS的服务实例
     * @return
     */
    @Bean(name = "indonesiaJssService")
    public JingdongStorageService indonesiaStorageService() {
        logger.info("Init private jss client : access_key_id(" + indonesiaJssAccessKeyId + "), " +
                "secret_access_key(" + indonesiaJssSecretAccessKey + "), " +
                "endpoint(" + indonesiaJssEndpoint + ")");
        return getJssServiceInstance(indonesiaJssAccessKeyId, indonesiaJssSecretAccessKey, indonesiaJssEndpoint);
    }

    /**
     * 产生一个完整的JSS客户端实例
     * @param accessKey
     * @param secretAccess
     * @param endpoint
     * @return
     */
    private JingdongStorageService getJssServiceInstance(String accessKey, String secretAccess, String endpoint) {
        ClientConfig config = getBaseJssClientConfig();
        JingdongStorageService jss = new JingdongStorageService(new Credential(accessKey, secretAccess), config);
        jss.setEndpoint(endpoint);
        return jss;
    }

    /**
     * 产生一个基本的jss客户端配置实例
     * @return
     */
    private ClientConfig getBaseJssClientConfig() {
        ClientConfig config = new ClientConfig();
        logger.info("set connect timeout : " + String.valueOf(connectTimeout) + " s");
        config.setConnectionTimeout(connectTimeout * 1000);
        logger.info("set socket connect timeout : " + String.valueOf(socketConnectTimeout) + " s");
        config.setSocketTimeout(socketConnectTimeout * 1000);
        logger.info("set max connect count : " + String.valueOf(maxConnects));
        config.setMaxConnections(maxConnects);
        logger.info("set transfer part size : " + String.valueOf(transferPartSize) + " MB");
        config.setPartSize(transferPartSize * 1024 * 1024);

        return config;
    }
}
