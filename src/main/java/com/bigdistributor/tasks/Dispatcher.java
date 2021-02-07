package com.bigdistributor.tasks;

import com.bigdistributor.aws.task.AWSSparkDistributedTaskV2;
import com.bigdistributor.biglogger.adapters.Log;
import com.bigdistributor.core.app.BigDistributorApp;
import com.bigdistributor.core.task.BlockTask;
import org.reflections.Reflections;
import picocli.CommandLine;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Set;

public class Dispatcher {
    private static final Log logger = Log.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public Dispatcher(String[] args) {
        if (args.length == 0)
            throw new RuntimeException("Invalid arguments");
        String task = args[0];
        if (task.equalsIgnoreCase("get")) {
            showTasks();
            System.exit(0);
        }
        BlockTask application = getApplications(task);
        if (application == null)
            throw new RuntimeException("Task not exist");
        String[] taskArgs = Arrays.copyOfRange(args, 1, args.length);
        int exitCode = new CommandLine(new AWSSparkDistributedTaskV2<>(application)).execute(taskArgs);
        System.exit(exitCode);
    }

    private void showTasks() {
        logger.info("Showing evaluable tasks: ");
        final Set<Class<?>> indexableClasses = new Reflections("com.bigdistributor").getTypesAnnotatedWith(BigDistributorApp.class);
        indexableClasses.forEach(c -> {
            String appTask = c.getAnnotation(BigDistributorApp.class).task();
            logger.info(c.getSimpleName() + " : " + appTask);
        });
    }

    public static void main(String[] args) {
        new Dispatcher(args);
    }

    private static BlockTask getApplications(String task) {
        final Set<Class<?>> indexableClasses = new Reflections("com.bigdistributor").getTypesAnnotatedWith(BigDistributorApp.class);
        for (Class<?> c : indexableClasses) {
            String appTask = c.getAnnotation(BigDistributorApp.class).task();
            if (appTask.equalsIgnoreCase(task)) {
                if (BlockTask.class.isAssignableFrom(c)) {
                    try {
                        BlockTask application = BlockTask.class.cast(c.newInstance());
                        return application;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
