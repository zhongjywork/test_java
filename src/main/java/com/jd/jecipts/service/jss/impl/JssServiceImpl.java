package com.jd.jecipts.service.jss.impl;

import com.jcloud.jss.JingdongStorageService;
import com.jcloud.jss.domain.Bucket;
import com.jcloud.jss.exception.StorageClientException;
import com.jcloud.jss.exception.StorageServerException;
import com.jd.jecipts.service.jss.JssService;
import com.jd.jecipts.service.jss.type.JssTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jss服务的实现类
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
@Repository("JssService")
public class JssServiceImpl implements JssService {
    private static final Logger logger = LoggerFactory.getLogger(JssServiceImpl.class);

    private static final String CURRENT_RESOURCE_TYPE_JSS_CLIENT = "jss_client";
    private static final String CURRENT_RESOURCE_TYPE_JSS_BUCKET = "jss_bucket";

    @Value("${jss.public.bucket}")
    private String publicJssBucket;

    @Value("${jss.private.bucket}")
    private String privateJssBucket;

    @Value("${jss.indonesia.bucket}")
    private String indonesiaJssBucket;

    @Value("${jss.transfer.partSize}")
    private int bigFileThreshold;

    @Autowired
    private JingdongStorageService publicJssService;

    @Autowired
    private JingdongStorageService privateJssService;

    @Autowired
    private JingdongStorageService indonesiaJssService;

    /**
     * 确保指定的bucket存在
     * @param type
     * @return
     */
    @Override
    public boolean checkBucketExist(JssTypeEnum type) {
        Map<String, Object> resource = getCurrentJssResource(type);
        JingdongStorageService jss = (JingdongStorageService) resource.get(CURRENT_RESOURCE_TYPE_JSS_CLIENT);
        String bucketName = (String) resource.get(CURRENT_RESOURCE_TYPE_JSS_BUCKET);

        logger.info("Will Check bucket(" + bucketName + ") is exist in " + type.getDescription());

        List<Bucket> buckets = jss.listBucket();
        for (Bucket bucket : buckets) {
            if (bucketName.equals(bucket.getName())) {
                return true;
            }
        }

        // 根据bucket名没有查到对应的bucket 则新建一个bucket
        try {
            jss.bucket(bucketName).create();
            logger.info("Check bucket(" + bucketName + ") is exist in " + type.getDescription() + " success!");
            return true;
        } catch (StorageClientException e) {
            logger.error("Check bucket(" + bucketName + ") is exist in " + type.getDescription() + " failure!");
            e.printStackTrace();
            return false;
        } catch (StorageServerException e) {
            logger.error("Check bucket(" + bucketName + ") is exist in " + type.getDescription() + " failure!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 执行文件的上传
     * @param type
     * @param objName
     * @param filePath
     * @param md5
     * @return
     */
    @Override
    public boolean uploadObject(JssTypeEnum type, String objName, String filePath, String md5) {

        Map<String, Object> resource = getCurrentJssResource(type);
        JingdongStorageService jss = (JingdongStorageService) resource.get(CURRENT_RESOURCE_TYPE_JSS_CLIENT);
        String bucketName = (String) resource.get(CURRENT_RESOURCE_TYPE_JSS_BUCKET);

        logger.info("Will process upload file(" + filePath + ") to " + type.getDescription());

        String md5Result = null;

        // 获取文件大小 看是否需要按照大文件方式传输
        if (checkBigFile(filePath)) {
            // 按照大文件分块传输
            logger.info("object(" + objName + ") is big file, execute big file upload");
            //md5Result = jss.bucket(bucketName).object(objName).entity(new File(filePath)).resumableUpload();
            md5Result = jss.bucket(bucketName).object(objName).entity(new File(filePath)).contentType("application/zip").put();
        } else {
            // 按照小文件传输
            logger.info("object(" + objName + ") is small file, execute normal upload");
            md5Result = jss.bucket(bucketName).object(objName).entity(new File(filePath)).contentType("application/zip").put();
        }

        // 比对md5是否一致
        if (md5Result.equals(md5)) {
            logger.info("Upload file(" + filePath + ") success!");
            return true;
        } else {
            logger.error("Upload file(" + filePath + ") failure! source md5(" + md5 + "), get md5(" + md5Result + ")");
            return false;
        }
    }

    /**
     * 获取上传文件的限时下载URI
     * @param type
     * @param objName
     * @param timeout
     * @return
     */
    @Override
    public String getObjectUri(JssTypeEnum type, String objName, int timeout) {
        Map<String, Object> resource = getCurrentJssResource(type);
        JingdongStorageService jss = (JingdongStorageService) resource.get(CURRENT_RESOURCE_TYPE_JSS_CLIENT);
        String bucketName = (String) resource.get(CURRENT_RESOURCE_TYPE_JSS_BUCKET);

        logger.info("Will process upload object(" + objName + ")'s url ");

        // 首先判断对象是否存在
        if (!jss.bucket(bucketName).object(objName).exist()) {
            logger.error("Given object(" + objName + ") is not exist in bucket(" + bucketName + ") in " + type.getDescription());
            return null;
        }

        // 获取限时下载URI
        URI uri = jss.bucket(bucketName).object(objName).generatePresignedUrl(timeout);
        logger.info("Get Object(" + objName + ") url(" + uri.toString() + ") in bucket(" + bucketName + ") in " + type.getDescription());
        return uri.toString();
    }

    /**
     * 根据jss类型获取当前的客户端实例或对应的bucket名
     * @param jssType
     * @return
     */
    private Map<String, Object> getCurrentJssResource(JssTypeEnum jssType) {
        Map<String, Object> resource = new HashMap<String, Object>(2);
        switch (jssType) {
            case jssTypePublic:
                resource.put(CURRENT_RESOURCE_TYPE_JSS_CLIENT, publicJssService);
                resource.put(CURRENT_RESOURCE_TYPE_JSS_BUCKET, publicJssBucket);
                break;
            case jssTypePrivate:
                resource.put(CURRENT_RESOURCE_TYPE_JSS_CLIENT, privateJssService);
                resource.put(CURRENT_RESOURCE_TYPE_JSS_BUCKET, privateJssBucket);
                break;
            case jssTypeIndonesia:
                resource.put(CURRENT_RESOURCE_TYPE_JSS_CLIENT, indonesiaJssService);
                resource.put(CURRENT_RESOURCE_TYPE_JSS_BUCKET, indonesiaJssBucket);
                break;
                default:
                    break;
        }
        return resource;
    }

    /**
     * 确认当前文件是否属于大文件范畴
     * @param filePath
     * @return
     */
    private boolean checkBigFile(String filePath) {
        File f = new File(filePath);
        long length = f.length();
        return length > (bigFileThreshold << 20);
    }
}


























