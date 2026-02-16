package com.et.SudburyCityPlatform.models.jobs;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;

public interface ApplicationSummary {

    ApplicationStatus getStatus();
    long getCount();
}
