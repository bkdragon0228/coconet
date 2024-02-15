package com.coconet.articleservice.client;

import com.coconet.articleservice.common.response.Response;
import com.coconet.articleservice.dto.member.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name="member-service")
public interface MemberClient {
    @GetMapping("/member-service/open-api/clientMemberAllInfo/{memberUUID}")
    Response<MemberResponse> clientMemberAllInfo(@PathVariable UUID memberUUID);

    @GetMapping("/member-service/open-api/clientMemberProfile/{memberUUID}")
    Response<MemberResponse> clientMemberProfile(@PathVariable UUID memberUUID);
}