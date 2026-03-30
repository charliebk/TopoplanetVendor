package com.vutron.backend.manager.appmessage.DTO;

import com.vutron.backend.manager.appmessage.Repository.AppMessageRepository;

public record GetHelloWorldResponseDto(String key, String value) {
    public static GetHelloWorldResponseDto from(AppMessageRepository.AppMessageRecord record) {
        return new GetHelloWorldResponseDto(record.key(), record.value());
    }
}
