package com.example.demo.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    // Every controller in our package is served under /api, so controllers map "/{version}/users", not "/api/{version}/users"
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forBasePackage("com.example.demo"))
    }

    // The version is the 2nd path segment: /api/1.0/users -> segments [api, 1.0, users]
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.usePathSegment(1).addSupportedVersions("1.0")
    }
}
