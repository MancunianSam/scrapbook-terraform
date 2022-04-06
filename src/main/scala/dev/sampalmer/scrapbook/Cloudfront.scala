package dev.sampalmer.scrapbook

import com.hashicorp.cdktf.providers.aws.cloudfront.{CloudfrontDistribution, CloudfrontDistributionDefaultCacheBehavior, CloudfrontDistributionDefaultCacheBehaviorForwardedValues, CloudfrontDistributionDefaultCacheBehaviorForwardedValuesCookies, CloudfrontDistributionOrigin, CloudfrontDistributionOriginS3OriginConfig, CloudfrontDistributionRestrictions, CloudfrontDistributionRestrictionsGeoRestriction, CloudfrontDistributionViewerCertificate, CloudfrontOriginAccessIdentity}
import software.constructs.Construct

import scala.jdk.CollectionConverters._

object Cloudfront {
  def oai(stack: Construct): CloudfrontOriginAccessIdentity =
    CloudfrontOriginAccessIdentity.Builder
      .create(stack, "cloudfront-oai")
      .comment("OAI to access S3")
      .build()

  def distribution(stack: Construct, domainName: String, oai: CloudfrontOriginAccessIdentity): CloudfrontDistribution = {
    val s3OriginId = "s3Origin"
    val s3Origin = CloudfrontDistributionOriginS3OriginConfig.builder()
      .originAccessIdentity(oai.getCloudfrontAccessIdentityPath)
      .build()

    val origin = CloudfrontDistributionOrigin.builder()
      .originId(s3OriginId)
      .domainName(domainName)
      .s3OriginConfig(s3Origin)
      .build()

    val cookies = CloudfrontDistributionDefaultCacheBehaviorForwardedValuesCookies.builder()
      .forward("none")
      .build()

    val forwardedValues = CloudfrontDistributionDefaultCacheBehaviorForwardedValues.builder()
      .queryString(true)
      .cookies(cookies)
      .build()

    val defaultCacheBehavior = CloudfrontDistributionDefaultCacheBehavior.builder()
      .allowedMethods(List("GET", "HEAD", "OPTIONS").asJava)
      .cachedMethods(List("GET", "HEAD").asJava)
      .targetOriginId(s3OriginId)
      .viewerProtocolPolicy("https-only")
      .forwardedValues(forwardedValues)
      .build()

    val geoRestriction = CloudfrontDistributionRestrictionsGeoRestriction.builder()
      .restrictionType("whitelist")
      .locations(List("GB").asJava)
      .build()

    val restrictions = CloudfrontDistributionRestrictions.builder()
      .geoRestriction(geoRestriction)
      .build()

    val certificate = CloudfrontDistributionViewerCertificate.builder()
      .cloudfrontDefaultCertificate(true)
      .build()

    CloudfrontDistribution.Builder.create(stack, "cloudfront-distribution")
      .origin(List(origin).asJava)
      .defaultCacheBehavior(defaultCacheBehavior)
      .priceClass("PriceClass_100")
      .restrictions(restrictions)
      .enabled(true)
      .viewerCertificate(certificate)
      .build()
  }
}
