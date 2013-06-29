tokenbucket
===========

Token Bucket Implementation Framework

Usage
===========


* Use the TokenBuckets class to instantiate token buckets providing the parameters required
* The current available implementation of TokenBuckets are based on semaphores and a scheduled refilling mechanism.
* Essentially, the scheduled refiller would periodically add tokens to the bucket. The bucket in turn maintains these allowing a maximum burst of 'capacity'.
* The current token bucket implementations can be guaranteed not to provide more than X tokens per period N. Examples for usage is a cap on available uploaded files for any 24 hours. Note also that you need a warm up period to fill the bucket with some tokens before you use it, given the filler provided gives a steady stream of tokens based on the X/N equation.
