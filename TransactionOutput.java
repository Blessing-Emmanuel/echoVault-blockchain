import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey receipient;
    public float value;
    public String parentTransactionID;

    public TransactionOutput(PublicKey receipient, float value, String parentTransactionID) {
        this.receipient = receipient;
        this.value = value;
        this.parentTransactionID = parentTransactionID;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(receipient)+Float.toString(value)+parentTransactionID);
    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey==receipient);
    }
}
