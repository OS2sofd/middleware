openssl pkcs12 -export -out workdir/cert.pfx -inkey workdir/client.key -in workdir/signed-certificate.cer -CAfile workdir/cafile.pem
