package com.example.footballdiscussion.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.footballdiscussion.databinding.FragmentUserPostsBinding;
import com.example.footballdiscussion.fragments.recycler_adapters.UserPostsRecyclerAdapter;
import com.example.footballdiscussion.models.entities.UserPost;
import com.example.footballdiscussion.utils.LoadingState;
import com.example.footballdiscussion.view_modals.UserPostsViewModel;


public class UserPostsFragment extends Fragment {

    private UserPostsViewModel viewModel;
    private UserPostsRecyclerAdapter userPostsRecyclerAdapter;
    private FragmentUserPostsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserPostsBinding.inflate(inflater, container, false);
        binding.userPostsRecyclerView.setHasFixedSize(true);
        binding.userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userPostsRecyclerAdapter = new UserPostsRecyclerAdapter(getLayoutInflater(), viewModel.getAllUserPosts().getValue(), this.viewModel.getCurrentUser().getId());
        binding.userPostsRecyclerView.setAdapter(this.userPostsRecyclerAdapter);
        setListeners();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserPostsViewModel.class);
    }

    private void setListeners() {
        viewModel.getAllUserPosts().observe(getViewLifecycleOwner(), list -> {
            userPostsRecyclerAdapter.setData(list);
        });

        userPostsRecyclerAdapter.setOnItemClickListener(pos -> {
            UserPostsFragmentDirections.ActionUserPostsFragmentToUserPostDetailsFragment action =
                    UserPostsFragmentDirections.actionUserPostsFragmentToUserPostDetailsFragment(viewModel.getAllUserPosts()
                            .getValue().get(pos).getId());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        userPostsRecyclerAdapter.setOnIconClickListener(pos -> {
            UserPost userPost = viewModel.getAllUserPosts().getValue().get(pos);
            if (!viewModel.isOwnPost(userPost)) {
                viewModel.handleUserPostLike(userPost);
            } else {
                Navigation.findNavController(binding.getRoot()).navigate(UserPostsFragmentDirections
                        .actionUserPostsFragmentToEditOwnUserPostFragment(userPost.getId()));
            }
        });
        userPostsRecyclerAdapter.setOnDeleteClickListener((pos) -> {
            viewModel.deleteUserPost(viewModel.getAllUserPosts().getValue().get(pos));
        });

        viewModel.getEventUserPostsLoadingState().observe(getViewLifecycleOwner(), status -> {
            binding.userPostsSwipeRefresh.setRefreshing(status == LoadingState.LOADING);
        });

        binding.userPostsSwipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshAllUserPosts();
        });
    }
}