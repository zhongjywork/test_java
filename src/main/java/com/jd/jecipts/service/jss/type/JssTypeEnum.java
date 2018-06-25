package com.jd.jecipts.service.jss.type;

/**
 * 枚举一下JSS的类型
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public enum JssTypeEnum {

    jssTypePublic("public oss"),
    jssTypePrivate("private oss"),
    jssTypeIndonesia("indonesia oss");

    private String description;

    JssTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
