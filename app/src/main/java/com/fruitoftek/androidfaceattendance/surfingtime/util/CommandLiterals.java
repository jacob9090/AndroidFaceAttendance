package com.fruitoftek.androidfaceattendance.surfingtime.util;

public class CommandLiterals {

    public static final String SUCCESS = "0";
    public static final String FAILED = "-1000";
    public static final String FAILED_UNKNOWN = "-1002";
    public static final String UNKNOWN_COMMAND_ERROR_MSG = "Command [%s] unknown or unsupported";
    public static final String INFO_LOG_TEMPLATE = "Executing command %s";
    public static final String USER_CMD_FIELD = "PIN=";
    public static final String STARTTIME_CMD_FIELD = "StartTime=";
    public static final String ENDTIME_CMD_FIELD = "EndTime=";

    /**CLEAR ATTENDANCE RECORD*/
    public static final String DEV_CMD_CLEAR_LOG = "CLEAR LOG";
    public static final String DEV_CMD_CLEAR_LOG_SUCCESS_MSG = "Attendance records cleared successfully";
    public static final String DEV_CMD_CLEAR_LOG_ERROR_MSG = "Error while clearing attendance records %s";

    /**CLEAR ALL DATA*/
    public static final String DEV_CMD_CLEAR_DATA = "CLEAR DATA";
    public static final String DEV_CMD_CLEAR_DATA_SUCCESS_MSG = "All data cleared successfully";
    public static final String DEV_CMD_CLEAR_DATA_ERROR_MSG = "Error while clearing all the data %s";

    /**SEND DEVICE INFO TO SERVER*/
    public static final String DEV_CMD_INFO = "INFO";
    public static final String DEV_CMD_INFO_SUCCESS_MSG = "Info synced with SurfingTime";
    public static final String DEV_CMD_INFO_ERROR_MSG = "INFO";

    /**REBOOT DEVICE*/
    public static final String DEV_CMD_REBOOT = "REBOOT";
    public static final String DEV_CMD_REBOOT_SUCCESS_MSG = "Device rebooted successfully";
    public static final String DEV_CMD_REBOOT_ERROR_MSG = "Error while trying to reboot device %s";

    /**UPDATE USER INFO*/
    // "DATA UPDATE USERINFO PIN={0}\tName={1}\tPri={2}\tPasswd={3}\tCard={4}\tGrp={5}\tTZ={6}\tCategory={7}"
    public static final String DEV_CMD_DATA_UPDATE_USERINFO = "DATA UPDATE USERINFO";
    public static final String DEV_CMD_DATA_UPDATE_USERINFO_SUCCESS_MSG = "User synced successfully";
    public static final String DEV_CMD_DATA_UPDATE_USERINFO_ERROR_MSG = "Error while syncing user %s";

    /**UPDATE BIOPHOTO URL*/
    // "DATA UPDATE BIOPHOTO PIN={0}\tType={1}\tFormat={2}\tUrl=iclock/doc/biophoto/{3}/{4}/{5}/{6}"
    public static final String DEV_CMD_DATA_UPDATE_BIOPHOTO_URL = "DATA UPDATE BIOPHOTO";
    public static final String DEV_CMD_DATA_UPDATE_BIOPHOTO_URL_SUCCESS_MSG = "Biophoto synced successfully";
    public static final String DEV_CMD_DATA_UPDATE_BIOPHOTO_URL_ERROR_MSG = "Error while syncing biophoto %s";

    /**DELETE USER INFO*/
    // "DATA DELETE USERINFO PIN={0}"
    public static final String DEV_CMD_DATA_DELETE_USERINFO = "DATA DELETE USERINFO";
    public static final String DEV_CMD_DATA_DELETE_USERINFO_SUCCESS_MSG = "User deleted successfully";
    public static final String DEV_CMD_DATA_DELETE_USERINFO_ERROR_MSG = "Error while deleting user %s";

    /**CLEAR USER*/
    public static final String DEV_CMD_DATA_CLEAR_USERINFO = "CLEAR ALL USERINFO";
    public static final String DEV_CMD_DATA_CLEAR_USERINFO_SUCCESS_MSG = "All user data deleted successfully";
    public static final String DEV_CMD_DATA_CLEAR_USERINFO_ERROR_MSG = "Error while deleting all user data %s";

    /**QUERY ATTENDANCE RECORD*/
    // "DATA QUERY ATTLOG StartTime={0}\tEndTime={1}"
    public static final String DEV_CMD_DATA_QUERY_ATTLOG = "DATA QUERY ATTLOG";
    public static final String DEV_CMD_DATA_QUERY_ATTLOG_SUCCESS_MSG = "All attendance logs synced successfully";
    public static final String DEV_CMD_DATA_QUERY_ATTLOG_ERROR_MSG = "Error while syncing attendance logs %s";

    /**QUERY USER INFO FOR USER*/
    // "DATA QUERY USERINFO PIN={0}"
    public static final String DEV_CMD_DATA_QUERY_USERINFO_BY_PIN = "DATA QUERY USERINFO PIN";
    public static final String DEV_CMD_DATA_QUERY_USERINFO_BY_PIN_SUCCESS_MSG = "User info synced successfully";
    public static final String DEV_CMD_DATA_QUERY_USERINFO_BY_PIN_ERROR_MSG = "Error while syncing user info %s";

    /**QUERY ALL USER INFO*/
    public static final String DEV_CMD_DATA_QUERY_USERINFO = "DATA QUERY USERINFO";
    public static final String DEV_CMD_DATA_QUERY_USERINFO_SUCCESS_MSG = "All user info synced successfully";
    public static final String DEV_CMD_DATA_QUERY_USERINFO_ERROR_MSG = "Error while syncing all user info %s";

}
