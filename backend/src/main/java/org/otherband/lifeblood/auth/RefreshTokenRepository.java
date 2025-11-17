package org.otherband.lifeblood.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    List<RefreshTokenEntity> findRefreshTokenEntityByUsername(String username);
}
