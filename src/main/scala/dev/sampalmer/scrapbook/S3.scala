package dev.sampalmer.scrapbook

import com.hashicorp.cdktf.providers.aws.iam.{DataAwsIamPolicyDocument, DataAwsIamPolicyDocumentStatement, DataAwsIamPolicyDocumentStatementPrincipals}
import com.hashicorp.cdktf.providers.aws.s3.{S3Bucket, S3BucketPolicy, S3BucketPublicAccessBlock}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

object S3 {
  def s3Policy(stack: Construct, oaiIam: String, bucket: S3Bucket): S3BucketPolicy = {
    val principals = DataAwsIamPolicyDocumentStatementPrincipals.builder()
      .`type`("AWS")
      .identifiers(List(oaiIam).asJava)
      .build()
    val statement = DataAwsIamPolicyDocumentStatement.builder()
      .principals(List(principals).asJava)
      .actions(List("s3:GetObject").asJava)
      .resources(List(s"${bucket.getArn}/*").asJava)
      .build()
    val policy = DataAwsIamPolicyDocument.Builder.create(stack, "s3-policy")
      .statement(List(statement).asJava)
      .build()
    S3BucketPolicy.Builder.create(stack, "s3-bucket-policy")
      .bucket(bucket.getBucket)
      .policy(policy.getJson).build()
  }

  def s3(stack: Construct): S3Bucket = {
    val bucket = S3Bucket.Builder
      .create(stack, "sam-scrapbook-files")
      .bucket("sam-scrapbook-files")
      .build

    S3BucketPublicAccessBlock.Builder
      .create(stack, "block-public-acls")
      .bucket(bucket.getBucket)
      .blockPublicAcls(true)
      .blockPublicPolicy(true)
      .ignorePublicAcls(true)
      .restrictPublicBuckets(true)
      .build()

    bucket
  }
}

