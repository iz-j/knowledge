package com.izj.knowledge.service.base.aws;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public final class AwsProperties {
    public final String s3BucketSysPrivate;
    public final String s3BucketSysPublic;
    public final String s3BucketUsrPrivate;
    public final String s3BucketUsrPublic;

    public final String cloudfrontDomain;

    public final String sqsWorkerQueueName;
    public final String sqsWorkerDlqName;
    public final String sqsSyncDlqName;
}
