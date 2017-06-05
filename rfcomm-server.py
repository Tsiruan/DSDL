import bluetooth
from Crypto.PublicKey import RSA
from Crypto import Random
import hashlib


NONCE_LENGTH = 100
public_key_loc = {
    'user_a': '/xxx/xxx',
}

token = {}
token_cnt = 0

def generate_token(string):
    if string in token:
        return token[string]
    else:
        token_cnt += 1
        token[string] = token_cnt
        return token_cnt


def authenication_process(client_sock, sender):
    random_generator = Random.new().read
    rsa_key = RSA.importKey(open(public_key_loc[sender], "rb").read())



    random_generator = Random.new().read
    nonce = random_generator(NONCE_LENGTH)
    # send nonce
    client_sock.send(nonce)

    # recevice response
    res_nonce = client_sock.recv(1024)
    enc_nonce = rsa_key.encrypt(nonce, 32)

    # decrypt and check content
    dec_nonce = rsa_key.decrypt(enc_nonce)

    # Failed, return
    if dec_nonce != nonce:
        print('Failed Authenicated %s' % sender)
        return

    # wait the msg
    print('Authenicated %s' % sender)
    client_sock.send('OK')
    res_msg = client_sock.recv(1024)

    # return token
    token = generate_token(res_msg)
    hashed_token = hashlib.sha256(token).hexdigest()
    enc_token = rsa_key.encrypt(hashed_token, 32)
    client_sock.send(enc_token)




if __name__ == "__main__":

    server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM)
    port = 1
    server_sock.bind(("", port))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    bluetooth.advertise_service( server_sock, "pi",
                       service_id = uuid,
                       service_classes = [ uuid, bluetooth.SERIAL_PORT_CLASS ],
                       profiles = [ bluetooth.SERIAL_PORT_PROFILE ])

    try:
      while True:
        client_sock,address = server_sock.accept()
        print "Accepted connection from ", address

        data = client_sock.recv(1024)
        if len(data) == 0:
          break

        hello_msg = 'Hello_Pi|'
        print data
        client_sock.send('received')

        if data.startswith(hello_msg):
            sender = data.replace(hello_msg, '')
            # authenication_process(client_sock, sender)

        client_sock.close()
    except IOError:
      pass


    server_sock.close()

