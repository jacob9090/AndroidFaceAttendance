package com.fruitoftek.androidfaceattendance.ui.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.databinding.FragmentUsersBinding;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;

public class UsersFragment extends Fragment {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "UsersFragment";
    private FragmentUsersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UsersViewModel usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonAddUser.setOnClickListener(view -> {
            com.fruitoftek.androidfaceattendance.ui.users.UsersFragmentDirections.ActionUsersFragmentToUsersUpsertFragment action =
                    UsersFragmentDirections.actionUsersFragmentToUsersUpsertFragment();
            // Passing -1 so that action is Add New
            action.setUserId(-1);
            Navigation.findNavController(view).navigate(action);
        });

        RecyclerView recyclerViewUsers = binding.recyclerViewUsers;
        final UserListItemAdapter userListItemAdapter = new UserListItemAdapter(new UserListItemAdapter.UsersDiff());
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUsers.setAdapter(userListItemAdapter);

        binding.editTextListUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty on purpose
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty on purpose
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterUsersAndRefreshView(binding.editTextListUserSearch.getText(), usersViewModel.allUsers, userListItemAdapter);
            }
        });

        usersViewModel.getAllUsersLive().observe(this.getViewLifecycleOwner(), users -> {
            usersViewModel.allUsers = users;
            filterUsersAndRefreshView(binding.editTextListUserSearch.getText(), users, userListItemAdapter);
        });

        return root;
    }

    private void filterUsersAndRefreshView(Editable editable, List<Users> users, UserListItemAdapter userListItemAdapter) {
        List<Users> filteredUsers = users;
        if(editable != null && StringUtils.isNotBlank(editable.toString())) {
            String searchString = editable.toString();
            filteredUsers = users.stream().filter(u ->  u.search(searchString)).collect(Collectors.toList());
        }
        userListItemAdapter.submitList(filteredUsers);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}