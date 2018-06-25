package com.jd.jecipts.api.controller;

import com.jd.jecipts.api.domain.response.BaseResponse;
import com.jd.jecipts.api.domain.response.ResponseStatusEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本服务的探活接口
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
@Api(tags = "探活接口")
@RestController
@RequestMapping(value = "/explore.json")
public class ExploreController {

    /**
     * 探活接口 GET方法
     * @return
     */
    @ApiOperation(value = "jeci-pts server端的探活接口", notes = "探活")
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    BaseResponse explore() {
        return new BaseResponse(ResponseStatusEnum.responseStatusSuccess.getCode(),
                ResponseStatusEnum.responseStatusSuccess.getInfo());
    }

}
