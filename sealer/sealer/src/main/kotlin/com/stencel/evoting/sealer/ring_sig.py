# needs: pip install PyCryptodome
import hmac
import os
import random
import Crypto.PublicKey.RSA
from hashlib import sha256


def gen_key_pair():
    r = Crypto.PublicKey.RSA.generate(1024, os.urandom)
    return {'e': r.e, 'd': r.d, 'n': r.n}


def gen_public_key():
    r = Crypto.PublicKey.RSA.generate(1024, os.urandom)
    return {'e': r.e, 'n': r.n}


def crypto_hash(msg):
    return int(sha256(msg.encode("utf-8")).hexdigest(), 16)


def keyed_hash(key, msg):
    return int(hmac.new(bytes(str(key), 'UTF-8'), str(msg).encode("utf-8"), sha256).hexdigest(), 16)


def rsa_encrypt_or_decrypt(msg, e_or_d, n):
    q, r = divmod(msg, n)
    if ((q + 1) * n) <= (pow(2, 1024) - 1):
        result = q * n + pow(r, e_or_d, n)
    else:
        result = msg
    return result


def xor(a, b):
    return a ^ b


def sign(msg, signer_key_pair, other_public_keys):
    ring_size = len(other_public_keys) + 1
    key = crypto_hash(msg)
    u = random.randint(0, pow(2, 1023))
    v = [0] * ring_size
    v[0] = keyed_hash(key, u)

    s = [0] + [random.randint(0, pow(2, 1023)) for i in range(1, ring_size)]
    for i in range(1, ring_size):
        v[i] = keyed_hash(key, xor(v[i - 1], rsa_encrypt_or_decrypt(s[i], other_public_keys[i - 1]['e'], other_public_keys[i - 1]['n'])))
    s[0] = rsa_encrypt_or_decrypt(xor(v[ring_size - 1], u), signer_key_pair['d'], signer_key_pair['n'])

    signature = {
        'msg': msg,
        'rows':
            [{'e': signer_key_pair['e'], 'n': signer_key_pair['n'], 's': s[0]}] +
            [{'e': other_public_keys[i - 1]['e'], 'n': other_public_keys[i - 1]['n'], 's': s[i]} for i in range(1, ring_size)]
    }

    # rotate signature randomly to conceal position of true signer
    rotation = 0
    signature['v'] = rotate(v, rotation)[ring_size - 1]
    signature['rows'] = rotate(signature['rows'], rotation)

    return signature


def rotate(list, n):
    return list[-n:] + list[:-n]


def verify(signature):
    ring_size = len(signature['rows'])
    key = crypto_hash(signature['msg'])
    v = signature['v']
    for i in range(0, ring_size):
        row = signature['rows'][i]
        v = keyed_hash(key, xor(v, rsa_encrypt_or_decrypt(row['s'], row['e'], row['n'])))
    return v == signature['v']


verified = 0
for i in range(1):
    print("iter", i)
    ring_size = 3

    # signer_key_pair = gen_key_pair()
    signer_key_pair = {
        'e': 65537,
        'd': 42970916808742496973851860887379727528198338206336350469989764772671110306200910026264340338242109185938667330190706338563529971014121272702235035326077080684584246908972764608273338808272018652917785198348316689482631370957124876157810726432813107844708097241266616290342950972393906275698663228288779571523,
        'n': 97181875352228618602596042133864941353608174716726725011705892779355265322134998029272946244538989586033177728961413506925412416458838996120792225621240375080597718077428482118791075856677551257816837241540080539238808595425529699859561754523316511913024999957295243147129514730796455543019957803115974043857
    }
    # other_public_keys = [gen_public_key() for i in range(ring_size - 1)]
    other_public_keys = [
        {
            'e': 65537,
            'n': 131609606392516964933747599249992014476599922186287034758719673751117073462150807141685265071513072141072017717179650398632817474415428149837567494205195453784130928026237551468236921356933246192682849681384292588992588229029255481489632372929366339218359852290975056399698366604672282269206945189120865738901
        },
        {
            'e': 65537,
            'n': 129549869806433833222484246850483177188962295635735426739924107105168969778657895315409525910799172225812786115487484609178367173094058495886252027595338118597586111666925623882878186286358402438038648085131670674888912352481134722392303978665464705081585722504859245431151284849446322660059654183572673824321
        }
    ]

    signature = sign("hello world!", signer_key_pair, other_public_keys)
    isVerified = verify(signature)

    if isVerified:
        verified += 1

print("verified", verified)