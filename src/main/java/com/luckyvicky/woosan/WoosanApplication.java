package com.luckyvicky.woosan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.luckyvicky.woosan.domain.board.repository.jpa",
		"com.luckyvicky.woosan.domain.member.repository.jpa",
		"com.luckyvicky.woosan.domain.likes.repository",
		"com.luckyvicky.woosan.domain.report.repository",
		"com.luckyvicky.woosan.domain.messages.repository",
		"com.luckyvicky.woosan.domain.fileImg.repository",
		"com.luckyvicky.woosan.domain.matching.repository"
})
@EnableElasticsearchRepositories(basePackages = "com.luckyvicky.woosan.domain.board.repository.elasticsearch")
@EnableRedisRepositories(basePackages = {
		"com.luckyvicky.woosan.domain.member.repository.redis",
		"com.luckyvicky.woosan.global.auth.repository"
})
@MapperScan(basePackages = {
		"com.luckyvicky.woosan.domain.board.mapper",
		"com.luckyvicky.woosan.domain.likes.mapper",
		"com.luckyvicky.woosan.domain.member.mybatisMapper"
})
public class WoosanApplication {

	public static void main(String[] args) {
		SpringApplication.run(WoosanApplication.class, args);
	}
}
