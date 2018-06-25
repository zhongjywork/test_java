package com.jd.jecipts.api.domain.response;

/**
 * 基本的response body类
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public class BaseResponse {
    private String rspCode;
    private String rspInfo;

    public BaseResponse(String rspCode, String rspInfo) {
        this.rspCode = rspCode;
        this.rspInfo = rspInfo;
    }

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspInfo() {
        return rspInfo;
    }

    public void setRspInfo(String rspInfo) {
        this.rspInfo = rspInfo;
    }
}
