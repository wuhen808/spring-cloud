package com.wuhen.springcloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ConfigTestController {

	@Value("${from}")
	private String from;
	@Value("${from1}")
	private String from1;
	
	@GetMapping("/test")
	public String properties(){
		return from+"---"+from1;
	}
	
}
