openssl genpkey -algorithm RSA -out workdir/client.key -pkeyopt rsa_keygen_bits:2048
# Generate a CSR
openssl req -new -key workdir/client.key -out workdir/client.csr -config openssl.cnf
