//package echoVault-blockchain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public HashMap<String, TransactionOutput> UXTOs = new HashMap<String, TransactionOutput>();
    public Wallet() {}
    public void generateKeyPair(){}

    // returns balance and stores the UXTOs owned by this wallet in this.UXTOs
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: EchoVault.UTXOs.entrySet()) {
            TransactionOutput UXTO = item.getValue();
            if (UXTO.isMine(publicKey)) {
                UXTOs.put(UXTO.id, UXTO);
                total += UXTO.value;
            }
        }
        return total;
    }

    // generates and returns a new transaction from this wallet
    public Transaction sendFunds(PublicKey _recipient, float value) {
        if (getBalance() < value) {
            System.out.println("#Not enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        // create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UXTOs.entrySet()) {
            TransactionOutput UXTO = item.getValue();
            total += UXTO.value;
            inputs.add(new TransactionInput(UXTO.id));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UXTOs.remove(input.transactionOutputID);
        }
        return newTransaction;
    }
}
