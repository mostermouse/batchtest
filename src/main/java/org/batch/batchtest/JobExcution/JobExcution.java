package org.batch.batchtest.JobExcution;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class JobExcution {
    private BatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
