This folder contains 3rd party pre-packaged configuration. Note that this configuration
is based on Creative Commons Attribution 4.0 International (CC BY 4.0) license
(https://creativecommons.org/licenses/by/4.0/).

Note that CC BY 4.0 is not automatically compatible with Apache Software License 2.0 and
therefore is NOT included into official Apache source release. This configuration can
be downloaded separately if CC BY 4.0 derived content is acceptable by the end user.

To use pre-packaged configuration files (expanded in this directory or elsewhere) you
need to provide NLPCRAFT_RESOURCE_EXT system property or environment variable to NLPCraft
server that should point to the absolute path of the folder where these files are located.

For example, if you obtained the 'cc_by40_geo_config.zip' file from this folder and unzipped
into '/home/nlpcraft/ext' folder, then you need to run NLPCraft server with the
following system property configuration '-DNLPCRAFT_RESOURCE_EXT=/home/nlpcraft/ext'.