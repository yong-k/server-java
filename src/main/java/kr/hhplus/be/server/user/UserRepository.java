package kr.hhplus.be.server.user;

import kr.hhplus.be.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
