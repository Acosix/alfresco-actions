[![Build Status](https://travis-ci.org/Acosix/alfresco-actions.svg?branch=master)](https://travis-ci.org/Acosix/alfresco-actions)

# About

This addon aims to provide a collection of generic, re-usable actions for the Alfresco Repository and Share tiers.

## Compatbility

This module is built to be compatible with Alfresco 5.0d and above. It may be used on either Community or Enterprise Edition.

## Features

Currently this module provides the following action(s):

- Webhook Call: a simple action which can be used e.g. in rules to generate events in external systems via webhooks, using FreeMarker to generate the webhook target URL / payload message

In order to support the use of more or less complex actions in configured folder rules, this module also provides a generic action form dialog template and enhances the rule configuration UI to enable the use of complex forms for configuration. 

# Maven usage

This addon is being built using the [Acosix Alfresco Maven framework](https://github.com/Acosix/alfresco-maven) and produces both AMP and installable JAR artifacts. Depending on the setup of a project that wants to include the addon, different approaches can be used to include it in the build.

## Build

This project can be build simply by executing the standard Maven build lifecycles for package, install or deploy depending on the intent for further processing. A Java Development Kit (JDK) version 8 or higher is required for the build.

TBD: Installation instructions for SDK 4 / Docker users