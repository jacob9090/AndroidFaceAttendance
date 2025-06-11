package com.fruitoftek.androidfaceattendance.ui.users;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import org.apache.commons.lang3.StringUtils;

import com.fruitoftek.androidfaceattendance.data.model.Users;

public class UserListItemAdapter extends ListAdapter<Users, UserListItemViewHolder> {


    protected UserListItemAdapter(@NonNull DiffUtil.ItemCallback<Users> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public UserListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return UserListItemViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListItemViewHolder holder, int position) {
        Users user = getItem(position);
        holder.bind(user);
        holder.itemView.setOnClickListener(view -> {
            com.fruitoftek.androidfaceattendance.ui.users.UsersFragmentDirections.ActionUsersFragmentToUsersUpsertFragment action =
                    UsersFragmentDirections.actionUsersFragmentToUsersUpsertFragment();

            // Passing the User Id
            action.setUserId(user.user);
            Navigation.findNavController(view).navigate(action);
        });
    }

    public static class UsersDiff extends DiffUtil.ItemCallback<Users> {
        @Override
        public boolean areItemsTheSame(@NonNull Users oldItem, @NonNull Users newItem) {
            return oldItem.user == newItem.user;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Users oldItem, @NonNull Users newItem) {
            return oldItem.user == newItem.user &&
                    StringUtils.equals(oldItem.name, newItem.name) &&
                    StringUtils.equals(oldItem.lastUpdated, newItem.lastUpdated) &&
                    oldItem.isSync == newItem.isSync;
        }
    }

}
