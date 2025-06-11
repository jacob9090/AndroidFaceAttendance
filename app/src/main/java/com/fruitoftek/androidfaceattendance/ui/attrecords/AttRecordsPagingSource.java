package com.fruitoftek.androidfaceattendance.ui.attrecords;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.fruitoftek.androidfaceattendance.data.dto.SearchAttendanceRecordsQuery;
import com.fruitoftek.androidfaceattendance.data.dto.SearchAttendanceRecordsResponse;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;

public class AttRecordsPagingSource extends ListenableFuturePagingSource<Integer, AttendanceRecord> {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "AttRecordsPagingSource";

    @NonNull
    private Executor mBgExecutor = Executors.newSingleThreadExecutor();
    private AttendanceRecordsRepository attendanceRecordsRepository;
    private SearchAttendanceRecordsQuery query;
    public static int PageSize = 50;

    public AttRecordsPagingSource(AttendanceRecordsRepository attendanceRecordsRepository,
                                  SearchAttendanceRecordsQuery query) {
        this.attendanceRecordsRepository = attendanceRecordsRepository;
        this.query= query;
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, AttendanceRecord> pagingState) {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        Integer anchorPosition = pagingState.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, AttendanceRecord> anchorPage = pagingState.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }

        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<Integer, AttendanceRecord>> loadFuture(@NonNull LoadParams<Integer> loadParams) {
        // Start refresh at page 1 if undefined.
        Integer nextPageNumber = loadParams.getKey();
        if (nextPageNumber == null) {
            nextPageNumber = 1;
        }

        ListenableFuture<LoadResult<Integer, AttendanceRecord>> pageFuture =
                Futures.transform(attendanceRecordsRepository.searchAttendanceRecords(query, nextPageNumber, PageSize),
                        this::toLoadResult, mBgExecutor);

        ListenableFuture<LoadResult<Integer, AttendanceRecord>> partialLoadResultFuture =
                Futures.catching(pageFuture, Exception.class, this::logError, mBgExecutor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, this::logError, mBgExecutor);
    }

    private LoadResult.Error logError(Exception exception) {
        LOGGER.e(TAG, exception, "Error while fetching Attendance Records from DB");
        return new LoadResult.Error(exception);
    }


    private LoadResult<Integer, AttendanceRecord> toLoadResult(@NonNull SearchAttendanceRecordsResponse response) {
        return new LoadResult.Page<>(response.getAttendanceRecords(),
                null, // Only paging forward.
                response.getNextPageNumber(),
                LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED);
    }
}
