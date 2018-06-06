package com.izj.lambda;

import java.util.UUID;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author iz-j
 *
 */
@Slf4j
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class LambdaApp {

    /**
     * If you want to run specific task directly, call this with tenantId and taskId.
     *
     * @param args
     *            [tenantId, partition, taskId]
     */
    public static void main(String[] args) {
        String tenantId = null;
        Long partition = null;
        UUID taskId = null;
        if (args.length >= 2) {
            log.info("tenantId assigned. -> {}", args[0]);
            log.info("partition assigned. -> {}", args[1]);
            log.info("taskId assigned. -> {}", args[2]);
            tenantId = args[0];
            partition = Long.valueOf(args[1]);
            taskId = UUID.fromString(args[2]);
        } else {
            throw new IllegalArgumentException();
        }

        log.info("Start...");
        try (ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(LambdaApp.class)
            .web(false)
            .run(args)) {
            LambdaApp app = applicationContext.getBean(LambdaApp.class);
            // app.run(null, tenantId, partition, taskId);
            System.exit(0);

        } catch (Exception e) {
            log.error("Caught exception!", e);
            System.exit(1);
        } finally {
            log.info("End...");
        }
    }

    /*
     * public boolean run(Context context, String tenantId, long partition, UUID taskId) { WorkerTask task =
     * loadTask(tenantId, partition, taskId);
     *
     * if (task == null) { log.warn("No task to process."); return false; }
     *
     * try { taskReceiver.markTaskAsProcessing(task.getPartition(), task.getTaskId(), Objects.isNull(context) ? null :
     * context.getLogStreamName()); taskInvoker.invoke(task);
     * taskReceiver.markTaskAsDoneWithoutDeletingMessage(task.getPartition(), task.getTaskId()); return true; } catch
     * (WorkerTaskException e) { taskReceiver.markTaskAsError(task.getPartition(), task.getTaskId(),
     * task.getExclusionKey()); return false; } }
     */

    /*
     * public void moveToDLQ(WorkerQueueMessage message) { WorkerTask task = loadTask(message.tenantId,
     * message.partition, message.taskId);
     *
     * if (task == null) { log.warn("No task to process."); return; }
     *
     * taskReceiver.markTaskAsError(task.getPartition(), task.getTaskId(), task.getExclusionKey()); }
     */

    /*
     * private WorkerTask loadTask(String tenantId, long partition, UUID taskId) { Tenant tenant =
     * tenants.get(tenantId); Assert.notNull(tenant, "Tenant not found for " + tenantId);
     * ((SingletonScopeTenantHolder)tenantHolder).set(tenant);
     *
     * WorkerTask task = taskReceiver.getTask(partition, taskId); return task; }
     */
}
