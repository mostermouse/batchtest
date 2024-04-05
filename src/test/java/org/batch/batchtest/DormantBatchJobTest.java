package org.batch.batchtest;

import org.batch.batchtest.JobExcution.BatchStatus;
import org.batch.batchtest.JobExcution.JobExcution;
import org.batch.batchtest.customer.Customer;
import org.batch.batchtest.customer.CustomoerRepository;
import org.batch.batchtest.dormantbatchjob.DormantBatchJob;
import org.batch.batchtest.emailprovider.EmailProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DormantBatchJobTest {
    @Autowired
    private CustomoerRepository customerRepository;

    @Autowired
    private DormantBatchJob dormantBatchJob;

    @BeforeEach
    public void setup() {
        customerRepository.deleteAll();
    }

    @DisplayName("로그인 시간이 1년을 경과 한 고객이 3명이고 1년 이내에 로그인한 고객이 5명이면 3명의 고객이 휴먼전환 대상자")
    @Test
    void test1() {
        saveCustomer(366);
        saveCustomer(366);
        saveCustomer(366);

        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);

        final JobExcution result = dormantBatchJob.excute();
        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETE);

    }

    @DisplayName("고객이 10명이고 모두다 휴먼전환 대상이면 휴먼전환 대상은 10명이다.")
    @Test
    void test2() {

        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        final JobExcution result = dormantBatchJob.excute();

        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETE);

    }


    @DisplayName("고객이 없는 경우에도 배치는 정상동작해야 한다.")
    @Test
    void test3() {
        final JobExcution result = dormantBatchJob.excute();

        final long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETE);

    }

    @Test
    @DisplayName("배치가 실패하면 BatchStatus는 Failed를 반환해야 한다.")
    void test4(){

        final DormantBatchJob dormantBatchJob1= new DormantBatchJob(null);
        final JobExcution result = dormantBatchJob1.excute();

        assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    public void saveCustomer(long loginMinusDays) {
        final String uuid = UUID.randomUUID().toString();
        final Customer test = new Customer(uuid, uuid + "@naver.com");
        test.setloginAt(LocalDateTime.now().minusDays(loginMinusDays));
        customerRepository.save(test);
    }
}
