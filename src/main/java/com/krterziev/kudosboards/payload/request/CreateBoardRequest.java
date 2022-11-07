package com.krterziev.kudosboards.payload.request;

import com.krterziev.kudosboards.models.EBoardAccessLevel;
import javax.validation.constraints.NotBlank;

public record CreateBoardRequest(@NotBlank String name,
                                 @NotBlank EBoardAccessLevel accessLevel) {

}
