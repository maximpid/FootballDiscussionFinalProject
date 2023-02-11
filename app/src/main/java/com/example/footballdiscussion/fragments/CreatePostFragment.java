package com.example.footballdiscussion.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.footballdiscussion.R;
import com.example.footballdiscussion.databinding.FragmentCreatePostBinding;
import com.example.footballdiscussion.databinding.FragmentUserPostsBinding;
import com.example.footballdiscussion.models.entities.User;
import com.example.footballdiscussion.models.entities.UserPost;
import com.example.footballdiscussion.view_modals.UserPostsViewModel;

import java.util.UUID;


public class CreatePostFragment extends Fragment {
    private UserPostsViewModel viewModel;
    private FragmentCreatePostBinding binding;
    ActivityResultLauncher<String> galleryLauncher;
    boolean isPostImageSelected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null){
                    binding.newPostImg.setImageURI(result);
                    isPostImageSelected = true;
                }
            }
        });

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);

        binding.newPostGalleryButton.setOnClickListener(view->{
            galleryLauncher.launch("image/*");
        });

        binding.publishPostButton.setOnClickListener(view -> {
            String title = binding.postTitleEt.getText().toString();
            User currentUser = viewModel.getCurrentUser();
            String postId = currentUser.getId() + "_"  +UUID.randomUUID().toString();
            UserPost userPost = new UserPost(postId,currentUser.getId(), currentUser.getUsername(),title, "" );

            if (isPostImageSelected){
                binding.newPostImg.setDrawingCacheEnabled(true);
                binding.newPostImg.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) binding.newPostImg.getDrawable()).getBitmap();
               viewModel.uploadImage(postId, bitmap, url->{
                    if (url != null){
                        userPost.setImageUrl(url);
                    }
                   viewModel.publishUserPost(userPost, (unused) -> {
                        Navigation.findNavController(view).navigate(R.id.action_createPostFragment_to_ownUserPostsFragment);
                    });
                });
            }else {
                viewModel.publishUserPost(userPost, (unused) -> {
                    Navigation.findNavController(view).navigate(R.id.action_createPostFragment_to_ownUserPostsFragment);
                });
            }
        });
        return binding.getRoot();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserPostsViewModel.class);

    }
}