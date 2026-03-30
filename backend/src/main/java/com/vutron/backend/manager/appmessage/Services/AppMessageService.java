package com.vutron.backend.manager.appmessage.Services;

import com.vutron.backend.manager.appmessage.DTO.GetHelloWorldResponseDto;
import com.vutron.backend.manager.appmessage.Repository.AppMessageRepository;

public final class AppMessageService {
    private final AppMessageRepository repository;

    public AppMessageService(AppMessageRepository repository) {
        this.repository = repository;
    }

    public GetHelloWorldResponseDto findHelloWorld() {
        AppMessageRepository.AppMessageRecord record = repository.findByKey("HELLO_WORLD");

        if (record == null) {
            return null;
        }

        return GetHelloWorldResponseDto.from(record);
    }
}
