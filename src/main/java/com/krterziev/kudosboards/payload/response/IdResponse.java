package com.krterziev.kudosboards.payload.response;

public class CreateBoardResponse {
    private String id;

    public CreateBoardResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
