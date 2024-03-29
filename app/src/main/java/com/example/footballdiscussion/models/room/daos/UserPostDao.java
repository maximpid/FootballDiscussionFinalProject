package com.example.footballdiscussion.models.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.footballdiscussion.models.entities.UserPost;

import java.util.List;

@Dao
public interface UserPostDao {
    @Query("select * from UserPost order by id")
    LiveData<List<UserPost>> getAll();


    @Query("select * from UserPost where userId = :userId")
    LiveData<List<UserPost>> getUserPostsByUserId(String userId);

    @Query("select * from UserPost where id = :id")
    LiveData<UserPost> getUserPostById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UserPost... userPosts);

    @Delete
    void delete(UserPost userPost);
}
