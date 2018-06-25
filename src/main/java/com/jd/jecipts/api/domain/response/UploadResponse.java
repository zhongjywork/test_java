package com.jd.jecipts.api.domain.response;

/**
 * 上传文件请求的response body
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public class UploadResponse extends BaseResponse {

    private String url;

    public UploadResponse(String rspCode, String rspInfo, String url) {
        super(rspCode, rspInfo);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
