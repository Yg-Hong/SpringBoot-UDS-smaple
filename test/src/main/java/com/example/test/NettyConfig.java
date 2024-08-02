package com.example.test;

import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;

import java.util.Collections;

@Configuration
public class NettyConfig {

    @Bean
    public NettyReactiveWebServerFactory factory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.setServerCustomizers(Collections.singletonList(new NettyServerCustomizer() {
            @Override
            public HttpServer apply(HttpServer httpServer) {
                return httpServer.bindAddress(() -> new DomainSocketAddress("/tmp/test.sock"));
            }
        }));

        return factory;
    }
}
