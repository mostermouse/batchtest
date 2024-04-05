package org.batch.batchtest.dormantbatchjob;

import org.batch.batchtest.JobExcution.BatchStatus;
import org.batch.batchtest.JobExcution.JobExcution;
import org.batch.batchtest.customer.Customer;
import org.batch.batchtest.customer.CustomoerRepository;
import org.batch.batchtest.emailprovider.EmailProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DormantBatchJob {

    private final CustomoerRepository customoerRepository;

    private final EmailProvider emailProvider;

    public DormantBatchJob(CustomoerRepository customoerRepository) {
        this.customoerRepository = customoerRepository;
        this.emailProvider = new EmailProvider.Fake();
    }

    public JobExcution excute() {
        final JobExcution jobExcution = new JobExcution();
        jobExcution.setStatus(BatchStatus.STARTING);
        jobExcution.setStartTime(LocalDateTime.now());
        int pageNo = 0;
        try{
            while(true) {
                //1. 유저를 조회한다.
                final PageRequest pageRequest = PageRequest.of(pageNo, 1, Sort.by("id").ascending());
                final Page<Customer> page = customoerRepository.findAll(pageRequest);

                final Customer customer;
                if(page.isEmpty()){
                    break;
                }else {
                    pageNo++;
                    customer = page.getContent().get(0);
                }
                //2. 휴먼계정 대상을 추출 및 변환
                final boolean isDormantTarget = LocalDateTime.now()
                        .minusDays(365)
                        .isAfter(customer.getLoginAt().toLocalDate().atStartOfDay());
                if(isDormantTarget){
                    customer.setStatus(Customer.Status.DORMANT);
                }else{
                    continue;
                }

                //3. 휴먼계정으로 상태를 변경한다.
                customoerRepository.save(customer);
                //4. 메일을 보낸다.
                emailProvider.send(customer.getEmail(), "휴먼전환 안내 메일입니다." , "내용");
            }
            jobExcution.setStatus(BatchStatus.COMPLETE);
        }catch (Exception e){
            jobExcution.setStatus(BatchStatus.FAILED);
        }
        jobExcution.setEndTime(LocalDateTime.now());
        emailProvider.send("admin@naver.com" , "배치 완료 알림" , "DormantBatchJob이 수행되었습니다. status :" + jobExcution.getStatus());

        return jobExcution;
    }
}
