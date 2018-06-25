package com.jd.jecipts.api.domain.response;

/**
 * 列一下response body的状态
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public enum ResponseStatusEnum {
    responseStatusSuccess("200", "success"),
    responseStatusParamsError("400400", "request params error"),
    responseStatusLocalStorageDirError("400401", "create local storage dir error"),
    responseStatusUploadEmptyFile("400402", "get upload file is empty"),
    responseStatusLocalSaveFileError("400403", "save file to local storage error"),
    responseStatusJssBucketError("400404", "operate oss bucket error"),
    responseStatusUpload2OssError("400405", "Upload file to oss error"),
    responseStatusDownloadUrlError("400406", "get oss download url error");

    private String code;
    private String info;

    private ResponseStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}
