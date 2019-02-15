package com.example.springbootredission.handler;

import com.example.springbootredission.redisson.DistributedLocker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zhang tong
 * date: 2019/2/15 10:25
 * description:
 */
@Component
@AllArgsConstructor
@Slf4j
public class TestHandler {


    private final DistributedLocker distributedLocker;

    public Mono<ServerResponse> getTest(ServerRequest serverRequest) {

        IntStream.range(0, 100).asLongStream().parallel().forEach(i -> {
            System.err.println("=============线程开启============" + Thread.currentThread().getName());
            boolean flag = false;
            //实现同步锁

            String lockKey = "redisson_key";
            try {
                //若任务执行时间过短，则有可能在等锁的过程中2个服务任务都会获取到锁，这与实际需要的功能不一致，故需要将waitTime设置为0
                flag = distributedLocker.tryLock(lockKey,TimeUnit.SECONDS,5,10); //尝试获取锁，等待5秒，自己获得锁后一直不解锁则10秒后自动解锁
                if (flag) {
                    Thread.sleep(100); //获得锁之后可以进行相应的处理
                    System.err.println("======获得锁后进行相应的操作======" + Thread.currentThread().getName());
                    //distributedLocker.unlock(key);
                    System.err.println("=============================" + Thread.currentThread().getName());
                }

            } catch (Exception e) {
                log.error("定时任务执行失败，分布式锁异常" + e);
            } finally {
                if (flag) {
                    distributedLocker.unlock(lockKey);
                }
            }
        });
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromObject("ok"));
    }
}
