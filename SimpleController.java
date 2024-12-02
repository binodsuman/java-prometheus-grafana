package com.example.Grafanademo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class SimpleController {

    private Counter visitCounter;

    private List<Integer> queue = new ArrayList<>();

    private DistributionSummary httpRequestsDurationHistogram;


    SimpleController(MeterRegistry registry){
       // this.visitCounter = registry.counter("visitor-demo-counter");
        this.visitCounter =  Counter.builder("demo_visit_counter")
                .tags("demo-counter-tage","Number_Visitors")
                //.tags("environment", "development")
                .description("Number of visitors on this api/website")
                .register(registry);

        Gauge.builder("Queue_Size_demo", queue, queue -> queue.size())
                .register(registry);

        httpRequestsDurationHistogram = DistributionSummary.builder("histogram_demo")
                .description("Histogram Demo Demo")
                .tag("histogram_tag","histogram_tag_value")
                //.publishPercentileHistogram()
                // To remove above and added below two line to minimise create buckets in this very large range and you will end up with a large number of buckets.
                .maximumExpectedValue(20.0)
                .publishPercentiles(0.25, 0.5, 0.75, 0.95)
                .register(registry);
    }

    @GetMapping("visitor")
    public String visitorCount(){
        visitCounter.increment();
        return "Visitor Counter : "+visitCounter.count();
    }

    @GetMapping("queue")
    public String queue(){
        queue.add(new Random().nextInt(100)+1);
        return "Gauge demo, Queue size :"+queue.size();
    }

    @GetMapping("queueremove")
    public String queueRemove(){
        if(queue.size() > 0){
            queue.remove(0);
        }
        return "Gauge demo, Queue size :"+queue.size();
    }


    @GetMapping("histogram")
    public String histrogramDemo() throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(new Random().nextInt(1000)+1);
        long duration = System.currentTimeMillis() - start;
        httpRequestsDurationHistogram.record(duration);
        return "Histogram Demo : "+duration;
    }
}
