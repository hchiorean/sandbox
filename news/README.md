# How to use
1. compile with `-encoding utf8` (JDK >= 8 required)
2. start server
3. start client

## Server
1. start 
`java org.test.news.Analyzer`
2. passing args (command line)
`Usage: Analyzer [port:8913]`

## Client
1. start 
`java org.test.news.Feed`
2. passing args (command line)
`Usage: Feed [number_of_items:100] [frequency_seconds:10] [host:localhost] [port:8913]`
