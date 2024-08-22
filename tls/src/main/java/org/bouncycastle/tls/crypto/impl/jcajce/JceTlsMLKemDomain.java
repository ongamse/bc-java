package org.bouncycastle.tls.crypto.impl.jcajce;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMExtractor;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.bouncycastle.tls.NamedGroup;
import org.bouncycastle.tls.crypto.TlsAgreement;
import org.bouncycastle.tls.crypto.TlsKemConfig;
import org.bouncycastle.tls.crypto.TlsKemDomain;

public class JceTlsMLKemDomain implements TlsKemDomain
{
    protected static MLKEMParameters getKyberParameters(int namedGroup)
    {
        switch (namedGroup)
        {
        case NamedGroup.OQS_mlkem512:
            return MLKEMParameters.ml_kem_512;
        case NamedGroup.OQS_mlkem768:
        case NamedGroup.DRAFT_mlkem768:
            return MLKEMParameters.ml_kem_768;
        case NamedGroup.OQS_mlkem1024:
        case NamedGroup.DRAFT_mlkem1024:
            return MLKEMParameters.ml_kem_1024;
        default:
            return null;
        }
    }

    protected final JcaTlsCrypto crypto;
    protected final MLKEMParameters kyberParameters;
    protected final boolean isServer;

    public JceTlsMLKemDomain(JcaTlsCrypto crypto, TlsKemConfig kemConfig)
    {
        this.crypto = crypto;
        this.kyberParameters = getKyberParameters(kemConfig.getNamedGroup());
        this.isServer = kemConfig.isServer();
    }

    public JceTlsSecret adoptLocalSecret(byte[] secret)
    {
        return crypto.adoptLocalSecret(secret);
    }

    public TlsAgreement createKem()
    {
        return new JceTlsMLKem(this);
    }

    public JceTlsSecret decapsulate(MLKEMPrivateKeyParameters privateKey, byte[] ciphertext)
    {
        MLKEMExtractor kemExtract = new MLKEMExtractor(privateKey);
        byte[] secret = kemExtract.extractSecret(ciphertext);
        return adoptLocalSecret(secret);
    }

    public MLKEMPublicKeyParameters decodePublicKey(byte[] encoding)
    {
        return new MLKEMPublicKeyParameters(kyberParameters, encoding);
    }

    public SecretWithEncapsulation encapsulate(MLKEMPublicKeyParameters publicKey)
    {
        MLKEMGenerator kemGen = new MLKEMGenerator(crypto.getSecureRandom());
        return kemGen.generateEncapsulated(publicKey);
    }

    public byte[] encodePublicKey(MLKEMPublicKeyParameters publicKey)
    {
        return publicKey.getEncoded();
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        MLKEMKeyPairGenerator keyPairGenerator = new MLKEMKeyPairGenerator();
        keyPairGenerator.init(new MLKEMKeyGenerationParameters(crypto.getSecureRandom(), kyberParameters));
        return keyPairGenerator.generateKeyPair();
    }

    public boolean isServer()
    {
        return isServer;
    }
}
