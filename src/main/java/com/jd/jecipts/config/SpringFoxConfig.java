package com.jd.jecipts.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 配置SpringFox 用于生成在线Api Doc
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
@Configuration
@EnableSwagger2
public class SpringFoxConfig {

    @Bean
    public Docket petApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(generateApiInfo())
                .useDefaultResponseMessages(false)
                .select()
                .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * Api Doc information
     * @return
     */
    private ApiInfo generateApiInfo() {
        return new ApiInfoBuilder()
                .title("Jeci-pts Api Doc")
                .contact(new Contact("zhongjinyan", "135-8153-5669", "zhongjinyan@jd.com"))
                .description("jeci-pts包传输服务的Api文档")
                .build();
    }
}
