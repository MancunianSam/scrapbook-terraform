package dev.sampalmer.scrapbook

import com.hashicorp.cdktf.providers.aws.AwsProvider
import com.hashicorp.cdktf._
import dev.sampalmer.scrapbook.Cloudfront._
import dev.sampalmer.scrapbook.S3._

object Main extends App {
  def main(args: Array[String]): Unit = {
    val app = new App()
    val stack = new TerraformStack(app, "scrapbook-terraform")
    AwsProvider.Builder.create(stack, "AWS").region("eu-west-2").build

    val bucket = s3(stack)
    val accessIdentity = oai(stack)
    distribution(stack, bucket.getBucketRegionalDomainName, accessIdentity)
    s3Policy(stack, accessIdentity.getIamArn, bucket)

    TerraformOutput.Builder.create(stack, "bucket-name").value(bucket.getBucket).build
    val backendProps = RemoteBackendProps.builder.hostname("app.terraform.io")
      .organization("scrapbook")
      .workspaces(new NamedRemoteWorkspace("scrapbook-terraform")).build
    new RemoteBackend(stack, backendProps)
    app.synth()
  }
}