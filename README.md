# Holiday Ping [![Build Status](https://travis-ci.org/lambdaclass/holiday_ping.svg?branch=master)](https://travis-ci.org/lambdaclass/holiday_ping)

Holiday ping is an Erlang/OTP application that allows users to send national
holiday reminders through different channels (e.g. email, Slack).

## Project setup for development

The project requires Elrang/OTP 20 and Leningen for the ClojureScript web application.

Setup the database (using Docker):

    $ make ops

Create the tables:

    $ make ops_start

Compile and run a development shell:

    $ make dev

Run the tests:

    $ make test

Build a release:

    $ make release

### UI setup

The UI is a ClojureScript project that uses [re-frame](https://github.com/Day8/re-frame).
The code is in [priv/ui](https://github.com/lambdaclass/holiday_ping/tree/master/priv/ui) and is build as part of the `dev` and `release` targets.

For UI development, run a dev shell and then:

    $ make dev_ui

Which uses figwheel to provide a REPL and hot-reload of the code changes.

## Production install

Inside the devops folder you can find an [install shell script](devops/sh/install.sh) for installing holiday_ping in a debian 9 server. On the other hand, you can do `make app_image` to generate a Docker image of HolidayPing called `holiday-ping`.

The release generated for `prod` expects some config values from the environment. If you prefer, you can modify [prod.config](conf/prod.config) to directly set the values and re-make the release or image. The expected environment variables are this:

- `RELX_REPLACE_OS_VARS=true`
- `SSL_CERTFILE=</dir/of/ssl/fullchain.pem>`
- `SSL_KEYFILE=</dir/of/ssl/privkey.pem>`
- `TOKEN_SECRET=<secret_token_hash>`
- `POSTGRES_HOST=<holiday_ping_postgres_1>`
- `POSTGRES_USER=<postgres user>`
- `POSTGRES_PASSWORD=<postgres user password>`
- `POSTGRES_DB=<postgres database>`

**NOTE:** Remember to mount the folder where `SSL_CERTFILE` and `SSL_KEYFILE` are stored into the container.

### Credentials
#### GitHub login configuration

For the GitHub login option to work, [OAuth cretentials](https://github.com/settings/applications/new)
need to be generated and set as `GITHUB_CLIENTID` and `GITHUB_SECRET`
environment variables.

#### Amazon SES

To send emails with amazon, set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
environment variables. Also, make sure `email_enabled` is set to true in the
application environment (e.g. in conf/dev.config).

## Rest API Reference

Not yet.
