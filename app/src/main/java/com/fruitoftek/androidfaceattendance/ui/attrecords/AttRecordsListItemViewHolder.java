package com.fruitoftek.androidfaceattendance.ui.attrecords;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.lang3.StringUtils;
import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;
import com.fruitoftek.androidfaceattendance.util.Literals;
import com.fruitoftek.androidfaceattendance.util.Util;

public class AttRecordsListItemViewHolder extends RecyclerView.ViewHolder {
    private Application application;
    private AppCompatActivity appCompatActivity;
    private UsersRepository usersRepository;

    private TextView textViewVerifyTime;
    private TextView textViewName;
    private TextView textVieVerifyType;
    private CheckBox checkBoxIsSync;

    public AttRecordsListItemViewHolder(@NonNull View itemView, Application application, AppCompatActivity appCompatActivity) {
        super(itemView);
        textViewVerifyTime = itemView.findViewById(R.id.textView_attrecord_verifyTime);
        textViewName = itemView.findViewById(R.id.textView_attrecord_name);
        textVieVerifyType = itemView.findViewById(R.id.textView_attrecord_verifyType);
        checkBoxIsSync = itemView.findViewById(R.id.checkbox_attrecord_isSync);
        usersRepository = new UsersRepository(application);
        this.application = application;
        this.appCompatActivity = appCompatActivity;
    }

    public void bind(AttendanceRecord attendanceRecord) {
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            Users user = usersRepository.findFullById(attendanceRecord.user);
            String userTruncName = StringUtils.truncate(user.name, 25);
            String fullNameDesc = user.user + " " + userTruncName;
            attendanceRecord.verifyTypeStr = Util.getVerifyTypeString(attendanceRecord.verifyType, appCompatActivity.getApplication());
            appCompatActivity.runOnUiThread(() -> {
                textViewVerifyTime.setText(attendanceRecord.verifyTime);
                textViewName.setText(fullNameDesc);
                textVieVerifyType.setText(attendanceRecord.verifyTypeStr);
                checkBoxIsSync.setChecked(attendanceRecord.isSync == Literals.TRUE);
            });
        });
    }

    public static AttRecordsListItemViewHolder create(ViewGroup parent) {
        Application application = (Application) parent.getContext().getApplicationContext();
        AppCompatActivity appCompatActivity = (AppCompatActivity) parent.getContext();
        return new AttRecordsListItemViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_attrecords_list_attrecord_item, parent, false),
                application, appCompatActivity);
    }
}
