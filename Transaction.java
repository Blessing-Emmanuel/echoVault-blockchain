import java.security.*;
import java.util.ArrayList;

public class Transaction {
    public String transactionID;
    public PublicKey sender;
    public PublicKey receipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.receipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // This calculates the transaction hash (which will be used as its id)
    private String calculateHash() {
        sequence++; // increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(receipient) +
                        Float.toString(value) + sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receipient) + Float.toString(value);
        signature = StringUtil.applyECDSAig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receipient) + Float.toString(value);
        return StringUtil.verifyECDSAig(sender, data,signature);
    }

    public boolean processTransaction() {
        if (verifySignature() == false) {
            System.out.println("#Transaction Signature failede to verify");
            return false;
        }

        // gather transaction inputs (Make sure they are unspent)
        for(TransactionInput i : inputs) {
            i.UTXO = EchoVault.UTXOs.get(i.transactionOutputID);
        }

        // check if transaction is valid
        if (getInputsValue() < EchoVault.minimumTransaction) {
            System.out.println("#Transaction inputs to small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs
        float leftOver = getInputsValue() - value;
        transactionID = calculateHash();
        outputs.add(new TransactionOutput(this.receipient, value, transactionID));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionID));

        // add outputs to unspent list
        for (TransactionOutput o : outputs) {
            EchoVault.UTXOs.put(o.id, o);
        }

        // remove transaction inputs from UXTO lists as spent
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            EchoVault.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // returns sum of inputs (UXTOs) values
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;;
            total += i.UTXO.value;
        }
        return total;
    }

    // returns sum of outputs
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
 }
