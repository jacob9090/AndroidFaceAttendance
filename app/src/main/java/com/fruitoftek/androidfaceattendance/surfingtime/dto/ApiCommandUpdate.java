package com.fruitoftek.androidfaceattendance.surfingtime.dto;

public class ApiCommandUpdate {
    
    private long id;
    
    // For Example:
    // "0"  - Means the command executed successfully
    // "-1" - Means Unknown Command
    private String cmdReturn;

    // The description of the command return code
    // Like
    // "Command executed successfully"
    // "Unknown Command"
    private String cmdReturnInfo;

    /** ---------------------------------------------------------------------------------------- **/
    /** Getters and Setters **/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCmdReturn() {
        return cmdReturn;
    }

    public void setCmdReturn(String cmdReturn) {
        this.cmdReturn = cmdReturn;
    }

    public String getCmdReturnInfo() {
        return cmdReturnInfo;
    }

    public void setCmdReturnInfo(String cmdReturnInfo) {
        this.cmdReturnInfo = cmdReturnInfo;
    }
}
