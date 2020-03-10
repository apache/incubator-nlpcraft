#
#  "Commons Clause" License, https://commonsclause.com/
#
#  The Software is provided to you by the Licensor under the License,
#  as defined below, subject to the following condition.
#
#  Without limiting other conditions in the License, the grant of rights
#  under the License will not include, and the License does not grant to
#  you, the right to Sell the Software.
#
#  For purposes of the foregoing, "Sell" means practicing any or all of
#  the rights granted to you under the License to provide to third parties,
#  for a fee or other consideration (including without limitation fees for
#  hosting or consulting/support services related to the Software), a
#  product or service whose value derives, entirely or substantially, from
#  the functionality of the Software. Any license notice or attribution
#  required by the License must also include this Commons Clause License
#  Condition notice.
#
#  Software:    NLPCraft
#  License:     Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0
#  Licensor:    Copyright (C) NLPCraft. https://nlpcraft.org
#
#      _   ____      ______           ______
#     / | / / /___  / ____/________ _/ __/ /_
#    /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
#   / /|  / / /_/ / /___/ /  / /_/ / __/ /_
#  /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
#         /_/
#
#!/bin/bash

# Quick shortcut script for localhost testing with curl (w/o excessive command line).
#
# Usage:
# - 1st parameter is REST URL unique suffix (i.e. /signin) w/o leading '/'
# - 2nd parameter is JSON payload string
#
# Example usage:
#   $./nccurl.sh signin '{"email": "admin@admin.com", "passwd": "admin"}'
#   $./nccurl.sh ask '{"acsTok": "OgJanjDzk", "txt": "Hi!", "mdlId": "nlpcraft.helloworld.ex"}'
#   $./nccurl.sh check '{"acsTok": "OgJanjDzk"}'
#
# For pretty JSON output pipe curl to 'python -m json.tool':
#   $./nccurl.sh check '{"acsTok": "OgJanjDzk"}' | python -m json.tool

curl -s -d"$2" -H 'Content-Type: application/json' http://localhost:8081/api/v1/$1
