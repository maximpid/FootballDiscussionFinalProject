package com.example.footballdiscussion.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.footballdiscussion.activities.PostsActivity;
import com.example.footballdiscussion.databinding.FragmentRegisterPageBinding;
import com.example.footballdiscussion.models.entities.User;
import com.example.footballdiscussion.models.models.UserPostModel;
import com.example.footballdiscussion.utils.UserUtils;
import com.example.footballdiscussion.view_modals.RegisterPageViewModel;

public class RegisterPageFragment extends Fragment {
    private FragmentRegisterPageBinding binding;
    private RegisterPageViewModel mViewModel;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;
    private Boolean isImageSelected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap result) {
                if (result != null) {
                    binding.avatarImg.setImageBitmap(result);
                    isImageSelected = true;
                }
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    binding.avatarImg.setImageURI(result);
                    isImageSelected = true;
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterPageBinding.inflate(inflater, container, false);
        setListeners();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this).get(RegisterPageViewModel.class);
    }

    private void setListeners() {
        binding.registerButton.setOnClickListener(view -> {
            String username = binding.usernameRegisterEt.getText().toString();
            String password = binding.passwordRegisterEt.getText().toString();
            String phone = binding.phoneRegisterEt.getText().toString();
            String email = binding.emailRegisterEt.getText().toString();
            if (!UserUtils.isEmailValid(email) || password.length() < 6) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Invalid Email or password too short")
                        .setMessage("The email address is invalid or the password isn't 6 characters at least")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                User user = new User(username, phone, email, "");
                createUser(user, password, view);
            }
        });

        binding.cameraButton.setOnClickListener(view1 -> {
            cameraLauncher.launch(null);
        });

        binding.galleryButton.setOnClickListener(view1 -> {
            galleryLauncher.launch("image/*");
        });
    }

    private void createUser(User user, String password, View view){
        if (isImageSelected) {
            binding.avatarImg.setDrawingCacheEnabled(true);
            binding.avatarImg.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) binding.avatarImg.getDrawable()).getBitmap();
            UserPostModel.instance().uploadImage(user.getUsername(), bitmap, url -> {
                if (url != null) {
                    user.setImageUrl(url);
                    binding.registerProgressIndicator.show();
                    mViewModel.addUser(user, password, (e) -> openPostsActivity(), (e) -> onCallbackFail(view, e));
                }
            });
        } else {
            binding.registerProgressIndicator.show();
            mViewModel.addUser(user, password, (e) -> openPostsActivity(), (e) -> onCallbackFail(view, e));
        }
    }

    private void openPostsActivity() {
        Intent postsActivityIntent = new Intent(getActivity(), PostsActivity.class);
        startActivity(postsActivityIntent);
        getActivity().finish();
    }

    private void onCallbackFail(View view, String errorMessage) {
        binding.registerProgressIndicator.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Error at registration")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }
}