package com.khu.bbangting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class FollowDto {

    private Long userId;

    private Long storeId;

}
