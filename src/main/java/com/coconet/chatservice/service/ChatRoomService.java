package com.coconet.chatservice.service;

import com.coconet.chatservice.client.ArticleClient;
import com.coconet.chatservice.client.MemberClient;
import com.coconet.chatservice.common.errorcode.ErrorCode;
import com.coconet.chatservice.common.exception.ApiException;
import com.coconet.chatservice.common.response.Response;
import com.coconet.chatservice.converter.ChatRoomEntityConverter;
import com.coconet.chatservice.dto.ChatRoomDeleteDto;
import com.coconet.chatservice.dto.ChatroomRequestDto;
import com.coconet.chatservice.dto.ChatroomResponseDto;
import com.coconet.chatservice.dto.client.ArticleResponse;
import com.coconet.chatservice.dto.client.MemberResponse;
import com.coconet.chatservice.entity.ChatRoomEntity;
import com.coconet.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ArticleClient articleClient;
    private final MemberClient memberClient;
    private final ChatRoomSubService chatRoomSubService;

    public ChatroomResponseDto createRoom(ChatroomRequestDto createRequestDto, UUID memberUUID){
        if(chatRoomSubService.existChatRoom(createRequestDto.getArticleUUID(), memberUUID)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Already exist");
        }

        ArticleResponse article = articleClient.sendChatClient(createRequestDto.getArticleUUID()).getData();

        if (article.getWriterUUID().equals(memberUUID)){
            throw new ApiException(ErrorCode.BAD_REQUEST, "The author is unable to apply");
        }

        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .roomUUID(UUID.randomUUID())
                .articleUUID(createRequestDto.getArticleUUID())
                .applicantUUID(memberUUID)
                .writerUUID(article.getWriterUUID())
                .roomName(chatRoomSubService.punctuateTitle(article.getRoomName()))
                .build();

        ChatRoomEntity newChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomEntityConverter.convertToDto(newChatRoom, memberUUID);
    }

    // Todo: Paging
    public List<ChatroomResponseDto> getRooms(UUID memberUUID) {
        List<ChatRoomEntity> chatRoomEntities = chatRoomRepository.findByAllMemberUUID(memberUUID);
        List<ChatroomResponseDto> chatroomResponseDtos = chatRoomEntities.stream()
                .map(chatRoomEntity -> ChatRoomEntityConverter.convertToDto(chatRoomEntity, memberUUID))
                .toList();

        chatroomResponseDtos.stream()
                .forEach(chatRoomDto -> {
                    UUID opponentUUID = chatRoomSubService.getOpponentUUID(memberUUID, chatRoomDto.getRoomUUID());
                    String opponentName = opponentUUID != null ? memberClient.sendChatClient(opponentUUID).getData().getName() : "N/A";
                    chatRoomDto.changeName(chatRoomDto.getRoomName() + " With " + chatRoomSubService.punctuateTitle(opponentName));
                });


        return chatroomResponseDtos;
    }

    public ChatroomResponseDto getRoomWithRoomUUID(UUID memberUUID, UUID roomUUID) {
        if (!chatRoomRepository.isMember(memberUUID, roomUUID))
            throw new ApiException(ErrorCode.BAD_REQUEST, "Not Authorised");

        ChatRoomEntity roomEntity = chatRoomRepository.findByRoomUUID(roomUUID);
        ChatroomResponseDto chatRoomDto = ChatRoomEntityConverter.convertToDto(roomEntity, memberUUID);
        UUID opponentUUID = chatRoomSubService.getOpponentUUID(memberUUID, chatRoomDto.getRoomUUID());
        String opponentName = opponentUUID != null ? memberClient.sendChatClient(opponentUUID).getData().getName() : "N/A";
        chatRoomDto.changeName(chatRoomDto.getRoomName() + " With " + chatRoomSubService.punctuateTitle(opponentName));
        return chatRoomDto;
    }

    public ChatroomResponseDto getRoomWithArticleUUID(UUID memberUUID, UUID articleUUId){
        UUID roomUUID = chatRoomRepository.getRoomUUID(articleUUId, memberUUID);

        if (!chatRoomRepository.isMember(memberUUID, roomUUID))
            throw new ApiException(ErrorCode.BAD_REQUEST, "Not Authorised");

        ChatRoomEntity roomEntity = chatRoomRepository.findByRoomUUID(roomUUID);
        return ChatRoomEntityConverter.convertToDto(roomEntity, memberUUID);
    }

    public ChatroomResponseDto leaveRoom(UUID memberUUID, ChatRoomDeleteDto chatRoomDeleteDto) {
        if (!chatRoomRepository.isMember(memberUUID, chatRoomDeleteDto.getRoomUUID()))
            throw new ApiException(ErrorCode.BAD_REQUEST, "Not Authorised");

        ChatRoomEntity roomEntity = chatRoomRepository
                .findByRoomUUID(chatRoomDeleteDto.getRoomUUID());
        roomEntity.leave(memberUUID);
        ChatRoomEntity response = chatRoomRepository.save(roomEntity);

        return ChatRoomEntityConverter.convertToDto(response, memberUUID);
    }
}

// 로그 파일을 생성하는 .. extra.
// member => 삭제
// article => 삭제
// chatroom / chats => 삭제
