package com.izj.lambda;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author iz-j
 *
 */
public class LambdaHandler implements RequestHandler<Map<String, ?>, Object> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Object handleRequest(Map<String, ?> input, Context context) {
        LambdaLogger log = context.getLogger();

        log.log("START Worker process ...");

        /*
         * String snsMessage = getMessagesStr(input); List<WorkerQueueMessage> messages = getTaskMessages(snsMessage);
         * 
         * boolean succeed = processMessages(context, messages);
         * 
         * log.log("END Worker process."); if (!succeed) { throw new
         * RuntimeException("Some error occurred in WorkerTask process, please read logs."); }
         * 
         */
        return "SUCCESS";
    }

    @SuppressWarnings("unchecked")
    private String getMessagesStr(Map<String, ?> input) {
        List<?> records = (List<?>)input.get("Records");
        Assert.notNull(records, "Not found Resords attribute from input.");
        Map<String, ?> record = (Map<String, ?>)records.get(0);
        Map<String, ?> sns = (Map<String, ?>)record.get("Sns");
        Assert.notNull(sns, "Not found Sns attribute from input.");
        String messagesStr = (String)sns.get("Message");
        return messagesStr;
    }

    /*
     * private boolean processMessages(Context context, List<WorkerQueueMessage> messages) { boolean success = true;
     * LambdaLogger log = context.getLogger();
     * 
     * try (ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(LambdaApp.class) .run()) {
     * LambdaApp app = applicationContext.getBean(LambdaApp.class);
     * 
     * for (int i = 0; i < messages.size(); i++) { WorkerQueueMessage message = messages.get(i);
     * log.log(">>> START message " + (i + 1) + " of " + messages.size() + ""); log.log("Parameter -> " +
     * message.toString());
     * 
     * try { if (!success) { log.log("Move to dead letter queue."); app.moveToDLQ(message); continue; }
     * 
     * if (!app.run(context, message)) { log.log("Some problem occurred."); success = false; } } catch (Throwable e) {
     * log.log("Caught exception!"); log.log(ExceptionUtils.getStackTrace(e)); success = false; } finally {
     * log.log("<<< END message " + (i + 1) + "."); } } }
     * 
     * return success; }
     */

}
