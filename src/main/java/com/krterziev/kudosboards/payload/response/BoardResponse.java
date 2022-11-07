package com.krterziev.kudosboards.payload.response;

import java.util.List;

public record BoardResponse(String id, String name, List<MessageResponse> messages) {

}

