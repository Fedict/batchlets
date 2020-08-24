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
| fromSite  | no       | 22        |
| fromPort  |          |           |
| fromUser  |          |           |
| fromPass  |          |           |
| toFile    |          |           |
| toSite    |          |           |
| toPort    | no       | 22        |
| toUser    |          |           |
| toPass    |          |           |
| insecure  | no       | false     |

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

## VerifyFileBatchlet

Verifies names, date and size of (a series of) files

| Parameter     | Required | Default   |
|---------------|----------|-----------|
| file	        |          |           |
| directory     |          |           |
| filterPattern | no       |           |
| matchStart    | no       | *         |
| matchEnd      | no       | *         |
| matchPattern  | no       | ^.*$      |
| minSize       | no       | 0         |
| maxSize       | no       | infinite  |
| minDate       | no       | 0         |
| maxDate       | no       |           |
| minAgeDays    | no       | 0         |
| maxAgeDays    | no       |           | 
