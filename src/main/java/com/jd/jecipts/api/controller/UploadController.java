package com.jd.jecipts.api.controller;

import com.jd.jecipts.api.domain.response.ResponseStatusEnum;
import com.jd.jecipts.api.domain.response.UploadResponse;
import com.jd.jecipts.service.jss.JssService;
import com.jd.jecipts.service.jss.type.JssTypeEnum;
import com.jd.jecipts.util.FileUtil;
import com.jd.jecipts.util.StringUtil;
import com.jd.ump.profiler.proxy.Profiler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 上传文件服务的接口
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
@Api(tags = "上传文件的接口")
@RestController
@RequestMapping(value = "/upload")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private static final String REQUEST_FORM_FIELD_FILE = "upload_file";
    private static final String REQUEST_FORM_FIELD_MD5 = "md5";
    private static final String REQUEST_FORM_FIELD_OSS_TYPE = "oss_type";
    private static final String REQUEST_FORM_FIELD_COMMIT_ID = "git_commit_id";

    private static final String OSS_TYPE_PUBLIC = "public";
    private static final String OSS_TYPE_PRIVATE = "private";
    private static final String OSS_TYPE_INDONESIA = "indonesia";

    @Value("${file.local.storage.path}")
    private String localStorageDir;

    @Value("${jss.object.retain.timeout}")
    private int objectRetainTimeout;

    @Value("${ump.monitor.key.oss.bucket.unavailable}")
    private String umpKeyOssBucketUnavailable;

    @Value("${ump.monitor.key.oss.upload.failure}")
    private String umpKeyOssUploadFailure;

    @Value("${ump.monitor.key.oss.download.url.failure}")
    private String umpKeyOssDownloadUrlFailure;

    @Autowired
    private JssService jssService;

    /**
     * 上传文件的API入口
     * @param request
     * @return
     */
    @ApiOperation(value = "jeci-pts server端上传文件接口", notes = "上传文件")
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    UploadResponse upload(HttpServletRequest request) {
        MultipartHttpServletRequest params = (MultipartHttpServletRequest) request;
        List<MultipartFile> files = params.getFiles(REQUEST_FORM_FIELD_FILE);
        String md5 = params.getParameter(REQUEST_FORM_FIELD_MD5);
        String ossType = params.getParameter(REQUEST_FORM_FIELD_OSS_TYPE);
        String commitId = params.getParameter(REQUEST_FORM_FIELD_COMMIT_ID);

        // 验证参数有效性
        if (files.size() != 1 || StringUtil.isEmpty(md5) || StringUtil.isEmpty(ossType) || StringUtil.isEmpty(commitId)) {
            logger.error("Upload file request params error! " +
                    "files(" + String.valueOf(files.size()) + ") " +
                    "md5(" + md5 + ") " +
                    "oss type(" + ossType + ") " +
                    "commit id(" + commitId + ")");
            return new UploadResponse(ResponseStatusEnum.responseStatusParamsError.getCode(),
                    ResponseStatusEnum.responseStatusParamsError.getInfo(),
                    null);
        }

        JssTypeEnum jssType = getJssType(ossType);
        if (jssType == null) {
            logger.error("upload file request params oss_type(" + ossType + ") error");
            return new UploadResponse(ResponseStatusEnum.responseStatusParamsError.getCode(),
                    ResponseStatusEnum.responseStatusParamsError.getInfo(),
                    null);
        }

        logger.info("Get upload file request params: " +
                "files(" + String.valueOf(files.size()) + ") " +
                "md5(" + md5 + ") " +
                "oss type(" + ossType + ") " +
                "commit id(" + commitId + ")");

        // 确认本地接收存储文件的目录存在
        if (!FileUtil.checkDirExist(localStorageDir)) {
            logger.error("Create local storage dir error!");
            return new UploadResponse(ResponseStatusEnum.responseStatusLocalStorageDirError.getCode(),
                    ResponseStatusEnum.responseStatusLocalStorageDirError.getInfo(),
                    null);
        }

        // 判断上传的文件是否为空
        MultipartFile f = files.get(0);
        if (f.isEmpty()) {
            logger.error("Get upload file(" + f.getOriginalFilename() + ") is empty!");
            return new UploadResponse(ResponseStatusEnum.responseStatusUploadEmptyFile.getCode(),
                    ResponseStatusEnum.responseStatusUploadEmptyFile.getInfo(),
                    null);
        }

        // 落盘
        try {
            byte[] bytes = f.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(
                            new File(FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename())));
            stream.write(bytes);
            stream.close();
        } catch (Exception e) {
            logger.error("Storage file(" + f.getOriginalFilename() + ") to local storage error");
            e.printStackTrace();
            return new UploadResponse(ResponseStatusEnum.responseStatusLocalSaveFileError.getCode(),
                    ResponseStatusEnum.responseStatusLocalSaveFileError.getInfo(),
                    null);
        }
        logger.info("Save file(" + f.getOriginalFilename() + ") to local storage success");

        // 确认oss上的bucket可用
        if (!jssService.checkBucketExist(jssType)) {
            logger.error("there is not target bucket or create target bucket error");
            FileUtil.deleteFile(FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename());
            Profiler.businessAlarm(umpKeyOssBucketUnavailable, (new Date()).getTime(), "Bucket is unavailable in oss_" + ossType);
            return new UploadResponse(ResponseStatusEnum.responseStatusJssBucketError.getCode(),
                    ResponseStatusEnum.responseStatusJssBucketError.getInfo(),
                    null);
        }

        // 执行文件上传 如果失败 重试两次
        boolean flag = false;
        for (int i = 0; i < 3; i++) {
            if (jssService.uploadObject(jssType, f.getOriginalFilename(),
                    FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename(),
                    md5)) {
                flag = true;
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                logger.error("TimeUnit sleep exception");
                e.printStackTrace();
            }
        }
        if (!flag) {
            logger.error("upload file(" + f.getOriginalFilename() + ") to oss(" + ossType + ") error");
            FileUtil.deleteFile(FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename());
            Profiler.businessAlarm(umpKeyOssUploadFailure,
                    (new Date()).getTime(),
                    "Upload file(" + f.getOriginalFilename() + ") to oss failure with commit id(" + commitId + ")");
            return new UploadResponse(ResponseStatusEnum.responseStatusUpload2OssError.getCode(),
                    ResponseStatusEnum.responseStatusUpload2OssError.getInfo(),
                    null);
        }

        // 获取下载的限时uri
        String downloadUrl = jssService.getObjectUri(jssType, f.getOriginalFilename(), objectRetainTimeout * 24 * 60 * 60);
        if (downloadUrl == null) {
            logger.error("Get file(" + f.getOriginalFilename() +")'s download_url error");
            FileUtil.deleteFile(FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename());
            Profiler.businessAlarm(umpKeyOssDownloadUrlFailure,
                    (new Date()).getTime(),
                    "Get oss file(" + f.getOriginalFilename() + ")'s download url failure with commit id(" + commitId + ")");
            return new UploadResponse(ResponseStatusEnum.responseStatusDownloadUrlError.getCode(),
                    ResponseStatusEnum.responseStatusDownloadUrlError.getInfo(),
                    null);
        }

        // 返回成功状态及下载url
        logger.info("Upload file(" + f.getOriginalFilename() + ") to oss(" + ossType + ") success, and get download url : " + downloadUrl);
        FileUtil.deleteFile(FileUtil.getWholeDirPath(localStorageDir) + f.getOriginalFilename());
        return new UploadResponse(ResponseStatusEnum.responseStatusSuccess.getCode(),
                ResponseStatusEnum.responseStatusSuccess.getInfo(),
                downloadUrl);
    }

    /**
     * 获取Jss客户端类型
     * @param ossType
     * @return
     */
    private JssTypeEnum getJssType(String ossType) {
        if (ossType.equals(OSS_TYPE_PUBLIC)) {
            return JssTypeEnum.jssTypePublic;
        } else if (ossType.equals(OSS_TYPE_PRIVATE)) {
            return JssTypeEnum.jssTypePrivate;
        } else if (ossType.equals(OSS_TYPE_INDONESIA)) {
            return JssTypeEnum.jssTypeIndonesia;
        } else {
            logger.error("From OSS_TYPE(" + ossType + ") get JssTypeEnum failure");
            return null;
        }
    }
}



















