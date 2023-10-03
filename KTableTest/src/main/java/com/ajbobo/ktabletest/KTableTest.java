package com.ajbobo.ktabletest;

import KTableTest.TestObjectOuterClass.TestObject;
import com.ajbobo.ktabletest.util.AppProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KTableTest {
  private static final Logger logger = LoggerFactory.getLogger(KTableTest.class);
  private static final String INPUT_TOPIC = "KTableTest";
  private static final String OUTPUT_TOPIC = "Output";

  public static void main(String[] args) {
    Properties props = AppProperties.getAll();
    StreamsRunner runner = new StreamsRunner(props, INPUT_TOPIC, OUTPUT_TOPIC);

    Scanner scanner = new Scanner(System.in);
    boolean done = false;
    while (!done) {
      System.out.println("*** Available options ***");
      System.out.println("1) Start KTable thread");
      System.out.println("2) End KTable thread");
      System.out.println("3) Add data to Input Topic");
      System.out.println("4) Get Materialized data");
      System.out.println("0) Quit");
      System.out.print("Select an option: ");
      int option = scanner.nextInt();
      switch (option) {
        case 1:
          runner.start();
          break;
        case 2:
          runner.stop();
          break;
        case 3:
          addData(props);
          break;
        case 4:
          runner.getMaterializedData();
          break;
        case 0:
          done = true;
          break;
      }
    }

    runner.stop();
  }

  private static void addData(Properties props) {
    int numObjects = 5;
    Random rand = new Random();

    try (KafkaProducer<String, TestObject> producer = new KafkaProducer<>(props)) {
      for (int x = 0; x < numObjects; x++) {
        int objNum = rand.nextInt(5);
        int val1 = rand.nextInt(100, 200);
        int val2 = rand.nextInt(300, 500);
        boolean enabled = rand.nextBoolean();

        // Create an object - The names may be reused so that values in the "table" are changing
        TestObject obj = TestObject.newBuilder()
            .setName("Obj" + objNum)
            .setVal1(val1)
            .setVal2(val2)
            .setEnabled(enabled)
            .build();

        // Write each object to Kafka
        Future<RecordMetadata> metadata = producer.send(
            new ProducerRecord<>(INPUT_TOPIC, obj.getName(), obj),
            (event, ex) -> {
              if (ex != null)
                logger.error("Error: {}", ex.getMessage());
              else
                logger.info("Message: {}|{}|{}|{}", obj.getName(), obj.getVal1(), obj.getVal2(), obj.getEnabled());
            }
        );

        try {
          RecordMetadata res = metadata.get();
          logger.info("TestObject {} sent to Kafka", res);
        }
        catch (InterruptedException | ExecutionException ex) {
          logger.error("Exception: {}", ex.getMessage());
        }
      }
    }
  }
}