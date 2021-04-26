package com.bigdistributor.tasks.test;

import com.bigdistributor.aws.dataexchange.aws.s3.func.auth.AWSCredentialInstance;
import com.bigdistributor.aws.job.utils.JarParams;
import com.bigdistributor.aws.utils.AWS_DEFAULT;
import com.bigdistributor.biglogger.adapters.Log;
import com.bigdistributor.tasks.Dispatcher;

import java.util.logging.Level;

public class HeadlessLocalTask {
    private final static String input = "s3://marwan-test-new/dataset-n5.xml";
    private final static String output = "s3://marwan-test-new/new_b.n5";
//    private final static String metadata = "metadata.json";

    private static final Log logger = Log.getLogger(HeadlessLocalTask.class.getSimpleName());

    public static void main(String[] args) {

        Log.setLevel(Level.WARNING);
        AWSCredentialInstance.init(AWS_DEFAULT.AWS_CREDENTIALS_PATH);

        JarParams params = new JarParams("fusion", "job_withblocks", input,  output, "",
                "", AWSCredentialInstance.get());


        String taskParams = params.toString();
        System.out.println(taskParams);

        new Dispatcher(taskParams.split(" "));
    }

}
