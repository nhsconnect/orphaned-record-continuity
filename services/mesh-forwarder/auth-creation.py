import hmac
import uuid
import hashlib
import datetime
import sys

def buildmeshauthorizationheader():

  auth_schema_name = "NHSMESH "
  env_shared_secret = sys.argv[1]
  mailbox_id = sys.argv[2]
  mailbox_password = sys.argv[3]
  nonce = str(uuid.uuid4())
  nonce_count = 0

  #Current time formatted as yyyyMMddHHmm
  #for example, 4th May 2020 13:05 would be 202005041305
  timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M")

  #for example, NHSMESH AMP01HC001:bd0e2bd5-218e-41d0-83a9-73fdec414803:0:202005041305
  hmac_msg = mailbox_id + ":" + nonce + ":" + str(nonce_count) + ":" + mailbox_password + ":" + timestamp

  #HMAC is a standard crypto hash method built in the python standard library.
  hash_code = hmac.HMAC(env_shared_secret.encode(), hmac_msg.encode(), hashlib.sha256).hexdigest()

  print(
    auth_schema_name # Note: No colon between 1st and 2nd elements.
    + mailbox_id + ":"
    + nonce + ":"
    + str(nonce_count) + ":"
    + timestamp+ ":"
    + hash_code
)


if len(sys.argv) < 4:
  print('Script Required Arguments: env_shared_secret, mailbox_id, mailbox_password')
else:
  buildmeshauthorizationheader()