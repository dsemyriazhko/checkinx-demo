package com.checkinx.demo2.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkinx.demo2.models.Pet;

@Repository
public interface PetsRepository extends JpaRepository<Pet, UUID> {

    List<Pet> findByName(@Param("name") String name);

    List<Pet> findByLocation(@Param("location") String location);

    List<Pet> findByAge(@Param("age") Integer age);
}
