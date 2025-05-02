package com.heim.api.users.application.dto;

import com.heim.api.ScheduleMove.application.dto.MovingResponse;
import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String  document;
    private String role;
    private String createdAt;
    private boolean active;
    private List<MovingResponse> reservationMovingList = new ArrayList<>();


    public UserResponse(
            Long userId,
            String fullName,
            String email,
            String role,
            String phone,
            String urlAvatarProfile,
            String document,
            List<ReservationMoving> reservationMovingList) {
    }

    public UserResponse(Long userId,
                        String fullName,
                        String email,
                        String phone,
                        String document,
                        String urlAvatarProfile,
                        String role,
                        String string,
                        boolean active,
                        Object o) {
    }
}
