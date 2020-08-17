# JSR-352 Batchlets

## HttpGetBatchlet

Downloading a file using HTTP(S)

| Parameter | Required |
|-----------|----------|
| fromURL   | yes      |
| toFile    | yes      |

## MailBatchlet

Send an email

| Parameter | Required | Default   |
|-----------|----------|-----------|
| from      | yes      |           |
| to        | yes      |           |
| subject   | yes      |           |
| server    | no       | localhost |
| port      | no       | 25        |

## SftpBatchlet

Download or upload a file using SFTP

| Parameter | Required | Default   |
|-----------|----------|-----------|
| fromFile  |          |           |
| fromSite  |          |           |
| fromPort  |          |           |
| fromUser  |          |           |
| fromPass  |          |           |
| toFile    |          |           |
| toSite    |          |           |
| toPort    |          |           |
| toUser    |          |           |
| toPass    |          |           |
| insecure  |          |           |

## SleepBatchlet

Sleep for a number of seconds

| Parameter | Required | Default   |
|-----------|----------|-----------|
| seconds   | yes      |           |

## UnpackBatchlet

Unpack an archive file (e.g. a ZIP)

| Parameter | Required | Default   |
|-----------|----------|-----------|
| inputFile | yes      |           |
| outputDir | yes      |           |

