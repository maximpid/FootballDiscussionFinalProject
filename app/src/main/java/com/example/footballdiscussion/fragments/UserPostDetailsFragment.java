package com.example.footballdiscussion.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.footballdiscussion.R;
import com.example.footballdiscussion.databinding.FragmentPostDetailsBinding;
import com.example.footballdiscussion.fragments.recycler_adapters.UserCommentsRecyclerAdapter;
import com.example.footballdiscussion.models.entities.UserPost;
import com.example.footballdiscussion.utils.ImageUtils;
import com.example.footballdiscussion.utils.LoadingState;
import com.example.footballdiscussion.view_modals.UserPostsViewModel;

import java.util.ArrayList;

public class UserPostDetailsFragment extends Fragment {
    private UserPostsViewModel viewModel;
    private String userPostId;
    private UserPost userPost;
    private FragmentPostDetailsBinding binding;
    private UserCommentsRecyclerAdapter userCommentsRecyclerAdapter;

    public static UserPostRowFragment newInstance(String userPostId) {
        return new UserPostRowFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false);
        this.userPostId = UserPostDetailsFragmentArgs.fromBundle(getArguments()).getUserPostId();

        binding.userCommentsRecyclerView.setHasFixedSize(true);
        binding.userCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userCommentsRecyclerAdapter = new UserCommentsRecyclerAdapter(getLayoutInflater(), new ArrayList<>(), this.viewModel.getCurrentUser().getId());
        binding.userCommentsRecyclerView.setAdapter(this.userCommentsRecyclerAdapter);

        refreshUserPost();
        setListeners();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserPostsViewModel.class);
    }

    private void setUserPostData(UserPost userPost) {
        if (userPost != null) {
            binding.postDetailsRowTitle.setText(userPost.getPostTitle());
            binding.postDetailsRowUsername.setText(userPost.getUsername());
            ImageUtils.loadImage(userPost.getImageUrl(), binding.postDetailsRowImage, R.drawable.football_stadium);
        }
    }

    private void refreshUserPost() {
        viewModel.getUserPostById(userPostId).observe(getViewLifecycleOwner(), userPost -> {
            setUserPostData(userPost);
            this.userPost = userPost;
            userCommentsRecyclerAdapter.setData(this.userPost.getUserPostComments());
        });
    }

    private void setListeners() {
        viewModel.getEventUserPostsLoadingState().observe(getViewLifecycleOwner(), status -> {
            binding.userPostDetailsSwipeRefresh.setRefreshing(status == LoadingState.LOADING);
        });

        binding.userPostDetailsSwipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshAllUserPosts();
        });

        binding.publishButton.setOnClickListener(view -> {
            viewModel.publishUserComment(String.valueOf(binding.commentInputText.getText()), this.userPost, (unused) -> {
                Log.d("TAG", "Added comment");
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Added new comment!")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }
}
