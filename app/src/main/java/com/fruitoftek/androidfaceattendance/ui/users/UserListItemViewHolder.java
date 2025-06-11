package com.fruitoftek.androidfaceattendance.ui.users;

import android.app.Application;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.BioPhotosRepository;

public class UserListItemViewHolder extends RecyclerView.ViewHolder {

    private Application application;
    private AppCompatActivity appCompatActivity;
    private BioPhotosRepository bioPhotosRepository;

    private CircleImageView imageViewUserPicture;
    private TextView textViewUserName;
    private TextView textViewBioData;

    public UserListItemViewHolder(@NonNull View itemView, Application application, AppCompatActivity appCompatActivity) {
        super(itemView);
        imageViewUserPicture = itemView.findViewById(R.id.circleimageview_user_picture);
        textViewUserName = itemView.findViewById(R.id.textView_user_name);
        textViewBioData = itemView.findViewById(R.id.textView_biodata);
        this.application = application;
        this.appCompatActivity = appCompatActivity;
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public void bind(Users user) {
        String userTruncName = StringUtils.truncate(user.name, 25);
        String fullNameDesc = user.user + " " + userTruncName;
        textViewUserName.setText(fullNameDesc);
        textViewBioData.setText(R.string.loadingData);
        Bitmap profilePhoto = user.getPhoto();
        if (profilePhoto != null) {
            imageViewUserPicture.setImageBitmap(user.getPhoto());
        } else {
            imageViewUserPicture.setImageResource(R.drawable.account_circle);
        }

        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            int countForUser = bioPhotosRepository.countAllBioPhotosForUser(user.user);
            appCompatActivity.runOnUiThread(() -> {
                if (countForUser > 0) {
                    textViewBioData.setText(R.string.bioPhotoSet);
                } else {
                    textViewBioData.setText(R.string.noPhoto);
                }
            });
        });
    }

    public static UserListItemViewHolder create(ViewGroup parent) {
        Application application = (Application) parent.getContext().getApplicationContext();
        AppCompatActivity appCompatActivity = (AppCompatActivity) parent.getContext();
        return new UserListItemViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_users_list_user_item, parent, false),
                application, appCompatActivity);
    }

}
