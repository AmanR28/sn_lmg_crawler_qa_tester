package com.lmg.crawler_qa_tester.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitFlow {
    @Qualifier("prodChannel")
    private final PublishSubscribeChannel prodChannel;

    @Qualifier("preProdChannel")
    private final PublishSubscribeChannel preProdChannel;

    @Qualifier("prodDomain")
    private final Domain prodDomain;

    @Qualifier("preProdDomain")
    private final Domain preProdDomain;

    @Scheduled(fixedRate =  1000 * 60 * 10, initialDelay = 1000)
    public void initiate() {

        List<LinkEntity> links = new ArrayList<>();
        links.add(LinkEntity.builder().url(preProdDomain.getBaseUrl()).type(preProdDomain.getName())
            .processed("N").build());
        preProdChannel.send(MessageBuilder.withPayload(links).build());
        log.info("Initiated Pre-Prod Domain: " + preProdDomain.getName());
    }

}

