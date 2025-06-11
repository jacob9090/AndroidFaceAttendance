package com.fruitoftek.androidfaceattendance.ui.attrecords;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.List;

import kotlinx.coroutines.CoroutineScope;
import com.fruitoftek.androidfaceattendance.data.dto.SearchAttendanceRecordsQuery;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;

public class AttRecordsViewModel extends AndroidViewModel {

    private AttendanceRecordsRepository attendanceRecordsRepository;

    public AttRecordsViewModel(Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRecordsRepository.getAll();
    }

    public LiveData<List<AttendanceRecord>> getAllAttendanceRecordsLive() {
        return attendanceRecordsRepository.getAllAttendanceRecordsLive();
    }

    public LiveData<PagingData<AttendanceRecord>> searchAttendanceRecords(SearchAttendanceRecordsQuery query) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        Pager<Integer, AttendanceRecord> pager = new Pager<>(
                new PagingConfig(/* pageSize = */ AttRecordsPagingSource.PageSize),
                () -> new AttRecordsPagingSource(attendanceRecordsRepository, query));
        return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), viewModelScope);
    }

}