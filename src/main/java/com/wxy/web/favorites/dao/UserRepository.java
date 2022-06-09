package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameOrEmail(String username, String email);

    User findByUsernameAndEmail(String username, String email);

    @Modifying
    @Query(nativeQuery = true,value = "update t_favorites set is_share = 1")
    void updateShareAll();
}
