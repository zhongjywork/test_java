package com.jd.jecipts.service.jss;

import com.jd.jecipts.service.jss.type.JssTypeEnum;

/**
 * JSS服务 统一操作公有云/私有云/印尼云
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public interface JssService {

    /**
     * 确保指定的bucket存在
     * @param type
     */
    boolean checkBucketExist(JssTypeEnum type);

    /**
     * 执行本地文件的上传 上传小文件和大文件统一走这个入口
     * @param type
     * @param objName
     * @param filePath
     * @param md5
     * @return
     */
    boolean uploadObject(JssTypeEnum type, String objName, String filePath, String md5);

    /**
     * 获取有时限的下载url
     * @param type
     * @param objName
     * @param timeout
     * @return
     */
    String getObjectUri(JssTypeEnum type, String objName, int timeout);
}
