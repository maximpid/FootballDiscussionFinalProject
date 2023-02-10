package com.example.footballdiscussion.models.models;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.footballdiscussion.enums.LoadingState;
import com.example.footballdiscussion.models.common.Listener;
import com.example.footballdiscussion.models.entities.UserPost;
import com.example.footballdiscussion.models.firebase.FirebaseModel;
import com.example.footballdiscussion.models.room.FootballDiscussionLocalDb;
import com.example.footballdiscussion.models.room.FootballDiscussionLocalDbRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserPostModel {
    private static final UserPostModel _instance = new UserPostModel();

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private FirebaseModel firebaseModel = new FirebaseModel();
    FootballDiscussionLocalDbRepository localDb = FootballDiscussionLocalDb.getAppDb();

    final public MutableLiveData<LoadingState> eventUserPostsLoadingState = new MutableLiveData<>(LoadingState.NOT_LOADING);
    private LiveData<List<UserPost>> userPostList;

    public static UserPostModel instance(){
        return _instance;
    }
    private UserPostModel(){
    }


    public void uploadImage(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name,bitmap,listener);
    }

    public void publishUserPost(UserPost userPost, Listener<Void> callback){
        firebaseModel.addUserPost(userPost,(Void)->{
           refreshAllUserPosts();
            callback.onComplete(null);
        });
    }

    public void refreshAllUserPosts(){
        eventUserPostsLoadingState.setValue(LoadingState.LOADING);
        Long localLastUpdate = UserPost.getLocalLastUpdate();
        firebaseModel.getAllUserPostsSince(localLastUpdate, list->{
            executor.execute(()->{
                Long time = localLastUpdate;

                for(UserPost userPost:list){
                    localDb.userPostDao().insertAll(userPost);
                    if(userPost.isDeleted()){
                        localDb.userPostDao().delete(userPost);
                    }
                    if (time < userPost.getLastUpdated()){
                        time = userPost.getLastUpdated();
                    }
                }
                UserPost.setLocalLastUpdate(time);
                eventUserPostsLoadingState.postValue(LoadingState.NOT_LOADING);
            });
        });
    }
    public LiveData<List<UserPost>>  getAllUserPosts(){
        if(userPostList == null){
            userPostList = localDb.userPostDao().getAll();
            refreshAllUserPosts();
        }
        return userPostList;
    }

    public MutableLiveData<LoadingState> getEventUserPostsLoadingState(){
        return eventUserPostsLoadingState;
    }
}
