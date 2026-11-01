#!/usr/bin/env python3
"""Revokes the Apple Development certificates that CI builds leave behind.

Every build on a fresh CI machine makes Xcode create a certificate named "Created via API" for the App Store Connect
API key, and an account holds only a limited number, after which archiving fails. Only certificates with that name
are touched; the ones made from a person's Xcode, and distribution certificates, are left alone.

  ASC_KEY_ID, ASC_ISSUER_ID and ASC_KEY_PATH are read from the environment. Needs PyJWT and cryptography.
"""
import os
import sys
import time

import jwt
import requests

API = "https://api.appstoreconnect.apple.com"
token = jwt.encode(
    {"iss": os.environ["ASC_ISSUER_ID"], "iat": int(time.time()), "exp": int(time.time()) + 600, "aud": "appstoreconnect-v1"},
    open(os.environ["ASC_KEY_PATH"]).read(),
    algorithm="ES256",
    headers={"kid": os.environ["ASC_KEY_ID"]},
)
headers = {"Authorization": f"Bearer {token}"}

response = requests.get(f"{API}/v1/certificates", headers=headers, params={"limit": 200}, timeout=60)
response.raise_for_status()
stale = [
    c["id"]
    for c in response.json()["data"]
    if c["attributes"]["certificateType"] == "DEVELOPMENT" and c["attributes"].get("displayName") == "Created via API"
]
for certificate in stale:
    requests.delete(f"{API}/v1/certificates/{certificate}", headers=headers, timeout=60).raise_for_status()
print(f"Revoked {len(stale)} leftover CI certificate(s)", file=sys.stderr)
