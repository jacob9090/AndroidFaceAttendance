package com.fruitoftek.androidfaceattendance.ui.attrecords;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import org.apache.commons.lang3.StringUtils;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;

public class AttRecordsPagingDataAdapter extends PagingDataAdapter<AttendanceRecord, AttRecordsListItemViewHolder> {

    protected AttRecordsPagingDataAdapter(@NonNull DiffUtil.ItemCallback<AttendanceRecord> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public AttRecordsListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return AttRecordsListItemViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull AttRecordsListItemViewHolder holder, int position) {
        AttendanceRecord attendanceRecord = getItem(position);
        holder.bind(attendanceRecord);
    }

    public static class AttendaceRecordDiff extends DiffUtil.ItemCallback<AttendanceRecord> {
        @Override
        public boolean areItemsTheSame(@NonNull AttendanceRecord oldItem, @NonNull AttendanceRecord newItem) {
            return oldItem.user == newItem.user && oldItem.verifyTimeEpochMilliSeconds == newItem.verifyTimeEpochMilliSeconds;
        }

        @Override
        public boolean areContentsTheSame(@NonNull AttendanceRecord oldItem, @NonNull AttendanceRecord newItem) {
            return oldItem.user == newItem.user &&
                    oldItem.verifyTimeEpochMilliSeconds == newItem.verifyTimeEpochMilliSeconds &&
                    StringUtils.equals(oldItem.verifyTime, newItem.verifyTime) &&
                    oldItem.isSync == newItem.isSync;
        }
    }

}
