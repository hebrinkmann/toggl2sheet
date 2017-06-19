package de.henningbrinkmann.toggl2sheet;

import java.util.List;

public class TimeSheet {
    private List<TimeSheetRecord> timeSheetRecords;
    private List<String> projects;

    public TimeSheet(List<TimeSheetRecord> timeSheetRecords, List<String> projects) {
        this.timeSheetRecords = timeSheetRecords;
        this.projects = projects;
    }

    public List<TimeSheetRecord> getTimeSheetRecords() {
        return timeSheetRecords;
    }

    public List<String> getProjects() {
        return projects;
    }
}
