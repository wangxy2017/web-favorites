package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserName(String userName);
}
