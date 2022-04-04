package com.mycompany.app

import com.hashicorp.cdktf.{App, NamedRemoteWorkspace, RemoteBackend, RemoteBackendProps, TerraformOutput, TerraformStack, TerraformVariable, TerraformVariableConfig}
import software.constructs.Construct
import com.hashicorp.cdktf.providers.aws.AwsProvider
import com.hashicorp.cdktf.providers.aws.s3.{S3Bucket, S3BucketPolicy, S3BucketPolicyConfig, S3BucketPublicAccessBlock}
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

object Main {
  def main(args: Array[String]): Unit = {
    val app = new App()
    val stack = new TerraformStack(app, "scrapbook-terraform")
    AwsProvider.Builder.create(stack, "AWS").region("eu-west-2").build
    val accountNumber = TerraformVariable.Builder.create(stack, "accountNumber").sensitive(true).build.getStringValue

    case class Principal(AWS: List[String])
    case class Statement(Sid: String, principal: Principal, Effect: String, Action: List[String], resource: List[String])
    case class Policy(Version: String, Statement: List[Statement])
    val principal = Principal(List(s"arn:aws:iam::$accountNumber:root"))
    val statement = Statement("Statement1", principal, "Allow", List("s3:*"), List("arn:aws:s3:::sam-scrapbook-terraform-test-bucket"))
    val policy = Policy("2012-10-17", List(statement)).asJson.printWith(Printer.noSpaces)

    val bucket = S3Bucket.Builder
      .create(stack, "sam-scrapbook-terraform-test-bucket")
      .bucket("sam-scrapbook-terraform-test-bucket")
      .policy(policy)
      .build

    S3BucketPublicAccessBlock.Builder
      .create(stack, "block-public-acls")
      .bucket(bucket.getBucket)
      .blockPublicAcls(true)
      .blockPublicPolicy(true)
      .ignorePublicAcls(true)
      .restrictPublicBuckets(true)
      .build()

    TerraformOutput.Builder.create(stack, "bucket-name").value(bucket.getBucket).build
    val backendProps = RemoteBackendProps.builder.hostname("app.terraform.io")
      .organization("scrapbook")
      .workspaces(new NamedRemoteWorkspace("scrapbook-terraform")).build
    new RemoteBackend(stack, backendProps)
    app.synth()
  }
}