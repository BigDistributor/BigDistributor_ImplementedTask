package com.bigdistributor.tasks.test;

import com.bigdistributor.aws.dataexchange.aws.s3.func.auth.AWSCredentialInstance;
import com.bigdistributor.aws.utils.AWS_DEFAULT;
import com.bigdistributor.aws.job.utils.JarParams;
import com.bigdistributor.biglogger.adapters.Log;
import com.bigdistributor.tasks.Dispatcher;

import java.util.logging.Level;

public class HeadlessLocalTask {
    private final static String input = "dataset-n5.xml";
    private final static String output = "delete_test.n5";
    private final static String metadata = "metadata.json";

    private static final Log logger = Log.getLogger(HeadlessLocalTask.class.getSimpleName());

    public static void main(String[] args) {

        Log.setLevel(Level.WARNING);
        AWSCredentialInstance.init(AWS_DEFAULT.AWS_CREDENTIALS_PATH);

        JarParams params = new JarParams("fusion", "delete_test", AWS_DEFAULT.bucket_name,
                input,  output, metadata,
                "", AWSCredentialInstance.get());

        new Dispatcher(params.toString().split(" "));
    }

}
