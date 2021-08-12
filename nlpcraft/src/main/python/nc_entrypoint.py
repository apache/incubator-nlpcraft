import pathlib
import os
import subprocess
from pyhocon import ConfigFactory

CURRENT_FILE_NAME = os.path.abspath(__file__)
CURRENT_DIR = os.path.dirname(CURRENT_FILE_NAME)

conf = ConfigFactory.parse_file(os.path.join(CURRENT_DIR, os.pardir, 'resources', 'nlpcraft.conf'))

FLASK_ENV = conf.get_string('nlpcraft.server.python.api.env')

if FLASK_ENV is not None:
    os.environ['FLASK_ENV'] = FLASK_ENV
else:
    os.environ['FLASK_ENV'] = "development"

# Get the absolute path of parent folder
parent_folder = pathlib.Path(__file__).parent.absolute()

os.environ['FLASK_APP'] = os.path.join(parent_folder, 'nc_server.py')
subprocess.call(['python3', '-m', 'flask', 'run'])
