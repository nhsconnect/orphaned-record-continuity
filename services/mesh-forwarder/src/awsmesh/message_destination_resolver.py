from dataclasses import dataclass
from typing import Optional

import boto3

from awsmesh.s3 import S3Uploader
from awsmesh.sns import SNSUploader
from awsmesh.uploader import MessageUploader


@dataclass
class MessageDestinationConfig:
    message_destination: str
    s3_bucket_name: Optional[str]
    endpoint_url: Optional[str]
    sns_topic_arn: Optional[str]


class UnknownMessageDestination(Exception):
    pass


def resolve_message_uploader(config: MessageDestinationConfig, aws=boto3) -> MessageUploader:
    if config.message_destination == "s3":
        s3 = aws.client(service_name=config.message_destination, endpoint_url=config.endpoint_url)
        return S3Uploader(s3, config.s3_bucket_name)
    elif config.message_destination == "sns":
        sns = aws.client(service_name=config.message_destination, endpoint_url=config.endpoint_url)
        return SNSUploader(sns, config.sns_topic_arn)
    else:
        raise UnknownMessageDestination
