package org.batch.batchtest.JobExcution;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum BatchStatus {
    STARTING,
    FAILED,
    COMPLETE;

}
