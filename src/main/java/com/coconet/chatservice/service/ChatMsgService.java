package com.coconet.chatservice.service;

import com.coconet.chatservice.client.MemberClient;
import com.coconet.chatservice.converter.ChatMsgEntityToConverter;
import com.coconet.chatservice.dto.ChatMsgCreateRequestDto;
import com.coconet.chatservice.dto.ChatMsgResponseDto;
import com.coconet.chatservice.entity.ChatMsgEntity;
import com.coconet.chatservice.mongo.ChatMsgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMsgService {
    private final ChatMsgRepository chatMsgRepository;
    private final MemberClient memberClient;

    public ChatMsgResponseDto sendChat(ChatMsgCreateRequestDto requestDto){
        ChatMsgEntity chatMsgEntity = ChatMsgEntity.builder()
                .chatUUID(UUID.randomUUID())
                .senderUUID(requestDto.getSenderUUID())
                .roomUUID(requestDto.getRoomUUID())
                .message(requestDto.getMessage())
                .build();
        ChatMsgEntity savedMsgEntity = chatMsgRepository.save(chatMsgEntity);
        return ChatMsgEntityToConverter.convertToDto(savedMsgEntity, savedMsgEntity.getSenderUUID());
    }

    public List<ChatMsgResponseDto> loadChats(UUID roomUUID){
        List<ChatMsgEntity> chats = chatMsgRepository.findAllByRoomUUID(roomUUID);

        return chats.stream()
                .map(chatMsgEntity -> ChatMsgEntityToConverter.convertToDto(chatMsgEntity, chatMsgEntity.getSenderUUID()))
                .toList();
    }
}
