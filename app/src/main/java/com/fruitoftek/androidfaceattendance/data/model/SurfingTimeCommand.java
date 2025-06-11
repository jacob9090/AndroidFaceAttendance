package com.fruitoftek.androidfaceattendance.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommand;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommandUpdate;
import com.fruitoftek.androidfaceattendance.util.Util;

@Entity(indices = {
        @Index(value = {"isExecuted", "isSync"}),
        @Index("isExecuted"),
        @Index("isSync")
})
public class SurfingTimeCommand {

    // The command Id
    // Same Id as SurfingTime's Id for same command
    @NonNull
    @PrimaryKey
    public long id;

    public String command;

    // Date format: "yyyy-MM-dd HH:mm:ss"
    public String receivedOn;

    // Date format: "yyyy-MM-dd HH:mm:ss"
    public String executedOn;

    // For Example:
    // "0"  - Means the command executed successfully
    // "-1002" - Means Unknown Command
    public String cmdReturn;

    // The description of the command return code
    // Like
    // "Command executed successfully"
    // "Unknown Command"
    public String cmdReturnInfo;

    // Boolean. Is command already executed by this device?
    public int isExecuted;

    // Boolean. Is this record synced to SurfingTime already?
    public int isSync;

    /** ---------------------------------------------------------------------------------------- **/
    /** Method Overloads **/
    @Override
    public String toString() {
        return "SurfingTimeCommand{" +
                "id=" + id +
                ", command='" + command + '\'' +
                '}';
    }

    /** ---------------------------------------------------------------------------------------- **/
    /** Useful calculated fields and methods **/

    @Ignore
    public ApiCommandUpdate toApiCommandUpdate() {
        ApiCommandUpdate apiCommandUpdate = new ApiCommandUpdate();
        apiCommandUpdate.setId(id);
        apiCommandUpdate.setCmdReturn(cmdReturn);
        apiCommandUpdate.setCmdReturnInfo(cmdReturnInfo);
        return apiCommandUpdate;
    }


    @Ignore
    public static SurfingTimeCommand fromApiCommand(ApiCommand apiCommand) {
        SurfingTimeCommand surfingTimeCommand = new SurfingTimeCommand();
        surfingTimeCommand.id = apiCommand.getId();
        surfingTimeCommand.command = apiCommand.getCommand();
        surfingTimeCommand.receivedOn = Util.getDateTimeNow();
        surfingTimeCommand.isExecuted = 0;
        surfingTimeCommand.isSync = 0;
        return surfingTimeCommand;
    }
}
