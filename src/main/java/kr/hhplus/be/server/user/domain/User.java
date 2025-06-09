package kr.hhplus.be.server.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.point.domain.UserPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private int point;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(UUID id, int point) {
        this.id = id;
        this.point = point;
    }

    public UserPoint toUserPoint() {
        return new UserPoint(id, point);
    }

    public void updatePoint(int updatedPoint) {
        this.point = updatedPoint;
    }
}
